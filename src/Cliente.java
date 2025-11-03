import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Cliente implements ChatClientInterface {

    private HashMap<String, Chat> chats;
    private String nombre;
    private String clave;
    private Integer puerto;
    private ClienteCallback cb;
    private ArrayList<String> amigosConectados;
    private boolean conectado = false;

    public Integer setPuerto(Integer puerto) {
        return this.puerto = puerto;
    }

    public String setNombre(String nombre) {
        return this.nombre = nombre;
    }

    public String setClave(String clave) {
        return this.clave = clave;
    }

    public Cliente() {
        chats = new HashMap<>();
        amigosConectados = new ArrayList<>();
    }

    private String pedirNombre(BufferedReader br) {
        try {
            System.out.print("Mi Nombre: ");
            return this.setNombre(br.readLine());

        } catch (Exception e) {
            System.out.println("pedirNombre: " + e);
            return "";
        }
    }

    private String pedirClave(BufferedReader br) {
        try {
            System.out.print("Mi clave: ");
            return this.setClave(br.readLine());

        } catch (Exception e) {
            System.out.println("pedirClave: " + e);
            return "";
        }
    }

    private Boolean pedirSn(BufferedReader br) {
        try {
            String resp = br.readLine();
            return resp.toLowerCase().equals("s") ||
                    resp.toLowerCase().equals("si") ||
                    resp.toLowerCase().equals("y") ||
                    resp.toLowerCase().equals("yes");

        } catch (Exception e) {
            System.out.println("pedirSn: " + e);
            return false;
        }
    }

    private Integer pedirPuerto(BufferedReader br) {
        try {
            System.out.print("Mi puerto: ");
            return this.setPuerto(Integer.parseInt(br.readLine()));

        } catch (Exception e) {
            System.out.println("pedirPuerto: " + e);
            return 0;
        }
    }

    /* NOMBRE es el nombre del receptor */
    public void nuevoChat(String nombre, String direccion, Integer puerto) throws Exception {
        try {
            System.out.println("New Chat :: " + this.nombre + " -> " + nombre + " @ " + direccion + ":" + puerto);
            chats.put(nombre, new Chat(this.nombre, nombre, direccion, puerto, this.puerto));
        } catch (Exception e) {
            System.out.println("nuevoChat exception: " + e);
            throw e;
        }
    }

    public void mostrarSolicitudAmistad(String deUsuario) {
        System.out.println("=== SOLICITUD DE AMISTAD ===");
        System.out.println("El usuario " + deUsuario + " quiere ser tu amigo");
        System.out.println("Usa el comando 'aceptar " + deUsuario + "' para aceptar");
    }

    public void mainloop(BufferedReader br) throws Exception {
        try {
            cb = new ClienteCallback(this);
            while (true) {
                try {
                    pedirNombre(br);
                    pedirClave(br);
                    pedirPuerto(br);

                    if (cb.login(this.nombre, this.clave, this.puerto)) {
                        break;
                    } else {
                        System.out.println("Error! Invalid login");
                        System.out.println("¿Quieres crear el nuevo usuario? (S/n) ");
                        if (pedirSn(br)) {
                            if (cb.registrarUsuario(this.nombre, this.clave)) {
                                System.out.println("Usuario registrado. Ahora puedes hacer login.");
                            } else {
                                System.out.println("Error al registrar el usuario.");
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error durante login: " + e);
                }
            }

            System.out.println("=== MIS AMIGOS ===");
            List<String> amigos = cb.getAmigos();
            if (amigos != null) {
                for (String a : amigos) {
                    System.out.println("Amigo: " + a);
                }
            }

            System.out.println("=== AMIGOS CONECTADOS ===");
            List<String> amigosConectadosList = cb.getAmigosConectados();
            if (amigosConectadosList != null) {
                for (String a : amigosConectadosList) {
                    System.out.println("Amigo Conectado: " + a);
                    String direccion = cb.getDireccion(a);
                    if (direccion != null) {
                        addAmigoConectado(a, direccion);
                    }
                }
            }

            // Mostrar solicitudes pendientes
            List<String> solicitudes = cb.getSolicitudesPendientes();
            if (solicitudes != null && !solicitudes.isEmpty()) {
                System.out.println("=== SOLICITUDES PENDIENTES ===");
                for (String solicitud : solicitudes) {
                    System.out.println("Solicitud de: " + solicitud);
                }
            }

            System.out.println("\nComandos disponibles:");
            System.out.println("  mensaje <amigo> <texto> - Enviar mensaje");
            System.out.println("  solicitar <usuario> - Enviar solicitud de amistad");
            System.out.println("  aceptar <usuario> - Aceptar solicitud de amistad");
            System.out.println("  rechazar <usuario> - Rechazar solicitud de amistad");
            System.out.println("  amigos - Listar amigos");
            System.out.println("  conectados - Listar amigos conectados");
            System.out.println("  exit - Salir");

            while (true) {
                System.out.print(">> ");
                String input = br.readLine();

                if (input.equals("exit")) {
                    break;
                }

                String[] partes = input.split(" ", 3);
                String comando = partes[0].toLowerCase();

                switch (comando) {
                    case "mensaje":
                        if (partes.length >= 3) {
                            String amigo = partes[1];
                            String mensaje = partes[2];
                            if (chats.containsKey(amigo)) {
                                chats.get(amigo).enviar(mensaje);
                            } else {
                                System.out.println("No tienes chat con " + amigo);
                            }
                        }
                        break;

                    case "solicitar":
                        if (partes.length >= 2) {
                            cb.solicitarAmistad(partes[1]);
                        }
                        break;

                    case "aceptar":
                        if (partes.length >= 2) {
                            if (cb.aceptarSolicitudAmistad(partes[1])) {
                                System.out.println("Solicitud aceptada");
                            } else {
                                System.out.println("Error al aceptar solicitud");
                            }
                        }
                        break;

                    case "rechazar":
                        if (partes.length >= 2) {
                            if (cb.rechazarSolicitudAmistad(partes[1])) {
                                System.out.println("Solicitud rechazada");
                            } else {
                                System.out.println("Error al rechazar solicitud");
                            }
                        }
                        break;

                    case "amigos":
                        List<String> listaAmigos = cb.getAmigos();
                        if (listaAmigos != null) {
                            for (String amigo : listaAmigos) {
                                System.out.println("Amigo: " + amigo);
                            }
                        }
                        break;

                    case "conectados":
                        List<String> conectados = cb.getAmigosConectados();
                        if (conectados != null) {
                            for (String amigo : conectados) {
                                System.out.println("Conectado: " + amigo);
                            }
                        }
                        break;

                    default:
                        System.out.println("Comando no reconocido");
                }

                // Leer mensajes de todos los chats
                for (String amigo : chats.keySet()) {
                    Chat chat = chats.get(amigo);
                    while (!chat.isEmpty()) {
                        String mensaje = chat.leer();
                        if (mensaje != null) {
                            System.out.println("[" + amigo + "]: " + mensaje);
                        }
                    }
                }
            }

            cb.logout();

        } catch (Exception e) {
            System.out.println("Mainloop exception: " + e);
            e.printStackTrace();
        }
    }

    public void addAmigoConectado(String nombre, String direccion) throws Exception {
        if (!amigosConectados.contains(nombre)) {
            amigosConectados.add(nombre);
            String[] host_port = direccion.split(":");
            if (host_port.length == 2) {
                nuevoChat(nombre, host_port[0], Integer.parseInt(host_port[1]));
            } else {
                System.out.println("Dirección inválida: " + direccion);
            }
        }
    }

    public void removeAmigoConectado(String nombre) throws Exception {
        amigosConectados.remove(nombre);
        chats.remove(nombre);
        System.out.println("Chat con " + nombre + " eliminado");
    }

    @Override
    public boolean login(String username, String password, int port) {
        try {
            // inicializa callback si hace falta
            if (cb == null)
                cb = new ClienteCallback(this);
            this.nombre = username;
            this.clave = password;
            this.puerto = port;

            boolean ok = cb.login(username, password, port);
            this.conectado = ok;
            return ok;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void logout() {
        try {
            if (cb != null)
                cb.logout();
        } catch (Exception ignore) {
        }
        conectado = false;
    }

    @Override
    public boolean signUp(String username, String password) {
        try {
            if (cb == null)
                cb = new ClienteCallback(this);
            return cb.registrarUsuario(username, password);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isConnected() {
        return conectado;
    }

    // ======= ChatClientInterface: MENSAJERÍA =======

    @Override
    public boolean sendMessage(String contact, String message) {
        // ya tienes este método en Cliente; delegamos en él
        return enviarMensaje(contact, message); // existente en tu clase
    }

    @Override
    public List<String> getNewMessages(String contact) {
        // método ya existente en tu clase (devuelve y drena)
        return getNuevosMensajesChat(contact); // existente en tu clase
    }

    @Override
    public java.util.Map<String, Integer> checkNewMessages() {
        java.util.Map<String, Integer> res = new java.util.HashMap<>();
        try {
            // Recorre los chats activos y marca 1 si hay al menos uno pendiente (peek no
            // destructivo)
            for (String amigo : new java.util.ArrayList<>(chats.keySet())) {
                Chat c = chats.get(amigo);
                if (c == null)
                    continue;
                try {
                    res.put(amigo, c.isEmpty() ? 0 : 1);
                } catch (Exception e) {
                    res.put(amigo, 0);
                }
            }
        } catch (Exception ignore) {
        }
        return res;
    }

    @Override
    public List<String> getActiveChats() {
        return new java.util.ArrayList<>(chats.keySet());
    }

    // ======= ChatClientInterface: AMIGOS =======

    @Override
    public List<String> searchUsers(String query) {
        // Si tienes búsqueda en el servidor, delega:
        // return cb.buscarUsuarios(query);

        // Fallback mínimo: si el usuario existe exactamente, devuélvelo.
        java.util.List<String> out = new java.util.ArrayList<>();
        try {
            if (cb != null && cb.existeUsuario(query) && !query.equals(nombre)) {
                out.add(query);
            }
        } catch (Exception ignore) {
        }
        return out;
    }

    @Override
    public boolean sendFriendRequest(String username) {
        return cb.solicitarAmistad(username); // ya lo tienes implementado
    }

    @Override
    public List<String> getFriends() {
        return cb.getAmigos(); // ya lo usas en tu mainloop
    }

    @Override
    public List<String> getPendingFriendRequests() {
        return cb.getSolicitudesPendientes(); // ya existe
    }

    @Override
    public boolean acceptFriendRequest(String username) {
        return cb.aceptarSolicitudAmistad(username); // ya existe
    }

    @Override
    public boolean rejectFriendRequest(String username) {
        return cb.rechazarSolicitudAmistad(username); // ya existe
    }

    public List<String> getNuevosMensajesChat(String contact) {
        ArrayList<String> mensaje = new ArrayList<>();
        try {
            while (chats.get(contact) != null && !chats.get(contact).isEmpty()) {
                mensaje.add(contact + ": " + chats.get(contact).leer());
            }
        } catch (Exception e) {
            return mensaje;
        }
        return mensaje;
    }

    public boolean enviarMensaje(String contacto, String mensaje) {
        if (chats.get(contacto) == null) {
            return false;
        }

        try {
            chats.get(contacto).enviar(mensaje);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        Cliente yo = new Cliente();
        try {
            yo.mainloop(br);
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }
}
