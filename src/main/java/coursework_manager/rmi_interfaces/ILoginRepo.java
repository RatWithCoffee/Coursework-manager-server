package coursework_manager.rmi_interfaces;

import coursework_manager.models.users.User;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ILoginRepo extends Remote {
    User login(User user) throws RemoteException;
}
