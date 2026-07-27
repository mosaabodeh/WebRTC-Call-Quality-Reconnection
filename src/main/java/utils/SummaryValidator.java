package utils;

public class SummaryValidator {

    private static final String[] EXPECTED_IDEAS = {
            "team status meeting",
            "software automation progress",
            "functional tests completed",
            "mobile application",
            "live recording",
            "speech transcription",
            "multi-device calls",
            "speech to text",
            "review the test summary report",
            "feedback before the end of the day",
            "conference call",
            "project chat"
    };


    public static boolean isSummaryValid(String summary) {

        if (summary == null || summary.isBlank()) {
            return false;
        }

        summary = normalize(summary);

        int matchedIdeas = 0;

        System.out.println("\n===== Summary Validation =====");

        for (String idea : EXPECTED_IDEAS) {

            String normalizedIdea = normalize(idea);

            if (summary.contains(normalizedIdea)) {
                matchedIdeas++;
                System.out.println("✓ Found: " + idea);
            } else {
                System.out.println("✗ Missing: " + idea);
            }
        }

        System.out.println("--------------------------------");
        System.out.println("Matched Ideas: " + matchedIdeas + "/" + EXPECTED_IDEAS.length);

        // Require at least 75% of the ideas.
        int required = (int) Math.ceil(EXPECTED_IDEAS.length * 0.75);

        System.out.println("Required: " + required);

        return matchedIdeas >= required;
    }


    private static String normalize(String text) {

        return text.toLowerCase()
                .replace("\r", " ")
                .replace("\n", " ")
                .replace("-", " ")
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
