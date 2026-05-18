package Services;

import Interfaces.IScrubDigits;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DigitScrubberTest {

    private IScrubDigits scrubber;

    @BeforeEach
    void setUp() {
        scrubber = new DigitScrubber();
    }

    // ------------------------------------------------------------------ //
    // P1 – null input
    // ------------------------------------------------------------------ //

    /** Null input must throw NullPointerException. */
    @Test
    void testScrub_NullInput_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> scrubber.scrub(null));
    }

    // ------------------------------------------------------------------ //
    // P2 – blank input
    // ------------------------------------------------------------------ //

    /** Empty string must throw IllegalArgumentException. (B3) */
    @Test
    void testScrub_EmptyString_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> scrubber.scrub(""));
    }

    /** Single space must throw IllegalArgumentException. (B4) */
    @Test
    void testScrub_SingleSpace_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> scrubber.scrub(" "));
    }

    /** Multiple spaces must throw IllegalArgumentException. */
    @Test
    void testScrub_MultipleSpaces_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> scrubber.scrub(" "));
    }

    // ------------------------------------------------------------------ //
    // P3 – string with NO digits
    // ------------------------------------------------------------------ //

    /** A plain alphabetic string should be returned unchanged. */
    @Test
    void testScrub_NoDigits_ReturnsUnchanged() {
        String input = "Hello World";
        assertEquals("Hello World", scrubber.scrub(input));
    }

    /** A single non-digit character should be returned unchanged. (B2) */
    @Test
    void testScrub_SingleNonDigitChar_ReturnsUnchanged() {
        assertEquals("a", scrubber.scrub("a"));
    }

    /** Special characters only — should be returned unchanged. */
    @Test
    void testScrub_SpecialCharsOnly_ReturnsUnchanged() {
        assertEquals("!@#$%", scrubber.scrub("!@#$%"));
    }

    // ------------------------------------------------------------------ //
    // P4 – string with ONLY digits
    // ------------------------------------------------------------------ //

    /** A single digit should become a single X. (B1) */
    @Test
    void testScrub_SingleDigit_ReturnsX() {
        assertEquals("X", scrubber.scrub("5"));
    }

    /** Multiple digits should all become X's. */
    @Test
    void testScrub_MultipleDigits_AllReplacedWithX() {
        assertEquals("XXXXXXXXXX", scrubber.scrub("1234567890"));
    }

    // ------------------------------------------------------------------ //
    // P5 – mixed string (digits + other chars)
    // ------------------------------------------------------------------ //

    /** Digits inside a sentence should be replaced; letters kept. */
    @Test
    void testScrub_MixedString_OnlyDigitsReplaced() {
        assertEquals("Call me at XXX-XXX-XXXX", scrubber.scrub("Call me at 123-456-7890"));
    }

    /** Phone number format. */
    @Test
    void testScrub_PhoneNumber_DigitsReplacedWithX() {
        assertEquals("+XX XXX XXX XXXX", scrubber.scrub("+20 123 456 7890"));
    }

    /** String starting with digits. */
    @Test
    void testScrub_StringStartingWithDigit_DigitsReplaced() {
        assertEquals("X item on shelf X", scrubber.scrub("1 item on shelf 5"));
    }

    /** String ending with digits. */
    @Test
    void testScrub_StringEndingWithDigit_DigitsReplaced() {
        assertEquals("Room X", scrubber.scrub("Room 9"));
    }
}