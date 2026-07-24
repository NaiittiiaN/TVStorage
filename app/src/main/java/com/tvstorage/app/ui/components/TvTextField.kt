package com.tvstorage.app.ui.components

import android.view.KeyEvent
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun TvTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    initialEditing: Boolean = false,
    placeholder: String? = null,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true
) {
    var isEditing by remember { mutableStateOf(initialEditing) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Если нажали (на тачскрине), активируем редактирование
    LaunchedEffect(isPressed) {
        if (isPressed) {
            isEditing = true
        }
    }

    // При активации редактирования показываем клавиатуру
    LaunchedEffect(isEditing) {
        if (isEditing) {
            keyboardController?.show()
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = {
            if (isEditing) onValueChange(it)
        },
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { 
                if (!it.isFocused) {
                    isEditing = false 
                }
            }
            .onKeyEvent {
                // Если нажата центральная кнопка пульта (D-pad Center / Enter)
                if (it.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                    (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                     it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)
                ) {
                    if (!isEditing) {
                        isEditing = true
                        return@onKeyEvent true
                    }
                }
                false
            },
        readOnly = !isEditing,
        singleLine = singleLine,
        shape = RoundedCornerShape(12.dp),
        isError = isError,
        supportingText = supportingText,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource
    )
}
