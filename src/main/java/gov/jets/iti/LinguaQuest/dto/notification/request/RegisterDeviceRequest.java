package gov.jets.iti.LinguaQuest.dto.notification.request;
import gov.jets.iti.LinguaQuest.enums.DevicePlatform;

public record RegisterDeviceRequest(String token, DevicePlatform platform) {}