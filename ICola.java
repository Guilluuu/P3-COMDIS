import java.rmi.*;

public interface ICola extends Remote {

        /* Add and element to the tail of the queue and return this element */
        public String push(String s) throws java.rmi.RemoteException;

        /* Remove the fist element of the queue and return it */
        public String pop() throws java.rmi.RemoteException;

        /* Get the fist element if the queue */
        public String get() throws java.rmi.RemoteException;

}
