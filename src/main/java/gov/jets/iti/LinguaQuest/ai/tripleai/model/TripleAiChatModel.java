package gov.jets.iti.LinguaQuest.ai.tripleai.model;

import gov.jets.iti.LinguaQuest.ai.tripleai.client.TripleAiApiClient;
import gov.jets.iti.LinguaQuest.ai.tripleai.client.TripleAiException;
import gov.jets.iti.LinguaQuest.ai.tripleai.config.TripleAiProperties;
import gov.jets.iti.LinguaQuest.ai.tripleai.dto.request.ChatMessageDto;
import gov.jets.iti.LinguaQuest.ai.tripleai.dto.request.ImageDataDto;
import gov.jets.iti.LinguaQuest.ai.tripleai.dto.request.MultimodalChatRequest;
import gov.jets.iti.LinguaQuest.ai.tripleai.dto.request.MultimodalMessageDto;
import gov.jets.iti.LinguaQuest.ai.tripleai.dto.request.TextChatRequest;
import gov.jets.iti.LinguaQuest.ai.tripleai.dto.response.GatewayChatResponse;
import gov.jets.iti.LinguaQuest.ai.tripleai.dto.response.GatewayUsage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeType;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Spring AI ChatModel implementation for the University AI Gateway (TripleAI).
 */
public class TripleAiChatModel implements ChatModel {

    private final TripleAiApiClient apiClient;
    private final TripleAiProperties properties;
    private final TripleAiChatOptions defaultOptions;

    public TripleAiChatModel(TripleAiApiClient apiClient, TripleAiProperties properties) {
        this.apiClient = Objects.requireNonNull(apiClient, "TripleAiApiClient must not be null");
        this.properties = Objects.requireNonNull(properties, "TripleAiProperties must not be null");
        this.defaultOptions = TripleAiChatOptions.builder()
                .model(properties.getDefaultModel())
                .maxTokens(properties.getDefaultMaxTokens())
                .build();
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        Objects.requireNonNull(prompt, "Prompt must not be null");

        List<Message> messages = prompt.getInstructions();
        if (messages == null || messages.isEmpty()) {
            throw new TripleAiException("Prompt must contain at least one message.");
        }

        boolean isMultimodal = isMultimodalPrompt(messages);
        String modelId = resolveModelId(prompt, isMultimodal);
        Integer maxTokens = resolveMaxTokens(prompt);

        GatewayChatResponse gatewayResponse;
        if (isMultimodal) {
            MultimodalChatRequest request = buildMultimodalRequest(messages, modelId, maxTokens);
            gatewayResponse = apiClient.sendMultimodalChat(request);
        } else {
            TextChatRequest request = buildTextChatRequest(messages, modelId, maxTokens);
            gatewayResponse = apiClient.sendTextChat(request);
        }

        return mapToChatResponse(gatewayResponse);
    }

    @Override
    @SuppressWarnings("deprecation")
    public ChatOptions getDefaultOptions() {
        return defaultOptions.copy();
    }

    private String resolveModelId(Prompt prompt, boolean isMultimodal) {
        if (prompt.getOptions() != null && prompt.getOptions().getModel() != null && !prompt.getOptions().getModel().isBlank()) {
            return prompt.getOptions().getModel();
        }
        if (isMultimodal && properties.getMultimodalModel() != null && !properties.getMultimodalModel().isBlank()) {
            return properties.getMultimodalModel();
        }
        return properties.getDefaultModel();
    }

    private Integer resolveMaxTokens(Prompt prompt) {
        if (prompt.getOptions() instanceof TripleAiChatOptions tripleOptions && tripleOptions.getMaxTokens() != null) {
            return tripleOptions.getMaxTokens();
        }
        return properties.getDefaultMaxTokens();
    }

