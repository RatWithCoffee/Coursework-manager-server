package coursework_manager.models;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class Group implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;

    // Конструктор
    public Group(int id, String name) {
        this.id = id;
        this.name = name;
    }


    @Override
    public String toString() {
        return name;
    }
}
