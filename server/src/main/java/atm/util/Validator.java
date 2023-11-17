package atm.util;

public class Validator {
    public static boolean isValidSyntax(String request) {
        String[] values = request.split(",");
        if (values.length != 3)
            return false;

        // Check if client sent numbers
        return (values[0].matches("[0-2]") &&
                values[1].matches("[0-9]+") &&
                values[2].matches("([0-9]+\\.)?[0-9]+"));
    }
}
