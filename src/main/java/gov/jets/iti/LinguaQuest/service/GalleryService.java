package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.dto.response.GalleryResponseDto;
import gov.jets.iti.LinguaQuest.entity.User;
import org.springframework.stereotype.Service;

@Service
public class GalleryService {

    public GalleryResponseDto getUserGallery(User user) {
        return new GalleryResponseDto(0,null);
    }
}
