package gov.jets.iti.LinguaQuest.enums;

/**
 * Per-user status of a single Level (LevelSummary.status in the API).
 * Not to be confused with WorldStatus, which uses a different set of values
 * (LOCKED, IN_PROGRESS, COMPLETED) and is derived, not stored.
 */
public enum LevelStatus {
    LOCKED,
    AVAILABLE,
    COMPLETED
}
