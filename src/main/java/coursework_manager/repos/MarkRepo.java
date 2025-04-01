package coursework_manager.repos;

import coursework_manager.models.Mark;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MarkRepo extends UnicastRemoteObject implements coursework_manager.rmi_interfaces.IMarkRepo {

    public MarkRepo() throws RemoteException {
        super(); // экспортирует объект для RMI
    }

    // Метод для получения оценок по ID курсовой записи
    public List<Mark> getMarksByCourseworkRecordId(int cwId, int groupId) {
        List<Mark> marks = new ArrayList<>();
        String query = "SELECT mark.id, mark.mark, student.name FROM mark " +
                "JOIN student ON student.id = mark.student_id WHERE mark.cw_id = ? AND student.group_id = ?";

        try (Connection connection = DbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, cwId);
            statement.setInt(2, groupId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                Integer markVal = resultSet.getObject("mark", Integer.class); // Получаем объект Integer, который может быть null

                String stName = resultSet.getString("name");

                Mark mark = new Mark(id, stName, markVal);
                marks.add(mark);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return marks;
    }


    // Метод для обновления оценки
    public void updateMark(Mark mark) {
        String query = "UPDATE mark SET mark = ? WHERE id = ?";

        try (Connection connection = DbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            if (mark.getMark() == null) {
                statement.setNull(1, Types.INTEGER);

            } else {
                statement.setInt(1, mark.getMark());

            }
            statement.setInt(2, mark.getId());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Метод для удаления оценки по ID
    public void deleteMark(int id) {
        String query = "DELETE FROM mark WHERE id = ?";

        try (Connection connection = DbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}