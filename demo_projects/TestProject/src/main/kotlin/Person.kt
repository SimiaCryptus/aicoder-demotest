data class Person(
    val name: String,
    val age: Int,
    val email: String
    fun isNamePalindrome(): Boolean {
        return utils.StringUtils.isPalindrome(name)
    }
)