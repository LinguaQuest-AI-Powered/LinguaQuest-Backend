package gov.jets.iti.LinguaQuest.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.io.UncheckedIOException;

@Service
public class AIService {

    private final ChatClient chatClient;

    public AIService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public boolean verifyImage(MultipartFile image, String targetWord) {
        MimeType mimeType = resolveMimeType(image);

        Media media = new Media(mimeType, toResource(image));

        String promptText = """
                You are verifying whether a photo depicts a specific object.
                The target word is: "%s"
                Does this image clearly show an instance of "%s"?
                Respond with ONLY the single word "true" or "false" — no punctuation, no explanation.
                """.formatted(targetWord, targetWord);

        UserMessage userMessage = UserMessage.builder()
                .text(promptText)
                .media(media)
                .build();

        ChatResponse response = chatClient.prompt(new Prompt(userMessage)).call().chatResponse();

        String result = response.getResult().getOutput().getText().trim().toLowerCase();
        return result.startsWith("true");
    }

    private MimeType resolveMimeType(MultipartFile image) {
        String contentType = image.getContentType();
        return contentType != null
                ? MimeTypeUtils.parseMimeType(contentType)
                : MimeTypeUtils.IMAGE_JPEG;
    }

    private Resource toResource(MultipartFile image) {
        try {
            return new org.springframework.core.io.ByteArrayResource(image.getBytes());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read uploaded image bytes", e);
        }
    }
}
