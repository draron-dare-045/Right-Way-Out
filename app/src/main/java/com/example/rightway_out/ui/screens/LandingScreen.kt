package com.example.rightway_out.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rightway_out.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun LandingScreen(onGetStarted: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(900), label = "fade"
    )
    val slideUp by animateDpAsState(
        targetValue = if (visible) 0.dp else 40.dp,
        animationSpec = tween(900, easing = EaseOutCubic), label = "slide"
    )

    // Badge pulse
    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(
        initialValue = 1f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1400, easing = EaseInOutSine),
            RepeatMode.Reverse), label = "scale"
    )

    LaunchedEffect(Unit) { delay(200); visible = true }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Maroon900, Maroon800, Maroon700))
        )
    ) {
        // Decorative circles background
        Box(modifier = Modifier.size(380.dp)
            .offset(x = (-80).dp, y = (-60).dp)
            .alpha(0.06f)
            .background(White, CircleShape))
        Box(modifier = Modifier.size(260.dp)
            .align(Alignment.BottomEnd)
            .offset(x = 80.dp, y = 60.dp)
            .alpha(0.06f)
            .background(Gold, CircleShape))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .offset(y = -slideUp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // KHS Badge
            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(110.dp)
                    .background(
                        Brush.radialGradient(listOf(Gold, Color(0xFFAA8530))),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("KHS", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = White)
                    Text("EST. 1925", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                        color = White.copy(alpha = 0.8f), letterSpacing = 1.5.sp)
                }
            }

            Spacer(Modifier.height(28.dp))

            Column(
                modifier = Modifier.alpha(alpha),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("KAPSABET", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold,
                    color = White, letterSpacing = 4.sp)
                Text("HIGH SCHOOL", fontSize = 18.sp, fontWeight = FontWeight.Light,
                    color = Gold, letterSpacing = 6.sp)

                Spacer(Modifier.height(8.dp))
                Divider(modifier = Modifier.width(80.dp), color = Gold, thickness = 1.5.dp)
                Spacer(Modifier.height(8.dp))

                Text("RightWay Out", fontSize = 15.sp, fontWeight = FontWeight.Medium,
                    color = White.copy(alpha = 0.85f))
                Text("Digital Clearance System", fontSize = 12.sp,
                    color = White.copy(alpha = 0.55f), letterSpacing = 0.5.sp)
            }

            Spacer(Modifier.height(56.dp))

            // Feature pills
            Column(
                modifier = Modifier.alpha(alpha),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeaturePill(Icons.Default.VerifiedUser,  "Track clearance status in real-time")
                FeaturePill(Icons.Default.ShoppingCart,  "Manage your school shopping list")
                FeaturePill(Icons.Default.Message,       "Message admin directly from the app")
            }

            Spacer(Modifier.height(52.dp))

            Button(
                onClick = onGetStarted,
                modifier = Modifier.fillMaxWidth().height(56.dp).alpha(alpha),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Get Started", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Maroon900)
                    Icon(Icons.Default.ArrowForward, null, tint = Maroon900,
                        modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("© 2026 Kapsabet High School", fontSize = 11.sp,
                color = White.copy(alpha = 0.3f))
        }
    }
}

@Composable
private fun FeaturePill(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(White.copy(alpha = 0.07f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, null, tint = Gold, modifier = Modifier.size(20.dp))
        Text(text, fontSize = 13.sp, color = White.copy(alpha = 0.85f))
    }
}
