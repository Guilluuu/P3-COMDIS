// ModernChatUI.java
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
    private JTextField messageField;
    private DefaultListModel<String> contactsModel, friendsModel;
    private String currentUser, currentChatContact;
    
    private static final Color PRIMARY = new Color(70, 130, 180);
    
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
        mainFrame.setSize(900, 650);
        mainFrame.setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainFrame.add(mainPanel);
    }
    
    private void createLoginPanel() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(new Color(240, 248, 255));
        
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY, 2),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        content.setPreferredSize(new Dimension(400, 400));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Título
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel title = new JLabel("🚀 ChatApp", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(PRIMARY);
        content.add(title, gbc);
        
        // Campos
        gbc.gridwidth = 1;
        loginUserField = addField(content, gbc, "Usuario:", 1);
        loginPassField = addField(content, gbc, "Contraseña:", 2);
        loginPortField = addField(content, gbc, "Puerto:", 3);
        loginPortField.setText("1234");
        
        // Botones
        gbc.gridy = 4; gbc.gridwidth = 2;
        JPanel buttons = new JPanel(new FlowLayout());
        JButton login = createButton("Iniciar Sesión", PRIMARY);
        JButton register = createButton("Registrarse", Color.GRAY);
        
        login.addActionListener(e -> performLogin());
        register.addActionListener(e -> showRegisterDialog());
        
        buttons.add(login);
        buttons.add(register);
        content.add(buttons, gbc);
        
        loginPanel.add(content);
        mainPanel.add(loginPanel, "login");
    }
    
    private JTextField addField(JPanel parent, GridBagConstraints gbc, String label, int row) {
        gbc.gridy = row; gbc.gridx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        parent.add(lbl, gbc);
        
        gbc.gridx = 1;
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(200, 35));
        parent.add(field, gbc);
        return field;
    }
    
    private void createMainPanel() {
        JPanel mainAppPanel = new JPanel(new BorderLayout());
        
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel title = new JLabel("💬 ChatApp");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        
        JButton logout = new JButton("Cerrar Sesión");
        logout.setBackground(Color.WHITE);
        logout.addActionListener(e -> performLogout());
        header.add(logout, BorderLayout.EAST);
        
        mainAppPanel.add(header, BorderLayout.NORTH);
        
        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("💬 Chat", createChatPanel());
        tabs.addTab("👥 Amigos", createFriendsPanel());
        mainAppPanel.add(tabs, BorderLayout.CENTER);
        
        mainPanel.add(mainAppPanel, "main");
    }
    
    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Lista de contactos
        contactsModel = new DefaultListModel<>();
        JList<String> contactsList = new JList<>(contactsModel);
        contactsList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contactsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String contact = contactsList.getSelectedValue();
                if (contact != null) openChat(contact);
            }
        });
        
        JScrollPane contactsScroll = new JScrollPane(contactsList);
        contactsScroll.setBorder(BorderFactory.createTitledBorder("Chats Activos"));
        contactsScroll.setPreferredSize(new Dimension(200, 0));
        panel.add(contactsScroll, BorderLayout.WEST);
        
        // Área de chat
        JPanel chatPanel = new JPanel(new BorderLayout());
        
        JLabel chatTitle = new JLabel("Selecciona un chat");
        chatTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        chatTitle.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        chatPanel.add(chatTitle, BorderLayout.NORTH);
        
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        
        // Input de mensaje
        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        messageField.setEnabled(false);
        messageField.addActionListener(e -> sendMessage());
        
        JButton sendBtn = createButton("Enviar", PRIMARY);
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
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        friendsModel = new DefaultListModel<>();
        JList<String> friendsList = new JList<>(friendsModel);
        friendsList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JScrollPane friendsScroll = new JScrollPane(friendsList);
        friendsScroll.setBorder(BorderFactory.createTitledBorder("Mis Amigos"));
        panel.add(friendsScroll, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return btn;
    }
    
    private void performLogin() {
        String user = loginUserField.getText().trim();
        String pass = loginPassField.getText().trim();
        String port = loginPortField.getText().trim();
        
        if (user.isEmpty() || pass.isEmpty() || port.isEmpty()) {
            showError("Completa todos los campos");
            return;
        }
        
        new SwingWorker<Boolean, Void>() {
            protected Boolean doInBackground() {
                return client.login(user, pass, Integer.parseInt(port));
            }
            protected void done() {
                try {
                    if (get()) {
                        currentUser = user;
                        onLoginSuccess();
                    } else {
                        showError("Login fallido");
                    }
                } catch (Exception e) {
                    showError("Error: " + e.getMessage());
                }
            }
        }.execute();
    }
    
    private void onLoginSuccess() {
        cardLayout.show(mainPanel, "main");
        mainFrame.setTitle("ChatApp - " + currentUser);
        updateContacts();
        updateFriends();
        showInfo("Bienvenido " + currentUser + "!");
    }
    
    private void performLogout() {
        client.logout();
        cardLayout.show(mainPanel, "login");
        loginUserField.setText("");
        loginPassField.setText("");
        loginPortField.setText("1234");
        contactsModel.clear();
        friendsModel.clear();
        chatArea.setText("");
    }
    
    private void openChat(String contact) {
        currentChatContact = contact;
        messageField.setEnabled(true);
        chatArea.setText("Chat con " + contact + "\n\n");
        loadMessages(contact);
    }
    
    private void loadMessages(String contact) {
        new SwingWorker<List<String>, Void>() {
            protected List<String> doInBackground() {
                return client.getNewMessages(contact);
            }
            protected void done() {
                try {
                    for (String msg : get()) {
                        chatArea.append(msg + "\n");
                    }
                } catch (Exception e) {
                    showError("Error cargando mensajes");
                }
            }
        }.execute();
    }
    
    private void sendMessage() {
        String msg = messageField.getText().trim();
        if (msg.isEmpty() || currentChatContact == null) return;
        
        new SwingWorker<Boolean, Void>() {
            protected Boolean doInBackground() {
                return client.sendMessage(currentChatContact, msg);
            }
            protected void done() {
                try {
                    if (get()) {
                        chatArea.append("Tú: " + msg + "\n");
                        messageField.setText("");
                    }
                } catch (Exception e) {
                    showError("Error enviando mensaje");
                }
            }
        }.execute();
    }
    
    private void updateContacts() {
        new SwingWorker<List<String>, Void>() {
            protected List<String> doInBackground() {
                return client.getActiveChats();
            }
            protected void done() {
                try {
                    contactsModel.clear();
                    for (String contact : get()) {
                        contactsModel.addElement(contact);
                    }
                } catch (Exception e) {
                    // Silencioso
                }
            }
        }.execute();
    }
    
    private void updateFriends() {
        new SwingWorker<List<String>, Void>() {
            protected List<String> doInBackground() {
                return client.getFriends();
            }
            protected void done() {
                try {
                    friendsModel.clear();
                    for (String friend : get()) {
                        friendsModel.addElement(friend);
                    }
                } catch (Exception e) {
                    // Silencioso
                }
            }
        }.execute();
    }
    
    private void showRegisterDialog() {
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JPasswordField confirmField = new JPasswordField();
        
        Object[] fields = {
            "Usuario:", userField,
            "Contraseña:", passField,
            "Confirmar:", confirmField
        };
        
        int option = JOptionPane.showConfirmDialog(mainFrame, fields, 
            "Registrar", JOptionPane.OK_CANCEL_OPTION);
        
        if (option == JOptionPane.OK_OPTION) {
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword());
            String confirm = new String(confirmField.getPassword());
            
            if (!pass.equals(confirm)) {
                showError("Las contraseñas no coinciden");
                return;
            }
            
            new SwingWorker<Boolean, Void>() {
                protected Boolean doInBackground() {
                    return client.signUp(user, pass);
                }
                protected void done() {
                    try {
                        if (get()) {
                            showInfo("Cuenta creada");
                        } else {
                            showError("Error creando cuenta");
                        }
                    } catch (Exception e) {
                        showError("Error: " + e.getMessage());
                    }
                }
            }.execute();
        }
    }
    
    private void showError(String msg) {
        JOptionPane.showMessageDialog(mainFrame, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(mainFrame, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {}
            
            // ChatClientInterface client = new TuCliente();
            // new ModernChatUI(client);
        });
    }
}
