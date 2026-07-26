package gov.jets.iti.LinguaQuest.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
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

    public String generateHint(String wordText, String nativeLanguageName) {
        String promptText = """
            Generate a short, simple hint in %s for a language learner trying to guess a word.
            The word/concept is: "%s"
            The hint must describe the word without stating it directly, and must be written entirely in %s.
            Respond with ONLY the hint text — no quotes, no translation, no explanation.
            """.formatted(nativeLanguageName, wordText, nativeLanguageName);

        ChatResponse response = chatClient.prompt(new Prompt(new UserMessage(promptText))).call().chatResponse();
        return response.getResult().getOutput().getText().trim();
    }

    private Resource toResource(MultipartFile image) {
        try {
            return new ByteArrayResource(image.getBytes());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read uploaded image bytes", e);
        }
    }
}
