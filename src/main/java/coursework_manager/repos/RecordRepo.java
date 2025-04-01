package coursework_manager.repos;

import coursework_manager.models.Coursework;
import coursework_manager.models.CourseworkRecord;
import coursework_manager.models.Group;
import coursework_manager.models.Teacher;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecordRepo extends UnicastRemoteObject implements coursework_manager.rmi_interfaces.IRecordRepo {
    public RecordRepo() throws RemoteException {
        super();
    }

    public int add(int cwId, int teacherId, int groupId) {
        String query = "INSERT INTO coursework_record (cw_id, teacher_id, group_id) VALUES (?, ?, ?)";
        int generatedId = -1; // Значение по умолчанию, если что-то пойдет не так

        try (Connection connection = DbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, cwId);
            statement.setInt(2, teacherId);
            statement.setInt(3, groupId);
            statement.executeUpdate();

            // Получаем сгенерированный ID
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    generatedId = generatedKeys.getInt(1); // Получаем значение первого столбца (id)
                } else {
                    throw new SQLException("Не удалось получить сгенерированный ID.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return generatedId; // Возвращаем ID вставленной записи
    }

    // Метод для редактирования записи в coursework_record
    public  void edit(int recordId, int cwId, int teacherId, int groupId) {
        String query = "UPDATE coursework_record SET cw_id = ?, teacher_id = ?, group_id = ? WHERE cw_id = ?";

        try (Connection connection = DbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, cwId);
            statement.setInt(2, teacherId);
            statement.setInt(3, groupId);
            statement.setInt(4, recordId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Метод для получения всех записей из coursework_record
    public  List<CourseworkRecord> getAll() {
        List<CourseworkRecord> records = new ArrayList<>();
        String query = "SELECT cr.id, cr.cw_id, c.id AS coursework_id, c.name AS coursework_name, " +
                "t.id AS teacher_id, t.name AS teacher_name, t.job_title AS job_title " +
                "g.id AS group_id, g.name AS group_name " +
                "FROM coursework_record cr " +
                "JOIN coursework c ON cr.cw_id = c.id " +
                "LEFT JOIN teacher t ON (cr.teacher_id = t.id) " +
                "JOIN \"group\" g ON cr.group_id = g.id";

        try (Connection connection = DbManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                // Получаем данные из ResultSet
                int id = resultSet.getInt("id");
                System.out.println("Get id");
                System.out.println(id);

                int cwId = resultSet.getInt("cw_id");

                // Создаем объект Coursework
                int courseworkId = resultSet.getInt("coursework_id");
                String courseworkName = resultSet.getString("coursework_name");
                Coursework coursework = new Coursework(courseworkId, courseworkName);

                // Создаем объект Teacher
                int teacherId = resultSet.getInt("teacher_id");
                String teacherName = resultSet.getString("teacher_name");
                String jobTitle = resultSet.getString("job_title");
                Teacher teacher = new Teacher(teacherId, teacherName, jobTitle);

                // Создаем объект Group
                int groupId = resultSet.getInt("group_id");
                String groupName = resultSet.getString("group_name");
                Group group = new Group(groupId, groupName);

                // Создаем объект CourseworkRecord
                CourseworkRecord record = new CourseworkRecord(id, teacher, coursework, group);
                records.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return records;
    }

    // here is the problem
    public  List<CourseworkRecord> getAllByGroup(int groupId) {
        List<CourseworkRecord> records = new ArrayList<>();
        String query = "SELECT cr.id, cr.cw_id, c.id AS coursework_id, c.name AS coursework_name, " +
                "t.id AS teacher_id, t.name AS teacher_name, t.job_title AS job_title, " +
                "g.id AS group_id, g.name AS group_name " +
                "FROM coursework_record cr " +
                "JOIN coursework c ON cr.cw_id = c.id " +
                "LEFT JOIN teacher t ON cr.teacher_id = t.id " +
                "JOIN \"group\" g ON cr.group_id = g.id " +
                "WHERE g.id = ?"; // Filter by group ID

        try (Connection connection = DbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Set the groupId parameter in the query
            statement.setInt(1, groupId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    // Получаем данные из ResultSet
                    int id = resultSet.getInt("id");
                    int cwId = resultSet.getInt("cw_id");

                    // Создаем объект Coursework
                    int courseworkId = resultSet.getInt("coursework_id");
                    String courseworkName = resultSet.getString("coursework_name");
                    Coursework coursework = new Coursework(courseworkId, courseworkName);

                    // Создаем объект Teacher
                    int teacherId = resultSet.getInt("teacher_id");
                    String teacherName = resultSet.getString("teacher_name");
                    String jobTitle = resultSet.getString("job_title");
                    Teacher teacher = new Teacher(teacherId, teacherName, jobTitle);

                    // Создаем объект Group
                    int fetchedGroupId = resultSet.getInt("group_id");
                    String groupName = resultSet.getString("group_name");
                    Group group = new Group(fetchedGroupId, groupName);

                    // Создаем объект CourseworkRecord
                    CourseworkRecord record = new CourseworkRecord(id, teacher, coursework, group);
                    records.add(record);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return records;
    }

    // Метод для удаления записи из coursework_record
    public  void remove(int cwId) {
        String query = "DELETE FROM coursework_record WHERE cw_id = ?";

        try (Connection connection = DbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, cwId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}