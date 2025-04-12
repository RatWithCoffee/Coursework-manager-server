package coursework_manager.http_server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CourseworkRecordDto {
    private List<CourseworkDTO> courseworks;


    @JsonProperty("group_id")
    private int groupId;

    @JsonProperty("teacher_id")
    private int teacherId;

}