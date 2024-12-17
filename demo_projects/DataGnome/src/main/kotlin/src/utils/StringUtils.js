// StringUtils.js
// Utility class for common string manipulation operations

class StringUtils {
  /**
   * Trims whitespace from both ends of a string.
   * @param {string} str - The string to trim.
   * @returns {string} - The trimmed string.
   */
  static trim(str) {
    if (typeof str !== 'string') {
      throw new TypeError('Expected a string');
    }
    return str.trim();
  }
 /**
  * Converts the first character of a string to uppercase.
  * @param {string} str - The string to modify.
  * @returns {string} - The string with the first character in uppercase.
  */
 static capitalizeFirst(str) {
   if (typeof str !== 'string') {
     throw new TypeError('Expected a string');
   }
   return str.charAt(0).toUpperCase() + str.slice(1);
 }
 /**
  * Converts a string to kebab-case.
  * @param {string} str - The string to convert.
  * @returns {string} - The kebab-cased string.
  */
 static toKebabCase(str) {
   if (typeof str !== 'string') {
     throw new TypeError('Expected a string');
   }
   return str
     .replace(/([a-z])([A-Z])/g, '$1-$2')
     .replace(/\s+/g, '-')
     .toLowerCase();
 }
 /**
  * Converts a string to snake_case.
  * @param {string} str - The string to convert.
  * @returns {string} - The snake_cased string.
  */
 static toSnakeCase(str) {
   if (typeof str !== 'string') {
     throw new TypeError('Expected a string');
   }
   return str
     .replace(/([a-z])([A-Z])/g, '$1_$2')
     .replace(/\s+/g, '_')
     .toLowerCase();
 }
 /**
  * Repeats a string a specified number of times.
  * @param {string} str - The string to repeat.
  * @param {number} count - The number of times to repeat the string.
  * @returns {string} - The repeated string.
  */
 static repeat(str, count) {
   if (typeof str !== 'string') {
     throw new TypeError('Expected a string');
   }
   if (typeof count !== 'number' || count < 0) {
     throw new TypeError('Expected a non-negative number');
   }
   return str.repeat(count);
 }
 /**
  * Checks if a string contains a substring.
  * @param {string} str - The string to search within.
  * @param {string} substring - The substring to search for.
  * @returns {boolean} - True if the substring is found, false otherwise.
  */
 static contains(str, substring) {
   if (typeof str !== 'string' || typeof substring !== 'string') {
     throw new TypeError('Expected strings');
   }
   return str.includes(substring);
 }
 /**
  * Pads a string with another string until the resulting string reaches the given length.
  * @param {string} str - The string to pad.
  * @param {number} targetLength - The length of the resulting string once the current string has been padded.
  * @param {string} padString - The string to pad the current string with.
  * @returns {string} - The padded string.
  */
 static pad(str, targetLength, padString = ' ') {
   if (typeof str !== 'string' || typeof padString !== 'string') {
     throw new TypeError('Expected strings');
   }
   if (typeof targetLength !== 'number') {
     throw new TypeError('Expected a number for targetLength');
   }
   return str.padStart((str.length + targetLength) / 2, padString).padEnd(targetLength, padString);
 }

  /**
   * Splits a string into an array of substrings using a specified separator.
   * @param {string} str - The string to split.
   * @param {string} separator - The separator to use for splitting the string.
   * @returns {string[]} - An array of substrings.
   */
  static split(str, separator) {
    if (typeof str !== 'string') {
      throw new TypeError('Expected a string');
    }
    return str.split(separator);
  }

  /**
   * Converts a string to uppercase.
   * @param {string} str - The string to convert.
   * @returns {string} - The uppercase string.
   */
  static toUpperCase(str) {
    if (typeof str !== 'string') {
      throw new TypeError('Expected a string');
    }
    return str.toUpperCase();
  }

  /**
   * Converts a string to lowercase.
   * @param {string} str - The string to convert.
   * @returns {string} - The lowercase string.
   */
  static toLowerCase(str) {
    if (typeof str !== 'string') {
      throw new TypeError('Expected a string');
    }
    return str.toLowerCase();
  }
  /**
   * Reverses a string.
   * @param {string} str - The string to reverse.
   * @returns {string} - The reversed string.
   */
  static reverse(str) {
    if (typeof str !== 'string') {
      throw new TypeError('Expected a string');
    }
    return str.split('').reverse().join('');
  }
  /**
   * Checks if a string is a palindrome.
   * @param {string} str - The string to check.
   * @returns {boolean} - True if the string is a palindrome, false otherwise.
   */
  static isPalindrome(str) {
    if (typeof str !== 'string') {
      throw new TypeError('Expected a string');
    }
    const reversed = str.split('').reverse().join('');
    return str === reversed;
  }
  /**
   * Capitalizes the first letter of each word in a string.
   * @param {string} str - The string to capitalize.
   * @returns {string} - The capitalized string.
   */
  static capitalizeWords(str) {
    if (typeof str !== 'string') {
      throw new TypeError('Expected a string');
    }
    return str.replace(/\b\w/g, char => char.toUpperCase());
  }
}

module.exports = StringUtils;