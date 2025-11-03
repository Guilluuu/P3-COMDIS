// ChatClientInterface.java
import java.util.List;
import java.util.Map;

/**
 * Interfaz que debe implementar el cliente de chat para conectar con la UI
 */
public interface ChatClientInterface {
    
    // ============ OPERACIONES DE AUTENTICACIÓN ============
    
    /**
     * Inicia sesión en el servidor de chat
     * @param username Nombre de usuario
     * @param password Contraseña
     * @param port Puerto de conexión
     * @return true si el login fue exitoso, false en caso contrario
     */
    boolean login(String username, String password, int port);
    
    /**
     * Cierra sesión en el servidor
     */
    void logout();
    
    /**
     * Registra un nuevo usuario en el sistema
     * @param username Nombre de usuario
     * @param password Contraseña
     * @return true si el registro fue exitoso, false si el usuario ya existe
     */
    boolean signUp(String username, String password);
    
    /**
     * Verifica si el cliente está conectado al servidor
     * @return true si está conectado, false en caso contrario
     */
    boolean isConnected();
    
    
    // ============ OPERACIONES DE MENSAJERÍA ============
    
    /**
     * Envía un mensaje a un contacto
     * @param contact Nombre del contacto
     * @param message Mensaje a enviar
     * @return true si el mensaje se envió correctamente
     */
    boolean sendMessage(String contact, String message);
    
    /**
     * Obtiene los mensajes nuevos de un contacto específico
     * @param contact Nombre del contacto
     * @return Lista de mensajes en formato String
     */
    List<String> getNewMessages(String contact);
    
    /**
     * Verifica si hay mensajes nuevos de todos los contactos
     * @return Mapa con contacto como clave y cantidad de mensajes nuevos como valor
     */
    Map<String, Integer> checkNewMessages();
    
    /**
     * Obtiene la lista de chats activos (contactos con los que se ha conversado)
     * @return Lista de nombres de contactos
     */
    List<String> getActiveChats();
    
    
    // ============ OPERACIONES DE AMIGOS ============
    
    /**
     * Busca usuarios en el sistema
     * @param query Texto de búsqueda
     * @return Lista de usuarios que coinciden con la búsqueda
     */
    List<String> searchUsers(String query);
    
    /**
     * Envía una solicitud de amistad
     * @param username Usuario al que se envía la solicitud
     * @return true si la solicitud se envió correctamente
     */
    boolean sendFriendRequest(String username);
    
    /**
     * Obtiene la lista de amigos del usuario actual
     * @return Lista de nombres de amigos
     */
    List<String> getFriends();
    
    /**
     * Obtiene las solicitudes de amistad pendientes
     * @return Lista de usuarios que han enviado solicitudes
     */
    List<String> getPendingFriendRequests();
    
    /**
     * Acepta una solicitud de amistad
     * @param username Usuario cuya solicitud se acepta
     * @return true si se aceptó correctamente
     */
    boolean acceptFriendRequest(String username);
    
    /**
     * Rechaza una solicitud de amistad
     * @param username Usuario cuya solicitud se rechaza
     * @return true si se rechazó correctamente
     */
    boolean rejectFriendRequest(String username);
}
