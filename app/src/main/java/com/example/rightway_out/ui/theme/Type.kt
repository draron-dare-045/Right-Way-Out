package com.example.rightway_out.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Two-voice type system:
 *  - DisplayFace (serif): used sparingly for the app name, screen titles and
 *    the big numbers on the dashboard — gives the "certificate / official
 *    record" feel that fits a clearance app.
 *  - BodyFace (sans): everything else — names, lists, form fields, chat.
 *    Optimised for legibility at small sizes on a phone.
 */
val DisplayFace = FontFamily.Serif
val BodyFace    = FontFamily.SansSerif

val KapsabetTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = DisplayFace,
        fontWeight = FontWeight.Bold,
        fontSize   = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = DisplayFace,
        fontWeight = FontWeight.Bold,
        fontSize   = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = BodyFace,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = BodyFace,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFace,
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFace,
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodySmall = TextStyle(
        fontFamily = BodyFace,
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontFamily = BodyFace,
        fontWeight = FontWeight.Bold,
        fontSize   = 14.sp,
        letterSpacing = 0.3.sp
    ),
    labelMedium = TextStyle(
        fontFamily = BodyFace,
        fontWeight = FontWeight.Bold,
        fontSize   = 12.sp,
        letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily = BodyFace,
        fontWeight = FontWeight.Bold,
        fontSize   = 10.sp,
        letterSpacing = 0.5.sp
    ),
)