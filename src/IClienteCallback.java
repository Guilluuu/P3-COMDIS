import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IClienteCallback extends Remote {
    void notificarConexionAmigo(String amigo, String direccion) throws RemoteException;
    void notificarDesconexionAmigo(String amigo) throws RemoteException;
    void notificarSolicitudAmistad(String deUsuario) throws RemoteException;
    void notificarAmistadAceptada(String amigo) throws RemoteException;
}
