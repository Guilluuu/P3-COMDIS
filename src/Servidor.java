import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.io.*;

public class Servidor extends UnicastRemoteObject implements IServidor {
    private Map<String, List<String>> amigos = new HashMap<>();
    private Map<String, String> passwords = new HashMap<>();
    private Map<String, String> direcciones = new HashMap<>();
    private Map<String, List<String>> solicitudesPendientes = new HashMap<>();
    private Map<String, IClienteCallback> callbacksClientes = new HashMap<>();

    private final static String PASSWORDS_FILE = "./data/user/passwords/pswd";
    private final static String FRIENDS_FILE = "./data/user/friends/amigos";
    private final static String PENDING_REQUESTS_FILE = "./data/user/requests/solicitudes";
    private static final int RMI_PORT = 1099;

    public Servidor() throws RemoteException {
        super();
        loadAllData();
        System.out.println("Servidor RMI iniciado");
        System.out.println("Usuarios registrados: " + passwords.keySet());
    }

    // ==========================
    // ===== FUNCIONES DATA =====
    // ==========================

    private void loadAllData() {
        HashMap<String, String> loadedUsers = ManejadorUsuarios.loadUsersFromFile(PASSWORDS_FILE);
        passwords.putAll(loadedUsers);

        HashMap<String, List<String>> loadedFriends = ManejadorUsuarios.loadFriendsFromFile(FRIENDS_FILE);
        amigos.putAll(loadedFriends);

        HashMap<String, List<String>> loadedRequests = ManejadorUsuarios.loadFriendsFromFile(PENDING_REQUESTS_FILE);
        solicitudesPendientes.putAll(loadedRequests);

        for (String user : passwords.keySet()) {
            amigos.putIfAbsent(user, new ArrayList<>());
            solicitudesPendientes.putIfAbsent(user, new ArrayList<>());
        }
    }

    private void saveAllData() {
        ManejadorUsuarios.saveUsersToFile(passwords, PASSWORDS_FILE);
        ManejadorUsuarios.saveFriendsToFile(amigos, FRIENDS_FILE);
        ManejadorUsuarios.saveFriendsToFile(solicitudesPendientes, PENDING_REQUESTS_FILE);
    }


    // ===========================================
    // ===== INICIAR/CERRAR/REGISTRAR SESION =====
    // ===========================================

    @Override
    public synchronized boolean login(String user, String password, String direccion) throws RemoteException {
        System.out.println("Intento de login: " + user + " desde " + direccion);

        if (!passwords.containsKey(user) || !passwords.get(user).equals(password)) {
            System.out.println("Login fallido: " + user);
            return false;
        }

        direcciones.put(user, direccion);
        System.out.println("Login exitoso: " + user);

        notificarConexionUsuario(user);

        return true;
    }

    @Override
    public synchronized void logout(String user) throws RemoteException {
        System.out.println("Logout: " + user);

        notificarDesconexionUsuario(user);

        direcciones.remove(user);
        eliminarCallback(user);
    }

    @Override
    public synchronized boolean registrarUsuario(String user, String password) throws RemoteException {
        if (passwords.containsKey(user)) {
            System.out.println("Registro fallido: usuario " + user + " ya existe");
            return false;
        }

        amigos.put(user, new ArrayList<>());
        passwords.put(user, password);
        solicitudesPendientes.put(user, new ArrayList<>());

        saveAllData();
        System.out.println("Usuario registrado: " + user);
        return true;
    }

    // =============================
    // ===== FUNCIONES AMISTAD =====
    // =============================

    @Override
    public synchronized void solicitarAmistad(String deUsuario, String aUsuario) throws RemoteException {
        if (!passwords.containsKey(deUsuario) || !passwords.containsKey(aUsuario)) {
            System.out.println("Solicitud fallida: usuario no existe");
            return;
        }

        if (deUsuario.equals(aUsuario)) {
            System.out.println("Solicitud fallida: no puede enviarse solicitud a sí mismo");
            return;
        }

        if (amigos.get(deUsuario).contains(aUsuario)) {
            System.out.println("Solicitud fallida: ya son amigos");
            return;
        }

        if (!solicitudesPendientes.get(aUsuario).contains(deUsuario)) {
            solicitudesPendientes.get(aUsuario).add(deUsuario);
            saveAllData();
            System.out.println("Solicitud de amistad: " + deUsuario + " -> " + aUsuario);

            notificarSolicitudAmistad(aUsuario, deUsuario);
        }
    }

    @Override
    public synchronized List<String> getSolicitudesPendientes(String usuario) throws RemoteException {
        return new ArrayList<>(solicitudesPendientes.getOrDefault(usuario, new ArrayList<>()));
    }

