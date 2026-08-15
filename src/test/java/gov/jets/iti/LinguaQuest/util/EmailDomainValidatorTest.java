package gov.jets.iti.LinguaQuest.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailDomainValidatorTest {

    private EmailDomainValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EmailDomainValidator();
    }

    @Test
    void testValidDomainHasMxRecord() {
        assertTrue(validator.hasValidMxRecord("test@gmail.com"));
        assertTrue(validator.hasValidMxRecord("test@outlook.com"));
    }

    @Test
    void testNonExistentDomainReturnsFalse() {
        assertFalse(validator.hasValidMxRecord("test@fakexyz999nonexistent123.com"));
    }

    @Test
    void testNullOrMalformedEmailReturnsFalse() {
        assertFalse(validator.hasValidMxRecord(null));
        assertFalse(validator.hasValidMxRecord("invalidemail"));
        assertFalse(validator.hasValidMxRecord("@nodomain.com"));
    }
}
