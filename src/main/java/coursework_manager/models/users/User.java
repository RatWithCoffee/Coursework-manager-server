package coursework_manager.models.users;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String login;
    private String password;
    private Role role;
    private int userId;

    public User() {
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

}
