package com.example.basicapp.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun MainScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5DC)) // Beige background
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to Face Identification System",
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Take Image Button
        Button(
            onClick = { navController.navigate("camera") },
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(0.6f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "Take Image")
        }

        // Upload Image Button
        // Upload Image Button
        val context = LocalContext.current
        var selectedUri by remember { mutableStateOf<Uri?>(null) }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            selectedUri = uri
            if (uri != null) {
                // URL encode the URI to safely pass it as a parameter
                val encodedUri = Uri.encode(uri.toString())
                navController.navigate("upload/$encodedUri")
            }
        }

        Button(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(0.6f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "Upload Image")
        }

// Logout Button
        Spacer(modifier = Modifier.height(64.dp)) // More vertical spacing
        Button(
            onClick = { navController.navigate("home") },
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(0.6f),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Gray,
                contentColor = Color.White
            )
        ) {
            Text(text = "Logout")
        }
    }
}