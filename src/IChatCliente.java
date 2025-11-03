import java.util.List;

public interface IChatCliente {
    // Autenticación
    boolean login(String username, String password, int port);

    boolean registrarUsuario(String username, String password);

    void logout();

    // Gestión de chats
    List<String> getChatsActivos();

    List<String> getNuevosMensajesChat(String contact);

    boolean enviarMensaje(String contact, String message);

    // Gestión de amistades
    List<String> buscarUsuarios(String query);

    boolean solicitarAmistad(String username);

    List<String> getSolicitudesPendientes();

    boolean aceptarSolicitudAmistad(String username);

    boolean rechazarSolicitudAmistad(String username);
}
