package com.sirius.firegov.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sirius.firegov.data.model.Incident
import com.sirius.firegov.ui.theme.FiregovTheme

@Composable
fun HistoryScreen(
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val incidents by viewModel.incidents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchHistory()
    }

    HistoryScreenContent(
        incidents = incidents,
        isLoading = isLoading,
        onNavigateToDetail = onNavigateToDetail,
        modifier = modifier
    )
}

@Composable
fun HistoryScreenContent(
    incidents: List<Incident>,
    isLoading: Boolean,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "Incident History",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Track your reports and responder status.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (incidents.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No reports filed yet.", style = MaterialTheme.typography.titleMedium)
                    Text("Your emergency reports will appear here.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(incidents) { incident ->
                    IncidentCard(incident = incident) {
                        onNavigateToDetail(incident.incidentId)
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun IncidentCard(incident: Incident, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (incident.photoUrls.isNotEmpty()) {
                AsyncImage(
                    model = incident.photoUrls.first(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    incident.address,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Text(
                    incident.createdAt?.toDate()?.toString()?.take(16) ?: "Just now",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                
                if (!incident.assignedStation.isNullOrEmpty()) {
                    Text(
                        text = "Responder: ${incident.assignedStation}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            StatusChip(status = incident.status)
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val (color, text) = when (status.lowercase()) {
        "pending" -> Color(0xFFF59E0B) to "Pending"
        "responding" -> Color(0xFF3B82F6) to "En Route"
        "resolved" -> Color(0xFF10B981) to "Resolved"
        else -> Color.Gray to status.replaceFirstChar { it.uppercase() }
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        contentColor = color,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.sp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    FiregovTheme {
        HistoryScreenContent(
            incidents = listOf(
                Incident(
                    incidentId = "1",
                    address = "Connaught Place, New Delhi",
                    status = "pending",
                    description = "Electrical fire"
                ),
                Incident(
                    incidentId = "2",
                    address = "Sector 5, Salt Lake, Kolkata",
                    status = "responding",
                    assignedStation = "FS-WB-002"
                )
            ),
            isLoading = false,
            onNavigateToDetail = {}
        )
    }
}
