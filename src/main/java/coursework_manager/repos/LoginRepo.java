package coursework_manager.repos;


import coursework_manager.models.users.Teacher;
import coursework_manager.models.users.User;
import coursework_manager.models.users.Role;
import coursework_manager.rmi_interfaces.ILoginRepo;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginRepo extends UnicastRemoteObject implements ILoginRepo {

    public LoginRepo() throws RemoteException {
        super(); // экспортирует объект для RMI
    }

    @Override
    public User login(User user) throws RemoteException {
        String query = "SELECT id, role FROM \"user\" WHERE login = ? AND password = ?";

        try (Connection connection = DbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Устанавливаем параметры запроса
            statement.setString(1, user.getLogin());
            statement.setString(2, user.getPassword());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // Если пользователь найден, устанавливаем его роль
                    user.setUserId(resultSet.getInt("id"));
                    String roleStr = resultSet.getString("role");
                    Role role = Role.valueOf(roleStr.toUpperCase());
                    user.setRole(role);

                    if (user.getRole() == Role.TEACHER) {
                        return getTeacher(user);
                    }
                    return user;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Ошибка при проверке логина и пароля", e);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new RemoteException("Неверная роль пользователя в базе данных", e);
        }



        return null;
    }

    public Teacher getTeacher(User user) throws RemoteException {
        String query = "SELECT name, job_title FROM teacher WHERE id = ?";

        try (Connection connection = DbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Устанавливаем параметры запроса
            statement.setInt(1, user.getUserId());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // Если пользователь найден, устанавливаем его роль
                    String name = resultSet.getString("name");
                    String jobTitle = resultSet.getString("job_title");
                    return new Teacher(user.getUserId(), name, jobTitle, user.getLogin(), user.getPassword());
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Ошибка при проверке логина и пароля", e);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new RemoteException("Неверная роль пользователя в базе данных", e);
        }

        return null;

    }
}