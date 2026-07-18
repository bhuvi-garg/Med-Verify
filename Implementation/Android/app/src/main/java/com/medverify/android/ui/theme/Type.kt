package com.medverify.android.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Default Material3 type scale for now. REQ-11's always-simplified, large-text
// requirement applies to the elderly-facing screens specifically (not this
// caretaker-operated onboarding form, per ARCH-01) — revisit sizing once those
// screens are designed.
val Typography = Typography(
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    )
)
