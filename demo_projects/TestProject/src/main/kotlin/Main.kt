package org.example
import utils.StringUtils

fun main() {
    println("Hello World!")
    // Example usage of StringUtils
    val originalString = "Hello World"
    println("Reversed: ${StringUtils.reverse(originalString)}")
    println("Uppercase: ${StringUtils.toUpperCase(originalString)}")
    println("Lowercase: ${StringUtils.toLowerCase(originalString)}")
    println("Is Palindrome: ${StringUtils.isPalindrome(originalString)}")
}