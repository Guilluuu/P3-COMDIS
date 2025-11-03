// ModernChatUI.java
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Interfaz gráfica moderna para aplicación de chat.
 * Utiliza Java Swing con diseño limpio y componentes estilizados.
 */
public class ModernChatUI {
    
    // ============ COMPONENTES PRINCIPALES ============
    private final JFrame mainFrame;
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final ChatClientInterface client;
    
    // Timer para actualizar mensajes automáticamente
    private ScheduledExecutorService scheduler;
    
    // Usuario actual
    private String currentUser;
    
    // ============ PANELES ============
    private JPanel loginPanel;
    private JPanel mainAppPanel;
    private JTabbedPane mainTabbedPane;
    
    // ============ COMPONENTES DE LOGIN ============
    private JTextField loginUserField;
    private JPasswordField loginPassField;
    private JTextField loginPortField;
    private JProgressBar loginProgress;
    
    // ============ COMPONENTES DE CHAT ============
    private JList<String> contactsList;
    private DefaultListModel<String> contactsListModel;
    private JTextArea chatArea;
    private JTextField messageField;
    private JLabel currentChatLabel;
    private String currentChatContact;
    
    // ============ COMPONENTES DE AMIGOS ============
    private JList<String> friendsList;
    private DefaultListModel<String> friendsListModel;
    private JList<String> searchResultsList;
    private DefaultListModel<String> searchResultsModel;
    private JTextField searchField;
    
    // ============ COMPONENTES DE SOLICITUDES ============
    private JList<String> requestsList;
    private DefaultListModel<String> requestsListModel;
    
