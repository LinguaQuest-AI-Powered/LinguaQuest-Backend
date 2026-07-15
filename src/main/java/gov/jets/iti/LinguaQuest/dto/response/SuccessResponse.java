package gov.jets.iti.LinguaQuest.dto.response;


public record SuccessResponse<T>(boolean success, T data) {}