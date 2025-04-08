package coursework_manager.repos;

import coursework_manager.models.Coursework;
import coursework_manager.models.CourseworkRecord;
import coursework_manager.models.Group;
import coursework_manager.models.users.Teacher;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecordRepo extends UnicastRemoteObject implements coursework_manager.rmi_interfaces.IRecordRepo {
    public RecordRepo() throws RemoteException {
        super();
    }

    public CourseworkRecord getById(int id) throws RemoteException {
        String query = "SELECT cr.id, c.id as cw_id, c.name as cw_name, " +
                "t.id as teacher_id, t.name as teacher_name, t.job_title, " +
                "g.id as group_id, g.name as group_name " +
                "FROM coursework_record cr " +
                "JOIN coursework c ON cr.cw_id = c.id " +
                "JOIN teacher t ON cr.teacher_id = t.id " +
                "JOIN \"group\" g ON cr.group_id = g.id " +
                "WHERE cr.id = ?";

        try (Connection connection = DbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                // Создаем объекты для CourseworkRecord
                Coursework coursework = new Coursework(
                        rs.getInt("cw_id"),
                        rs.getString("cw_name")
                );

                Teacher teacher = new Teacher(
                        rs.getInt("teacher_id"),
                        rs.getString("teacher_name"),
                        rs.getString("job_title")
                );

                Group group = new Group(
                        rs.getInt("group_id"),
                        rs.getString("group_name")
                );

                return new CourseworkRecord(
                        rs.getInt("id"),
                        teacher,
                        coursework,
                        group
                );
            } else {
                throw new RemoteException("Coursework record with id " + id + " not found");
            }
        } catch (SQLException e) {
            throw new RemoteException("Error getting coursework record: " + e.getMessage());
        }
    }

    public static int addNewCw(String title, int teacherId, int groupId) {
        Connection connection = null;
        int generatedCwId = -1;
        int generatedRecordId = -1;

        try {
            connection = DbManager.getConnection();
            connection.setAutoCommit(false); // Начинаем транзакцию

            // 1. Добавляем курсовую работу
            String cwQuery = "INSERT INTO coursework (name) VALUES (?)";
            try (PreparedStatement cwStatement = connection.prepareStatement(cwQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
                cwStatement.setString(1, title);
                cwStatement.executeUpdate();

                try (ResultSet generatedKeys = cwStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedCwId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Не удалось получить ID курсовой работы.");
                    }
                }
            }

            // 2. Добавляем запись о связи
            String recordQuery = "INSERT INTO coursework_record (cw_id, teacher_id, group_id) VALUES (?, ?, ?)";
            try (PreparedStatement recordStatement = connection.prepareStatement(recordQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
                recordStatement.setInt(1, generatedCwId);
                recordStatement.setInt(2, teacherId);
                recordStatement.setInt(3, groupId);
                recordStatement.executeUpdate();

                try (ResultSet generatedKeys = recordStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedRecordId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Не удалось получить ID записи о связи.");
                    }
                }
            }

            // 3. Добавляем записи с NULL оценками для всех студентов группы
            String studentsQuery = "SELECT id FROM student WHERE group_id = ?";
            String insertMarkQuery = "INSERT INTO mark (student_id, mark, cw_id) VALUES (?, NULL, ?)";

            try (PreparedStatement studentsStmt = connection.prepareStatement(studentsQuery);
                 PreparedStatement markStmt = connection.prepareStatement(insertMarkQuery)) {

                // Получаем всех студентов группы
                studentsStmt.setInt(1, groupId);
                ResultSet studentsRs = studentsStmt.executeQuery();

                // Для каждого студента добавляем запись в mark
                while (studentsRs.next()) {
                    int studentId = studentsRs.getInt("id");
                    markStmt.setInt(1, studentId);
                    markStmt.setInt(2, generatedCwId);
                    markStmt.addBatch(); // Добавляем в batch для эффективности
                }

                markStmt.executeBatch(); // Выполняем все INSERT одним запросом
            }

            connection.commit(); // Фиксируем транзакцию
            return generatedRecordId;

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback(); // Откатываем транзакцию при ошибке
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return -1;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true); // Восстанавливаем авто-коммит
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
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

    public List<CourseworkRecord> getAllByGroupAndTeacher(int groupId, int teacherId) {
        List<CourseworkRecord> records = new ArrayList<>();
        String query = "SELECT cr.id, cr.cw_id, c.id AS coursework_id, c.name AS coursework_name, " +
                "t.name AS teacher_name, t.job_title AS job_title, " +
                "g.id AS group_id, g.name AS group_name " +
                "FROM coursework_record cr " +
                "JOIN coursework c ON cr.cw_id = c.id " +
                "LEFT JOIN teacher t ON cr.teacher_id = t.id " +
                "JOIN \"group\" g ON cr.group_id = g.id " +
                "WHERE g.id = ? AND t.id = ?"; // Filter by group ID

        try (Connection connection = DbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Set the groupId parameter in the query
            statement.setInt(1, groupId);
            statement.setInt(2, teacherId);
            System.out.println(statement.toString());

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