package gov.jets.iti.LinguaQuest.dto.gallery;

import gov.jets.iti.LinguaQuest.dto.world.WordDto;

import java.util.List;

public record GalleryResponseDto(Integer totalCount, List<WordDto> words) {
}
