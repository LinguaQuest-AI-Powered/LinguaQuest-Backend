package gov.jets.iti.LinguaQuest.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AIService {

    public boolean verifyImage(MultipartFile image, String targetWord) {
        // TODO: replace with real AI call (Spring AI / Bedrock) once ready
        return true;
    }
}