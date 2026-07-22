package gov.jets.iti.LinguaQuest.dto.response;

import gov.jets.iti.LinguaQuest.enums.LevelStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LevelDto {
    private Long id;
    private Integer order;
    private LevelStatus status;
    private String word;
}
