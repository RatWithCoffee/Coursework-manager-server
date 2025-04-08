package coursework_manager;

import coursework_manager.http_server.HttpServerCw;
import coursework_manager.repos.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Main {
    public static void main(String[] args) {
        try {
            GroupRepo groupRepo = new GroupRepo();
            MarkRepo markRepo = new MarkRepo();
            RecordRepo recordRepo = new RecordRepo();
            TeacherRepo teacherRepo = new TeacherRepo();
            LoginRepo loginRepo = new LoginRepo();
            System.out.println("sdf");

            Registry registry = LocateRegistry.createRegistry(1099);

            registry.bind("GroupRepo", groupRepo);
            registry.bind("MarkRepo", markRepo);
            registry.bind("RecordRepo", recordRepo);
            registry.bind("TeacherRepo", teacherRepo);
            registry.bind("LoginRepo", loginRepo);

            HttpServerCw.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}