package Services;

import Interfaces.*;
import Models.ScrubMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MainScrubberTest {

    @Mock
    private IScrubDigits mockDigitScrubber;

    @Mock
    private IScrubEmails mockEmailScrubber;

    private IScrub mainScrubber;

    @BeforeEach
    void setUp() {
        mainScrubber = new MainScrubber(mockDigitScrubber, mockEmailScrubber);
    }

    // ========== PART 1: POSITIVE TESTS (HAPPY PATH) ==========

    @Test
    @DisplayName("POSITIVE: Should scrub only digits when mode is ONLY_DIGITS")
    void testPositive_OnlyDigitsMode_ReturnsScrubbedDigits() {
        // Arrange
        String input = "John 123-456-7890";
        String expected = "John XXX-XXX-XXXX";
        when(mockDigitScrubber.scrub(input)).thenReturn(expected);

        // Act
        String result = mainScrubber.scrub(input, ScrubMode.ONLY_DIGITS);

        // Assert
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("POSITIVE: Should scrub only emails when mode is ONLY_EMAILS")
    void testPositive_OnlyEmailsMode_ReturnsScrubbedEmails() {
        // Arrange
        String input = "Contact john@example.com";
        String expected = "Contact [EMAIL_HIDDEN]";
        when(mockEmailScrubber.scrub(input)).thenReturn(expected);

        // Act
        String result = mainScrubber.scrub(input, ScrubMode.ONLY_EMAILS);

        // Assert
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("POSITIVE: Should scrub both digits and emails when mode is FULL_SCRUBBING")
    void testPositive_FullScrubbingMode_ReturnsFullyScrubbed() {
        // Arrange
        String input = "John 123 and email john@example.com";
        String afterDigits = "John XXX and email john@example.com";
        String expected = "John XXX and email [EMAIL_HIDDEN]";

        when(mockDigitScrubber.scrub(input)).thenReturn(afterDigits);
        when(mockEmailScrubber.scrub(afterDigits)).thenReturn(expected);

        // Act
        String result = mainScrubber.scrub(input, ScrubMode.FULL_SCRUBBING);

        // Assert
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("POSITIVE: Should handle input with no scrubbing needed")
    void testPositive_NoScrubbingNeeded_ReturnsSameString() {
        // Arrange
        String input = "Hello World";
        String expected = "Hello World";
        when(mockDigitScrubber.scrub(input)).thenReturn(expected);

        // Act
        String result = mainScrubber.scrub(input, ScrubMode.ONLY_DIGITS);

        // Assert
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("POSITIVE: Should handle very long input string")
    void testPositive_VeryLongInput_HandlesCorrectly() {
        // Arrange
        StringBuilder input = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            input.append("test");
        }
        String expected = "scrubbed";
        when(mockDigitScrubber.scrub(anyString())).thenReturn(expected);

        // Act
        String result = mainScrubber.scrub(input.toString(), ScrubMode.ONLY_DIGITS);

        // Assert
        assertEquals(expected, result);
    }

    // ========== PART 2: NEGATIVE TESTS (BAD PATH) ==========

    @Test
    @DisplayName("NEGATIVE: Should propagate NullPointerException when input is null")
    void testNegative_NullInput_ThrowsNullPointerException() {
        // Note: This test will FAIL because original MainScrubber catches exceptions
        // But it's included to document the bug
        assertThrows(NullPointerException.class,
                () -> mainScrubber.scrub(null, ScrubMode.ONLY_DIGITS));
    }

    @Test
    @DisplayName("NEGATIVE: Should propagate IllegalArgumentException when input is empty")
    void testNegative_EmptyInput_ThrowsIllegalArgumentException() {
        // Note: This test will FAIL because original MainScrubber catches exceptions
        assertThrows(IllegalArgumentException.class,
                () -> mainScrubber.scrub("", ScrubMode.ONLY_EMAILS));
    }

    @Test
    @DisplayName("NEGATIVE: Should propagate IllegalArgumentException when input is blank")
    void testNegative_BlankInput_ThrowsIllegalArgumentException() {
        // Note: This test will FAIL because original MainScrubber catches exceptions
        assertThrows(IllegalArgumentException.class,
                () -> mainScrubber.scrub("   ", ScrubMode.FULL_SCRUBBING));
    }

    @Test
    @DisplayName("NEGATIVE: Should handle exception from digit scrubber (BUG - returns null)")
    void testNegative_WhenDigitScrubberThrows_ReturnsNull() {
        // Arrange
        String input = "test";
        when(mockDigitScrubber.scrub(input)).thenThrow(new IllegalArgumentException("Invalid input"));

        // Act
        String result = mainScrubber.scrub(input, ScrubMode.ONLY_DIGITS);

        // Assert - Demonstrates the bug (should throw, but returns null)
        assertNull(result, "BUG: Should throw exception but returns null");
    }

    @Test
    @DisplayName("NEGATIVE: Should handle exception from email scrubber (BUG - returns null)")
    void testNegative_WhenEmailScrubberThrows_ReturnsNull() {
        // Arrange
        String input = "test@email.com";
        when(mockEmailScrubber.scrub(input)).thenThrow(new NullPointerException("Invalid input"));

        // Act
        String result = mainScrubber.scrub(input, ScrubMode.ONLY_EMAILS);

        // Assert - Demonstrates the bug (should throw, but returns null)
        assertNull(result, "BUG: Should throw exception but returns null");
    }

    // ========== PART 3: BEHAVIORAL VERIFICATION TESTS ==========

    @Test
    @DisplayName("BEHAVIOR: ONLY_DIGITS mode should call digit scrubber exactly once")
    void testBehavior_OnlyDigitsMode_CallsDigitScrubberOnce() {
        // Arrange
        String input = "Phone: 123-456-7890";
        when(mockDigitScrubber.scrub(input)).thenReturn("Phone: XXX-XXX-XXXX");

        // Act
        mainScrubber.scrub(input, ScrubMode.ONLY_DIGITS);

        // Assert - Verify exactly one call
        verify(mockDigitScrubber, times(1)).scrub(input);
        verify(mockEmailScrubber, never()).scrub(anyString());
    }

    @Test
    @DisplayName("BEHAVIOR: ONLY_EMAILS mode should call email scrubber exactly once")
    void testBehavior_OnlyEmailsMode_CallsEmailScrubberOnce() {
        // Arrange
        String input = "Email: john@example.com";
        when(mockEmailScrubber.scrub(input)).thenReturn("Email: [EMAIL_HIDDEN]");

        // Act
        mainScrubber.scrub(input, ScrubMode.ONLY_EMAILS);

        // Assert - Verify exactly one call
        verify(mockEmailScrubber, times(1)).scrub(input);
        verify(mockDigitScrubber, never()).scrub(anyString());
    }

    @Test
    @DisplayName("BEHAVIOR: FULL_SCRUBBING mode should call both scrubbers exactly once")
    void testBehavior_FullScrubbingMode_CallsBothScrubbersOnce() {
        // Arrange
        String input = "test123@email.com";
        String afterDigits = "testXXX@email.com";
        when(mockDigitScrubber.scrub(input)).thenReturn(afterDigits);
        when(mockEmailScrubber.scrub(afterDigits)).thenReturn("[EMAIL_HIDDEN]");

        // Act
        mainScrubber.scrub(input, ScrubMode.FULL_SCRUBBING);

        // Assert - Verify each was called exactly once
        verify(mockDigitScrubber, times(1)).scrub(input);
        verify(mockEmailScrubber, times(1)).scrub(afterDigits);
    }

    @Test
    @DisplayName("BEHAVIOR: Verify correct call order in FULL_SCRUBBING mode")
    void testBehavior_VerifiesCallOrder() {
        // Arrange
        String input = "test123@email.com";
        String afterDigits = "testXXX@email.com";
        when(mockDigitScrubber.scrub(input)).thenReturn(afterDigits);
        when(mockEmailScrubber.scrub(afterDigits)).thenReturn("[EMAIL_HIDDEN]");

        // Act
        mainScrubber.scrub(input, ScrubMode.FULL_SCRUBBING);

        // Assert - Verify digit scrubber called before email scrubber
        InOrder inOrder = inOrder(mockDigitScrubber, mockEmailScrubber);
        inOrder.verify(mockDigitScrubber).scrub(input);
        inOrder.verify(mockEmailScrubber).scrub(afterDigits);
    }

    @Test
    @DisplayName("BEHAVIOR: Different inputs should be passed correctly to dependencies")
    void testBehavior_DifferentInputs_PassedCorrectly() {
        // Arrange
        String input1 = "user1 123";
        String input2 = "user2 456";
        when(mockDigitScrubber.scrub(input1)).thenReturn("user1 XXX");
        when(mockDigitScrubber.scrub(input2)).thenReturn("user2 XXX");

        // Act
        mainScrubber.scrub(input1, ScrubMode.ONLY_DIGITS);
        mainScrubber.scrub(input2, ScrubMode.ONLY_DIGITS);

        // Assert - Verify each input was passed correctly
        verify(mockDigitScrubber, times(1)).scrub(input1);
        verify(mockDigitScrubber, times(1)).scrub(input2);
    }

    @Test
    @DisplayName("BEHAVIOR: Multiple calls should increase call count")
    void testBehavior_MultipleCalls_IncreasesCallCount() {
        // Arrange
        String input = "test123";
        when(mockDigitScrubber.scrub(input)).thenReturn("testXXX");

        // Act - Call 3 times
        mainScrubber.scrub(input, ScrubMode.ONLY_DIGITS);
        mainScrubber.scrub(input, ScrubMode.ONLY_DIGITS);
        mainScrubber.scrub(input, ScrubMode.ONLY_DIGITS);

        // Assert - Called exactly 3 times
        verify(mockDigitScrubber, times(3)).scrub(input);
    }

    @Test
    @DisplayName("BEHAVIOR: Different modes should invoke different dependencies")
    void testBehavior_DifferentModes_InvokesCorrectDependencies() {
        // Arrange
        String input = "test123@email.com";
        when(mockDigitScrubber.scrub(anyString())).thenReturn("scrubbed");
        when(mockEmailScrubber.scrub(anyString())).thenReturn("scrubbed");

        // Act
        mainScrubber.scrub(input, ScrubMode.ONLY_DIGITS);
        mainScrubber.scrub(input, ScrubMode.ONLY_EMAILS);

        // Assert
        verify(mockDigitScrubber, times(1)).scrub(anyString());
        verify(mockEmailScrubber, times(1)).scrub(anyString());
    }

    @Test
    @DisplayName("BEHAVIOR: Verify that FULL_SCRUBBING passes correct intermediate result")
    void testBehavior_PassesCorrectIntermediateResult() {
        // Arrange
        String input = "user123@email.com";
        String afterDigits = "userXXX@email.com";
        when(mockDigitScrubber.scrub(input)).thenReturn(afterDigits);
        when(mockEmailScrubber.scrub(afterDigits)).thenReturn("[EMAIL_HIDDEN]");

        // Act
        mainScrubber.scrub(input, ScrubMode.FULL_SCRUBBING);

        // Assert - Verify email scrubber received the digit-scrubbed result
        verify(mockEmailScrubber).scrub(afterDigits);
        verify(mockEmailScrubber, never()).scrub(input);
    }

    @Test
    @DisplayName("BEHAVIOR: ONLY_DIGITS mode should never call email scrubber")
    void testBehavior_OnlyDigitsMode_NeverCallsEmailScrubber() {
        // Arrange
        String[] inputs = {"test", "123", "test@email.com"};
        when(mockDigitScrubber.scrub(anyString())).thenReturn("result");

        // Act
        for (String input : inputs) {
            mainScrubber.scrub(input, ScrubMode.ONLY_DIGITS);
        }

        // Assert
        verify(mockEmailScrubber, never()).scrub(anyString());
        verify(mockDigitScrubber, times(inputs.length)).scrub(anyString());
    }

    @Test
    @DisplayName("BEHAVIOR: ONLY_EMAILS mode should never call digit scrubber")
    void testBehavior_OnlyEmailsMode_NeverCallsDigitScrubber() {
        // Arrange
        String[] inputs = {"test", "123", "test@email.com"};
        when(mockEmailScrubber.scrub(anyString())).thenReturn("result");

        // Act
        for (String input : inputs) {
            mainScrubber.scrub(input, ScrubMode.ONLY_EMAILS);
        }

        // Assert
        verify(mockDigitScrubber, never()).scrub(anyString());
        verify(mockEmailScrubber, times(inputs.length)).scrub(anyString());
    }
}
