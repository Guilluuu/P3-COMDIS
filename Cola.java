import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Cola extends UnicastRemoteObject implements ICola {

    private BlockingQueue<String> queue;

    public Cola() throws RemoteException {
        this.queue = new LinkedBlockingQueue<>();
    }

    @Override
    public String push(String s) throws RemoteException {
        synchronized (this) {
            queue.add(s);
            return s;
        }
    }

    @Override
    public String pop() throws RemoteException {
        synchronized (this) {
            return queue.poll();
        }
    }

    @Override
    public String get() throws RemoteException {
        synchronized (this) {
            return queue.peek();
        }
    }

    public void servir(Cola lcola, Integer puerto, String otro) {

        try {
            startRegistry(puerto);
            String registryURL = "rmi://localhost:" + puerto + "/" + otro;
            Naming.rebind(registryURL, lcola);
            System.out.println("Server registered. Registry currently contains:");

            listRegistry(registryURL);
            System.out.println("Server ready.");
        } catch (Exception re) {
            System.out.println("servir: " + re);
        }
    }

    private static void startRegistry(int RMIPortNum) throws RemoteException {
        try {
            Registry registry = LocateRegistry.getRegistry(RMIPortNum);
            registry.list();

        } catch (RemoteException e) {

            System.out.println("RMI registry cannot be located at port "
                    + RMIPortNum);
            Registry registry = LocateRegistry.createRegistry(RMIPortNum);
            System.out.println("RMI registry created at port " + RMIPortNum);
        }
    }

    private static void listRegistry(String registryURL) throws RemoteException, MalformedURLException {
        System.out.println("Registry " + registryURL + " contains: ");
        String[] names = Naming.list(registryURL);
        for (String name : names) {
            System.out.println(name);
        }
    }
}
