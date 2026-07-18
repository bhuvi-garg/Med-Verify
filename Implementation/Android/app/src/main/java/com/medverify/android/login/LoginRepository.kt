package com.medverify.android.login

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Declared at file scope (not inside the class) so there's exactly one
// DataStore<Preferences> instance for this file name for the whole app —
// DataStore throws if you accidentally create two for the same file.
private val Context.patientProfileDataStore by preferencesDataStore(name = "patient_profile")

/**
 * Local-only persistence for the one-time onboarding form. There is no
 * backend account-creation call yet (see PatientProfile's doc comment) —
 * this is exactly the "UI-only, mocked data first" slice.
 */
class LoginRepository(private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val FULL_NAME = stringPreferencesKey("full_name")
        val PHONE_NUMBER = stringPreferencesKey("phone_number")
        val AGE = intPreferencesKey("age")
        val GENDER = stringPreferencesKey("gender")
        val HEIGHT_CM = doublePreferencesKey("height_cm")
        val WEIGHT_KG = doublePreferencesKey("weight_kg")
        val EMERGENCY_CONTACT_NUMBER = stringPreferencesKey("emergency_contact_number")
        val PHARMACY_CONTACT_NUMBER = stringPreferencesKey("pharmacy_contact_number")
    }

    /** True once [saveProfile] has been called successfully, false until then. */
    val isOnboardingComplete: Flow<Boolean> = context.patientProfileDataStore.data
        .map { prefs -> prefs[Keys.ONBOARDING_COMPLETE] ?: false }

    suspend fun saveProfile(profile: PatientProfile) {
        context.patientProfileDataStore.edit { prefs ->
            prefs[Keys.FULL_NAME] = profile.fullName
            prefs[Keys.PHONE_NUMBER] = profile.phoneNumber
            prefs[Keys.AGE] = profile.age
            prefs[Keys.GENDER] = profile.gender.name
            if (profile.heightCm != null) {
                prefs[Keys.HEIGHT_CM] = profile.heightCm
            } else {
                prefs.remove(Keys.HEIGHT_CM)
            }
            prefs[Keys.WEIGHT_KG] = profile.weightKg
            prefs[Keys.EMERGENCY_CONTACT_NUMBER] = profile.emergencyContactNumber
            if (profile.pharmacyContactNumber != null) {
                prefs[Keys.PHARMACY_CONTACT_NUMBER] = profile.pharmacyContactNumber
            } else {
                prefs.remove(Keys.PHARMACY_CONTACT_NUMBER)
            }
            // Set last, so a crash partway through never leaves this true
            // with incomplete data written alongside it.
            prefs[Keys.ONBOARDING_COMPLETE] = true
        }
    }

    /** For the placeholder home screen to show something real was saved. */
    suspend fun getProfileOnce(): PatientProfile? {
        val prefs = context.patientProfileDataStore.data.first()
        val fullName = prefs[Keys.FULL_NAME] ?: return null
        val phoneNumber = prefs[Keys.PHONE_NUMBER] ?: return null
        val age = prefs[Keys.AGE] ?: return null
        val gender = prefs[Keys.GENDER]?.let { Gender.valueOf(it) } ?: return null
        val weightKg = prefs[Keys.WEIGHT_KG] ?: return null
        val emergencyContactNumber = prefs[Keys.EMERGENCY_CONTACT_NUMBER] ?: return null
        return PatientProfile(
            fullName = fullName,
            phoneNumber = phoneNumber,
            age = age,
            gender = gender,
            heightCm = prefs[Keys.HEIGHT_CM],
            weightKg = weightKg,
            emergencyContactNumber = emergencyContactNumber,
            pharmacyContactNumber = prefs[Keys.PHARMACY_CONTACT_NUMBER],
        )
    }
}
