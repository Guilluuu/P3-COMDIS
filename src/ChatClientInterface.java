// ChatClientInterface.java
import java.util.List;

/**
 * Interfaz que debe implementar el cliente de chat.
 * Define todas las operaciones necesarias para la comunicación con el servidor.
 */
public interface ChatClientInterface {
    
    // ============ AUTENTICACIÓN ============
    
    /**
     * Inicia sesión con las credenciales del usuario.
     * @param username Nombre de usuario
     * @param password Contraseña
     * @param port Puerto del servidor
     * @return true si el login fue exitoso, false en caso contrario
     */
    boolean login(String username, String password, int port);
    
    /**
     * Registra un nuevo usuario en el sistema.
     * @param username Nombre de usuario deseado
     * @param password Contraseña
     * @return true si el registro fue exitoso, false si el usuario ya existe
     */
    boolean signUp(String username, String password);
    
    /**
     * Cierra la sesión del usuario actual.
     */
    void logout();
    
    // ============ MENSAJERÍA ============
    
    /**
     * Envía un mensaje a otro usuario.
     * @param recipient Usuario destinatario
     * @param message Contenido del mensaje
     * @return true si el mensaje se envió correctamente
     */
    boolean sendMessage(String recipient, String message);
    
    /**
     * Obtiene los mensajes nuevos de un contacto específico.
     * @param contact Usuario del que se desean obtener mensajes
     * @return Lista de mensajes formateados como "remitente: contenido"
     */
    List<String> getNewMessages(String contact);
    
    /**
     * Verifica si hay mensajes nuevos de algún contacto.
     * @return Mapa con contactos como clave y cantidad de mensajes nuevos como valor
     */
    java.util.Map<String, Integer> checkNewMessages();
    
    // ============ CONTACTOS Y AMIGOS ============
    
    /**
     * Obtiene la lista de chats activos (usuarios con los que se ha intercambiado mensajes).
     * @return Lista de nombres de usuario
     */
    List<String> getActiveChats();
    
    /**
     * Busca usuarios en el sistema.
     * @param query Texto de búsqueda (nombre de usuario)
     * @return Lista de usuarios que coinciden con la búsqueda
     */
    List<String> searchUsers(String query);
    
    /**
     * Obtiene la lista de amigos del usuario actual.
     * @return Lista de nombres de usuario que son amigos
     */
    List<String> getFriends();
    
    // ============ SOLICITUDES DE AMISTAD ============
    
    /**
     * Envía una solicitud de amistad a otro usuario.
     * @param username Usuario al que se enviará la solicitud
     * @return true si la solicitud se envió correctamente
     */
    boolean sendFriendRequest(String username);
    
    /**
     * Obtiene las solicitudes de amistad pendientes.
     * @return Lista de nombres de usuario que han enviado solicitudes
     */
    List<String> getPendingFriendRequests();
    
    /**
     * Acepta una solicitud de amistad.
     * @param username Usuario cuya solicitud se aceptará
     * @return true si la operación fue exitosa
     */
    boolean acceptFriendRequest(String username);
    
    /**
     * Rechaza una solicitud de amistad.
     * @param username Usuario cuya solicitud se rechazará
     * @return true si la operación fue exitosa
     */
    boolean rejectFriendRequest(String username);
    
    // ============ ESTADO ============
    
    /**
     * Verifica si el cliente está conectado al servidor.
     * @return true si hay conexión activa
     */
    boolean isConnected();
    
    /**
     * Obtiene el nombre del usuario actual.
     * @return Nombre de usuario o null si no hay sesión activa
     */
    String getCurrentUsername();
}
