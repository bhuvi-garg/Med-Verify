package com.medverify.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medverify.android.home.HomeScreen
import com.medverify.android.login.LoginRepository
import com.medverify.android.login.LoginScreen
import com.medverify.android.login.LoginViewModel
import com.medverify.android.ui.theme.MedVerifyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = LoginRepository(applicationContext)

        setContent {
            MedVerifyTheme {
                MedVerifyApp(repository = repository)
            }
        }
    }
}

/** Gates the first-launch login form behind [LoginRepository.isOnboardingComplete]. */
@Composable
private fun MedVerifyApp(repository: LoginRepository) {
    val onboardingComplete by repository.isOnboardingComplete.collectAsState(initial = null)

    when (onboardingComplete) {
        null -> LoadingScreen()
        false -> {
            val viewModel: LoginViewModel = viewModel(factory = LoginViewModel.factory(repository))
            LoginScreen(
                viewModel = viewModel,
                // No manual navigation needed: saving flips the DataStore
                // flag, isOnboardingComplete emits true, and this `when`
                // recomposes into the true branch on its own.
                onOnboardingComplete = {},
            )
        }
        true -> HomeScreen(repository = repository)
    }
}

@Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
