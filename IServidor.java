import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IServidor extends Remote {

    // ===== AUTENTICACIÓN Y CONEXIÓN =====
    boolean login(String user, String password, String direccion) throws RemoteException;
    void logout(String user) throws RemoteException;
    boolean registrarUsuario(String user, String password) throws RemoteException;

    // ===== GESTIÓN DE AMISTADES =====
    void solicitarAmistad(String deUsuario, String aUsuario) throws RemoteException;
    List<String> getSolicitudesPendientes(String usuario) throws RemoteException;
    boolean aceptarSolicitudAmistad(String usuario, String amigo) throws RemoteException;
    boolean rechazarSolicitudAmistad(String usuario, String amigo) throws RemoteException;

    // ===== INFORMACIÓN DE USUARIOS =====
    List<String> getAmigos(String user) throws RemoteException;
    List<String> getAmigosConectados(String user) throws RemoteException;
    String getDireccion(String usuario) throws RemoteException;
    boolean existeUsuario(String usuario) throws RemoteException;

    // ===== NOTIFICACIONES (para callbacks) =====
    void registrarCallback(String usuario, IClienteCallback callback) throws RemoteException;
    void eliminarCallback(String usuario) throws RemoteException;
}
