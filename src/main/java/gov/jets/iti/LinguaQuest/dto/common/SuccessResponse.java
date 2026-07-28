package gov.jets.iti.LinguaQuest.dto.common;


public record SuccessResponse<T>(boolean success, T data) {}