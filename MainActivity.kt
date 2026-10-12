package com.maddy.studio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.io.OutputStreamWriter
import java.util.concurrent.Executors

private val Ink = Color(0xFF100D18)
private val Panel = Color(0xFF1B1626)
private val Violet = Color(0xFF9D75F5)
private val Muted = Color(0xFFBDB2D0)
private val executor = Executors.newSingleThreadExecutor()

data class Draft(
    val platform: String,
    val kind: String,
    val text: String,
    val approved: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaddyApp() }
    }
}

@Composable
fun MaddyApp() {
    var idea by remember { mutableStateOf("") }
    var tone by remember { mutableStateOf("Emotivo y motivador") }
    var formats by remember { mutableStateOf("Todos los formatos") }
    val platforms = remember { mutableStateListOf("Facebook", "Instagram", "TikTok", "YouTube") }
    val drafts = remember { mutableStateListOf<Draft>() }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Listo para crear borradores.") }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Violet, background = Ink, surface = Panel,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Column(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0xFF241536), Ink))
            ).padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).background(
                        Brush.linearGradient(listOf(Color(0xFFC084FC), Color(0xFF7C3AED))),
                        RoundedCornerShape(15.dp)
                    ),
                    contentAlignment = Alignment.Center
                ) { Text("M", fontSize = 25.sp, fontWeight = FontWeight.Black) }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("MADDY", fontSize = 25.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                    Text("ESTUDIO DE CONTENIDO IA", color = Muted, fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(18.dp))
            Text("Una idea. Todo tu contenido.", fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Text("Crea, revisa y aprueba antes de publicar.", color = Muted)
            Spacer(Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                        Column(Modifier.padding(15.dp)) {
                            Text("TU IDEA", fontWeight = FontWeight.Bold, color = Color(0xFFD8B4FE))
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = idea, onValueChange = { idea = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Escribe un párrafo o una idea…") },
                                minLines = 4,
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                            )
                            Spacer(Modifier.height(10.dp))
                            Text("REDES", fontWeight = FontWeight.Bold, color = Color(0xFFD8B4FE))
                            listOf("Facebook", "Instagram", "TikTok", "YouTube").forEach { p ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = p in platforms, onCheckedChange = {
                                        if (it) { if (p !in platforms) platforms.add(p) } else platforms.remove(p)
                                    })
                                    Text(p)
                                }
                            }
                            Text("ESTILO", fontWeight = FontWeight.Bold, color = Color(0xFFD8B4FE))
                            var toneExpanded by remember { mutableStateOf(false) }
                            Box {
                                OutlinedButton(onClick = { toneExpanded = true }) { Text(tone) }
                                DropdownMenu(expanded = toneExpanded, onDismissRequest = { toneExpanded = false }) {
                                    listOf("Emotivo y motivador", "Suspenso con giro final", "Inspirador y realista", "Reflexivo y espiritual").forEach {
                                        DropdownMenuItem(text = { Text(it) }, onClick = { tone = it; toneExpanded = false })
                                    }
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            var formatExpanded by remember { mutableStateOf(false) }
                            Box {
                                OutlinedButton(onClick = { formatExpanded = true }) { Text(formats) }
                                DropdownMenu(expanded = formatExpanded, onDismissRequest = { formatExpanded = false }) {
                                    listOf("Todos los formatos", "Historia + publicación", "Guion de video").forEach {
                                        DropdownMenuItem(text = { Text(it) }, onClick = { formats = it; formatExpanded = false })
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (idea.isBlank()) { message = "Escribe una idea primero."; return@Button }
                                    if (platforms.isEmpty()) { message = "Selecciona al menos una red."; return@Button }
                                    loading = true
                                    message = "Generando con IA…"
                                    val reqIdea = idea
                                    val reqPlatforms = platforms.toList()
                                    val reqTone = tone
                                    val reqFormats = formats
                                    executor.execute {
                                        try {
                                            val generated = generateDrafts(reqIdea, reqPlatforms, reqTone, reqFormats)
                                            android.os.Handler(mainLooper).post {
                                                drafts.clear(); drafts.addAll(generated)
                                                message = "Se generaron ${generated.size} borradores. Ninguno se publica sin aprobación."
                                                loading = false
                                            }
                                        } catch (e: Exception) {
                                            android.os.Handler(mainLooper).post {
                                                message = "No se pudo conectar con la IA. Configura la URL de backend y revisa la conexión."
                                                loading = false
                                            }
                                        }
                                    }
                                },
                                enabled = !loading,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(if (loading) "Generando…" else "✦ Generar borradores") }
                            Spacer(Modifier.height(7.dp))
                            Text(message, color = Muted, fontSize = 12.sp)
                        }
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("SALA DE APROBACIÓN", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Spacer(Modifier.weight(1f))
                        Text("${drafts.size} piezas", color = Muted, fontSize = 12.sp)
                    }
                }
                if (drafts.isEmpty()) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                            Text("Tus borradores aparecerán aquí. La publicación automática todavía no está conectada.", Modifier.padding(18.dp), color = Muted)
                        }
                    }
                }
                itemsIndexed(drafts) { index, d ->
                    Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                        Column(Modifier.padding(14.dp)) {
                            Text("${d.kind} · ${d.platform}", fontWeight = FontWeight.Bold, color = Color(0xFFD8B4FE))
                            Spacer(Modifier.height(6.dp))
                            var edited by remember(d.text) { mutableStateOf(d.text) }
                            OutlinedTextField(value = edited, onValueChange = {
                                edited = it
                                drafts[index] = drafts[index].copy(text = it)
                            }, modifier = Modifier.fillMaxWidth(), minLines = 4)
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (d.approved) "APROBADO (LOCAL)" else "PENDIENTE", color = if (d.approved) Color(0xFF86EFAC) else Color(0xFFFCD34D), fontSize = 11.sp)
                                Spacer(Modifier.weight(1f))
                                Button(onClick = { drafts[index] = drafts[index].copy(approved = true) }, enabled = !d.approved) {
                                    Text(if (d.approved) "Aprobado" else "Aprobar")
                                }
                            }
                            if (d.approved) Text("Aprobación guardada solo en esta sesión; no se ha publicado.", color = Muted, fontSize = 11.sp)
                        }
                    }
                }
            }
            Text("MADDY Starter · Se requiere backend HTTPS. No introduzcas claves API en la app.", color = Muted, fontSize = 10.sp)
        }
    }
}

private fun generateDrafts(idea: String, platforms: List<String>, tone: String, formats: String): List<Draft> {
    val endpoint = BuildConfig.MADDY_API_URL
    require(!endpoint.contains("YOUR_PROJECT")) { "Backend no configurado" }
    val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        connectTimeout = 20000
        readTimeout = 90000
        doOutput = true
        setRequestProperty("Content-Type", "application/json; charset=utf-8")
    }
    val body = JSONObject().apply {
        put("idea", idea); put("platforms", JSONArray(platforms)); put("tone", tone); put("formats", formats)
    }.toString()
    conn.outputStream.use { os -> OutputStreamWriter(os, Charsets.UTF_8).use { it.write(body) } }
    val code = conn.responseCode
    val stream = if (code in 200..299) conn.inputStream else conn.errorStream
    val response = stream.bufferedReader().use { it.readText() }
    if (code !in 200..299) throw IllegalStateException("Backend HTTP $code: $response")
    val arr = JSONObject(response).getJSONArray("drafts")
    return (0 until arr.length()).map { i ->
        val obj = arr.getJSONObject(i)
        Draft(obj.optString("platform"), obj.optString("kind"), obj.optString("text"))
    }
}
