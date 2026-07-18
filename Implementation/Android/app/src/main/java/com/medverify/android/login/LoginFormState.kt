package com.medverify.android.login

enum class LoginField {
    FULL_NAME, PHONE_NUMBER, AGE, GENDER, HEIGHT, WEIGHT,
    EMERGENCY_CONTACT_NUMBER, PHARMACY_CONTACT_NUMBER,
}

/** Raw text as typed, before validation/parsing — kept as strings so an
 * invalid partial entry (e.g. "1.2.3") can be shown back to the user with an
 * error instead of silently rejected keystroke by keystroke. */
data class LoginFormState(
    val fullName: String = "",
    val phoneNumber: String = "",
    val age: String = "",
    val gender: Gender? = null,
    val heightCm: String = "",
    val weightKg: String = "",
    val emergencyContactNumber: String = "",
    val pharmacyContactNumber: String = "",
    val errors: Map<LoginField, String> = emptyMap(),
    val isSaving: Boolean = false,
)

private val PHONE_REGEX = Regex("^[0-9]{7,15}$")

/**
 * Validates the form and, if there are no errors, returns the parsed
 * [PatientProfile]. Returns the error map (possibly empty on success) so the
 * caller can distinguish "valid" from "invalid" without a second parse pass.
 */
fun validateLoginForm(state: LoginFormState): Pair<Map<LoginField, String>, PatientProfile?> {
    val errors = mutableMapOf<LoginField, String>()

    if (state.fullName.isBlank()) {
        errors[LoginField.FULL_NAME] = "Full name is required"
    }

    if (!PHONE_REGEX.matches(state.phoneNumber.trim())) {
        errors[LoginField.PHONE_NUMBER] = "Enter a valid phone number"
    }

    val age = state.age.trim().toIntOrNull()
    if (age == null || age !in 1..120) {
        errors[LoginField.AGE] = "Enter a valid age"
    }

    if (state.gender == null) {
        errors[LoginField.GENDER] = "Select a gender"
    }

    val heightCm: Double? = if (state.heightCm.isBlank()) {
        null
    } else {
        val parsed = state.heightCm.trim().toDoubleOrNull()
        if (parsed == null || parsed !in 30.0..250.0) {
            errors[LoginField.HEIGHT] = "Enter a valid height in cm"
        }
        parsed
    }

    val weightKg = state.weightKg.trim().toDoubleOrNull()
    if (weightKg == null || weightKg !in 1.0..300.0) {
        errors[LoginField.WEIGHT] = "Enter a valid weight in kg"
    }

    if (!PHONE_REGEX.matches(state.emergencyContactNumber.trim())) {
        errors[LoginField.EMERGENCY_CONTACT_NUMBER] = "Enter a valid phone number"
    }

    val pharmacyContactNumber: String? = state.pharmacyContactNumber.trim().ifBlank { null }
    if (pharmacyContactNumber != null && !PHONE_REGEX.matches(pharmacyContactNumber)) {
        errors[LoginField.PHARMACY_CONTACT_NUMBER] = "Enter a valid phone number"
    }

    if (errors.isNotEmpty()) {
        return errors to null
    }

    val profile = PatientProfile(
        fullName = state.fullName.trim(),
        phoneNumber = state.phoneNumber.trim(),
        age = age!!,
        gender = state.gender!!,
        heightCm = heightCm,
        weightKg = weightKg!!,
        emergencyContactNumber = state.emergencyContactNumber.trim(),
        pharmacyContactNumber = pharmacyContactNumber,
    )
    return errors to profile
}