    @Override
    public synchronized boolean aceptarSolicitudAmistad(String usuario, String amigo) throws RemoteException {
        if (!solicitudesPendientes.getOrDefault(usuario, new ArrayList<>()).contains(amigo)) {
            return false;
        }

        solicitudesPendientes.get(usuario).remove(amigo);

        if (!amigos.get(usuario).contains(amigo)) {
            amigos.get(usuario).add(amigo);
        }
        if (!amigos.get(amigo).contains(usuario)) {
            amigos.get(amigo).add(usuario);
        }

        saveAllData();
        System.out.println("Amistad aceptada: " + usuario + " - " + amigo);

        notificarAmistadAceptada(usuario, amigo);
        notificarAmistadAceptada(amigo, usuario);

        return true;
    }

    @Override
    public synchronized boolean rechazarSolicitudAmistad(String usuario, String amigo) throws RemoteException {
        if (!solicitudesPendientes.getOrDefault(usuario, new ArrayList<>()).contains(amigo)) {
            return false;
        }

        solicitudesPendientes.get(usuario).remove(amigo);
        saveAllData();
        System.out.println("Solicitud rechazada: " + usuario + " rechazó a " + amigo);
        return true;
    }

    // ================================
    // ===== FUNCIONES AUXILIARES =====
    // ================================


    @Override
    public synchronized List<String> getAmigos(String user) throws RemoteException {
        return new ArrayList<>(amigos.getOrDefault(user, new ArrayList<>()));
    }

    @Override
    public synchronized List<String> getAmigosConectados(String user) throws RemoteException {
        List<String> conectados = new ArrayList<>();
        for (String amigo : amigos.getOrDefault(user, new ArrayList<>())) {
            if (direcciones.containsKey(amigo)) {
                conectados.add(amigo);
            }
        }
        return conectados;
    }

    @Override
    public synchronized String getDireccion(String usuario) throws RemoteException {
        return direcciones.get(usuario);
    }

    @Override
    public synchronized boolean existeUsuario(String usuario) throws RemoteException {
        return passwords.containsKey(usuario);
    }

    @Override
    public synchronized void registrarCallback(String usuario, IClienteCallback callback) throws RemoteException {
        callbacksClientes.put(usuario, callback);
        System.out.println("Callback registrado para: " + usuario);
    }

    @Override
    public synchronized void eliminarCallback(String usuario) throws RemoteException {
        callbacksClientes.remove(usuario);
        System.out.println("Callback eliminado para: " + usuario);
    }

    // ==============================
    // ===== NOTIFICAR USUARIOS =====
    // ==============================

    private void notificarConexionUsuario(String usuario) {
        String direccion = direcciones.get(usuario);
        for (String amigo : amigos.getOrDefault(usuario, new ArrayList<>())) {
            IClienteCallback callback = callbacksClientes.get(amigo);
            if (callback != null) {
                try {
                    callback.notificarConexionAmigo(usuario, direccion);
                } catch (RemoteException e) {
                    System.out.println("Error notificando conexión a " + amigo + ": " + e);
                }
            }
        }
    }

    private void notificarDesconexionUsuario(String usuario) {
        for (String amigo : amigos.getOrDefault(usuario, new ArrayList<>())) {
            IClienteCallback callback = callbacksClientes.get(amigo);
            if (callback != null) {
                try {
                    callback.notificarDesconexionAmigo(usuario);
                } catch (RemoteException e) {
                    System.out.println("Error notificando desconexión a " + amigo + ": " + e);
                }
            }
        }
    }

    private void notificarSolicitudAmistad(String usuarioDestino, String usuarioSolicitante) {
        IClienteCallback callback = callbacksClientes.get(usuarioDestino);
        if (callback != null) {
            try {
                callback.notificarSolicitudAmistad(usuarioSolicitante);
            } catch (RemoteException e) {
                System.out.println("Error notificando solicitud a " + usuarioDestino + ": " + e);
            }
        }
    }

    private void notificarAmistadAceptada(String usuario, String nuevoAmigo) {
        IClienteCallback callback = callbacksClientes.get(usuario);
        if (callback != null) {
            try {
                callback.notificarAmistadAceptada(nuevoAmigo);
            } catch (RemoteException e) {
                System.out.println("Error notificando amistad aceptada a " + usuario + ": " + e);
            }
        }
    }

    public static void main(String[] args) {
        try {
            java.rmi.registry.LocateRegistry.createRegistry(RMI_PORT);
            Servidor servidor = new Servidor();
            Naming.rebind("Servidor", servidor);
            System.out.println("Servidor RMI listo en puerto " + RMI_PORT);
        } catch (Exception e) {
            System.out.println("Error iniciando servidor: " + e);
        }
    }
}
