package gov.jets.iti.LinguaQuest.dto.mission.request;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

public record DailyMissionRequest(MultipartFile image,
                                  @NotEmpty(message = "word is required") String word) {
}
