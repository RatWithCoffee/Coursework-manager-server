package coursework_manager.repos;

import coursework_manager.models.users.Teacher;

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
        String query = "SELECT t.id, t.name, t.job_title, u.login, u.password  FROM teacher t JOIN \"user\" u ON u.id = t.id";

        try (Connection connection = DbManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String jobTitle = resultSet.getString("job_title");
                String login = resultSet.getString("login");
                String password = resultSet.getString("password");

                Teacher teacher = new Teacher(id, name, jobTitle, login, password);
                teachers.add(teacher);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return teachers;
    }

    public boolean addTeacher(Teacher teacher) {
        System.out.println("[INFO] Starting to add teacher: " + teacher);

        String insertUserQuery = "INSERT INTO \"user\"(login, password, role) VALUES (?, ?, CAST(? AS user_role)) RETURNING id";
        String insertTeacherQuery = "INSERT INTO teacher (id, name, job_title) VALUES (?, ?, ?)";

        Connection connection = null;
        try {
            System.out.println("[DEBUG] Getting database connection...");
            connection = DbManager.getConnection();
            if (connection == null) {
                System.out.println("[ERROR] Failed to get database connection!");
                return false;
            }

            connection.setAutoCommit(false);
            System.out.println("[DEBUG] Transaction started");

            // 1. Добавляем пользователя
            int generatedUserId;
            try (PreparedStatement userStatement = connection.prepareStatement(insertUserQuery)) {
                System.out.println("[DEBUG] Preparing user insert: " + insertUserQuery);
                userStatement.setString(1, teacher.getLogin());
                userStatement.setString(2, teacher.getPassword());
                userStatement.setString(3, teacher.getRole().toString().toLowerCase());

                System.out.println("[INFO] Executing user insert - login: " + teacher.getLogin()
                        + ", role: " + teacher.getRole());

                try (ResultSet rs = userStatement.executeQuery()) {
                    if (rs.next()) {
                        generatedUserId = rs.getInt(1);
                        teacher.setUserId(generatedUserId);
                        System.out.println("[INFO] User inserted successfully. ID: " + generatedUserId);
                    } else {
                        System.out.println("[ERROR] No ID returned from user insert!");
                        connection.rollback();
                        return false;
                    }
                }
            } catch (SQLException e) {
                System.out.println("[ERROR] Error inserting user: " + e.getMessage());
                connection.rollback();
                return false;
            }

            // 2. Добавляем преподавателя
            try (PreparedStatement teacherStatement = connection.prepareStatement(insertTeacherQuery)) {
                System.out.println("[DEBUG] Preparing teacher insert: " + insertTeacherQuery);
                teacherStatement.setInt(1, teacher.getUserId());
                teacherStatement.setString(2, teacher.getName());
                teacherStatement.setString(3, teacher.getJobTitle());

                System.out.println("[INFO] Executing teacher insert - userId: " + teacher.getUserId()
                        + ", name: " + teacher.getName()
                        + ", jobTitle: " + teacher.getJobTitle());

                int affectedRows = teacherStatement.executeUpdate();
                if (affectedRows > 0) {
                    connection.commit();
                    System.out.println("[INFO] Teacher inserted successfully. Transaction committed.");
                    return true;
                } else {
                    System.out.println("[ERROR] No rows affected by teacher insert!");
                    connection.rollback();
                    return false;
                }
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Database error: " + e.getMessage());
            if (connection != null) {
                try {
                    connection.rollback();
                    System.out.println("[INFO] Transaction rolled back due to error");
                } catch (SQLException ex) {
                    System.out.println("[ERROR] Rollback failed: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                    System.out.println("[DEBUG] Connection closed");
                } catch (SQLException e) {
                    System.out.println("[ERROR] Error closing connection: " + e.getMessage());
                }
            }
        }
    }
    // Метод для обновления данных преподавателя
    public boolean updateTeacher(Teacher teacher) {
        String insertUserQuery = "UPDATE \"user\" SET login = ?, password = ?, role = CAST(? AS user_role) WHERE id = ?";
        String insertTeacherQuery = "UPDATE teacher SET name = ?, job_title = ? WHERE id = ?";

        Connection connection = null;
        try {
            connection = DbManager.getConnection();
            connection.setAutoCommit(false); // Начинаем транзакцию

            // 1. Сначала добавляем пользователя
            try (PreparedStatement userStatement = connection.prepareStatement(insertUserQuery)) {
                userStatement.setString(1, teacher.getLogin());
                userStatement.setString(2, teacher.getPassword());
                userStatement.setString(3, teacher.getRole().toString().toLowerCase());
                userStatement.setInt(4, teacher.getUserId());

                try  {
                    userStatement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                    try {
                        connection.rollback(); // Откатываем при ошибке
                        return false;
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        return false;

                    }
                }
            }

            // 2. Затем добавляем преподавателя
            try (PreparedStatement teacherStatement = connection.prepareStatement(insertTeacherQuery)) {
                teacherStatement.setString(1, teacher.getName());
                teacherStatement.setString(2, teacher.getJobTitle());
                teacherStatement.setInt(3, teacher.getUserId());


                try {
                    teacherStatement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                    try {

                        connection.rollback();
                        return false;// Откатываем при ошибке
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        return false;

                    }
                }
                return true;


            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback(); // Откатываем при ошибке
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true); // Восстанавливаем auto-commit режим
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Метод для удаления преподавателя по ID
    public boolean deleteTeacher(int teacherId) {
        String query = "DELETE FROM \"user\" WHERE id = ?";

        try (Connection connection = DbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, teacherId);
            int rowsDeleted = statement.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}