package com.sirius.firegov.ui.screens.report

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.*
import com.sirius.firegov.ui.theme.FiregovTheme
import kotlinx.coroutines.delay
import java.io.File

@SuppressLint("MissingPermission")
@Composable
fun ReportScreen(
    onSuccess: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    val location by viewModel.location.collectAsState()
    val address by viewModel.address.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val state by viewModel.state.collectAsState()

    var showSuccessAnimation by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                loc?.let { viewModel.updateLocation(it.latitude, it.longitude) }
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addPhoto(it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraUri?.let { viewModel.addPhoto(it) }
        }
    }

    fun launchCamera() {
        val file = File(context.cacheDir, "camera_image_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        tempCameraUri = uri
        cameraLauncher.launch(uri)
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA
        ))
    }

    LaunchedEffect(state) {
        if (state is ReportState.Success) {
            showSuccessAnimation = true
            delay(2000)
            showSuccessAnimation = false
            viewModel.resetState()
            onSuccess()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        ReportScreenContent(
            location = location,
            address = address,
            photos = photos,
            state = state,
            onLocationRequest = {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    loc?.let { viewModel.updateLocation(it.latitude, it.longitude) }
                }
            },
            onAddressChange = viewModel::setAddress,
            onAddCameraPhoto = { launchCamera() },
            onAddGalleryPhoto = { galleryLauncher.launch("image/*") },
            onSubmit = viewModel::submitReport,
            modifier = Modifier.fillMaxSize()
        )

        AnimatedVisibility(
            visible = showSuccessAnimation,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                modifier = Modifier.size(280.dp),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 12.dp,
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Report Filed!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Help is on the way.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ReportScreenContent(
    location: GeoPoint?,
    address: String,
    photos: List<Uri>,
    state: ReportState,
    onLocationRequest: () -> Unit,
    onAddressChange: (String) -> Unit,
    onAddCameraPhoto: () -> Unit,
    onAddGalleryPhoto: () -> Unit,
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var description by remember { mutableStateOf("") }
    val cameraPositionState = rememberCameraPositionState()
    val isPreview = LocalInspectionMode.current

    LaunchedEffect(location) {
        location?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(it.latitude, it.longitude), 15f)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            "Emergency Report",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Describe the situation and provide your location.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isPreview) {
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                        Text("Interactive Map View")
                    }
                } else {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = true),
                        uiSettings = MapUiSettings(zoomControlsEnabled = false)
                    ) {
                        location?.let {
                            Marker(state = MarkerState(position = LatLng(it.latitude, it.longitude)))
                        }
                    }
                }
                
                IconButton(
                    onClick = onLocationRequest,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .size(48.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "My Location", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Location Details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            placeholder = { Text("Confirming address...") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Situation Description", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            placeholder = { Text("E.g. Fire in the residential building...") },
            modifier = Modifier.fillMaxWidth().height(120.dp).padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Evidence Photos (Max 3)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            photos.forEach { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            if (photos.size < 3) {
                // First Option: Camera
                Surface(
                    onClick = onAddCameraPhoto,
                    modifier = Modifier.size(90.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = "Take Photo", tint = MaterialTheme.colorScheme.primary)
                            Text("Camera", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Second Option: Gallery
                Surface(
                    onClick = onAddGalleryPhoto,
                    modifier = Modifier.size(90.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "Add Photo", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Gallery", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (state is ReportState.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Button(
                onClick = { onSubmit(description) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("SUBMIT EMERGENCY REPORT", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }

        if (state is ReportState.Error) {
            Text(
                state.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 16.dp).align(Alignment.CenterHorizontally)
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun ReportScreenPreview() {
    FiregovTheme {
        ReportScreenContent(
            location = null,
            address = "123 Emergency Way, New Delhi",
            photos = emptyList(),
            state = ReportState.Idle,
            onLocationRequest = {},
            onAddressChange = {},
            onAddCameraPhoto = {},
            onAddGalleryPhoto = {},
            onSubmit = {}
        )
    }
}
