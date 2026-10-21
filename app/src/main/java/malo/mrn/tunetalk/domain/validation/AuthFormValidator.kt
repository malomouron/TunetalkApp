package malo.mrn.tunetalk.domain.validation

object AuthFormValidator {
    private val emailPattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    fun validate(
        displayName: String,
        email: String,
        password: String,
        isRegistration: Boolean
    ): String? {
        if (isRegistration && displayName.trim().length !in 2..40) {
            return "Le pseudo doit contenir entre 2 et 40 caractères."
        }

        if (email.length > 254 || !emailPattern.matches(email.trim())) {
            return "Saisissez une adresse e-mail valide."
        }

        if (password.isBlank()) {
            return "Saisissez votre mot de passe."
        }

        if (isRegistration && (password.length < 10 || password.none(Char::isDigit))) {
            return "Le mot de passe doit contenir au moins 10 caractères et un chiffre."
        }

        return null
    }
}
