import java.rmi.Naming;
import java.rmi.RemoteException;

public class Chat {
    private ICola ecola;
    private Cola lcola;
    private String direccion_enviar;
    private Integer puerto_enviar;
    private Integer puerto_creador;
    private String creador;

    public Chat(String yo, String otro, String d, Integer p, Integer mip) {
        try {
            direccion_enviar = d;
            puerto_enviar = p;
            puerto_creador = mip;
            creador = yo;
            this.lcola = new Cola();
            this.lcola.servir(this.lcola, puerto_creador, otro);
        } catch (RemoteException e) {
            System.out.println("You are coocked! RMI fails with: " + e);
            System.exit(124);
        }
    }

    public void enviar(String msj) throws Exception {
        try {
            if (ecola == null) {
                if (creador == null)
                    throw new Exception("Creador is null");
                String regurl = "rmi://" + direccion_enviar + ":" + puerto_enviar + "/" + creador;
                System.out.println(regurl);
                ecola = (ICola) Naming.lookup(regurl);
            }
        } catch (Exception e) {
            System.out.println("enviar: " + e);
            throw new Exception(e);
        }
        ecola.push(msj);
    }

    public String leer() throws Exception {
        try {
            return lcola.pop();
        } catch (Exception e) {
            System.out.println("leer: " + e);
            throw new Exception(e);
        }
    }

    public boolean isEmpty() throws Exception {
        return lcola.get() == null;
    }

}
