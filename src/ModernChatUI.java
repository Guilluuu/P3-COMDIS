import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * ModernChatUI - UI de ejemplo con:
 * - Login
 * - Lista de chats con badge "• nuevo"
 * - Área de conversación
 * - Auto-refresco cada 1s que:
 * a) repuebla la lista de chats,
 * b) marca no leídos,
 * c) drena mensajes entrantes del chat abierto.
 *
 * Requisitos mínimos de Cliente:
 * - boolean login(String user, String pass)
 * - void logout()
 * - List<String> getActiveChats()
 * - Map<String, Integer> checkNewMessages() // 0/1 (o contador) por contacto
 * - List<String> getNewMessages(String contact)
 * - void sendMessage(String contact, String text)
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
    private JButton loginBtn;
    private JLabel loginMsg;
    private JTextField portField;

    // --- Panel principal (chat) ---
    private JPanel mainPanel;
    private DefaultListModel<String> contactsModel;
    private JList<String> contactsList;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendBtn;
    private JLabel chatTitle;

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
        mainFrame.setMinimumSize(new Dimension(950, 600));

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
        gc.gridx = 0;

        JLabel title = new JLabel("Iniciar sesión");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        gc.gridy = 0;
        panel.add(title, gc);

        userField = new JTextField();
        userField.putClientProperty("JTextField.placeholderText", "Usuario");
        gc.gridy = 1;
        panel.add(userField, gc);

        passField = new JPasswordField();
        passField.putClientProperty("JTextField.placeholderText", "Contraseña");
        gc.gridy = 2;
        panel.add(passField, gc);

        portField = new JTextField();
        portField.putClientProperty("JTextField.placeholderText", "Puerto RMI cliente (p. ej. 1099)");
        gc.gridy = 3;
        panel.add(portField, gc);

        loginBtn = new JButton("Entrar");
        gc.gridy = 4;
        panel.add(loginBtn, gc);

        loginMsg = new JLabel(" ");
        loginMsg.setForeground(new Color(180, 0, 0));
        gc.gridy = 5;
        panel.add(loginMsg, gc);

        // Eventos
        ActionListener doLogin = e -> performLogin();
        loginBtn.addActionListener(doLogin);
        passField.addActionListener(doLogin);

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
        JSplitPane split = new JSplitPane();
        split.setResizeWeight(0.25);
        split.setDividerSize(6);

        // Lista de contactos
        JPanel left = new JPanel(new BorderLayout());
        left.setBorder(new EmptyBorder(8, 8, 8, 4));
        JLabel contactsLbl = new JLabel("Chats");
        contactsLbl.setBorder(new EmptyBorder(0, 0, 6, 0));
        left.add(contactsLbl, BorderLayout.NORTH);

        contactsModel = new DefaultListModel<>();
        contactsList = new JList<>(contactsModel);
        contactsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactsList.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Renderer con badge "• nuevo"
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

        left.add(new JScrollPane(contactsList), BorderLayout.CENTER);

        // Área de chat
        JPanel right = new JPanel(new BorderLayout());
        right.setBorder(new EmptyBorder(8, 4, 8, 8));
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        right.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        split.setLeftComponent(left);
        split.setRightComponent(right);
        return split;
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
        Integer port = Integer.parseInt(portField.getText().trim());

        if (u.isEmpty() || p.isEmpty()) {
            loginMsg.setText("Usuario y contraseña requeridos.");
            return;
        }
        try {
            // TODO: ajusta si tu Cliente usa otra firma/retorno
            boolean ok = client.login(u, p, port); // ⬅️ aquí va el puerto RMI del cliente
            if (ok) {
                this.currentUser = u;
                onLoginSuccess();
            } else {
                loginMsg.setText("Credenciales inválidas.");
            }
        } catch (Exception ex) {
            loginMsg.setText("Error de conexión.");
        }
    }

    private void onLoginSuccess() {
        cardLayout.show(root, "main");
        mainFrame.setTitle("ChatApp - " + currentUser);
        updateContactsList();
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
        chatArea.setText("");
        messageField.setEnabled(false);
        sendBtn.setEnabled(false);
        cardLayout.show(root, "login");
        mainFrame.setTitle("ChatApp");
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
        // TODO: si tu Cliente expone un método de histórico, úsalo aquí.
        try {
            List<String> nuevos = client.getNewMessages(contact);
            if (nuevos != null) {
                for (String m : nuevos) {
                    appendInboundMessage(m);
                }
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

    // ---------- Refresco de listas ----------
    private void updateContactsList() {
        try {
            List<String> actives = client.getActiveChats();
            Map<String, Integer> flags = safeCheckNewMessages();

            String sel = contactsList.getSelectedValue();
            contactsModel.clear();
            unread.clear();

            if (actives != null) {
                for (String c : actives) {
                    contactsModel.addElement(c);
                    if (flags.getOrDefault(c, 0) > 0) {
                        unread.add(c);
                    }
                }
            }

            if (sel != null && actives != null && actives.contains(sel)) {
                contactsList.setSelectedValue(sel, true);
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
                    List<String> actives = client.getActiveChats();
                    Map<String, Integer> flags = safeCheckNewMessages();

                    SwingUtilities.invokeLater(() -> {
                        String sel = contactsList.getSelectedValue();
                        contactsModel.clear();
                        unread.clear();

                        if (actives != null) {
                            for (String c : actives) {
                                contactsModel.addElement(c);
                                if (flags.getOrDefault(c, 0) > 0)
                                    unread.add(c);
                            }
                        }

                        if (sel != null && actives != null && actives.contains(sel)) {
                            contactsList.setSelectedValue(sel, true);
                        }
                    });

                    // drenar mensajes del chat abierto
                    if (currentChatContact != null) {
                        List<String> nuevos = client.getNewMessages(currentChatContact);
                        if (nuevos != null && !nuevos.isEmpty()) {
                            SwingUtilities.invokeLater(() -> {
                                for (String msg : nuevos)
                                    appendInboundMessage(msg);
                                unread.remove(currentChatContact);
                                contactsList.repaint();
                            });
                        }
                    }
                } catch (Exception ignored) {
                    /* evitar parar el scheduler por una excepción */ }
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
            Map<String, Integer> flags = client.checkNewMessages();
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

    // ---------- Main de prueba local (opcional) ----------
    public static void main(String[] args) {
        Cliente cliente = new Cliente();
        SwingUtilities.invokeLater(() -> new ModernChatUI(cliente));
    }
}