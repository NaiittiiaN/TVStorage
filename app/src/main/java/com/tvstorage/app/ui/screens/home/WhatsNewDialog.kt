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
        title = { Text("Что нового в v1.2.5", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                val updates = listOf(
                    "⌨️ Клавиатура (TV): Исправлено автоматическое появление клавиатуры при переходе по полям. Теперь она открывается только после нажатия ОК/Enter.",
                    "🎯 Фокус: При добавлении нового ТВ поле S/N выбирается автоматически и сразу готово к вводу.",
                    "📊 Сортировка: Сортировка по дате и стоимости",
                    "🌐 Веб-сервер: Порт 4848 по умолчанию",
                    "🛠 Веб-интерфейс: Удаленное редактирование и управление паузой",
                    "🚪 Выход: Подтверждение выхода двойным нажатием «Назад»"
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
