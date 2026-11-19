package com.example.ordermanagement.util;

import java.util.Scanner;
import java.util.Set;

/** Reads console input safely so that bad input cannot crash the menu. */
public final class InputValidator {

    private static final Set<String> VALID_STATUSES = Set.of("Pending", "Shipped", "Delivered");

    private InputValidator() {
    }

    /** Reads a whole line and retries until it is a valid integer. */
    public static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    /** Rejects zero and negative values, used for the extra quantity. */
    public static int readPositiveInt(Scanner scanner, String prompt) {
        while (true) {
            int value = readInt(scanner, prompt);
            if (value > 0) {
                return value;
            }
            System.out.println("The quantity must be greater than 0.");
        }
    }

    public static String readNonEmptyString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("This field cannot be empty.");
        }
    }

    public static boolean isValidStatus(String status) {
        return VALID_STATUSES.contains(status);
    }
}
