import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * ModernChatUI - UI con:
 * - Login con labels y Puerto RMI cliente
 * - Botón de Registro
 * - Lista de chats con badge "• nuevo"
 * - Área de conversación
 * - Gestión de Solicitudes de amistad (pendientes y envío)
 * - Auto-refresco cada 1s (chats, amigos online, solicitudes, mensajes)
 *
 * Requisitos mínimos de Cliente (ver interfaz al final).
 */
public class ModernChatUI {

    // --- Dependencias / estado de aplicación ---
    private final Cliente client;

    // --- Ventana principal y layout ---
    private JFrame mainFrame;
    private JPanel root;
    private CardLayout cardLayout;

    // --- Panel login ---
    private JTextField userField;
    private JPasswordField passField;
    private JTextField portField;
    private JButton loginBtn;
    private JButton registerBtn;
    private JLabel loginMsg;

    // --- Panel principal (chat) ---
    private JPanel mainPanel;
    private DefaultListModel<String> contactsModel;
    private JList<String> contactsList;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendBtn;
    private JLabel chatTitle;

    // --- Panel lateral (amistades/solicitudes) ---
    private DefaultListModel<String> pendingModel;
    private JList<String> pendingList;
    private JTextField addFriendField;
    private JButton addFriendBtn;
    private DefaultListModel<String> onlineFriendsModel;
    private JList<String> onlineFriendsList;

    // --- Estado de sesión/chat ---
    private String currentUser;
    private String currentChatContact;

    // --- Auto refresco ---
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> refreshTask;
    private final Set<String> unread = new HashSet<>();

    public ModernChatUI(Cliente client) {
        this.client = client;
        buildUI();
    }

    // ---------- Construcción de UI ----------
    private void buildUI() {
        mainFrame = new JFrame("ChatApp");
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setMinimumSize(new Dimension(1100, 650));

        cardLayout = new CardLayout();
        root = new JPanel(cardLayout);

        root.add(buildLoginPanel(), "login");
        root.add(buildMainPanel(), "main");

        mainFrame.setContentPane(root);
        mainFrame.setLocationRelativeTo(null);
        cardLayout.show(root, "login");
        mainFrame.setVisible(true);
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        JLabel title = new JLabel("Iniciar sesión");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        gc.gridx = 0;
        gc.gridy = y++;
        gc.gridwidth = 2;
        panel.add(title, gc);

        // Usuario
        gc.gridwidth = 1;
        gc.gridx = 0;
        gc.gridy = y;
        panel.add(new JLabel("Usuario"), gc);
        userField = new JTextField();
        gc.gridx = 1;
        panel.add(userField, gc);
        y++;

        // Contraseña
        gc.gridx = 0;
        gc.gridy = y;
        panel.add(new JLabel("Contraseña"), gc);
        passField = new JPasswordField();
        gc.gridx = 1;
        panel.add(passField, gc);
        y++;

        // Puerto RMI cliente
        gc.gridx = 0;
        gc.gridy = y;
        panel.add(new JLabel("Puerto RMI cliente"), gc);
        portField = new JTextField();
        portField.setToolTipText("Ejemplo: 1099");
        gc.gridx = 1;
        panel.add(portField, gc);
        y++;

        // Botones
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        loginBtn = new JButton("Entrar");
        registerBtn = new JButton("Registrarse");
        btns.add(loginBtn);
        btns.add(registerBtn);
        gc.gridx = 0;
        gc.gridy = y;
        gc.gridwidth = 2;
        panel.add(btns, gc);
        y++;

        loginMsg = new JLabel(" ");
        loginMsg.setForeground(new Color(180, 0, 0));
        gc.gridx = 0;
        gc.gridy = y;
        gc.gridwidth = 2;
        panel.add(loginMsg, gc);

        // Eventos
        ActionListener doLogin = e -> performLogin();
        loginBtn.addActionListener(doLogin);
        passField.addActionListener(doLogin);

        registerBtn.addActionListener(e -> openRegisterDialog());

        return panel;
    }

