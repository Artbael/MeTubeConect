package com.metube.sender.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.metube.sender.data.SettingsRepository
import kotlinx.coroutines.launch

private fun parseStoredServerUrl(stored: String): List<String>? {
    if (stored.isBlank()) return null
    val regex = Regex("""(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})(?::(\d{1,5}))?""")
    val match = regex.find(stored) ?: return null
    val (o1, o2, o3, o4, port) = match.destructured
    return listOf(o1, o2, o3, o4, port)
}

private fun buildServerUrl(octets: List<String>, port: String): String {
    return "http://${octets.joinToString(".")}:$port"
}

private fun isComplete(octets: List<String>, port: String): Boolean {
    return octets.all { it.isNotBlank() } && port.isNotBlank()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var octet1 by remember { mutableStateOf(TextFieldValue("")) }
    var octet2 by remember { mutableStateOf(TextFieldValue("")) }
    var octet3 by remember { mutableStateOf(TextFieldValue("")) }
    var octet4 by remember { mutableStateOf(TextFieldValue("")) }
    var port by remember { mutableStateOf(TextFieldValue("")) }

    var saved by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(false) }

    val focusOctet2 = remember { FocusRequester() }
    val focusOctet3 = remember { FocusRequester() }
    val focusOctet4 = remember { FocusRequester() }
    val focusPort = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        val stored = settingsRepository.getServerUrl()
        val parsed = parseStoredServerUrl(stored)
        if (parsed != null) {
            octet1 = TextFieldValue(parsed[0])
            octet2 = TextFieldValue(parsed[1])
            octet3 = TextFieldValue(parsed[2])
            octet4 = TextFieldValue(parsed[3])
            port = TextFieldValue(parsed.getOrElse(4) { "" })
        }
        loaded = true
    }

    val octetsList = listOf(octet1.text, octet2.text, octet3.text, octet4.text)
    val canSave = loaded && isComplete(octetsList, port.text)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Servidor MeTube", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OctetField(
                    value = octet1,
                    onValueChange = { newValue ->
                        octet1 = sanitizeOctet(newValue)
                        saved = false
                        if (shouldAutoAdvance(newValue.text)) focusOctet2.requestFocus()
                    },
                    modifier = Modifier.weight(1f)
                )
                DotSeparator()

                OctetField(
                    value = octet2,
                    onValueChange = { newValue ->
                        octet2 = sanitizeOctet(newValue)
                        saved = false
                        if (shouldAutoAdvance(newValue.text)) focusOctet3.requestFocus()
                    },
                    modifier = Modifier.weight(1f).focusRequester(focusOctet2),
                    onBackspaceOnEmpty = { focusManager.moveFocus(FocusDirection.Previous) }
                )
                DotSeparator()

                OctetField(
                    value = octet3,
                    onValueChange = { newValue ->
                        octet3 = sanitizeOctet(newValue)
                        saved = false
                        if (shouldAutoAdvance(newValue.text)) focusOctet4.requestFocus()
                    },
                    modifier = Modifier.weight(1f).focusRequester(focusOctet3),
                    onBackspaceOnEmpty = { focusManager.moveFocus(FocusDirection.Previous) }
                )
                DotSeparator()

                OctetField(
                    value = octet4,
                    onValueChange = { newValue ->
                        octet4 = sanitizeOctet(newValue)
                        saved = false
                        if (shouldAutoAdvance(newValue.text)) focusPort.requestFocus()
                    },
                    modifier = Modifier.weight(1f).focusRequester(focusOctet4),
                    onBackspaceOnEmpty = { focusManager.moveFocus(FocusDirection.Previous) },
                    imeAction = ImeAction.Next
                )

                Text(
                    ":",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 1.dp)
                )

                Surface(
                    modifier = Modifier
                        .weight(1.3f)
                        .height(56.dp)
                        .focusRequester(focusPort),
                    shape = MaterialTheme.shapes.extraSmall,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (port.text.isEmpty()) {
                            Text(
                                text = "Port",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                        BasicTextField(
                            value = port,
                            onValueChange = { newValue ->
                                val digitsOnly = newValue.text.filter { it.isDigit() }.take(5)
                                port = newValue.copy(text = digitsOnly, selection = TextRange(digitsOnly.length))
                                saved = false
                            },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                fontWeight = FontWeight.Medium
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Text(
                "Enter the IP address and port of your local MeTube server " +
                        "(e.g., xxx.xxx.x.x : xxxx).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Button(
                onClick = {
                    scope.launch {
                        val url = buildServerUrl(octetsList, port.text)
                        settingsRepository.setServerUrl(url)
                        saved = true
                    }
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }

            if (saved) {
                Text(
                    "Guardado ✓",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(8.dp))
            Divider()
            Text(
                "Las opciones predeterminadas para YouTube y otros sitios (modo, " +
                        "calidad y formato) se guardan automáticamente cuando marcas " +
                        "\"Guardar como opciones predeterminadas\" en el modal de envío.",
                style = MaterialTheme.typography.bodySmall
            )

            // Empuja el contenido hacia arriba y deja la firma abajo del todo
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Developed by Artbael",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun OctetField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
    onBackspaceOnEmpty: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.height(56.dp),
        shape = MaterialTheme.shapes.extraSmall,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (value.text.isEmpty()) {
                Text(
                    text = "0",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Medium
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = imeAction
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    onBackspaceOnEmpty
}

@Composable
private fun DotSeparator() {
    Text(
        ".",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 2.dp)
    )
}

private fun sanitizeOctet(value: TextFieldValue): TextFieldValue {
    val digitsOnly = value.text.filter { it.isDigit() }.take(3)
    return value.copy(text = digitsOnly, selection = TextRange(digitsOnly.length))
}

private fun shouldAutoAdvance(text: String): Boolean {
    return text.length >= 3
}