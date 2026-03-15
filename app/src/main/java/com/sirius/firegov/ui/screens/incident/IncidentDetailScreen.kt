package com.sirius.firegov.ui.screens.incident

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.sirius.firegov.data.model.Incident
import com.sirius.firegov.ui.screens.history.StatusChip
import com.sirius.firegov.ui.theme.FiregovTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentDetailScreen(
    onBack: () -> Unit,
    viewModel: IncidentDetailViewModel = hiltViewModel()
) {
    val incident by viewModel.incident.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    IncidentDetailScreenContent(
        incident = incident,
        isLoading = isLoading,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentDetailScreenContent(
    incident: Incident?,
    isLoading: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isPreview = LocalInspectionMode.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Incident Report", 
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            incident?.let { data ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(scrollState)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusChip(status = data.status)
                        Text(
                            data.createdAt?.toDate()?.toString()?.take(16) ?: "Just now", 
                            style = MaterialTheme.typography.labelSmall, 
                            color = Color.Gray
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        data.address, 
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, lineHeight = 32.sp)
                    )
                    
                    if (data.reporterName.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.padding(top = 12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Reported by ${data.reporterName}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    if (data.photoUrls.isNotEmpty()) {
                        Text("EVIDENCE PHOTOS", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp))
                        LazyRow(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(data.photoUrls) { url ->
                                Card(
                                    modifier = Modifier.size(width = 240.dp, height = 160.dp),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    Text("SITUATION DETAILS", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp))
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            data.description, 
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    Text("LOCATION", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp))
                    Card(
                        modifier = Modifier.fillMaxWidth().height(200.dp).padding(vertical = 12.dp),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (isPreview) {
                                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                                    Text("Map View")
                                }
                            } else {
                                data.location?.let { loc ->
                                    val latLng = LatLng(loc.latitude, loc.longitude)
                                    GoogleMap(
                                        modifier = Modifier.fillMaxSize(),
                                        cameraPositionState = rememberCameraPositionState {
                                            position = CameraPosition.fromLatLngZoom(latLng, 15f)
                                        }
                                    ) {
                                        Marker(state = MarkerState(position = latLng))
                                    }
                                }
                            }
                        }
                    }

                    if (data.assignedStation != null) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("RESPONDING UNIT", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary))
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(data.assignedStation, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                
                                data.nearestStationDistance?.let { dist ->
                                    val distKm = "%.2f".format(dist / 1000.0)
                                    Text("Dispatched from $distKm km away", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                
                                Spacer(modifier = Modifier.height(20.dp))
                                
                                Button(
                                    onClick = {
                                        val gmmIntentUri = Uri.parse("google.navigation:q=${data.location?.latitude},${data.location?.longitude}")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                        mapIntent.setPackage("com.google.android.apps.maps")
                                        context.startActivity(mapIntent)
                                    },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Directions, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Navigate to Scene", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(48.dp))
                }
            } ?: Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Incident data unavailable.")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IncidentDetailScreenPreview() {
    FiregovTheme {
        IncidentDetailScreenContent(
            incident = Incident(
                incidentId = "1",
                address = "123 MG Road, Bangalore, KA",
                description = "Structure fire reported on the 4th floor of the commercial complex. Smoke visible from street level.",
                status = "responding",
                assignedStation = "FS-KA-001 CENTRAL STATION",
                nearestStationDistance = 4500.0
            ),
            isLoading = false,
            onBack = {}
        )
    }
}