    private boolean isMultimodalPrompt(List<Message> messages) {
        for (Message message : messages) {
            if (message instanceof UserMessage userMessage) {
                List<Media> mediaList = userMessage.getMedia();
                if (mediaList != null && !mediaList.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    private TextChatRequest buildTextChatRequest(List<Message> messages, String modelId, Integer maxTokens) {
        StringBuilder systemPromptBuilder = new StringBuilder();
        List<ChatMessageDto> chatMessages = new ArrayList<>();

        for (Message message : messages) {
            if (message.getMessageType() == MessageType.SYSTEM || message instanceof SystemMessage) {
                if (!systemPromptBuilder.isEmpty()) {
                    systemPromptBuilder.append("\n");
                }
                systemPromptBuilder.append(message.getText());
            } else {
                String role = mapMessageTypeToRole(message.getMessageType());
                chatMessages.add(new ChatMessageDto(role, message.getText()));
            }
        }

        String systemPrompt = systemPromptBuilder.isEmpty() ? null : systemPromptBuilder.toString();
        return new TextChatRequest(modelId, chatMessages, systemPrompt, maxTokens);
    }

    private MultimodalChatRequest buildMultimodalRequest(List<Message> messages, String modelId, Integer maxTokens) {
        List<MultimodalMessageDto> multimodalMessages = new ArrayList<>();

        for (Message message : messages) {
            if (message.getMessageType() == MessageType.SYSTEM || message instanceof SystemMessage) {
                // For multimodal chat endpoint, append system instruction as a user message or text prefix
                multimodalMessages.add(new MultimodalMessageDto("user", "System Instruction: " + message.getText(), null));
                continue;
            }

            String role = mapMessageTypeToRole(message.getMessageType());
            List<ImageDataDto> images = null;

            if (message instanceof UserMessage userMessage && userMessage.getMedia() != null && !userMessage.getMedia().isEmpty()) {
                images = new ArrayList<>();
                for (Media media : userMessage.getMedia()) {
                    images.add(mapMediaToImageData(media));
                }
            }

            multimodalMessages.add(new MultimodalMessageDto(role, message.getText(), images));
        }

        return new MultimodalChatRequest(modelId, multimodalMessages, maxTokens);
    }

    private ImageDataDto mapMediaToImageData(Media media) {
        String format = resolveImageFormat(media);
        String base64Data = encodeMediaToBase64(media);
        return new ImageDataDto(format, base64Data);
    }

    private String resolveImageFormat(Media media) {
        MimeType mimeType = media.getMimeType();
        if (mimeType != null && mimeType.getSubtype() != null && !mimeType.getSubtype().isBlank()) {
            String subtype = mimeType.getSubtype().toLowerCase();
            if (subtype.contains("png")) return "png";
            if (subtype.contains("jpeg") || subtype.contains("jpg")) return "jpeg";
            if (subtype.contains("webp")) return "webp";
            return subtype;
        }
        return "jpeg";
    }

    private String encodeMediaToBase64(Media media) {
        Object data = media.getData();
        try {
            if (data instanceof byte[] bytes) {
                return Base64.getEncoder().encodeToString(bytes);
            } else if (data instanceof Resource resource) {
                try (InputStream is = resource.getInputStream()) {
                    byte[] bytes = is.readAllBytes();
                    return Base64.getEncoder().encodeToString(bytes);
                }
            } else if (data instanceof InputStream is) {
                byte[] bytes = is.readAllBytes();
                return Base64.getEncoder().encodeToString(bytes);
            } else if (data instanceof String str) {
                // Check if already Base64
                return str;
            }
        } catch (Exception e) {
            throw new TripleAiException("Failed to read image media content for Base64 encoding", e);
        }

        throw new TripleAiException("Unsupported Media data type: " + (data != null ? data.getClass().getName() : "null"));
    }

    private String mapMessageTypeToRole(MessageType messageType) {
        return switch (messageType) {
            case USER -> "user";
            case ASSISTANT -> "assistant";
            case SYSTEM -> "system";
            default -> "user";
        };
    }

    private ChatResponse mapToChatResponse(GatewayChatResponse response) {
        String outputText = Optional.ofNullable(response.outputText()).orElse("");
        AssistantMessage assistantMessage = new AssistantMessage(outputText);
        Generation generation = new Generation(assistantMessage);

        Usage usage = null;
        if (response.usage() != null) {
            GatewayUsage gUsage = response.usage();
            int inputTokens = Optional.ofNullable(gUsage.inputTokens()).orElse(0);
            int outputTokens = Optional.ofNullable(gUsage.outputTokens()).orElse(0);
            int totalTokens = Optional.ofNullable(gUsage.totalTokens()).orElse(inputTokens + outputTokens);
            usage = new DefaultUsage(inputTokens, outputTokens, totalTokens);
        }

        ChatResponseMetadata metadata = ChatResponseMetadata.builder()
                .usage(usage != null ? usage : new DefaultUsage(0, 0, 0))
                .model(response.modelId() != null ? response.modelId() : properties.getDefaultModel())
                .build();

        return new ChatResponse(List.of(generation), metadata);
    }
}
