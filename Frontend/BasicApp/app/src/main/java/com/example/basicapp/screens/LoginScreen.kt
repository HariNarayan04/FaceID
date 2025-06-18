package com.example.basicapp.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.basicapp.api.RetrofitInstance
import com.example.basicapp.api.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material3.OutlinedTextFieldDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .wrapContentSize(Alignment.Center)
    ) {
        Text(
            text = "Login to FaceID System",
            fontSize = 24.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Email Input
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = Color.DarkGray,
                unfocusedLabelColor = Color.Gray,
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

// Password Input
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.LightGray,
                focusedLabelColor = Color.DarkGray,
                unfocusedLabelColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    scope.launch {
                        val loginSuccess = loginUser(email, password)
                        if (loginSuccess) {
                            Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                            navController.navigate("main")
                        } else {
                            errorMessage = "Invalid email or password!"
                        }
                    }
                } else {
                    errorMessage = "Please enter email and password!"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Login")
        }

        // Show Error Message if Any
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = errorMessage, color = Color.Red)
        }
    }
}

// API Call for Login
suspend fun loginUser(email: String, password: String): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val response = RetrofitInstance.api.loginUser(User(email, password)).awaitResponse()
            return@withContext response.isSuccessful && response.body()?.status == "success"
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}