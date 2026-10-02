package com.amoled.music.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.amoled.music.R

val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val maliGoogleFont = GoogleFont("Mali")

val MaliFontFamily = FontFamily(
    Font(googleFont = maliGoogleFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = maliGoogleFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = maliGoogleFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = maliGoogleFont, fontProvider = fontProvider, weight = FontWeight.Bold),
    Font(googleFont = maliGoogleFont, fontProvider = fontProvider, weight = FontWeight.Normal, style = FontStyle.Italic)
)

// Compact, crisp, minimalist typography with Color.Unspecified so Button contentColor and Text color work correctly
val AmoledTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = MaliFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        color = Color.Unspecified
    ),
    displayMedium = TextStyle(
        fontFamily = MaliFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        color = Color.Unspecified
    ),
    headlineLarge = TextStyle(
        fontFamily = MaliFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        color = Color.Unspecified
    ),
    headlineMedium = TextStyle(
        fontFamily = MaliFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = Color.Unspecified
    ),
    titleLarge = TextStyle(
        fontFamily = MaliFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = Color.Unspecified
    ),
    titleMedium = TextStyle(
        fontFamily = MaliFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        color = Color.Unspecified
    ),
    bodyLarge = TextStyle(
        fontFamily = MaliFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = Color.Unspecified
    ),
    bodyMedium = TextStyle(
        fontFamily = MaliFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = Color.Unspecified
    ),
    labelLarge = TextStyle(
        fontFamily = MaliFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        color = Color.Unspecified
    ),
    labelMedium = TextStyle(
        fontFamily = MaliFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        color = Color.Unspecified
    ),
    labelSmall = TextStyle(
        fontFamily = MaliFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        color = Color.Unspecified
    )
)
