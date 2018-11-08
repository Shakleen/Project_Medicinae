package Logic;

/**
 * User defined exception class.
 * Required to handle different types of exception.
 *
 * Types of exceptions are:
 * 1. NOT_VARCHAR                       - The Column is not a varchar.
 * 2. VARCHAR_SIZE_ZERO                 - The Size of the varchar is 0.
 * 3. EMPTY_DOMAIN                      - The domain for the column has zero elements in it.
 * 4. NOT_NUMBER                        - The column isn't a number type.
 * 5. SINGLE_VALUE_BOUND                - The upper and lower bounds are equal.
 * 6. ZERO_COLUMNS_FOR_RECORD           - Zero columns have been defined for the record that is to be inserted or updated.
 * 7. SQL_ACCOUNT_ERROR                 - SQL account not found. Log in required.
 * 8. NULL_COLUMN_ARRAY                 - When the column array is null.
 * 9. NULL_DOMAIN                       - When the domain array is null.
 */
public class Defined_Exceptions extends Exception {
    public String ExceptionName;

    public Defined_Exceptions(String exceptionName) {
        ExceptionName = exceptionName;
    }
}
