package com.example.core.utils

import android.util.Patterns

class UtilsVerificationAuthInfo {
    companion object {
        private const val LENGTH_OF_PASSWORD: Int = 6

        fun nameIsNotEmpty(name: String): Boolean = !name.isNullOrBlank()

        fun emailIsValid(email: String): Boolean {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        fun passwordsLengthNotLessSixSymbols(password: String): Boolean =
            (password.length >= LENGTH_OF_PASSWORD)
    }
}