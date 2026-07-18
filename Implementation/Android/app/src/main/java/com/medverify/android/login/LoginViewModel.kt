package com.medverify.android.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: LoginRepository) : ViewModel() {

    var uiState by mutableStateOf(LoginFormState())
        private set

    fun onFullNameChange(value: String) {
        uiState = uiState.copy(fullName = value, errors = uiState.errors - LoginField.FULL_NAME)
    }

    fun onPhoneNumberChange(value: String) {
        uiState = uiState.copy(phoneNumber = value, errors = uiState.errors - LoginField.PHONE_NUMBER)
    }

    fun onAgeChange(value: String) {
        uiState = uiState.copy(age = value, errors = uiState.errors - LoginField.AGE)
    }

    fun onGenderChange(value: Gender) {
        uiState = uiState.copy(gender = value, errors = uiState.errors - LoginField.GENDER)
    }

    fun onHeightChange(value: String) {
        uiState = uiState.copy(heightCm = value, errors = uiState.errors - LoginField.HEIGHT)
    }

    fun onWeightChange(value: String) {
        uiState = uiState.copy(weightKg = value, errors = uiState.errors - LoginField.WEIGHT)
    }

    fun onEmergencyContactChange(value: String) {
        uiState = uiState.copy(
            emergencyContactNumber = value,
            errors = uiState.errors - LoginField.EMERGENCY_CONTACT_NUMBER,
        )
    }

    fun onPharmacyContactChange(value: String) {
        uiState = uiState.copy(
            pharmacyContactNumber = value,
            errors = uiState.errors - LoginField.PHARMACY_CONTACT_NUMBER,
        )
    }

    /** Validates, and if valid, persists the profile and calls [onSaved]. If
     * invalid, updates [uiState] with field errors and returns without
     * calling [onSaved] — the screen re-renders showing them. */
    fun submit(onSaved: () -> Unit) {
        val (errors, profile) = validateLoginForm(uiState)
        if (profile == null) {
            uiState = uiState.copy(errors = errors)
            return
        }
        uiState = uiState.copy(isSaving = true)
        viewModelScope.launch {
            repository.saveProfile(profile)
            uiState = uiState.copy(isSaving = false)
            onSaved()
        }
    }

    companion object {
        fun factory(repository: LoginRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LoginViewModel(repository) as T
                }
            }
    }
}
