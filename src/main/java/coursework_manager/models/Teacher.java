package coursework_manager.models;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class Teacher implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private String jobTitle;

    public Teacher(int id, String name, String jobTitle) {
        this.id = id;
        this.name = name;
        this.jobTitle = jobTitle;
    }

    public Teacher(String name, String jobTitle) {
        this.name = name;
        this.jobTitle = jobTitle;
    }

}