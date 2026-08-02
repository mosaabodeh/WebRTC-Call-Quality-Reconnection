package utils;

import org.openqa.selenium.WebElement;

public class ParticipantParserUtils {
    public static String extractParticipantName(String contentDesc) {

        int firstCommaIndex = contentDesc.indexOf(",");

        if (firstCommaIndex == -1) {
            return contentDesc.trim();
        }

        return contentDesc.substring(0, firstCommaIndex).trim();
    }

    public static String determineParticipantRole(
            WebElement participantButton,
            String contentDesc) {

        if (containsExactRole(contentDesc, "Owner")) {
            return "Owner";
        }

        if (containsExactRole(contentDesc, "Organizer")) {
            return "Organizer";
        }

        // A member has no Owner or Organizer role icon
        return "Member";
    }
    private static boolean containsExactRole(String contentDesc, String role) {
        String[] parts = contentDesc.split(",");
        for (String part : parts) {
            if (part.trim().equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }
}
