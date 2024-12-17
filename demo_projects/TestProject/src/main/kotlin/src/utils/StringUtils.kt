package utils

/**
 * Utility class for common string manipulation operations.
 */
object StringUtils {

    /**
     * Reverses the given string.
     * @param input the string to reverse
     * @return the reversed string
     */
    fun reverse(input: String): String {
        return input.reversed()
    }

    /**
     * Converts the given string to uppercase.
     * @param input the string to convert
     * @return the uppercase string
     */
    fun toUpperCase(input: String): String {
        return input.uppercase()
    }

    /**
     * Converts the given string to lowercase.
     * @param input the string to convert
     * @return the lowercase string
     */
    fun toLowerCase(input: String): String {
        return input.lowercase()
    }

    /**
     * Checks if the given string is a palindrome.
     * @param input the string to check
     * @return true if the string is a palindrome, false otherwise
     */
    fun isPalindrome(input: String): Boolean {
        val cleanedInput = input.filter { it.isLetterOrDigit() }.lowercase()
        return cleanedInput == cleanedInput.reversed()
    }
}