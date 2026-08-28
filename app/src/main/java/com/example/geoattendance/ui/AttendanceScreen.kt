package com.example.geoattendance.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.geoattendance.util.OFFICE_RADIUS_METERS
import com.example.geoattendance.viewmodel.AttendanceUiState
import com.example.geoattendance.viewmodel.AttendanceViewModel
import com.example.geoattendance.viewmodel.AttendanceViewModelFactory
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    onBack: () -> Unit = {},
    viewModel: AttendanceViewModel = viewModel(
        factory = AttendanceViewModelFactory(androidx.compose.ui.platform.LocalContext.current)
    )
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OfficeContextCard(state = state, onSetOfficeLocation = viewModel::setOfficeLocation)

            Spacer(Modifier.height(32.dp))

            DistanceIndicator(state = state)

            Spacer(Modifier.height(32.dp))

            MarkAttendanceButton(state = state, onMark = viewModel::markAttendance)

            state.error?.let {
                Spacer(Modifier.height(16.dp))
                ErrorMessage(it)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OfficeContextCard(state: AttendanceUiState, onSetOfficeLocation: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Step header with indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "STEP 1: OFFICE CONTEXT",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp,
                    color = Color.Gray
                )
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Map placeholder with coordinates
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    if (state.currentLocation != null) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White,
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Lat: ${"%.4f".format(state.currentLocation!!.latitude)}, Lon: ${"%.4f".format(state.currentLocation!!.longitude)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        Text(
                            "Locating...",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "To mark your attendance, ensure your current office location is correctly identified.",
                fontSize = 13.sp,
                color = Color.Gray,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(16.dp))

            // Set Office Location Button
            OutlinedButton(
                onClick = onSetOfficeLocation,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !state.isSettingOffice
            ) {
                if (state.isSettingOffice) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (state.officeLocation == null) "Set Office Location" else "Update Office Location",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DistanceIndicator(state: AttendanceUiState) {
    val distance = state.distanceMeters
    val progress = distance?.let { (1f - (it / (it + OFFICE_RADIUS_METERS * 4))).coerceIn(0.05f, 1f) } ?: 0f
    val ringColor = if (state.isWithinRange) Color(0xFF2E7D32) else Color(0xFFD32F2F)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Distance ring
        Box(contentAlignment = Alignment.Center) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(160.dp)) {
                val stroke = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                drawArc(
                    color = Color(0xFFE7E9EC),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = stroke
                )
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = stroke
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = distance?.let { "${it.roundToInt()}m" } ?: "--",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text("AWAY", fontSize = 11.sp, color = Color.Gray, letterSpacing = 1.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Status badge
        Surface(
            shape = RoundedCornerShape(50),
            color = if (state.isWithinRange) Color(0xFFE8F5E9) else Color(0xFFFDECEA),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(ringColor)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (state.isWithinRange) "IN RANGE" else "OUT OF RANGE",
                    color = ringColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Move within ${OFFICE_RADIUS_METERS.roundToInt()} meters of the designated office location to enable check-in.",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun MarkAttendanceButton(state: AttendanceUiState, onMark: () -> Unit) {
    val enabled = state.canMarkAttendance

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 2.dp,
                color = Color(0xFFDADFE3),
                shape = RoundedCornerShape(16.dp)
            )
            .background(if (enabled) Color.White else Color(0xFFF5F5F5))
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary else Color(0xFFBDBDBD),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onMark,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = Color(0xFFBDBDBD)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (state.attendanceMarked) "✓ Attendance Marked" else "Mark Attendance",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = state.windowLabel.uppercase(),
                fontSize = 11.sp,
                color = Color.Gray,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Surface(
        color = Color(0xFFFDECEA),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                message,
                color = Color(0xFFD32F2F),
                fontSize = 13.sp
            )
        }
    }
}
