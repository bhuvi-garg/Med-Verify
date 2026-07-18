package com.medverify.android.login

/**
 * The data collected on first launch. This is a caretaker-operated setup form
 * (REQ-15), not the elderly-facing app itself, so it's exempt from REQ-11's
 * always-simplified UI constraint — see ARCH-01's decision on onboarding.
 *
 * None of these fields exist yet in the backend's data model (see
 * Design/Backend/db-schema.md's `account` table, which only has
 * id/username/created_at). This is stored locally for now; wiring it up to a
 * real backend account is future work once REQ-15/ARCH-02's auth flow is
 * implemented server-side.
 */
data class PatientProfile(
    val fullName: String,
    val phoneNumber: String,
    val age: Int,
    val gender: Gender,
    val heightCm: Double?,
    val weightKg: Double,
    val emergencyContactNumber: String,
    val pharmacyContactNumber: String?,
)

enum class Gender(val label: String) {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other"),
}
