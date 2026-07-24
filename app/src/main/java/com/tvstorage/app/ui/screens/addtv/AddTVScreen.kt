package com.tvstorage.app.ui.screens.addtv

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tvstorage.app.ui.components.TvTextField
import com.tvstorage.app.utils.DateUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTVScreen(
    tvId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: AddTVViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current

    LaunchedEffect(tvId) {
        if (tvId != null && tvId > 0) {
            viewModel.loadTelevision(tvId)
        }
    }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            try { focusRequester.requestFocus() } catch (e: Exception) {}
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (uiState.isEditing) "Редактировать ТВ" else "Добавить телевизор")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                TvTextField(
                    value = uiState.orderNumber,
                    onValueChange = viewModel::onOrderNumberChange,
                    label = "S/N *",
                    placeholder = "Введите серийный номер",
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    initialEditing = tvId == null,
                    singleLine = true,
                    isError = uiState.orderNumberError != null,
                    supportingText = uiState.orderNumberError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (uiState.orderNumber.isNotBlank()) viewModel.save() else focusManager.clearFocus()
                    })
                )

                Spacer(modifier = Modifier.height(12.dp))

                TvTextField(
                    value = uiState.brand,
                    onValueChange = viewModel::onBrandChange,
                    label = "Бренд",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.height(12.dp))
                TvTextField(
                    value = uiState.model,
                    onValueChange = viewModel::onModelChange,
                    label = "Модель",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.height(12.dp))
                TvTextField(
                    value = uiState.clientName,
                    onValueChange = viewModel::onClientNameChange,
                    label = "ФИО клиента",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                TvTextField(
                    value = uiState.dailyCost,
                    onValueChange = viewModel::onDailyCostChange,
                    label = "Стоимость хранения (₽)",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // РУЧНАЯ УСТАНОВКА ДАТЫ (ВСЕГДА ВИДИМА)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Дата и время приемки", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val currentDateTime = uiState.customReceivedDate ?: System.currentTimeMillis()
                        val calendar = remember(currentDateTime) {
                            Calendar.getInstance().apply { timeInMillis = currentDateTime }
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = {
                                    DatePickerDialog(context, { _, y, m, d ->
                                        calendar.set(Calendar.YEAR, y)
                                        calendar.set(Calendar.MONTH, m)
                                        calendar.set(Calendar.DAY_OF_MONTH, d)
                                        viewModel.onDateChange(calendar.timeInMillis)
                                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(DateUtils.formatDate(currentDateTime), fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = {
                                    TimePickerDialog(context, { _, h, min ->
                                        calendar.set(Calendar.HOUR_OF_DAY, h)
                                        calendar.set(Calendar.MINUTE, min)
                                        viewModel.onDateChange(calendar.timeInMillis)
                                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(DateUtils.formatDateTime(currentDateTime).split(" ").last(), fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = viewModel::save,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !uiState.isSaving && uiState.orderNumber.isNotBlank(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (uiState.isSaving) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                    else Text(if (uiState.isEditing) "Сохранить" else "Добавить", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

