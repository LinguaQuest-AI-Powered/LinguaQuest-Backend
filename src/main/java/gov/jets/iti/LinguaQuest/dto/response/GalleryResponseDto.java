package gov.jets.iti.LinguaQuest.dto.response;

import java.util.List;

public record GalleryResponseDto(Integer totalCount, List<WordDto> words) {
}
