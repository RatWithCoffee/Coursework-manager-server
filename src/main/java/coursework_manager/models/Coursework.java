package coursework_manager.models;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data

public class Coursework implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;

    public Coursework(int id, String name) {
        this.id = id;
        this.name = name;
    }

}