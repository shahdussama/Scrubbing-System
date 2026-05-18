import Services.DigitScrubber;
import Services.EmailScrubber;
import Services.MainScrubber;
import Interfaces.IScrub;
import Models.ScrubMode;

public class App {
    public static void main(String[] args) {
        DigitScrubber digitScrubber = new DigitScrubber();
        EmailScrubber emailScrubber = new EmailScrubber();
        IScrub scrubber = new MainScrubber(digitScrubber, emailScrubber);

        String testData = "Contact me at user123@domain456.com or call 123-456-7890";

        System.out.println("Original: " + testData);
        System.out.println("\n=== Different Scrubbing Modes ===\n");

        System.out.println("ONLY_DIGITS:  " + scrubber.scrub(testData, ScrubMode.ONLY_DIGITS));
        System.out.println("ONLY_EMAILS:  " + scrubber.scrub(testData, ScrubMode.ONLY_EMAILS));
        System.out.println("FULL_SCRUBBING: " + scrubber.scrub(testData, ScrubMode.FULL_SCRUBBING));

        System.out.println("\n=== Another Example ===\n");

        String phoneNumber = "My number is +1 (555) 123-4567";
        System.out.println("Original: " + phoneNumber);
        System.out.println("ONLY_DIGITS:  " + scrubber.scrub(phoneNumber, ScrubMode.ONLY_DIGITS));

        String emailOnly = "Send to user@example.com";
        System.out.println("\nOriginal: " + emailOnly);
        System.out.println("ONLY_EMAILS:  " + scrubber.scrub(emailOnly, ScrubMode.ONLY_EMAILS));

        System.out.println("\n=== Exception Handling ===\n");

        // Test null input
        try {
            System.out.println("Testing null input...");
            scrubber.scrub(null, ScrubMode.FULL_SCRUBBING);
            System.out.println("This line should not print if exception is thrown");
        } catch (NullPointerException e) {
            System.out.println("✓ Caught NullPointerException: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Caught other exception: " + e.getClass().getSimpleName());
        }

        // Test blank input
        try {
            System.out.println("\nTesting blank input...");
            scrubber.scrub("   ", ScrubMode.FULL_SCRUBBING);
            System.out.println("This line should not print if exception is thrown");
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Caught IllegalArgumentException: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Caught other exception: " + e.getClass().getSimpleName());
        }

        // Test that the exception is actually thrown (not returning null)
        System.out.println("\n=== Verifying Exception Propagation ===\n");

        String result = scrubber.scrub(null, ScrubMode.ONLY_DIGITS);
        System.out.println("Result: " + result);
        System.out.println("If you see this, the method returned null instead of throwing exception");
    }
}