    private JPanel buildMainPanel() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(buildHeader(), BorderLayout.NORTH);
        mainPanel.add(buildBody(), BorderLayout.CENTER);
        mainPanel.add(buildComposer(), BorderLayout.SOUTH);
        return mainPanel;
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(10, 12, 10, 12));

        chatTitle = new JLabel("Selecciona un chat");
        chatTitle.setFont(chatTitle.getFont().deriveFont(Font.BOLD, 16f));

        JButton logout = new JButton("Salir");
        logout.addActionListener(e -> performLogout());

        header.add(chatTitle, BorderLayout.WEST);
        header.add(logout, BorderLayout.EAST);
        return header;
    }

    private JComponent buildBody() {
        // Layout: [Left tabs] | [Center chat area] | [Right panel: amigos online /
        // solicitudes]
        JPanel center = new JPanel(new BorderLayout());
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setResizeWeight(0.28);
        mainSplit.setDividerSize(6);

        // -------- LEFT: Tabs con CHATS y SOLICITUDES --------
        JTabbedPane leftTabs = new JTabbedPane();

        // Tab CHATS
        JPanel chatsPanel = new JPanel(new BorderLayout());
        chatsPanel.setBorder(new EmptyBorder(8, 8, 8, 4));
        contactsModel = new DefaultListModel<>();
        contactsList = new JList<>(contactsModel);
        contactsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactsList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contactsList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String name = String.valueOf(value);
                if (unread.contains(name)) {
                    setText(name + "  • nuevo");
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    setText(name);
                    setFont(getFont().deriveFont(Font.PLAIN));
                }
                return c;
            }
        });
        contactsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String sel = contactsList.getSelectedValue();
                if (sel != null)
                    openChat(sel);
            }
        });
        chatsPanel.add(new JLabel("Chats"), BorderLayout.NORTH);
        chatsPanel.add(new JScrollPane(contactsList), BorderLayout.CENTER);
        leftTabs.addTab("Chats", chatsPanel);

        // Tab SOLICITUDES
        JPanel requestsPanel = new JPanel(new BorderLayout(6, 6));
        requestsPanel.setBorder(new EmptyBorder(8, 8, 8, 4));
        pendingModel = new DefaultListModel<>();
        pendingList = new JList<>(pendingModel);
        JPanel requestsTop = new JPanel(new BorderLayout());
        requestsTop.add(new JLabel("Solicitudes pendientes"), BorderLayout.WEST);
        JPanel requestsButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton acceptBtn = new JButton("Aceptar");
        JButton rejectBtn = new JButton("Rechazar");
        requestsButtons.add(acceptBtn);
        requestsButtons.add(rejectBtn);
        requestsTop.add(requestsButtons, BorderLayout.EAST);

        acceptBtn.addActionListener(e -> handleAcceptRequest());
        rejectBtn.addActionListener(e -> handleRejectRequest());

        // enviar solicitud
        JPanel addFriendPanel = new JPanel(new BorderLayout(6, 0));
        addFriendPanel.setBorder(new EmptyBorder(8, 0, 0, 0));
        addFriendField = new JTextField();
        addFriendBtn = new JButton("Enviar solicitud");
        addFriendBtn.addActionListener(e -> handleSendFriendRequest());
        addFriendPanel.add(new JLabel("Añadir amigo (usuario):"), BorderLayout.WEST);
        addFriendPanel.add(addFriendField, BorderLayout.CENTER);
        addFriendPanel.add(addFriendBtn, BorderLayout.EAST);

        requestsPanel.add(requestsTop, BorderLayout.NORTH);
        requestsPanel.add(new JScrollPane(pendingList), BorderLayout.CENTER);
        requestsPanel.add(addFriendPanel, BorderLayout.SOUTH);

        leftTabs.addTab("Amistades", requestsPanel);

        // -------- CENTER: Área de chat --------
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBorder(new EmptyBorder(8, 4, 8, 4));
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // -------- RIGHT: Amigos en línea --------
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(new EmptyBorder(8, 4, 8, 8));
        rightPanel.add(new JLabel("Amigos en línea"), BorderLayout.NORTH);
        onlineFriendsModel = new DefaultListModel<>();
        onlineFriendsList = new JList<>(onlineFriendsModel);
        onlineFriendsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rightPanel.add(new JScrollPane(onlineFriendsList), BorderLayout.CENTER);

        JButton openChatFromOnline = new JButton("Abrir chat");
        openChatFromOnline.addActionListener(e -> {
            String sel = onlineFriendsList.getSelectedValue();
            if (sel != null)
                openChat(sel);
        });
        rightPanel.add(openChatFromOnline, BorderLayout.SOUTH);

        // Compose SplitPane
        mainSplit.setLeftComponent(leftTabs);

        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatPanel, rightPanel);
        centerSplit.setResizeWeight(0.78);
        centerSplit.setDividerSize(6);

        mainSplit.setRightComponent(centerSplit);

        center.add(mainSplit, BorderLayout.CENTER);
        return center;
    }

    private JComponent buildComposer() {
        JPanel bar = new JPanel(new BorderLayout(8, 8));
        bar.setBorder(new EmptyBorder(8, 8, 8, 8));
        messageField = new JTextField();
        messageField.setEnabled(false);
        sendBtn = new JButton("Enviar");
        sendBtn.setEnabled(false);

        ActionListener doSend = e -> {
            if (currentChatContact == null)
                return;
            String text = messageField.getText().trim();
            if (text.isEmpty())
                return;
            try {
                // TODO: Ajusta si tu método es distinto
                client.sendMessage(currentChatContact, text);
                appendOwnMessage(text);
                messageField.setText("");
                messageField.requestFocus();
            } catch (Exception ex) {
                showError("No se pudo enviar el mensaje.");
            }
        };

        sendBtn.addActionListener(doSend);
        messageField.addActionListener(doSend);

        bar.add(messageField, BorderLayout.CENTER);
        bar.add(sendBtn, BorderLayout.EAST);
        return bar;
    }

    // ---------- Lógica de sesión ----------
    private void performLogin() {
        String u = userField.getText().trim();
        String p = new String(passField.getPassword());
        String portTxt = portField.getText().trim();

        if (u.isEmpty() || p.isEmpty() || portTxt.isEmpty()) {
            loginMsg.setText("Usuario, contraseña y puerto son obligatorios.");
            return;
        }

        final int port;
        try {
            port = Integer.parseInt(portTxt);
            if (port < 0 || port > 65535)
                throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            loginMsg.setText("El puerto debe ser un entero entre 0 y 65535.");
            return;
        }

        try {
            // ⬇️ El puerto es el RMI del cliente
            boolean ok = client.login(u, p, port); // TODO: ajusta firma si difiere
            if (ok) {
                this.currentUser = u;
                onLoginSuccess();
            } else {
                loginMsg.setText("Credenciales inválidas o puerto rechazado.");
            }
        } catch (Exception ex) {
            loginMsg.setText("Error de conexión: " + ex.getMessage());
        }
    }

    private void onLoginSuccess() {
        cardLayout.show(root, "main");
        mainFrame.setTitle("ChatApp - " + currentUser);
        updateAllPanelsOnce();
        startAutoRefresh();
        showInfo("Bienvenido " + currentUser + " 🎉");
    }

    private void performLogout() {
        stopAutoRefresh();
        try {
            client.logout();
        } catch (Exception ignored) {
        }
        currentUser = null;
        currentChatContact = null;
        contactsModel.clear();
        pendingModel.clear();
        onlineFriendsModel.clear();
        chatArea.setText("");
        messageField.setEnabled(false);
        sendBtn.setEnabled(false);
        cardLayout.show(root, "login");
        mainFrame.setTitle("ChatApp");
    }

    private void openRegisterDialog() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JTextField regUser = new JTextField();
        JPasswordField regPass = new JPasswordField();

        gc.gridx = 0;
        gc.gridy = 0;
        form.add(new JLabel("Usuario"), gc);
        gc.gridx = 1;
        form.add(regUser, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        form.add(new JLabel("Contraseña"), gc);
        gc.gridx = 1;
        form.add(regPass, gc);

        int opt = JOptionPane.showConfirmDialog(mainFrame, form, "Registro", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (opt == JOptionPane.OK_OPTION) {
            String u = regUser.getText().trim();
            String p = new String(regPass.getPassword());
            if (u.isEmpty() || p.isEmpty()) {
                showError("Usuario y contraseña obligatorios.");
                return;
            }
            try {
                boolean ok = client.register(u, p); // TODO
                if (ok)
                    showInfo("Usuario registrado.");
                else
                    showError("No se pudo registrar (usuario existente?).");
            } catch (Exception ex) {
                showError("Error registrando: " + ex.getMessage());
            }
        }
    }

    // ---------- Interacciones de chat ----------
    private void openChat(String contact) {
        currentChatContact = contact;
        updateChatTitle("💬 Chat con " + contact);
        chatArea.setText("");
        messageField.setEnabled(true);
        sendBtn.setEnabled(true);

        // limpiar estado de no leído y repintar
        unread.remove(contact);
        contactsList.repaint();

        // Cargar histórico si lo tienes; si no, drena lo pendiente
        try {
            List<String> nuevos = client.getNewMessages(contact);
            if (nuevos != null) {
                for (String m : nuevos)
                    appendInboundMessage(m);
            }
        } catch (Exception ignored) {
        }

        messageField.requestFocus();
    }

    private void appendOwnMessage(String text) {
        chatArea.append(currentUser + ": " + text + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void appendInboundMessage(String text) {
        chatArea.append((currentChatContact != null ? currentChatContact : "Amigo") + ": " + text + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void updateChatTitle(String t) {
        chatTitle.setText(t);
    }

    // ---------- Amistades (solicitudes) ----------
    private void handleAcceptRequest() {
        String sel = pendingList.getSelectedValue();
        if (sel == null)
            return;
        try {
            boolean ok = client.acceptFriendRequest(sel); // TODO
            if (!ok)
                showError("No se pudo aceptar la solicitud.");
        } catch (Exception ex) {
            showError("Error aceptando: " + ex.getMessage());
        }
        refreshPendingRequests();
        // también refrescar lista de chats por si añade chat nuevo
        updateContactsList();
    }

    private void handleRejectRequest() {
        String sel = pendingList.getSelectedValue();
        if (sel == null)
            return;
        try {
            boolean ok = client.rejectFriendRequest(sel); // TODO
            if (!ok)
                showError("No se pudo rechazar la solicitud.");
        } catch (Exception ex) {
            showError("Error rechazando: " + ex.getMessage());
        }
        refreshPendingRequests();
    }

    private void handleSendFriendRequest() {
        String target = addFriendField.getText().trim();
        if (target.isEmpty())
            return;
        if (target.equals(currentUser)) {
            showError("No puedes agregarte a ti mismo.");
            return;
        }
        try {
            boolean ok = client.sendFriendRequest(target); // TODO
            if (ok) {
                showInfo("Solicitud enviada a " + target);
                addFriendField.setText("");
            } else {
                showError("No se pudo enviar la solicitud.");
            }
        } catch (Exception ex) {
            showError("Error enviando solicitud: " + ex.getMessage());
        }
    }

    // ---------- Refrescos ----------
    private void updateAllPanelsOnce() {
        updateContactsList();
        refreshPendingRequests();
        refreshOnlineFriends();
    }

    private void updateContactsList() {
        try {
            // 1) Chats activos del cliente (creados por notificaciones o al conectar
            // amigos)
            List<String> actives = client.getActiveChats(); // TODO
            Map<String, Integer> flags = safeCheckNewMessages();

            // 2) Fallback: si por lo que sea el cliente aún no creó chats
            // para amigos en línea, añade amigos en línea como candidatos
            // para que no ocurra el “solo el primero ve al segundo”.
            Set<String> union = new LinkedHashSet<>();
            if (actives != null)
                union.addAll(actives);
            try {
                List<String> online = client.getOnlineFriends(); // TODO
                if (online != null)
                    union.addAll(online);
            } catch (Exception ignored) {
            }

            String sel = contactsList.getSelectedValue();
            contactsModel.clear();
            unread.clear();

            for (String c : union) {
                if (c.equals(currentUser))
                    continue;
                contactsModel.addElement(c);
                if (flags.getOrDefault(c, 0) > 0)
                    unread.add(c);
            }

            if (sel != null && union.contains(sel)) {
                contactsList.setSelectedValue(sel, true);
            }
        } catch (Exception ignored) {
        }
    }

    private void refreshPendingRequests() {
        try {
            List<String> pending = client.getPendingFriendRequests(); // TODO
            pendingModel.clear();
            if (pending != null)
                pending.forEach(pendingModel::addElement);
        } catch (Exception ignored) {
        }
    }

    private void refreshOnlineFriends() {
        try {
            List<String> online = client.getOnlineFriends(); // TODO
            onlineFriendsModel.clear();
            if (online != null) {
                for (String f : online)
                    if (!f.equals(currentUser))
                        onlineFriendsModel.addElement(f);
            }
        } catch (Exception ignored) {
        }
    }

    // ---------- Auto refresco ----------
    private void startAutoRefresh() {
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "ui-auto-refresh");
                t.setDaemon(true);
                return t;
            });
        }
        if (refreshTask == null || refreshTask.isCancelled()) {
            refreshTask = scheduler.scheduleAtFixedRate(() -> {
                try {
                    List<String> actives = client.getActiveChats(); // TODO
                    Map<String, Integer> flags = safeCheckNewMessages();
                    List<String> online = null;
                    try {
                        online = client.getOnlineFriends();
                    } catch (Exception ignored) {
                    }

                    // UI sync
                    final List<String> fOnline = online;
                    SwingUtilities.invokeLater(() -> {
                        // Mezcla chats activos + amigos online para evitar el “solo lo ve el primero”
                        Set<String> union = new LinkedHashSet<>();
                        if (actives != null)
                            union.addAll(actives);
                        if (fOnline != null)
                            union.addAll(fOnline);

                        String sel = contactsList.getSelectedValue();
                        contactsModel.clear();
                        unread.clear();

                        for (String c : union) {
                            if (c.equals(currentUser))
                                continue;
                            contactsModel.addElement(c);
                            if (flags.getOrDefault(c, 0) > 0)
                                unread.add(c);
                        }

                        if (sel != null && union.contains(sel)) {
                            contactsList.setSelectedValue(sel, true);
                        }
                    });

                    // drenar mensajes del chat abierto
                    if (currentChatContact != null) {
                        List<String> nuevos = client.getNewMessages(currentChatContact); // TODO
                        if (nuevos != null && !nuevos.isEmpty()) {
                            SwingUtilities.invokeLater(() -> {
                                for (String msg : nuevos)
                                    appendInboundMessage(msg);
                                unread.remove(currentChatContact);
                                contactsList.repaint();
                            });
                        }
                    }

                    // también refrescar solicitudes y amigos online
                    SwingUtilities.invokeLater(() -> {
                        refreshPendingRequests();
                        refreshOnlineFriends();
                    });

                } catch (Exception ignored) {
                    /* evitar que caiga el refresco */ }
            }, 0, 1, TimeUnit.SECONDS);
        }
    }

    private void stopAutoRefresh() {
        try {
            if (refreshTask != null) {
                refreshTask.cancel(true);
                refreshTask = null;
            }
            if (scheduler != null) {
                scheduler.shutdownNow();
                scheduler = null;
            }
        } catch (Exception ignored) {
        }
    }

    private Map<String, Integer> safeCheckNewMessages() {
        try {
            Map<String, Integer> flags = client.checkNewMessages(); // TODO
            return (flags != null) ? flags : Collections.emptyMap();
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    // ---------- Helpers UI ----------
    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(mainFrame, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(mainFrame, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ---------- Main de prueba local (opcional, elimina en producción) ----------
    public static void main(String[] args) {
        // TODO: reemplaza DummyCliente por tu implementación real
        Cliente cliente = new DummyCliente();
        SwingUtilities.invokeLater(() -> new ModernChatUI(cliente));
    }

    // ---------- Stub para pruebas locales (borra si no lo necesitas) ----------
    private static class DummyCliente implements Cliente {
        private final Map<String, List<String>> inbox = new HashMap<>();
        private final Set<String> online = new LinkedHashSet<>();
        private final Set<String> chats = new LinkedHashSet<>();
        private final List<String> pending = new ArrayList<>();
        private String user;

        @Override
        public boolean login(String user, String pass, int clientRmiPort) {
            this.user = user;
            online.add(user);
            // simula que “Hugo” entra antes y “Guille” después
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    online.add("Guille");
                    chats.add("Guille");
                    inbox.computeIfAbsent("Guille", k -> new ArrayList<>()).add("¡Hola, soy Guille!");
                }
            }, 4000);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    online.add("Hugo");
                    chats.add("Hugo");
                    inbox.computeIfAbsent("Hugo", k -> new ArrayList<>()).add("¡Hola, soy Hugo!");
                }
            }, 2000);
            // simula solicitud pendiente
            if (!"Hugo".equals(user))
                pending.add("Hugo");
            return true;
        }

        @Override
        public void logout() {
            online.remove(user);
            this.user = null;
        }

        @Override
        public boolean register(String user, String pass) {
            return true;
        }

        @Override
        public boolean sendFriendRequest(String targetUser) {
            pending.add(targetUser);
            return true;
        }

        @Override
        public List<String> getPendingFriendRequests() {
            return new ArrayList<>(pending);
        }

        @Override
        public boolean acceptFriendRequest(String fromUser) {
            pending.remove(fromUser);
            chats.add(fromUser);
            return true;
        }

        @Override
        public boolean rejectFriendRequest(String fromUser) {
            pending.remove(fromUser);
            return true;
        }

        @Override
        public List<String> getOnlineFriends() {
            return new ArrayList<>(online);
        }

        @Override
        public List<String> getActiveChats() {
            return new ArrayList<>(chats);
        }

        @Override
        public Map<String, Integer> checkNewMessages() {
            Map<String, Integer> m = new HashMap<>();
            for (String c : chats)
                m.put(c, inbox.getOrDefault(c, Collections.emptyList()).size());
            return m;
        }

        @Override
        public List<String> getNewMessages(String contact) {
            return inbox.remove(contact);
        }

        @Override
        public void sendMessage(String contact, String text) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    inbox.computeIfAbsent(contact, k -> new ArrayList<>()).add(contact + " eco: " + text);
                }
            }, 1000);
        }
    }

    // ---------- Interfaz mínima esperada por esta UI ----------
    public interface Cliente {
        // Sesión
        boolean login(String user, String pass, int clientRmiPort);

        void logout();

        // Registro
        boolean register(String user, String pass); // TODO: si tu firma difiere, ajusta openRegisterDialog()

        // Amistades / solicitudes
        boolean sendFriendRequest(String targetUser); // enviar solicitud

        List<String> getPendingFriendRequests(); // solicitudes recibidas pendientes

        boolean acceptFriendRequest(String fromUser);

        boolean rejectFriendRequest(String fromUser);

        // Amigos online / chats
        List<String> getOnlineFriends(); // amigos conectados ahora

        List<String> getActiveChats(); // chats “materializados” por el cliente

        Map<String, Integer> checkNewMessages(); // mapa contacto -> nº no leídos (>0)

        List<String> getNewMessages(String contact); // drenar mensajes del contacto actual

        void sendMessage(String contact, String text); // P2P (RMI entre clientes)
    }
}