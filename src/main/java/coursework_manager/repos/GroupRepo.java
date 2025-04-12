package coursework_manager.repos;

import coursework_manager.models.Group;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GroupRepo extends UnicastRemoteObject implements coursework_manager.rmi_interfaces.IGroupRepo {


    public GroupRepo() throws RemoteException {
        super(); // экспортирует объект для RMI
    }

    // Метод для получения всех групп
    public List<Group> getAllGroups()  throws RemoteException {
        List<Group> groups = new ArrayList<>();
        String query = "SELECT * FROM \"group\"";

        try (Connection connection = DbManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");

                Group group = new Group(id, name);
                groups.add(group);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return groups;
    }
}