import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ModernChatUI {
    private JFrame mainFrame;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private ChatClientInterface client;
    
    private JTextField loginUserField, loginPassField, loginPortField;
    private JTextArea chatArea;
    private JTextField messageField, searchField;
    private DefaultListModel<String> contactsModel, friendsModel, searchModel, requestsModel;
    private String currentUser, currentChatContact;
    
    private static final Color PRIMARY = new Color(70, 130, 180);
    private static final Color SECONDARY = new Color(240, 248, 255);
    
    public ModernChatUI(ChatClientInterface client) {
        this.client = client;
        initialize();
    }
    
    private void initialize() {
        setupMainFrame();
        createLoginPanel();
        createMainPanel();
        mainFrame.setVisible(true);
    }
    
    private void setupMainFrame() {
        mainFrame = new JFrame("ChatApp");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1000, 700); // Ventana más grande
        mainFrame.setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainFrame.add(mainPanel);
    }
    
    private void createLoginPanel() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(SECONDARY);
        
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY, 2),
            BorderFactory.createEmptyBorder(40, 40, 40, 40) // Más padding
        ));
        content.setPreferredSize(new Dimension(450, 500)); // Más grande
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15); // Más espacio
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Título
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel title = new JLabel("🚀 ChatApp", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32)); // Más grande
        title.setForeground(PRIMARY);
        content.add(title, gbc);
        
        gbc.gridy = 1;
        JLabel subtitle = new JLabel("Mensajería Instantánea", JLabel.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(Color.GRAY);
        content.add(subtitle, gbc);
        
        // Campos más grandes
        gbc.gridwidth = 1;
        loginUserField = addField(content, gbc, "Usuario:", 2, 300); // Ancho aumentado
        loginPassField = addField(content, gbc, "Contraseña:", 3, 300);
        loginPortField = addField(content, gbc, "Puerto:", 4, 300);
        loginPortField.setText("1234");
        
        // Botones
        gbc.gridy = 5; gbc.gridwidth = 2;
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0)); // Más espacio entre botones
        JButton login = createButton("Iniciar Sesión", PRIMARY, 140, 45); // Botones más grandes
        JButton register = createButton("Registrarse", new Color(100, 100, 100), 120, 45);
        
        login.addActionListener(e -> performLogin());
        register.addActionListener(e -> showStyledRegisterDialog());
        
        buttons.add(login);
        buttons.add(register);
        content.add(buttons, gbc);
        
        loginPanel.add(content);
        mainPanel.add(loginPanel, "login");
    }
    
    private JTextField addField(JPanel parent, GridBagConstraints gbc, String label, int row, int width) {
        gbc.gridy = row; gbc.gridx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Texto más grande
        parent.add(lbl, gbc);
        
        gbc.gridx = 1;
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(width, 40)); // Campos más altos
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10) // Más padding interno
        ));
        parent.add(field, gbc);
        return field;
    }
    
    private void createMainPanel() {
        JPanel mainAppPanel = new JPanel(new BorderLayout());
        mainAppPanel.setBackground(Color.WHITE);
        
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        header.setPreferredSize(new Dimension(0, 70)); // Header más alto
        
        JLabel title = new JLabel("💬 ChatApp - " + (currentUser != null ? currentUser : ""));
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        
        JButton logout = createButton("Cerrar Sesión", new Color(200, 60, 60), 120, 35);
        logout.addActionListener(e -> performLogout());
        header.add(logout, BorderLayout.EAST);
        
        mainAppPanel.add(header, BorderLayout.NORTH);
        
        // Tabs principales
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        tabs.addTab("💬 Chat", createChatPanel());
        tabs.addTab("👥 Amigos", createFriendsPanel());
        tabs.addTab("📨 Solicitudes", createRequestsPanel());
        tabs.addTab("⚙️ Perfil", createProfilePanel());
        
        mainAppPanel.add(tabs, BorderLayout.CENTER);
        mainPanel.add(mainAppPanel, "main");
    }
    
    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Lista de contactos
        contactsModel = new DefaultListModel<>();
        JList<String> contactsList = new JList<>(contactsModel);
        contactsList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contactsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String contact = contactsList.getSelectedValue();
                if (contact != null) openChat(contact);
            }
        });
        
        JScrollPane contactsScroll = new JScrollPane(contactsList);
        contactsScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY), "💬 Chats Activos"));
        contactsScroll.setPreferredSize(new Dimension(250, 0));
        panel.add(contactsScroll, BorderLayout.WEST);
        
        // Área de chat principal
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        
        JLabel chatTitle = new JLabel("Selecciona un chat para comenzar");
        chatTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        chatTitle.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        chatPanel.add(chatTitle, BorderLayout.NORTH);
        
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        
        // Panel de envío de mensajes
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        messageField = new JTextField();
        messageField.setEnabled(false);
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageField.setPreferredSize(new Dimension(0, 45));
        messageField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        messageField.addActionListener(e -> sendMessage());
        
        JButton sendBtn = createButton("Enviar", PRIMARY, 100, 45);
        sendBtn.setEnabled(false);
        sendBtn.addActionListener(e -> sendMessage());
        
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);
        
        panel.add(chatPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createFriendsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Panel de búsqueda
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createTitledBorder("🔍 Buscar Usuarios"));
        
        JPanel searchInputPanel = new JPanel(new BorderLayout(10, 0));
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(0, 40));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        searchField.addActionListener(e -> searchUsers());
        
        JButton searchBtn = createButton("Buscar", PRIMARY, 100, 40);
        searchBtn.addActionListener(e -> searchUsers());
        
        searchInputPanel.add(searchField, BorderLayout.CENTER);
        searchInputPanel.add(searchBtn, BorderLayout.EAST);
        searchPanel.add(searchInputPanel, BorderLayout.NORTH);
        
        // Resultados de búsqueda
        searchModel = new DefaultListModel<>();
        JList<String> searchList = new JList<>(searchModel);
        searchList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JScrollPane searchScroll = new JScrollPane(searchList);
        searchScroll.setBorder(BorderFactory.createTitledBorder("Resultados"));
        searchScroll.setPreferredSize(new Dimension(0, 200));
        searchPanel.add(searchScroll, BorderLayout.CENTER);
        
        // Botón enviar solicitud
        JButton requestBtn = createButton("Enviar Solicitud", new Color(34, 139, 34), 0, 35);
        requestBtn.setEnabled(false);
        requestBtn.addActionListener(e -> sendFriendRequest());
        
        searchList.addListSelectionListener(e -> 
            requestBtn.setEnabled(searchList.getSelectedValue() != null)
        );
        searchPanel.add(requestBtn, BorderLayout.SOUTH);
        
        // Lista de amigos
        friendsModel = new DefaultListModel<>();
        JList<String> friendsList = new JList<>(friendsModel);
        friendsList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JScrollPane friendsScroll = new JScrollPane(friendsList);
        friendsScroll.setBorder(BorderFactory.createTitledBorder("👥 Mis Amigos"));
        
        // Layout dividido
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, searchPanel, friendsScroll);
        splitPane.setResizeWeight(0.6);
        splitPane.setDividerLocation(300);
        
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createRequestsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        requestsModel = new DefaultListModel<>();
        JList<String> requestsList = new JList<>(requestsModel);
        requestsList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JScrollPane requestsScroll = new JScrollPane(requestsList);
        requestsScroll.setBorder(BorderFactory.createTitledBorder("📨 Solicitudes Pendientes"));
        panel.add(requestsScroll, BorderLayout.CENTER);
        
        // Botones de solicitudes
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        
        JButton acceptBtn = createButton("Aceptar", new Color(34, 139, 34), 100, 35);
        JButton rejectBtn = createButton("Rechazar", new Color(220, 53, 69), 100, 35);
        JButton refreshBtn = createButton("Actualizar", new Color(100, 100, 100), 100, 35);
        
        acceptBtn.setEnabled(false);
        rejectBtn.setEnabled(false);
        
        acceptBtn.addActionListener(e -> acceptFriendRequest());
        rejectBtn.addActionListener(e -> rejectFriendRequest());
        refreshBtn.addActionListener(e -> refreshRequests());
        
        requestsList.addListSelectionListener(e -> {
            boolean hasSelection = requestsList.getSelectedValue() != null;
            acceptBtn.setEnabled(hasSelection);
            rejectBtn.setEnabled(hasSelection);
        });
        
        buttonPanel.add(acceptBtn);
        buttonPanel.add(rejectBtn);
        buttonPanel.add(refreshBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        panel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        
        JLabel icon = new JLabel("👤", JLabel.CENTER);
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 80));
        panel.add(icon, gbc);
        
        gbc.gridy = 1;
        JLabel userLabel = new JLabel("Usuario Conectado:", JLabel.CENTER);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        userLabel.setForeground(Color.GRAY);
        panel.add(userLabel, gbc);
        
        gbc.gridy = 2;
        JLabel currentUserLabel = new JLabel(currentUser != null ? currentUser : "", JLabel.CENTER);
        currentUserLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        currentUserLabel.setForeground(PRIMARY);
        panel.add(currentUserLabel, gbc);
        
        gbc.gridy = 3; gbc.gridwidth = 1;
        JButton statsBtn = createButton("Estadísticas", PRIMARY, 150, 45);
        statsBtn.addActionListener(e -> showStats());
        panel.add(statsBtn, gbc);
        
        return panel;
    }
    
    private void showStyledRegisterDialog() {
        JDialog dialog = new JDialog(mainFrame, "Crear Nueva Cuenta", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 400); // Diálogo más grande
        dialog.setLocationRelativeTo(mainFrame);
        
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY, 2),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Título
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel title = new JLabel("📝 Crear Cuenta", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(PRIMARY);
        content.add(title, gbc);
        
        // Campos
        gbc.gridwidth = 1;
        JTextField userField = addField(content, gbc, "Usuario:", 1, 250);
        JPasswordField passField = (JPasswordField) addField(content, gbc, "Contraseña:", 2, 250);
        JPasswordField confirmField = (JPasswordField) addField(content, gbc, "Confirmar:", 3, 250);
        
        // Botones
        gbc.gridy = 4; gbc.gridwidth = 2;
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        JButton registerBtn = createButton("Registrar", PRIMARY, 120, 40);
        JButton cancelBtn = createButton("Cancelar", new Color(100, 100, 100), 120, 40);
        
        registerBtn.addActionListener(e -> {
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword());
            String confirm = new String(confirmField.getPassword());
            
            if (user.isEmpty() || pass.isEmpty()) {
                showError("Usuario y contraseña son obligatorios");
                return;
            }
            if (!pass.equals(confirm)) {
                showError("Las contraseñas no coinciden");
                return;
            }
            
            performRegistration(user, pass, dialog);
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttons.add(registerBtn);
        buttons.add(cancelBtn);
        content.add(buttons, gbc);
        
        dialog.add(content, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
    
    // ... (resto de métodos de funcionalidad se mantienen igual que antes)
    // [Los métodos performLogin, onLoginSuccess, etc. se mantienen igual]
    
    private JButton createButton(String text, Color color, int width, int height) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setPreferredSize(new Dimension(width, height));
        btn.setFocusPainted(false);
        return btn;
    }
    
    // ... (implementar los métodos de funcionalidad: performLogin, sendMessage, searchUsers, etc.)
}
