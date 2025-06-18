package com.example.basicapp.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.basicapp.model.SecurityUser
import com.example.basicapp.model.VerificationRecord
import com.example.basicapp.viewmodel.AdminViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5DC)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Admin Login",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(bottom = 16.dp)
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = {
                // Hard-coded admin credentials check
                if (email == "hari@admin.com" && password == "p123") {
                    navController.navigate("admin_dashboard")
                } else {
                    errorMessage = "Invalid credentials"
                }
            },
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(0.6f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Login")
        }

        Button(
            onClick = { navController.navigateUp() },
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(0.6f),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) {
            Text("Back")
        }
    }
}

@Composable
fun AdminDashboardScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5DC)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Admin Dashboard",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = { navController.navigate("edit_security") },
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(0.6f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Edit Security")
        }

        Button(
            onClick = { navController.navigate("view_report") },
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(0.6f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("View Report")
        }

        Button(
            onClick = { navController.navigateUp() },
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(0.6f),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) {
            Text("Logout")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSecurityScreen(navController: NavHostController, viewModel: AdminViewModel) {
    val securityUsers by viewModel.securityUsers.collectAsState()
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingUser by remember { mutableStateOf<SecurityUser?>(null) }

    LaunchedEffect(key1 = true) {
        viewModel.fetchSecurityUsers()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5DC))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                "Security Users",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add User")
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(securityUsers) { user ->
                SecurityUserItem(
                    user = user,
                    onEditClick = { editingUser = user },
                    onDeleteClick = {
                        scope.launch {
                            viewModel.deleteSecurityUser(user.email)
                        }
                    }
                )
            }
        }
    }

    // Add/Edit Dialog
    if (showAddDialog || editingUser != null) {
        SecurityUserDialog(
            user = editingUser,
            onDismiss = {
                showAddDialog = false
                editingUser = null
            },
            onSave = { email, password ->
                scope.launch {
                    if (editingUser != null) {
                        viewModel.updateSecurityUser(editingUser!!.email, email, password)
                    } else {
                        viewModel.addSecurityUser(email, password)
                    }
                    showAddDialog = false
                    editingUser = null
                }
            }
        )
    }
}

@Composable
fun SecurityUserItem(
    user: SecurityUser,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(user.email, modifier = Modifier.weight(1f))

            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }

            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityUserDialog(
    user: SecurityUser?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var email by remember { mutableStateOf(user?.email ?: "") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (user == null) "Add Security User" else "Edit Security User") },
        text = {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        isError = false
                    },
                    label = { Text("Email") },
                    singleLine = true,
                    isError = isError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password${if (user != null) " (Leave empty to keep current)" else ""}") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (isError) {
                    Text(
                        text = "Email cannot be empty",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (email.isBlank()) {
                        isError = true
                    } else {
                        onSave(email, password)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ViewReportScreen(navController: NavHostController, viewModel: AdminViewModel) {
    val records by viewModel.records.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        viewModel.fetchRecords()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5DC))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                "Verification Records",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Clear Records")
            }
        }

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(8.dp)
        ) {
            Text("Roll Number", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("Timestamp", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
            Text("Status", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
        }

        // Records List
        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No records found")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(records) { record ->
                    RecordItem(record)
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Clear All Records") },
            text = { Text("Are you sure you want to delete all verification records? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            viewModel.clearAllRecords()
                            showDeleteDialog = false
                        }
                    }
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun RecordItem(record: VerificationRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(0.5.dp, Color.LightGray)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(record.roll_number, modifier = Modifier.weight(1f))
        Text(record.timestamp, modifier = Modifier.weight(1.5f))
        Box(
            modifier = Modifier
                .weight(0.8f)
                .padding(end = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .background(
                        if (record.status == "Entry") Color(0xFF4CAF50) else Color(0xFFE91E63),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    record.status,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}