package com.sirius.firegov.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sirius.firegov.data.model.User
import com.sirius.firegov.ui.theme.FiregovTheme

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val updateStatus by viewModel.updateStatus.collectAsState()

    LaunchedEffect(updateStatus) {
        if (updateStatus is UpdateStatus.Success) {
            viewModel.resetUpdateStatus()
        }
    }

    ProfileScreenContent(
        user = user,
        isLoading = isLoading,
        updateStatus = updateStatus,
        onUpdate = viewModel::updateProfile,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(
    user: User?,
    isLoading: Boolean,
    updateStatus: UpdateStatus,
    onUpdate: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember(user) { mutableStateOf(user?.name ?: "") }
    var phoneNumber by remember(user) { mutableStateOf(user?.phoneNumber ?: "") }
    val isPreview = LocalInspectionMode.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            "Account Profile",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Keep your contact information up to date.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (isLoading && !isPreview) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    ProfileField(
                        label = "Full Name",
                        value = name,
                        onValueChange = { name = it },
                        icon = Icons.Default.Badge,
                        placeholder = "Enter your legal name"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    ProfileField(
                        label = "Phone Number",
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        icon = Icons.Default.Phone,
                        placeholder = "+91 XXXXX XXXXX",
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    ProfileField(
                        label = "Email Address",
                        value = user?.email ?: "loading...",
                        onValueChange = {},
                        icon = Icons.Default.Email,
                        enabled = false
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    if (updateStatus is UpdateStatus.Loading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        Button(
                            onClick = { onUpdate(name, phoneNumber) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("SAVE CHANGES", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        }
                    }

                    if (updateStatus is UpdateStatus.Error) {
                        Text(
                            text = updateStatus.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 16.dp).align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        OutlinedButton(
            onClick = { /* Handle delete or other settings */ },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
        ) {
            Text("Request Data Deletion", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    enabled: Boolean = true,
    placeholder: String = "",
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = enabled,
            singleLine = true,
            keyboardOptions = keyboardOptions,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = if (enabled) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                focusedContainerColor = Color.Transparent
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    FiregovTheme {
        ProfileScreenContent(
            user = User(name = "Sirius Black", email = "sirius.black@firegov.in", phoneNumber = "919876543210"),
            isLoading = false,
            updateStatus = UpdateStatus.Idle,
            onUpdate = { _, _ -> }
        )
    }
}
