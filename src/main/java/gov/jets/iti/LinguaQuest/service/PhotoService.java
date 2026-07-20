package gov.jets.iti.LinguaQuest.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import gov.jets.iti.LinguaQuest.dto.ImageUploadResponseDTO;
import gov.jets.iti.LinguaQuest.exception.ImageUploadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoService {

    private final Cloudinary cloudinary;

    public ImageUploadResponseDTO uploadPhoto(MultipartFile file) {
        validateImageFile(file);

        try {
            log.info("Uploading photo to Cloudinary. Original filename: {}", file.getOriginalFilename());
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

            String url = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            log.info("Successfully uploaded photo to Cloudinary. Public ID: {}", publicId);

            return new ImageUploadResponseDTO(url, publicId);

        } catch (IOException e) {
            log.error("Error occurred while uploading photo to Cloudinary", e);
            throw new ImageUploadException("Failed to upload photo to Cloudinary", e);
        }
    }

    public void deletePhoto(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            throw new ImageUploadException("Public ID cannot be null or empty for photo deletion");
        }

        try {
            log.info("Deleting photo from Cloudinary. Public ID: {}", publicId);
            Map<?, ?> deleteResult = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            
            String resultStatus = (String) deleteResult.get("result");
            if ("ok".equalsIgnoreCase(resultStatus)) {
                log.info("Successfully deleted photo from Cloudinary. Public ID: {}", publicId);
            } else {
                log.warn("Cloudinary photo deletion response for Public ID {}: {}", publicId, resultStatus);
            }

        } catch (IOException e) {
            log.error("Error occurred while deleting photo from Cloudinary. Public ID: {}", publicId, e);
            throw new ImageUploadException("Failed to delete photo from Cloudinary", e);
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ImageUploadException("File cannot be null or empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ImageUploadException("Invalid file format. Only image files are allowed.");
        }
    }
}
