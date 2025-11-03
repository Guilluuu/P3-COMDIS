import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class ClienteCallback extends UnicastRemoteObject implements IClienteCallback {
    private IServidor servidor;
    private String usuarioActual;
    private Cliente cliente;

    public ClienteCallback(Cliente cliente) throws RemoteException {
        super();
        this.cliente = cliente;
        try {
            this.servidor = (IServidor) Naming.lookup("rmi://localhost/Servidor");
        } catch (Exception e) {
            System.out.println("Error conectando al servidor RMI: " + e);
        }
    }

    // ===== IMPLEMENTACIÓN DE IClienteCallback =====

    @Override
    public void notificarConexionAmigo(String amigo, String direccion) throws RemoteException {
        System.out.println("[Callback] Amigo " + amigo + " se conectó desde " + direccion);
        try {
            cliente.addAmigoConectado(amigo, direccion);
        } catch (Exception e) {
            System.out.println("Error en notificarConexionAmigo: " + e);
        }
    }

    @Override
    public void notificarDesconexionAmigo(String amigo) throws RemoteException {
        System.out.println("[Callback] Amigo " + amigo + " se desconectó");
        try {
            cliente.removeAmigoConectado(amigo);
        } catch (Exception e) {
            System.out.println("Error en notificarDesconexionAmigo: " + e);
        }
    }

    @Override
    public void notificarSolicitudAmistad(String deUsuario) throws RemoteException {
        System.out.println("[Callback] Solicitud de amistad de " + deUsuario);
        // Aquí podrías agregar lógica para mostrar la solicitud al usuario
        cliente.mostrarSolicitudAmistad(deUsuario);
    }

    @Override
    public void notificarAmistadAceptada(String amigo) throws RemoteException {
        System.out.println("[Callback] " + amigo + " aceptó tu solicitud de amistad");
        try {
            String direccion = servidor.getDireccion(amigo);

            if (direccion != null) {
                cliente.addAmigoConectado(amigo, direccion);
            }
            // Aquí podrías agregar lógica para actualizar la lista de amigos
        } catch (Exception e) {

            System.out.println("Error creando chat con" + amigo);

        }
    }
    // ===== MÉTODOS PARA LLAMAR AL SERVIDOR =====

    public boolean login(String user, String password, Integer puerto) {
        try {
            String direccion = "localhost:" + puerto;
            boolean success = servidor.login(user, Encrypt.sha256(password), direccion);
            if (success) {
                usuarioActual = user;
                servidor.registrarCallback(user, this);
            }
            return success;
        } catch (Exception e) {
            System.out.println("Error en login: " + e);
            return false;
        }
    }

    public void logout() {
        try {
            servidor.logout(usuarioActual);
            servidor.eliminarCallback(usuarioActual);
        } catch (Exception e) {
            System.out.println("Error en logout: " + e);
        }
    }

    public boolean registrarUsuario(String user, String password) {
        try {
            return servidor.registrarUsuario(user, Encrypt.sha256(password));
        } catch (Exception e) {
            System.out.println("Error en registrarUsuario: " + e);
            return false;
        }
    }

    public boolean solicitarAmistad(String amigo) {
        try {
            servidor.solicitarAmistad(usuarioActual, amigo);
            return true;
        } catch (Exception e) {
            System.out.println("Error en solicitarAmistad: " + e);
            return false;
        }
    }

    public List<String> getSolicitudesPendientes() {
        try {
            return servidor.getSolicitudesPendientes(usuarioActual);
        } catch (Exception e) {
            System.out.println("Error en getSolicitudesPendientes: " + e);
            return null;
        }
    }

    public boolean aceptarSolicitudAmistad(String amigo) {
        try {
            return servidor.aceptarSolicitudAmistad(usuarioActual, amigo);
        } catch (Exception e) {
            System.out.println("Error en aceptarSolicitudAmistad: " + e);
        }
        return false;
    }

    public boolean rechazarSolicitudAmistad(String amigo) {
        try {
            return servidor.rechazarSolicitudAmistad(usuarioActual, amigo);
        } catch (Exception e) {
            System.out.println("Error en rechazarSolicitudAmistad: " + e);
        }
        return false;
    }

    public List<String> getAmigos() {
        try {
            return servidor.getAmigos(usuarioActual);
        } catch (Exception e) {
            System.out.println("Error en getAmigos: " + e);
            return null;
        }
    }

    public List<String> getAmigosConectados() {
        try {
            return servidor.getAmigosConectados(usuarioActual);
        } catch (Exception e) {
            System.out.println("Error en getAmigosConectados: " + e);
            return null;
        }
    }

    public String getDireccion(String usuario) {
        try {
            return servidor.getDireccion(usuario);
        } catch (Exception e) {
            System.out.println("Error en getDireccion: " + e);
            return null;
        }
    }

    public boolean existeUsuario(String usuario) {
        try {
            return servidor.existeUsuario(usuario);
        } catch (Exception e) {
            System.out.println("Error en existeUsuario: " + e);
            return false;
        }
    }
}
