package coursework_manager;

import coursework_manager.repos.GroupRepo;
import coursework_manager.repos.MarkRepo;
import coursework_manager.repos.RecordRepo;
import coursework_manager.repos.TeacherRepo;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Main {
    public static void main(String[] args) {
        try {
            GroupRepo groupRepo = new GroupRepo();
            MarkRepo markRepo = new MarkRepo();
            RecordRepo recordRepo = new RecordRepo();
            TeacherRepo teacherRepo = new TeacherRepo();
            System.out.println("sdf");

            Registry registry = LocateRegistry.createRegistry(1099);

            registry.bind("GroupRepo", groupRepo);
            registry.bind("MarkRepo", markRepo);
            registry.bind("RecordRepo", recordRepo);
            registry.bind("TeacherRepo", teacherRepo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}