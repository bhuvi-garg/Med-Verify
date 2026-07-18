package com.medverify.android.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * The one-time patient-details form shown on first launch (see PatientProfile.kt
 * for why this data isn't part of the backend's schema yet). Filled in by the
 * caretaker (REQ-15), not the elderly user, so this intentionally does not
 * follow REQ-11's always-simplified elderly-facing UI constraint.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onOnboardingComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = viewModel.uiState

    Scaffold(
        topBar = { TopAppBar(title = { Text("Set Up Med-Verify") }) },
        modifier = modifier,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Let's set up the patient's details. This only needs to be done once.",
                style = MaterialTheme.typography.bodyMedium,
            )

            FormTextField(
                label = "Full Name",
                value = state.fullName,
                onValueChange = viewModel::onFullNameChange,
                isError = state.errors.containsKey(LoginField.FULL_NAME),
                errorText = state.errors[LoginField.FULL_NAME],
            )

            FormTextField(
                label = "Phone Number",
                value = state.phoneNumber,
                onValueChange = viewModel::onPhoneNumberChange,
                keyboardType = KeyboardType.Phone,
                isError = state.errors.containsKey(LoginField.PHONE_NUMBER),
                errorText = state.errors[LoginField.PHONE_NUMBER],
            )

            FormTextField(
                label = "Age",
                value = state.age,
                onValueChange = viewModel::onAgeChange,
                keyboardType = KeyboardType.Number,
                isError = state.errors.containsKey(LoginField.AGE),
                errorText = state.errors[LoginField.AGE],
            )

            GenderSelector(
                selected = state.gender,
                onSelect = viewModel::onGenderChange,
                isError = state.errors.containsKey(LoginField.GENDER),
                errorText = state.errors[LoginField.GENDER],
            )

            FormTextField(
                label = "Height (cm)",
                value = state.heightCm,
                onValueChange = viewModel::onHeightChange,
                keyboardType = KeyboardType.Decimal,
                isError = state.errors.containsKey(LoginField.HEIGHT),
                errorText = state.errors[LoginField.HEIGHT],
                optional = true,
            )

            FormTextField(
                label = "Weight (kg)",
                value = state.weightKg,
                onValueChange = viewModel::onWeightChange,
                keyboardType = KeyboardType.Decimal,
                isError = state.errors.containsKey(LoginField.WEIGHT),
                errorText = state.errors[LoginField.WEIGHT],
            )

            FormTextField(
                label = "Emergency Contact Number",
                value = state.emergencyContactNumber,
                onValueChange = viewModel::onEmergencyContactChange,
                keyboardType = KeyboardType.Phone,
                isError = state.errors.containsKey(LoginField.EMERGENCY_CONTACT_NUMBER),
                errorText = state.errors[LoginField.EMERGENCY_CONTACT_NUMBER],
            )

            FormTextField(
                label = "Pharmacy Contact Number",
                value = state.pharmacyContactNumber,
                onValueChange = viewModel::onPharmacyContactChange,
                keyboardType = KeyboardType.Phone,
                isError = state.errors.containsKey(LoginField.PHARMACY_CONTACT_NUMBER),
                errorText = state.errors[LoginField.PHARMACY_CONTACT_NUMBER],
                optional = true,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.submit(onOnboardingComplete) },
                enabled = !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Continue")
                }
            }
        }
    }
}

@Composable
private fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    errorText: String? = null,
    optional: Boolean = false,
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(if (optional) "$label (optional)" else label) },
            isError = isError,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
        )
        if (isError && errorText != null) {
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenderSelector(
    selected: Gender?,
    onSelect: (Gender) -> Unit,
    isError: Boolean,
    errorText: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Gender",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            Gender.entries.forEachIndexed { index, gender ->
                SegmentedButton(
                    selected = selected == gender,
                    onClick = { onSelect(gender) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = Gender.entries.size),
                ) {
                    Text(gender.label)
                }
            }
        }
        if (isError && errorText != null) {
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
            )
        }
    }
}
