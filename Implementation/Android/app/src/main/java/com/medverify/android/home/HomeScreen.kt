package com.medverify.android.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.medverify.android.login.LoginRepository
import com.medverify.android.login.PatientProfile

/**
 * Placeholder shown once onboarding is complete. Loads the saved profile as
 * proof the login form actually persisted it — real scan/dosage features are
 * not built yet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(repository: LoginRepository, modifier: Modifier = Modifier) {
    var profile by remember { mutableStateOf<PatientProfile?>(null) }

    LaunchedEffect(Unit) {
        profile = repository.getProfileOnce()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Med-Verify") }) },
        modifier = modifier,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = profile?.let { "Welcome, ${it.fullName}!" } ?: "Welcome!",
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Setup is complete. Scanning and dosage features are not built yet — " +
                    "this is a placeholder screen.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}
