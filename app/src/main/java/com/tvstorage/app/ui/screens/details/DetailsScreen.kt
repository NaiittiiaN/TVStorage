package com.tvstorage.app.ui.screens.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tvstorage.app.data.entity.TelevisionEntity
import com.tvstorage.app.ui.theme.TVStorageTheme
import com.tvstorage.app.utils.DateUtils
import com.tvstorage.app.utils.QRUtils

@Composable
fun DetailsScreen(
    tvId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val television by viewModel.television.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val navigateBack by viewModel.navigateBack.collectAsState()

    LaunchedEffect(tvId) {
        viewModel.loadTelevision(tvId)
    }

    LaunchedEffect(navigateBack) {
        if (navigateBack) {
            onNavigateBack()
        }
    }

    DetailsScreenContent(
        television = television,
        isLoading = isLoading,
        onNavigateBack = onNavigateBack,
        onNavigateToEdit = onNavigateToEdit,
        onTogglePause = { viewModel.togglePause() },
        onArchive = { viewModel.archive() },
        onDelete = { viewModel.delete() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreenContent(
    television: TelevisionEntity?,
    isLoading: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onTogglePause: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали заказа", fontSize = 14.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onTogglePause, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (television?.isPaused == true) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Pause",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onArchive, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Archive, "Archive", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        if (isLoading || television == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val tv = television
            val daysSince = DateUtils.getDaysSince(tv.receivedDate)
            val totalCost = DateUtils.calculateTotalCost(tv.dailyCost, daysSince)
            var showQr by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Заголовок
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "S/N: ${tv.orderNumber}",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${tv.brand} ${tv.model}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
                            )
                        }
                        IconButton(onClick = { showQr = !showQr }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.QrCode, null, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                AnimatedVisibility(visible = showQr) {
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                            val qrBitmap = remember(tv.id) { QRUtils.generateQRCode("tvstorage://details/${tv.id}", 256) }
                            Image(bitmap = qrBitmap.asImageBitmap(), "QR", modifier = Modifier.size(100.dp))
                            Button(onClick = {}, modifier = Modifier.height(32.dp)) {
                                Text("Поделиться", fontSize = 10.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Компактные детали
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        DetailRowSmall("Дней хранения", "$daysSince дн.")
                        DetailRowSmall("Стоимость/день", "%.0f ₽".format(tv.dailyCost))
                        DetailRowSmall("ИТОГО", "%.0f ₽".format(totalCost), fontWeight = FontWeight.Black)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        DetailRowSmall("Дата ремонта", DateUtils.formatDate(tv.receivedDate))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Клиент", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(tv.clientName.ifBlank { "—" }, fontSize = 11.sp)
                        Text(tv.phoneNumber.ifBlank { "—" }, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Примечания", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(tv.notes.ifBlank { "Нет примечаний" }, fontSize = 11.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DetailRowSmall(label: String, value: String, fontWeight: FontWeight = FontWeight.Normal) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 10.sp)
        Text(value, fontSize = 11.sp, fontWeight = fontWeight)
    }
}
