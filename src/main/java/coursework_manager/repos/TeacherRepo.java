package coursework_manager.repos;

import coursework_manager.models.Teacher;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherRepo extends UnicastRemoteObject implements coursework_manager.rmi_interfaces.ITeacherRepo{

    public TeacherRepo() throws RemoteException {
        super(); // экспортирует объект для RMI
    }


    // Метод для получения всех преподавателей
    public List<Teacher> getAllTeachers() {
        List<Teacher> teachers = new ArrayList<>();
        String query = "SELECT * FROM teacher";

        try (Connection connection = DbManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String jobTitle = resultSet.getString("job_title");

                Teacher teacher = new Teacher(id, name, jobTitle);
                teachers.add(teacher);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return teachers;
    }

    // Метод для добавления нового преподавателя
    public boolean addTeacher(Teacher teacher) {
        String query = "INSERT INTO teacher (name, job_title) VALUES (?, ?) RETURNING id";

        try (Connection connection = DbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, teacher.getName());
            statement.setString(2, teacher.getJobTitle());
            ResultSet rs = statement.executeQuery(); // Используем executeQuery() для RETURNING

            if (rs.next()) {
                int generatedId = rs.getInt(1);
                teacher.setId(generatedId); // Устанавливаем ID в объект Teacher
                return true;
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Метод для обновления данных преподавателя
    public boolean updateTeacher(Teacher teacher) {
        String query = "UPDATE teacher SET name = ?, job_title = ? WHERE id = ?";

        try (Connection connection = DbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, teacher.getName());
            statement.setString(2, teacher.getJobTitle());
            statement.setInt(3, teacher.getId());
            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Метод для удаления преподавателя по ID
    public boolean deleteTeacher(int id) {
        String query = "DELETE FROM teacher WHERE id = ?";

        try (Connection connection = DbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, id);
            int rowsDeleted = statement.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}