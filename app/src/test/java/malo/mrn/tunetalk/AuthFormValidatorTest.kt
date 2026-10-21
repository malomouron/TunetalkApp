package malo.mrn.tunetalk

import malo.mrn.tunetalk.domain.validation.AuthFormValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthFormValidatorTest {
    @Test
    fun registrationRequiresValidProfileAndPassword() {
        assertNull(
            AuthFormValidator.validate(
                displayName = "Malo",
                email = "malo@example.com",
                password = "TuneTalk2026",
                isRegistration = true
            )
        )
        assertEquals(
            "Le mot de passe doit contenir au moins 10 caractères et un chiffre.",
            AuthFormValidator.validate(
                displayName = "Malo",
                email = "malo@example.com",
                password = "tunetalk",
                isRegistration = true
            )
        )
    }

    @Test
    fun loginAcceptsAnExistingPasswordWithoutRegistrationRules() {
        assertNull(
            AuthFormValidator.validate(
                displayName = "",
                email = "malo@example.com",
                password = "old-pass",
                isRegistration = false
            )
        )
    }
}
