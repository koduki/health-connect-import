package com.example.healthconnectimport

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.lifecycleScope
import com.example.healthconnectimport.data.*
import com.example.healthconnectimport.ui.theme.HealthConnectImportTheme
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZonedDateTime

class MainActivity : ComponentActivity() {
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(this) }
    private val csvParser = CsvParser()
    private lateinit var healthDataImporter: HealthDataImporter
    
    // UI State
    private var _importResult = mutableStateOf<ImportResult?>(null)
    private var _isImporting = mutableStateOf(false)
    private var _errorMessage = mutableStateOf<String?>(null)
    
    private val writePermissions = setOf(
        HealthPermission.getWritePermission(WeightRecord::class),
        HealthPermission.getWritePermission(HeightRecord::class),
        HealthPermission.getWritePermission(BodyFatRecord::class),
        HealthPermission.getWritePermission(BoneMassRecord::class),
        HealthPermission.getWritePermission(BasalMetabolicRateRecord::class),
        HealthPermission.getWritePermission(LeanBodyMassRecord::class),
    )
    
    private val readPermissions = setOf(
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(HeightRecord::class),
        HealthPermission.getReadPermission(BodyFatRecord::class),
        HealthPermission.getReadPermission(BoneMassRecord::class),
        HealthPermission.getReadPermission(BasalMetabolicRateRecord::class),
        HealthPermission.getReadPermission(LeanBodyMassRecord::class),
    )
    
    private val allPermissions = writePermissions + readPermissions
    
    // Activity Result Launcher for file picker
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleSelectedFile(uri)
            }
        }
    }
    
    // Activity Result Launcher for Health Connect permissions
    private val permissionLauncher = registerForActivityResult(
        HealthConnectClient.permissionController.createRequestPermissionsResultContract()
    ) { granted ->
        // Handle permission result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        healthDataImporter = HealthDataImporter(healthConnectClient)
        
        setContent {
            HealthConnectImportTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CsvImportScreen(
                        modifier = Modifier.padding(innerPadding),
                        importResult = _importResult.value,
                        isImporting = _isImporting.value,
                        errorMessage = _errorMessage.value,
                        onSelectFileClick = { openFilePicker() },
                        onRequestPermissionsClick = { requestHealthPermissions() },
                        onClearError = { _errorMessage.value = null },
                        onClearResult = { _importResult.value = null }
                    )
                }
            }
        }
    }
    
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/csv", "text/plain", "*/*"))
        }
        filePickerLauncher.launch(intent)
    }
    
    private fun requestHealthPermissions() {
        permissionLauncher.launch(allPermissions)
    }
    
    private fun handleSelectedFile(uri: Uri) {
        lifecycleScope.launch {
            _isImporting.value = true
            _errorMessage.value = null
            _importResult.value = null
            
            try {
                val inputStream = contentResolver.openInputStream(uri)
                inputStream?.use { stream ->
                    val bodyCompositionData = csvParser.parseBodyCompositionCsv(stream)
                    
                    if (bodyCompositionData.isNotEmpty()) {
                        val result = healthDataImporter.importBodyCompositionData(bodyCompositionData)
                        showImportResult(result)
                    } else {
                        showError("CSVファイルに有効なデータが見つかりませんでした")
                    }
                }
            } catch (e: Exception) {
                showError("ファイル読み取りエラー: ${e.message}")
            } finally {
                _isImporting.value = false
            }
        }
    }
    
    private fun showImportResult(result: ImportResult) {
        _importResult.value = result
    }
    
    private fun showError(message: String) {
        _errorMessage.value = message
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CsvImportScreen(
    modifier: Modifier = Modifier,
    importResult: ImportResult? = null,
    isImporting: Boolean = false,
    errorMessage: String? = null,
    onSelectFileClick: () -> Unit,
    onRequestPermissionsClick: () -> Unit,
    onClearError: () -> Unit = {},
    onClearResult: () -> Unit = {}
) {
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            text = "Health Connect CSV Import",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        // Description
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "体組成データのインポート",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "対応データ: 体重、身長、体脂肪率、骨量、基礎代謝、筋肉量",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "CSVフォーマット: カンマ区切り、データがない場合は「null」",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "※ 時刻、体重、身長は必須項目です",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
        
        // Permissions Button
        Button(
            onClick = onRequestPermissionsClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Health Connect権限を許可")
        }
        
        // File Selection Button
        Button(
            onClick = onSelectFileClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isImporting
        ) {
            Icon(
                imageVector = Icons.Default.FileUpload,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("CSVファイルを選択")
        }
        
        // Loading indicator
        if (isImporting) {
            CircularProgressIndicator()
            Text("データをインポート中...")
        }
        
        // Error message
        errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // Import result
        importResult?.let { result ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = if (result.isSuccess) {
                    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                } else {
                    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = if (result.isSuccess) "インポート完了" else "インポート失敗",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("総レコード数: ${result.totalRecords}")
                    Text("成功: ${result.successfulRecords}")
                    Text("失敗: ${result.failedRecords}")
                    
                    if (result.errors.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "エラー詳細:",
                            fontWeight = FontWeight.SemiBold
                        )
                        result.errors.forEach { error ->
                            Text(
                                text = "• $error",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
        
        // Sample format
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "CSVフォーマット例",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ヘッダー行:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "time,weight,height,bmi,fatRate,bodyWaterRate,boneMass,metabolism,muscleRate,visceralFat",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "データ行（通常）:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "2024-04-29 12:12:32+0000,77.8,170.0,26.9,25.966814,50.786766,2.9319906,1564.0,54.66583,12.0",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "データ行（null値含む）:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "2024-04-29 12:21:07+0000,75.0,170.0,25.9,null,null,null,null,null,null",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CsvImportScreenPreview() {
    HealthConnectImportTheme {
        CsvImportScreen(
            onSelectFileClick = { },
            onRequestPermissionsClick = { },
            onClearError = { },
            onClearResult = { }
        )
    }
} 