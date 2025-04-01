package coursework_manager.rmi_interfaces;

import coursework_manager.models.Group;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IGroupRepo extends Remote {
    List<Group> getAllGroups() throws RemoteException;
}
