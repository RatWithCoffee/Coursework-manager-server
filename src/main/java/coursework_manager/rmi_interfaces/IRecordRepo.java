package coursework_manager.rmi_interfaces;

import coursework_manager.models.CourseworkRecord;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IRecordRepo extends Remote {
    int add(int cwId, int teacherId, int groupId) throws RemoteException;

    void edit(int recordId, int cwId, int teacherId, int groupId) throws RemoteException;

    List<CourseworkRecord> getAll() throws RemoteException;

    List<CourseworkRecord> getAllByGroup(int groupId) throws RemoteException;

    void remove(int cwId) throws RemoteException;

    List<CourseworkRecord> getAllByGroupAndTeacher(int groupId, int teacherId) throws RemoteException;

    CourseworkRecord getById(int id) throws RemoteException;

}
