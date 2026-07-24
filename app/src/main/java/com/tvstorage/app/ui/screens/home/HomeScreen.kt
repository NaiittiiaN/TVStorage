package com.tvstorage.app.ui.screens.home

import android.app.Activity
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tvstorage.app.data.entity.TelevisionEntity
import com.tvstorage.app.ui.theme.TVStorageTheme
import com.tvstorage.app.utils.DateUtils

@Composable
fun HomeScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToArchive: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val televisions by viewModel.televisions.collectAsState()
    val activeCount by viewModel.activeCount.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showWhatsNew by viewModel.showWhatsNew.collectAsState()
    val currentSort by viewModel.sortOrder.collectAsState()
    
    val context = LocalContext.current
    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    // Двойное нажатие для выхода
    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < 2000) {
            (context as? Activity)?.finish()
        } else {
            lastBackPressTime = currentTime
            Toast.makeText(context, "Нажмите еще раз для выхода", Toast.LENGTH_SHORT).show()
        }
    }

    if (showWhatsNew) {
        WhatsNewDialog(onDismiss = { viewModel.dismissWhatsNew() })
    }

    HomeScreenContent(
        televisions = televisions,
        activeCount = activeCount,
        isSearching = isSearching,
        searchQuery = searchQuery,
        currentSort = currentSort,
        onSortChange = { viewModel.setSortOrder(it) },
        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
        onToggleSearch = { viewModel.toggleSearch() },
        onNavigateToAdd = onNavigateToAdd,
        onNavigateToDetails = onNavigateToDetails,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToArchive = onNavigateToArchive,
        onDeleteTelevision = { viewModel.deleteTelevision(it) },
        onTogglePause = { viewModel.togglePause(it) },
        onArchiveTelevision = { viewModel.archiveTelevision(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    televisions: List<TelevisionEntity>,
    activeCount: Int,
    isSearching: Boolean,
    searchQuery: String,
    currentSort: SortOrder,
    onSortChange: (SortOrder) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToArchive: () -> Unit,
    onDeleteTelevision: (TelevisionEntity) -> Unit,
    onTogglePause: (TelevisionEntity) -> Unit,
    onArchiveTelevision: (TelevisionEntity) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    
    val columns = if (isPortrait) 3 else 5
    val rowsVisible = if (isPortrait) 7 else 3
    val totalVisible = columns * rowsVisible

    val focusRequester = remember { FocusRequester() }
    var showSortMenu by remember { mutableStateOf(false) }

    LaunchedEffect(isSearching) {
        if (isSearching) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearching) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("Поиск по S/N...") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                        )
                    } else {
                        Column {
                            Text("TV Storage")
                            Text(
                                text = "Активных: $activeCount",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(imageVector = Icons.Default.Sort, contentDescription = "Сортировка")
                        }
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("По дате (новые)") },
                                onClick = { onSortChange(SortOrder.DATE_DESC); showSortMenu = false },
                                leadingIcon = { Icon(Icons.Default.Schedule, null) },
                                trailingIcon = { if(currentSort == SortOrder.DATE_DESC) Icon(Icons.Default.Check, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("По дате (старые)") },
                                onClick = { onSortChange(SortOrder.DATE_ASC); showSortMenu = false },
                                leadingIcon = { Icon(Icons.Default.History, null) },
                                trailingIcon = { if(currentSort == SortOrder.DATE_ASC) Icon(Icons.Default.Check, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("По стоимости (убыв.)") },
                                onClick = { onSortChange(SortOrder.COST_DESC); showSortMenu = false },
                                leadingIcon = { Icon(Icons.Default.TrendingDown, null) },
                                trailingIcon = { if(currentSort == SortOrder.COST_DESC) Icon(Icons.Default.Check, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("По стоимости (возр.)") },
                                onClick = { onSortChange(SortOrder.COST_ASC); showSortMenu = false },
                                leadingIcon = { Icon(Icons.Default.TrendingUp, null) },
                                trailingIcon = { if(currentSort == SortOrder.COST_ASC) Icon(Icons.Default.Check, null) }
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToAdd) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Добавить")
                    }
                    IconButton(onClick = onToggleSearch) {
                        Icon(
                            imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Поиск"
                        )
                    }
                    IconButton(onClick = onNavigateToArchive) {
                        Icon(imageVector = Icons.Default.Archive, contentDescription = "Архив")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Настройки")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { paddingValues ->
        if (televisions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Нет активных телевизоров",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateToAdd) {
                        Text("Добавить первый ТВ")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                userScrollEnabled = televisions.size > totalVisible 
            ) {
                items(televisions, key = { it.id }) { tv ->
                    TelevisionGridCard(
                        television = tv,
                        onClick = { onNavigateToDetails(tv.id) },
                        onDelete = { onDeleteTelevision(tv) },
                        onTogglePause = { onTogglePause(tv) },
                        onArchive = { onArchiveTelevision(tv) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TelevisionGridCard(
    television: TelevisionEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onTogglePause: () -> Unit,
    onArchive: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    
    val timeParts = DateUtils.getFormattedTimeSince(television.receivedDate)
    val daysSince = timeParts.days
    val totalCost = DateUtils.calculateTotalCost(television.dailyCost, daysSince)

    val backgroundColor = when {
        television.isPaused -> MaterialTheme.colorScheme.surfaceVariant
        daysSince >= 10 -> Color(0xFFFFEBEE)
        daysSince >= 7 -> Color(0xFFFFF3E0)
        daysSince >= 4 -> Color(0xFFE8F5E9)
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when {
        television.isPaused -> MaterialTheme.colorScheme.onSurfaceVariant
        daysSince >= 10 -> Color(0xFFC62828)
        daysSince >= 7 -> Color(0xFFEF6C00)
        daysSince >= 4 -> Color(0xFF2E7D32)
        else -> MaterialTheme.colorScheme.primary
    }

    val titleSize = 12.sp
    val timeValueSize = 14.sp
    val labelSize = 8.sp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 4.dp else 1.dp,
                color = if (isFocused) MaterialTheme.colorScheme.primary else contentColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isFocused) 8.dp else 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 1. S/N (Центр)
                Text(
                    text = "S/N: ${television.orderNumber}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    ),
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    color = contentColor
                )

                // 2. Бренд и Модель
                val brandText = if (television.brand.isNotBlank() || television.model.isNotBlank()) {
                    "${television.brand} ${television.model}".trim()
                } else ""
                
                if (brandText.isNotBlank()) {
                    Text(
                        text = brandText,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // 3. Время
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimeUnitColumn(timeParts.days.toString(), "д", timeValueSize, labelSize, contentColor, television.isPaused)
                    Spacer(modifier = Modifier.width(4.dp))
                    TimeUnitColumn(timeParts.hours.toString(), "ч", timeValueSize, labelSize, contentColor, television.isPaused)
                    Spacer(modifier = Modifier.width(4.dp))
                    TimeUnitColumn(timeParts.minutes.toString(), "м", timeValueSize, labelSize, contentColor, television.isPaused)
                }

                Spacer(modifier = Modifier.height(2.dp))

                // 4. Сумма
                Text(
                    text = "%.0f ₽".format(totalCost),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = titleSize,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    color = contentColor
                )
            }

            if (television.isPaused) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(12.dp),
                    tint = contentColor
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(if (television.isPaused) "Возобновить" else "Пауза") },
                    onClick = { showMenu = false; onTogglePause() }
                )
                DropdownMenuItem(
                    text = { Text("В архив") },
                    onClick = { showMenu = false; onArchive() }
                )
                DropdownMenuItem(
                    text = { Text("Удалить", color = Color.Red) },
                    onClick = { showMenu = false; onDelete() }
                )
            }
        }
    }
}

@Composable
fun TimeUnitColumn(
    value: String,
    label: String,
    valueSize: androidx.compose.ui.unit.TextUnit,
    labelSize: androidx.compose.ui.unit.TextUnit,
    contentColor: Color,
    isPaused: Boolean
) {
    val displayColor = if (isPaused) MaterialTheme.colorScheme.onSurfaceVariant else contentColor
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = valueSize,
                fontWeight = FontWeight.Black,
                lineHeight = valueSize
            ),
            color = displayColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = labelSize,
                lineHeight = labelSize
            ),
            color = displayColor.copy(alpha = 0.7f),
            modifier = Modifier.padding(start = 1.dp, bottom = 1.dp)
        )
    }
}
