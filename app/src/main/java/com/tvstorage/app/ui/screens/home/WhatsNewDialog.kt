package com.tvstorage.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun WhatsNewDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Что нового в v1.2.4", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                val updates = listOf(
                    "📊 Сортировка: Добавлена возможность сортировки по дате и стоимости",
                    "🌐 Веб-сервер: Порт 4848 возвращен для лучшей стабильности",
                    "🛠 Веб-интерфейс: Полноценное редактирование заказов и управление паузой",
                    "💰 Исправление цен: Поле стоимости теперь всегда предзаполнено (100 ₽)",
                    "📄 S/N: Поле «Номер заказа» заменено на «S/N» во всей системе",
                    "🚪 Выход: Теперь выход из приложения требует двойного нажатия «Назад»",
                    "💾 Безопасность: Веб-сервер закрывается при выходе из приложения",
                    "🎨 Стиль: Серый фон веб-интерфейса и улучшенная центровка данных"
                )
                items(updates) { update ->
                    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(update, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Принять") }
        }
    )
}
