package com.example.wastatusdownloader

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import coil.compose.AsyncImage
import java.io.InputStream

private val darkGreen = Color(0xFF14211B)
private val leaf = Color(0xFF36A765)
private data class StatusFile(val uri: Uri, val name: String, val mime: String)


class MainActivity : ComponentActivity() {
    private var selectedTree by mutableStateOf<Uri?>(null)
    private val folderPicker = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            selectedTree = uri
            getSharedPreferences("prefs", MODE_PRIVATE).edit().putString("tree", uri.toString()).apply()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        selectedTree = prefs.getString("tree", null)?.let(Uri::parse)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = leaf, background = darkGreen, surface = Color(0xFF23352C))) {
                StatusHome(
                    treeUri = selectedTree,
                    chooseFolder = { folderPicker.launch(null) }
                )
            }
        }
    }
}


@Composable
private fun StatusHome(treeUri: Uri?, chooseFolder: () -> Unit) {
    val context = LocalContext.current
    var files by remember { mutableStateOf<List<StatusFile>>(emptyList()) }
    var message by remember { mutableStateOf("") }
    var filterVideo by remember { mutableStateOf(false) }

    fun refresh() {
        val uri = treeUri
        if (uri == null) { files = emptyList(); return }
        val root = DocumentFile.fromTreeUri(context, uri)
        files = root?.listFiles()?.filter { it.isFile && (it.type?.startsWith("image/") == true || it.type?.startsWith("video/") == true) }
            ?.map { StatusFile(it.uri, it.name ?: "Status", it.type ?: "application/octet-stream") }
            ?.sortedByDescending { it.name } ?: emptyList()
    }
    LaunchedEffect(treeUri) { refresh() }
    val visible = files.filter { if (filterVideo) it.mime.startsWith("video/") else it.mime.startsWith("image/") }

    Column(Modifier.fillMaxSize().background(darkGreen).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("WA Status Downloader", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Simpan status dengan sekali sentuh", color = Color(0xFFB7C9BD))
            }
            TextButton(onClick = { refresh(); message = "Daftar status diperbarui" }) { Text("↻ Refresh", color = Color(0xFF9CE6B0)) }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = chooseFolder, colors = ButtonDefaults.buttonColors(containerColor = leaf), modifier = Modifier.fillMaxWidth()) {
            Text(if (treeUri == null) "Pilih folder .Statuses WhatsApp" else "Ganti folder sumber Status", color = Color.White)
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !filterVideo, onClick = { filterVideo = false }, label = { Text("Foto") })
            FilterChip(selected = filterVideo, onClick = { filterVideo = true }, label = { Text("Video") })
        }
        Text("${visible.size} file", color = Color(0xFFB7C9BD), modifier = Modifier.padding(vertical = 8.dp))
        if (treeUri == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Mulai dengan memilih folder Status WhatsApp.\nArahkan ke folder .Statuses lalu tekan 'Gunakan folder ini'.", color = Color.White)
            }
        } else if (visible.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada ${if (filterVideo) "video" else "foto"} status.\nLihat status di WhatsApp lalu tekan Refresh.", color = Color.White)
            }
        } else {
            LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(visible, key = { it.uri.toString() }) { file ->
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF26382E))) {
                        Column(Modifier.padding(8.dp)) {
                            AsyncImage(model = file.uri, contentDescription = file.name, modifier = Modifier.fillMaxWidth().height(160.dp))
                            Text(file.name, color = Color.White, maxLines = 1, modifier = Modifier.padding(top = 6.dp))
                            Button(onClick = {
                                val saved = copyToGallery(context, file)
                                message = if (saved) "Berhasil disimpan ke Pictures/WA Status Downloader" else "Gagal menyimpan file"
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }, colors = ButtonDefaults.buttonColors(containerColor = leaf), modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                                Text("⬇ UNDUH", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        if (message.isNotBlank()) Text(message, color = Color(0xFF9CE6B0), modifier = Modifier.padding(top = 4.dp))
    }
}


private fun copyToGallery(context: android.content.Context, item: StatusFile): Boolean {
    val resolver = context.contentResolver
    val isVideo = item.mime.startsWith("video/")
    val collection = if (isVideo) MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) else MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    val ext = item.name.substringAfterLast('.', if (isVideo) "mp4" else "jpg").let { ".$it" }
    val safeName = item.name.substringBeforeLast('.', item.name).replace(Regex("[^A-Za-z0-9._-]"), "_") + ext
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "WA_${System.currentTimeMillis()}_$safeName")
        put(MediaStore.MediaColumns.MIME_TYPE, item.mime)
        put(MediaStore.MediaColumns.RELATIVE_PATH, if (isVideo) "Movies/WA Status Downloader" else "Pictures/WA Status Downloader")
    }
    val destination = resolver.insert(collection, values) ?: return false
    return try {
        resolver.openInputStream(item.uri).use { input: InputStream? ->
            if (input == null) throw IllegalStateException("No input")
            resolver.openOutputStream(destination).use { output ->
                if (output == null) throw IllegalStateException("No output")
                input.copyTo(output)
            }
        }
        true
    } catch (e: Exception) {
        resolver.delete(destination, null, null)
        false
    }
}
