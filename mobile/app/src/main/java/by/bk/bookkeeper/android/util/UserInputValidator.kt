package by.bk.bookkeeper.android.util

/**
 *  Created by Evgenia Grinkevich on 19, May, 2023
 **/

object UserInputValidator {

    private val PACKAGE_NAME_PATTERN = "^([A-Za-z]{1}[A-Za-z\\d_]*\\.)+[A-Za-z][A-Za-z\\d_]*$".toRegex()

    fun isValidPackageName(input: String?): Boolean {
        if (input.isNullOrEmpty()) return false
        return PACKAGE_NAME_PATTERN.matches(input)
    }

}