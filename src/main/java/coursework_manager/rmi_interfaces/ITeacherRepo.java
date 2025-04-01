package coursework_manager.rmi_interfaces;

import coursework_manager.models.Teacher;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ITeacherRepo extends Remote {
    List<Teacher> getAllTeachers() throws RemoteException;

    boolean addTeacher(Teacher teacher) throws RemoteException;

    boolean updateTeacher(Teacher teacher) throws RemoteException;

    boolean deleteTeacher(int id) throws RemoteException;
}
