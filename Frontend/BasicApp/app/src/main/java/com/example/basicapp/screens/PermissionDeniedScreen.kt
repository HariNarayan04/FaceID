package com.example.basicapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PermissionDeniedScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5DC))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Camera and Storage Permissions Required",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "This app needs camera and storage permissions to identify faces. Please grant the permissions to continue.",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 32.dp),
            color = Color.DarkGray
        )

        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(0.6f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Grant Permissions")
        }
    }
}