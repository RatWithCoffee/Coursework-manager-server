package coursework_manager.models.users;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class Teacher extends User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String name;
    private String jobTitle;

    public Teacher(int id, String name, String jobTitle, String login, String password) {
        this.name = name;
        this.jobTitle = jobTitle;
        super.setUserId(id);
        super.setLogin(login);
        super.setPassword(password);
        super.setRole(Role.TEACHER);
    }

    public Teacher(int id, String name, String jobTitle) {
        super.setUserId(id);
        this.name = name;
        this.jobTitle = jobTitle;
        super.setRole(Role.TEACHER);
    }


}