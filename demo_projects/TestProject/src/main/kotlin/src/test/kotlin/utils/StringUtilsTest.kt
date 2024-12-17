package utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class StringUtilsTest {

    @Test
    fun testReverse() {
        assertEquals("dcba", StringUtils.reverse("abcd"))
        assertEquals("54321", StringUtils.reverse("12345"))
        assertEquals("", StringUtils.reverse(""))
        assertEquals("a", StringUtils.reverse("a"))
    }

    @Test
    fun testToUpperCase() {
        assertEquals("HELLO", StringUtils.toUpperCase("hello"))
        assertEquals("WORLD", StringUtils.toUpperCase("WORLD"))
        assertEquals("", StringUtils.toUpperCase(""))
        assertEquals("123", StringUtils.toUpperCase("123"))
    }

    @Test
    fun testToLowerCase() {
        assertEquals("hello", StringUtils.toLowerCase("HELLO"))
        assertEquals("world", StringUtils.toLowerCase("world"))
        assertEquals("", StringUtils.toLowerCase(""))
        assertEquals("123", StringUtils.toLowerCase("123"))
    }

    @Test
    fun testIsPalindrome() {
        assertTrue(StringUtils.isPalindrome("A man a plan a canal Panama"))
        assertTrue(StringUtils.isPalindrome("racecar"))
        assertFalse(StringUtils.isPalindrome("hello"))
        assertTrue(StringUtils.isPalindrome("12321"))
        assertFalse(StringUtils.isPalindrome("12345"))
        assertTrue(StringUtils.isPalindrome(""))
        assertTrue(StringUtils.isPalindrome("a"))
    }
}