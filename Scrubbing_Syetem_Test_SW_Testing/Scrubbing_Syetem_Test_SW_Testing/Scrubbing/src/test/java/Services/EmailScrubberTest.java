package Services;

import Interfaces.IScrubEmails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class EmailScrubberTest {

    private IScrubEmails emailScrubber;

    @BeforeEach
    void setUp() {
        emailScrubber = new EmailScrubber();
    }

    // ========== POSITIVE TEST CASES ==========

    @Test
    @DisplayName("Should replace simple email with [EMAIL_HIDDEN]")
    void testScrub_SimpleEmail() {
        String input = "Contact me at john@example.com";
        String expected = "Contact me at [EMAIL_HIDDEN]";
        assertEquals(expected, emailScrubber.scrub(input));
    }

    @Test
    @DisplayName("Should replace email with dots in local part")
    void testScrub_EmailWithDots() {
        String input = "john.doe@example.com";
        String expected = "[EMAIL_HIDDEN]";
        assertEquals(expected, emailScrubber.scrub(input));
    }

    @Test
    @DisplayName("Should replace email with plus addressing")
    void testScrub_EmailWithPlus() {
        String input = "john+spam@example.com";
        String expected = "[EMAIL_HIDDEN]";
        assertEquals(expected, emailScrubber.scrub(input));
    }

    @Test
    @DisplayName("Should replace email with numbers")
    void testScrub_EmailWithNumbers() {
        String input = "user123@domain456.com";
        String expected = "[EMAIL_HIDDEN]";
        assertEquals(expected, emailScrubber.scrub(input));
    }

    @Test
    @DisplayName("Should replace multiple emails in one string")
    void testScrub_MultipleEmails() {
        String input = "Send to john@example.com and jane@test.org";
        String expected = "Send to [EMAIL_HIDDEN] and [EMAIL_HIDDEN]";
        assertEquals(expected, emailScrubber.scrub(input));
    }

    @Test
    @DisplayName("Should handle string with no email")
    void testScrub_NoEmail() {
        String input = "Hello, this is a test message";
        String expected = "Hello, this is a test message";
        assertEquals(expected, emailScrubber.scrub(input));
    }

    @Test
    @DisplayName("Should handle email with subdomain")
    void testScrub_EmailWithSubdomain() {
        String input = "user@mail.example.com";
        String expected = "[EMAIL_HIDDEN]";
        assertEquals(expected, emailScrubber.scrub(input));
    }

    @Test
    @DisplayName("Should handle email with underscore")
    void testScrub_EmailWithUnderscore() {
        String input = "john_doe@example.com";
        String expected = "[EMAIL_HIDDEN]";
        assertEquals(expected, emailScrubber.scrub(input));
    }

    @Test
    @DisplayName("Should handle email with hyphen")
    void testScrub_EmailWithHyphen() {
        String input = "john-doe@example.com";
        String expected = "[EMAIL_HIDDEN]";
        assertEquals(expected, emailScrubber.scrub(input));
    }

    // ========== NEGATIVE TEST CASES ==========

    @Test
    @DisplayName("Should throw NullPointerException when input is null")
    void testScrub_NullInput_ThrowsNullPointerException() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> emailScrubber.scrub(null));
        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("Should throw NullPointerException when input is empty")
    void testScrub_EmptyString_ThrowsNullPointerException() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> emailScrubber.scrub(""));
        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("Should throw NullPointerException when input contains only spaces")
    void testScrub_OnlySpaces_ThrowsNullPointerException() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> emailScrubber.scrub("   "));
        assertNotNull(exception.getMessage());
    }

    // ========== EDGE CASES - Invalid Email Patterns ==========

    @Test
    @DisplayName("Should not replace invalid email (no @ symbol)")
    void testScrub_InvalidEmailNoAtSymbol() {
        String input = "john.doeexample.com";
        String expected = "john.doeexample.com";
        assertEquals(expected, emailScrubber.scrub(input));
    }

    @Test
    @DisplayName("Should not replace invalid email (no domain)")
    void testScrub_InvalidEmailNoDomain() {
        String input = "john@";
        String expected = "john@";
        assertEquals(expected, emailScrubber.scrub(input));
    }

    @Test
    @DisplayName("Should not replace invalid email (no TLD)")
    void testScrub_InvalidEmailNoTLD() {
        String input = "john@example";
        String expected = "john@example";
        assertEquals(expected, emailScrubber.scrub(input));
    }

    @Test
    @DisplayName("Should not replace invalid email (special chars at start)")
    void testScrub_InvalidEmailSpecialChars() {
        String input = "@example.com";
        String expected = "@example.com";
        assertEquals(expected, emailScrubber.scrub(input));
    }

    // ========== BOUNDARY VALUE ANALYSIS ==========

    @Test
    @DisplayName("Should handle very long valid email")
    void testScrub_VeryLongEmail() {
        StringBuilder localPart = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            localPart.append("a");
        }
        StringBuilder domain = new StringBuilder();
        for (int i = 0; i < 30; i++) {
            domain.append("b");
        }
        String input = localPart.toString() + "@" + domain.toString() + ".com";
        String expected = "[EMAIL_HIDDEN]";
        assertEquals(expected, emailScrubber.scrub(input));
    }

    @Test
    @DisplayName("Should handle email at beginning of string")
    void testScrub_EmailAtBeginning() {
        String input = "john@example.com is my email";
        String expected = "[EMAIL_HIDDEN] is my email";
        assertEquals(expected, emailScrubber.scrub(input));
    }

    @Test
    @DisplayName("Should handle email at end of string")
    void testScrub_EmailAtEnd() {
        String input = "My email is john@example.com";
        String expected = "My email is [EMAIL_HIDDEN]";
        assertEquals(expected, emailScrubber.scrub(input));
    }

    @Test
    @DisplayName("Should handle email with minimum length")
    void testScrub_MinimumLengthEmail() {
        String input = "a@b.c";
        String expected = "[EMAIL_HIDDEN]";
        assertEquals(expected, emailScrubber.scrub(input));
    }

    @ParameterizedTest
    @DisplayName("Should handle various valid email formats")
    @CsvSource({
            "'user@example.com', '[EMAIL_HIDDEN]'",
            "'user.name@example.co.uk', '[EMAIL_HIDDEN]'",
            "'user+tag@example.com', '[EMAIL_HIDDEN]'",
            "'user-name@example.org', '[EMAIL_HIDDEN]'",
            "'user123@example.net', '[EMAIL_HIDDEN]'"
    })
    void testScrub_VariousValidEmails(String input, String expected) {
        assertEquals(expected, emailScrubber.scrub(input));
    }
}