package com.nononsenseapps.feeder.ui.compose.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aallam.openai.client.OpenAIHost
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.OpenAISettings
import com.nononsenseapps.feeder.openai.toOpenAIHost
import com.nononsenseapps.feeder.openai.toUrl
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens

@Composable
fun OpenAISection(
    openAISettings: OpenAISettings,
    openAIModels: OpenAIModelsState,
    onEvent: (OpenAISettingsEvent) -> Unit,
    initialEdit: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var editMode by remember { mutableStateOf(initialEdit) }
    var current by remember(openAISettings) { mutableStateOf(openAISettings) }

    Box(
        modifier = modifier
            .padding(top = 8.dp, start = 64.dp, end = 16.dp, bottom = 16.dp)
            .width(LocalDimens.current.maxContentWidth)
    ) {
        Row(
            modifier = Modifier.align(alignment = Alignment.TopEnd)
        ) {
            if (editMode) {
                IconButton(onClick = {
                    onEvent(OpenAISettingsEvent.UpdateSettings(current))
                    editMode = false
                }) {
                    Icon(Icons.Outlined.Save, contentDescription = "Save")
                }

                IconButton(onClick = { editMode = false }) {
                    Icon(Icons.Outlined.Close, contentDescription = "Cancel")
                }
            } else {
                IconButton(onClick = { editMode = true }) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Edit")
                }
            }
        }
        if (editMode) {
            OpenAISectionEdit(
                modifier = Modifier.padding(top = 32.dp),
                settings = current,
                models = openAIModels,
                onEvent = { event ->
                    when (event) {
                        OpenAISettingsEvent.LoadModels -> onEvent(event)
                        is OpenAISettingsEvent.UpdateSettings -> {
                            current = event.settings
                        }
                    }
                }
            )
        } else {
            OpenAISectionReadOnly(
                modifier = Modifier.padding(top = 32.dp),
                settings = current
            )
        }
    }
}

@Composable
private fun OpenAISectionReadOnly(
    settings: OpenAISettings,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        val transformedKey = remember(settings.key) { VisualTransformationApiKey().filter(AnnotatedString(settings.key)) }
        Text(
            text = stringResource(R.string.api_key),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = transformedKey.text,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(R.string.model_id),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = settings.modelId,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(R.string.url),
            style = MaterialTheme.typography.titleMedium
        )
        val url = remember(settings) { settings.toOpenAIHost(withAzureDeploymentId = true).toUrl().buildString() }
        Text(
            text = url,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun OpenAISectionEdit(
    settings: OpenAISettings,
    models: OpenAIModelsState,
    onEvent: (OpenAISettingsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(true) {
        onEvent(OpenAISettingsEvent.LoadModels)
    }

    var modelsMenuExpanded by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.api_key),
            style = MaterialTheme.typography.titleMedium
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = settings.key,
            onValueChange = {
                onEvent(OpenAISettingsEvent.UpdateSettings(settings = settings.copy(key = it)))
                onEvent(OpenAISettingsEvent.LoadModels)
            },
            visualTransformation = VisualTransformationApiKey()
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(R.string.model_id),
            style = MaterialTheme.typography.titleMedium,
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = settings.modelId,
            onValueChange = {
                onEvent(OpenAISettingsEvent.UpdateSettings(settings = settings.copy(modelId = it)))
                onEvent(OpenAISettingsEvent.LoadModels)
            },
            trailingIcon = {
                IconButton(
                    onClick = { modelsMenuExpanded = true },
                    enabled = models is OpenAIModelsState.Success
                ) {
                    if (models is OpenAIModelsState.Loading) {
                        CircularProgressIndicator()
                    } else {
                        Icon(Icons.Filled.ExpandMore, contentDescription = "List of available models")
                    }
                }
            }
        )
        OpenAIModelsSection(
            menuExpanded = modelsMenuExpanded,
            state = models,
            onValueChange = {
                onEvent(OpenAISettingsEvent.UpdateSettings(settings = settings.copy(modelId = it)))
            },
            onDismissRequest = { modelsMenuExpanded = false }
        )

        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(R.string.url),
            style = MaterialTheme.typography.titleMedium,
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = settings.baseUrl,
            placeholder = {
                Text(OpenAIHost.OpenAI.baseUrl)
            },
            onValueChange = {
                onEvent(OpenAISettingsEvent.UpdateSettings(settings = settings.copy(baseUrl = it)))
                onEvent(OpenAISettingsEvent.LoadModels)
            }
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(R.string.azure_deployment_id),
            style = MaterialTheme.typography.titleMedium,
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = settings.azureDeploymentId,
            onValueChange = {
                onEvent(OpenAISettingsEvent.UpdateSettings(settings = settings.copy(azureDeploymentId = it)))
                onEvent(OpenAISettingsEvent.LoadModels)
            }
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(R.string.azure_api_version),
            style = MaterialTheme.typography.titleMedium,
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = settings.azureApiVersion,
            onValueChange = {
                onEvent(OpenAISettingsEvent.UpdateSettings(settings = settings.copy(azureApiVersion = it)))
                onEvent(OpenAISettingsEvent.LoadModels)
            }
        )
    }
}

@Composable
private fun OpenAIModelsSection(menuExpanded: Boolean, state: OpenAIModelsState, onValueChange: (String) -> Unit, onDismissRequest: () -> Unit) {
    when (state) {
        is OpenAIModelsState.Success -> {
            if (state.ids.isEmpty()) {
                OutlinedCard {
                    Text(
                        text = stringResource(R.string.no_models_were_found),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            } else {
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = onDismissRequest
                ) {
                    state.ids.forEach { id ->
                        DropdownMenuItem(
                            text = { Text(text = id) },
                            onClick = {
                                onValueChange(id)
                                onDismissRequest()
                            }
                        )
                    }
                }
            }
        }

        is OpenAIModelsState.Error -> {
            OutlinedCard {
                Text(
                    text = stringResource(R.string.unable_to_load_models) + " " + state.message,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        OpenAIModelsState.Loading -> {}
        OpenAIModelsState.None -> {}
    }
}

@Preview
@Composable
fun OpenAISectionReadPreview() {
    Surface {
        OpenAISection(
            openAISettings = OpenAISettings(
                key = "sk-test_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
                modelId = "gpt-4o-mini"
            ),
            openAIModels = OpenAIModelsState.None,
            onEvent = { }
        )
    }
}

@Preview
@Composable
fun OpenAISectionEditPreview() {
    Surface {
        OpenAISection(
            openAISettings = OpenAISettings(
                key = "sk-test_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
                modelId = "gpt-4o-mini"
            ),
            openAIModels = OpenAIModelsState.None,
            onEvent = { },
            initialEdit = true
        )
    }
}