    // ============ PALETA DE COLORES ============
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private static final Color SECONDARY_COLOR = new Color(240, 248, 255);
    private static final Color ACCENT_COLOR = new Color(30, 144, 255);
    private static final Color SUCCESS_COLOR = new Color(34, 139, 34);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);
    
    // ============ ICONOS (emojis) ============
    private static final String USER_ICON = "👤";
    private static final String CHAT_ICON = "💬";
    private static final String FRIENDS_ICON = "👥";
    private static final String REQUESTS_ICON = "📨";
    private static final String SETTINGS_ICON = "⚙️";
    private static final String ONLINE_ICON = "🟢";
    
    /**
     * Constructor principal de la interfaz.
     * @param client Implementación del cliente de chat
     */
    public ModernChatUI(ChatClientInterface client) {
        this.client = client;
        this.cardLayout = new CardLayout();
        this.mainPanel = new JPanel(cardLayout);
        this.mainFrame = new JFrame("ChatApp - Mensajería Instantánea");
        
        initialize();
    }
    
    // ============ INICIALIZACIÓN ============
    
    private void initialize() {
        setupMainFrame();
        createLoginPanel();
        createMainAppPanel();
        setupCardLayout();
        
        mainFrame.setVisible(true);
    }
    
    private void setupMainFrame() {
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(900, 650);
        mainFrame.setMinimumSize(new Dimension(800, 600));
        mainFrame.setLocationRelativeTo(null);
        
        // Cleanup al cerrar
        mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cleanup();
            }
        });
    }
    
    private void setupCardLayout() {
        mainPanel.add(loginPanel, "login");
        mainPanel.add(mainAppPanel, "main");
        mainFrame.add(mainPanel);
        cardLayout.show(mainPanel, "login");
    }
    
    // ============ PANEL DE LOGIN ============
    
    private void createLoginPanel() {
        loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(SECONDARY_COLOR);
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2, true),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        contentPanel.setPreferredSize(new Dimension(400, 450));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        
        // Título
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("🚀 ChatApp", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(PRIMARY_COLOR);
        contentPanel.add(titleLabel, gbc);
        
        gbc.gridy = 1;
        JLabel subtitleLabel = new JLabel("Mensajería Instantánea", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        contentPanel.add(subtitleLabel, gbc);
        
        // Campos de formulario
        gbc.gridwidth = 1;
        gbc.gridy = 2;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Usuario:"), gbc);
        gbc.gridx = 1;
        loginUserField = createStyledTextField();
        contentPanel.add(loginUserField, gbc);
        
        gbc.gridy = 3;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        loginPassField = new JPasswordField();
        styleTextField(loginPassField);
        contentPanel.add(loginPassField, gbc);
        
        gbc.gridy = 4;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Puerto:"), gbc);
        gbc.gridx = 1;
        loginPortField = createStyledTextField();
        loginPortField.setText("1234");
        contentPanel.add(loginPortField, gbc);
        
        // Barra de progreso
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        loginProgress = new JProgressBar();
        loginProgress.setVisible(false);
        loginProgress.setIndeterminate(true);
        contentPanel.add(loginProgress, gbc);
        
        // Botones
        gbc.gridy = 6;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton loginButton = createPrimaryButton("Iniciar Sesión");
        loginButton.addActionListener(_ -> performLogin());
        
        JButton registerButton = createSecondaryButton("Registrarse");
        registerButton.addActionListener(_ -> showRegisterDialog());
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        contentPanel.add(buttonPanel, gbc);
        
        // Enter key listeners
        loginPassField.addActionListener(_ -> performLogin());
        loginPortField.addActionListener(_ -> performLogin());
        
        loginPanel.add(contentPanel);
    }
    
    // ============ PANEL PRINCIPAL DE LA APP ============
    
    private void createMainAppPanel() {
        mainAppPanel = new JPanel(new BorderLayout());
        mainAppPanel.setBackground(Color.WHITE);
        
        createHeader();
        createMainTabs();
    }
    
    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        headerPanel.setPreferredSize(new Dimension(0, 60));
        
        // Logo
        JLabel titleLabel = new JLabel("💬 ChatApp");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Usuario y logout
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setBackground(PRIMARY_COLOR);
        
        JLabel usernameLabel = new JLabel();
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        usernameLabel.setForeground(Color.WHITE);
        
        JButton logoutButton = createTextButton("Cerrar Sesión");
        logoutButton.addActionListener(_ -> performLogout());
        
        userPanel.add(usernameLabel);
        userPanel.add(Box.createHorizontalStrut(10));
        userPanel.add(logoutButton);
        headerPanel.add(userPanel, BorderLayout.EAST);
        
        mainAppPanel.add(headerPanel, BorderLayout.NORTH);
    }
    
    private void createMainTabs() {
        mainTabbedPane = new JTabbedPane(JTabbedPane.TOP);
        mainTabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        mainTabbedPane.addTab(CHAT_ICON + " Chat", createChatPanel());
        mainTabbedPane.addTab(FRIENDS_ICON + " Amigos", createFriendsPanel());
        mainTabbedPane.addTab(REQUESTS_ICON + " Solicitudes", createRequestsPanel());
        mainTabbedPane.addTab(SETTINGS_ICON + " Perfil", createProfilePanel());
        
        mainAppPanel.add(mainTabbedPane, BorderLayout.CENTER);
    }
    
    // ============ PANEL DE CHAT ============
    
    private JPanel createChatPanel() {
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBackground(Color.WHITE);
        chatPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Lista de contactos (izquierda)
        JPanel contactsPanel = new JPanel(new BorderLayout());
        contactsPanel.setBorder(BorderFactory.createTitledBorder("💬 Chats Activos"));
        contactsPanel.setPreferredSize(new Dimension(200, 0));
        
        contactsListModel = new DefaultListModel<>();
        contactsList = new JList<>(contactsListModel);
        contactsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactsList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contactsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && contactsList.getSelectedValue() != null) {
                openChat(contactsList.getSelectedValue());
            }
        });
        
        contactsPanel.add(new JScrollPane(contactsList), BorderLayout.CENTER);
        
        // Panel principal del chat (derecha)
        JPanel chatMainPanel = new JPanel(new BorderLayout());
        chatMainPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        
        // Header del chat
        currentChatLabel = new JLabel("Selecciona un chat para comenzar");
        currentChatLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        currentChatLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        chatMainPanel.add(currentChatLabel, BorderLayout.NORTH);
        
        // Área de mensajes
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.getVerticalScrollBar().setUnitIncrement(16);
        chatMainPanel.add(chatScroll, BorderLayout.CENTER);
        
        // Panel de envío de mensajes
        JPanel messagePanel = new JPanel(new BorderLayout(10, 0));
        messagePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        messageField = createStyledTextField();
        messageField.setEnabled(false);
        messageField.setPreferredSize(new Dimension(0, 40));
        messageField.addActionListener(_ -> sendMessage());
        
        JButton sendButton = createPrimaryButton("Enviar");
        sendButton.setEnabled(false);
        sendButton.setPreferredSize(new Dimension(100, 40));
        sendButton.addActionListener(_ -> sendMessage());
        
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);
        chatMainPanel.add(messagePanel, BorderLayout.SOUTH);
        
        chatPanel.add(contactsPanel, BorderLayout.WEST);
        chatPanel.add(chatMainPanel, BorderLayout.CENTER);
        
        return chatPanel;
    }
    
    // ============ PANEL DE AMIGOS ============
    
    private JPanel createFriendsPanel() {
        JPanel friendsPanel = new JPanel(new BorderLayout());
        friendsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Panel de búsqueda (arriba)
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createTitledBorder("🔍 Buscar Usuarios"));
        
        JPanel searchInputPanel = new JPanel(new BorderLayout(10, 0));
        searchField = createStyledTextField();
        searchField.setPreferredSize(new Dimension(0, 35));
        searchField.addActionListener(_ -> searchUsers());
        
        JButton searchButton = createPrimaryButton("Buscar");
        searchButton.setPreferredSize(new Dimension(100, 35));
        searchButton.addActionListener(_ -> searchUsers());
        
        searchInputPanel.add(searchField, BorderLayout.CENTER);
        searchInputPanel.add(searchButton, BorderLayout.EAST);
        searchPanel.add(searchInputPanel, BorderLayout.NORTH);
        
        // Resultados de búsqueda
        searchResultsModel = new DefaultListModel<>();
        searchResultsList = new JList<>(searchResultsModel);
        searchResultsList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JScrollPane resultsScroll = new JScrollPane(searchResultsList);
        resultsScroll.setBorder(BorderFactory.createTitledBorder("Resultados"));
        searchPanel.add(resultsScroll, BorderLayout.CENTER);
        
        JButton sendRequestButton = createSuccessButton("Enviar Solicitud");
        sendRequestButton.setEnabled(false);
        sendRequestButton.addActionListener(_ -> sendFriendRequest());
        searchResultsList.addListSelectionListener(_ -> 
            sendRequestButton.setEnabled(searchResultsList.getSelectedValue() != null)
        );
        searchPanel.add(sendRequestButton, BorderLayout.SOUTH);
        
        // Lista de amigos (abajo)
        JPanel friendsListPanel = new JPanel(new BorderLayout());
        friendsListPanel.setBorder(BorderFactory.createTitledBorder("👥 Mis Amigos"));
        
        friendsListModel = new DefaultListModel<>();
        friendsList = new JList<>(friendsListModel);
        friendsList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        friendsListPanel.add(new JScrollPane(friendsList), BorderLayout.CENTER);
        
        // Layout con split
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, searchPanel, friendsListPanel);
        splitPane.setResizeWeight(0.6);
        splitPane.setDividerLocation(300);
        
        friendsPanel.add(splitPane, BorderLayout.CENTER);
        return friendsPanel;
    }
    
    // ============ PANEL DE SOLICITUDES ============
    
    private JPanel createRequestsPanel() {
        JPanel requestsPanel = new JPanel(new BorderLayout());
        requestsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("📨 Solicitudes Pendientes"));
        
        requestsListModel = new DefaultListModel<>();
        requestsList = new JList<>(requestsListModel);
        requestsList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        listPanel.add(new JScrollPane(requestsList), BorderLayout.CENTER);
        
        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton acceptButton = createSuccessButton("Aceptar");
        acceptButton.setEnabled(false);
        acceptButton.addActionListener(_ -> acceptSelectedRequest());
        
        JButton rejectButton = createDangerButton("Rechazar");
        rejectButton.setEnabled(false);
        rejectButton.addActionListener(_ -> rejectSelectedRequest());
        
        JButton refreshButton = createSecondaryButton("Actualizar");
        refreshButton.addActionListener(_ -> refreshRequests());
        
        requestsList.addListSelectionListener(_ -> {
            boolean hasSelection = requestsList.getSelectedValue() != null;
            acceptButton.setEnabled(hasSelection);
            rejectButton.setEnabled(hasSelection);
        });
        
        buttonPanel.add(acceptButton);
        buttonPanel.add(rejectButton);
        buttonPanel.add(refreshButton);
        
        requestsPanel.add(listPanel, BorderLayout.CENTER);
        requestsPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return requestsPanel;
    }
    
    // ============ PANEL DE PERFIL ============
    
    private JPanel createProfilePanel() {
        JPanel profilePanel = new JPanel(new GridBagLayout());
        profilePanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        
        // Icono de perfil
        JLabel profileIcon = new JLabel(USER_ICON, JLabel.CENTER);
        profileIcon.setFont(new Font("Segoe UI", Font.PLAIN, 64));
        profilePanel.add(profileIcon, gbc);
        
        // Info del usuario
        gbc.gridy = 1;
        JLabel userLabel = new JLabel("Usuario Conectado:", JLabel.CENTER);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        userLabel.setForeground(Color.GRAY);
        profilePanel.add(userLabel, gbc);
        
        gbc.gridy = 2;
        JLabel currentUserLabel = new JLabel("", JLabel.CENTER);
        currentUserLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        currentUserLabel.setForeground(PRIMARY_COLOR);
        profilePanel.add(currentUserLabel, gbc);
        
        // Botones
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        JButton statsButton = createPrimaryButton("Estadísticas");
        statsButton.addActionListener(_ -> showStats());
        profilePanel.add(statsButton, gbc);
        
        return profilePanel;
    }
    
    // ============ FUNCIONALIDAD DE LOGIN ============
    
    private void performLogin() {
        String username = loginUserField.getText().trim();
        String password = new String(loginPassField.getPassword());
        String portText = loginPortField.getText().trim();
        
        if (username.isEmpty() || password.isEmpty() || portText.isEmpty()) {
            showError("Todos los campos son obligatorios");
            return;
        }
        
        try {
            int port = Integer.parseInt(portText);
            
            loginProgress.setVisible(true);
            
            // Ejecutar login en background
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return client.login(username, password, port);
                }
                
                @Override
                protected void done() {
                    loginProgress.setVisible(false);
                    try {
                        if (get()) {
                            currentUser = username;
                            onLoginSuccess();
                        } else {
                            showError("Login fallido. Verifica tus credenciales.");
                        }
                    } catch (Exception e) {
                        showError("Error durante el login: " + e.getMessage());
                    }
                }
            };
            worker.execute();
            
        } catch (NumberFormatException e) {
            showError("El puerto debe ser un número válido");
        }
    }
    
    private void showRegisterDialog() {
        JDialog dialog = new JDialog(mainFrame, "Crear Nueva Cuenta", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(mainFrame);
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Campos
        gbc.gridx = 0; gbc.gridy = 0;
        contentPanel.add(createLabel("Usuario:"), gbc);
        gbc.gridx = 1;
        JTextField userField = createStyledTextField();
        contentPanel.add(userField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        contentPanel.add(createLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        JPasswordField passField = new JPasswordField();
        styleTextField(passField);
        contentPanel.add(passField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        contentPanel.add(createLabel("Confirmar:"), gbc);
        gbc.gridx = 1;
        JPasswordField confirmField = new JPasswordField();
        styleTextField(confirmField);
        contentPanel.add(confirmField, gbc);
        
        // Botones
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton registerBtn = createPrimaryButton("Registrar");
        JButton cancelBtn = createSecondaryButton("Cancelar");
        
        registerBtn.addActionListener(_ -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword());
            String confirm = new String(confirmField.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                showError("Usuario y contraseña son obligatorios");
                return;
            }
            
            if (!password.equals(confirm)) {
                showError("Las contraseñas no coinciden");
                return;
            }
            
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return client.signUp(username, password);
                }
                
                @Override
                protected void done() {
                    try {
                        if (get()) {
                            showInfo("✅ Cuenta creada exitosamente");
                            dialog.dispose();
                        } else {
                            showError("Error al crear cuenta. El usuario puede ya existir.");
                        }
                    } catch (Exception ex) {
                        showError("Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void refreshFriendsList() {
        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() {
                return client.getFriends();
            }
            
            @Override
            protected void done() {
                try {
                    friendsListModel.clear();
                    List<String> friends = get();
                    for (String friend : friends) {
                        friendsListModel.addElement(friend);
                    }
                } catch (Exception e) {
                    System.err.println("Error cargando amigos: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    // ============ FUNCIONALIDAD DE SOLICITUDES ============
    
    private void refreshRequests() {
        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() {
                return client.getPendingFriendRequests();
            }
            
            @Override
            protected void done() {
                try {
                    requestsListModel.clear();
                    List<String> requests = get();
                    for (String request : requests) {
                        requestsListModel.addElement(request);
                    }
                } catch (Exception e) {
                    System.err.println("Error cargando solicitudes: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void acceptSelectedRequest() {
        String selected = requestsList.getSelectedValue();
        if (selected == null) return;
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return client.acceptFriendRequest(selected);
            }
            
            @Override
            protected void done() {
                try {
                    if (get()) {
                        showInfo("✅ Solicitud de " + selected + " aceptada");
                        refreshRequests();
                        updateContactsList();
                        refreshFriendsList();
                    } else {
                        showError("Error aceptando solicitud");
                    }
                } catch (Exception e) {
                    showError("Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void rejectSelectedRequest() {
        String selected = requestsList.getSelectedValue();
        if (selected == null) return;
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return client.rejectFriendRequest(selected);
            }
            
            @Override
            protected void done() {
                try {
                    if (get()) {
                        showInfo("Solicitud de " + selected + " rechazada");
                        refreshRequests();
                    } else {
                        showError("Error rechazando solicitud");
                    }
                } catch (Exception e) {
                    showError("Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    // ============ ACTUALIZACIÓN AUTOMÁTICA ============
    
    /**
     * Inicia el polling automático de mensajes nuevos cada 2 segundos.
     */
    private void startMessagePolling() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            if (client.isConnected()) {
                checkForNewMessages();
            }
        }, 2, 2, TimeUnit.SECONDS);
    }
    
    /**
     * Detiene el polling de mensajes.
     */
    private void stopMessagePolling() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
    
    /**
     * Verifica si hay mensajes nuevos y actualiza la UI.
     */
    private void checkForNewMessages() {
        try {
            Map<String, Integer> newMessages = client.checkNewMessages();
            
            SwingUtilities.invokeLater(() -> {
                for (Map.Entry<String, Integer> entry : newMessages.entrySet()) {
                    String contact = entry.getKey();
                    int count = entry.getValue();
                    
                    if (count > 0) {
                        // Si es el chat actual, cargar mensajes
                        if (contact.equals(currentChatContact)) {
                            loadChatMessages(contact);
                        }
                        // Actualizar lista de contactos si es necesario
                        if (!contactsListModel.contains(contact)) {
                            contactsListModel.addElement(contact);
                        }
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Error checking messages: " + e.getMessage());
        }
    }
    
    // ============ UTILIDADES DE UI ============
    
    private void updateContactsList() {
        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() {
                return client.getActiveChats();
            }
            
            @Override
            protected void done() {
                try {
                    contactsListModel.clear();
                    List<String> chats = get();
                    for (String chat : chats) {
                        contactsListModel.addElement(chat);
                    }
                } catch (Exception e) {
                    System.err.println("Error cargando contactos: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void showStats() {
        JOptionPane.showMessageDialog(mainFrame,
            "📊 Estadísticas:\n" +
            "• Chats activos: " + contactsListModel.size() + "\n" +
            "• Amigos: " + friendsListModel.size() + "\n" +
            "• Solicitudes pendientes: " + requestsListModel.size(),
            "Estadísticas",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void clearLoginFields() {
        loginUserField.setText("");
        loginPassField.setText("");
        loginPortField.setText("1234");
    }
    
    private void clearAppData() {
        contactsListModel.clear();
        friendsListModel.clear();
        searchResultsModel.clear();
        requestsListModel.clear();
        chatArea.setText("");
        currentChatContact = null;
        currentChatLabel.setText("Selecciona un chat para comenzar");
        messageField.setEnabled(false);
    }
    
    private void cleanup() {
        stopMessagePolling();
        if (client.isConnected()) {
            client.logout();
        }
    }
    
    // ============ COMPONENTES ESTILIZADOS ============
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return label;
    }
    
    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        styleTextField(field);
        field.setPreferredSize(new Dimension(200, 35));
        return field;
    }
    
    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }
    
    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(ACCENT_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
            }
        });
        
        return button;
    }
    
    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setBackground(Color.WHITE);
        button.setForeground(PRIMARY_COLOR);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private JButton createSuccessButton(String text) {
        JButton button = createPrimaryButton(text);
        button.setBackground(SUCCESS_COLOR);
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0, 100, 0));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(SUCCESS_COLOR);
            }
        });
        
        return button;
    }
    
    private JButton createDangerButton(String text) {
        JButton button = createPrimaryButton(text);
        button.setBackground(DANGER_COLOR);
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(200, 35, 51));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(DANGER_COLOR);
            }
        });
        
        return button;
    }
    
    private JButton createTextButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(0, 0, 0, 0));
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setForeground(Color.LIGHT_GRAY);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setForeground(Color.WHITE);
            }
        });
        
        return button;
    }
    
    // ============ DIÁLOGOS ============
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(mainFrame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showInfo(String message) {
        JOptionPane.showMessageDialog(mainFrame, message, "Información", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // ============ MAIN ============
    
    /**
     * Punto de entrada principal.
     * Crea la UI con una implementación del cliente.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Aquí debes proporcionar tu implementación de ChatClientInterface
            // ChatClientInterface client = new TuImplementacionCliente();
            // new ModernChatUI(client);
            
            System.out.println("Por favor proporciona una implementación de ChatClientInterface");
        });
    }
} + ex.getMessage());
                    }
                }
            };
            worker.execute();
        });
        
        cancelBtn.addActionListener(_ -> dialog.dispose());
        
        buttonPanel.add(registerBtn);
        buttonPanel.add(cancelBtn);
        contentPanel.add(buttonPanel, gbc);
        
        dialog.add(contentPanel);
        dialog.setVisible(true);
    }
    
    private void onLoginSuccess() {
        cardLayout.show(mainPanel, "main");
        mainFrame.setTitle("ChatApp - " + currentUser);
        
        // Actualizar interfaz
        updateContactsList();
        refreshFriendsList();
        refreshRequests();
        
        // Iniciar polling de mensajes cada 2 segundos
        startMessagePolling();
        
        showInfo("Bienvenido, " + currentUser + "! 🎉");
    }
    
    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(mainFrame,
            "¿Estás seguro de que quieres cerrar sesión?",
            "Confirmar cierre de sesión",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            stopMessagePolling();
            client.logout();
            cardLayout.show(mainPanel, "login");
            clearLoginFields();
            clearAppData();
        }
    }
    
    // ============ FUNCIONALIDAD DE MENSAJERÍA ============
    
    private void openChat(String contact) {
        currentChatContact = contact;
        currentChatLabel.setText("💬 Chat con " + contact);
        chatArea.setText("");
        messageField.setEnabled(true);
        
        // Obtener el botón de enviar (necesitamos guardarlo como field)
        Component[] components = ((JPanel)messageField.getParent()).getComponents();
        for (Component c : components) {
            if (c instanceof JButton button) {
                button.setEnabled(true);
            }
        }
        
        loadChatMessages(contact);
        messageField.requestFocus();
    }
    
    private void loadChatMessages(String contact) {
        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() {
                return client.getNewMessages(contact);
            }
            
            @Override
            protected void done() {
                try {
                    List<String> messages = get();
                    for (String msg : messages) {
                        chatArea.append(msg + "\n");
                    }
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
                } catch (Exception e) {
                    System.err.println("Error cargando mensajes: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (message.isEmpty() || currentChatContact == null) {
            return;
        }
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return client.sendMessage(currentChatContact, message);
            }
            
            @Override
            protected void done() {
                try {
                    if (get()) {
                        chatArea.append("Tú: " + message + "\n");
                        messageField.setText("");
                        chatArea.setCaretPosition(chatArea.getDocument().getLength());
                    } else {
                        showError("Error al enviar mensaje");
                    }
                } catch (Exception e) {
                    showError("Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    // ============ FUNCIONALIDAD DE AMIGOS ============
    
    private void searchUsers() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            return;
        }
        
        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() {
                return client.searchUsers(query);
            }
            
            @Override
            protected void done() {
                try {
                    searchResultsModel.clear();
                    List<String> results = get();
                    for (String user : results) {
                        if (!user.equals(currentUser)) {
                            searchResultsModel.addElement(user);
                        }
                    }
                    if (searchResultsModel.isEmpty()) {
                        showInfo("No se encontraron usuarios");
                    }
                } catch (Exception e) {
                    showError("Error buscando usuarios: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void sendFriendRequest() {
        String selectedUser = searchResultsList.getSelectedValue();
        if (selectedUser == null || selectedUser.equals(currentUser)) {
            return;
        }
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return client.sendFriendRequest(selectedUser);
            }
            
            @Override
            protected void done() {
                try {
                    if (get()) {
                        showInfo("✅ Solicitud enviada a " + selectedUser);
                        searchField.setText("");
                        searchResultsModel.clear();
                    } else {
                        showError("Error al enviar solicitud");
                    }
                } catch (Exception e) {
                    showError("Error: "
