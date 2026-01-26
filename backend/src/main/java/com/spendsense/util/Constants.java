package com.spendsense.util;

public final class Constants {

    // HTTP Headers
    public static final String USER_ID_HEADER = "X-User-Id";

    // Validation Messages
    public static final String VALIDATION_AMOUNT_POSITIVE = "Amount must be greater than 0";
    public static final String VALIDATION_AMOUNT_NON_NEGATIVE = "Amount cannot be negative";
    public static final String VALIDATION_NAME_REQUIRED = "Name is required";
    public static final String VALIDATION_TYPE_REQUIRED = "Type is required";

    // Error Messages
    public static final String ERROR_USER_NOT_FOUND = "User not found";
    public static final String ERROR_ACCOUNT_NOT_FOUND = "Account not found";
    public static final String ERROR_TRANSACTION_NOT_FOUND = "Transaction not found";
    public static final String ERROR_BUDGET_NOT_FOUND = "Budget not found";
    public static final String ERROR_RECURRING_MISSING_DATA = "Recurring transaction requires date and recurringInterval";

    private Constants() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
