package coursework_manager.models;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class Mark implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private int id;
    private String studentName;
    private Integer mark;

    public Mark(int id, String studentName, Integer mark) {
        this.id = id;
        this.studentName = studentName;
        this.mark = mark;
    }

}