package coursework_manager.rmi_interfaces;

import coursework_manager.models.Mark;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IMarkRepo extends Remote {
    List<Mark> getMarksByCourseworkRecordId(int cwId, int groupId) throws RemoteException;

    void updateMark(Mark mark) throws RemoteException;

    void deleteMark(int id) throws RemoteException;
}