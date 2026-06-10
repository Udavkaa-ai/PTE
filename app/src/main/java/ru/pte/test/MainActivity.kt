package ru.pte.test

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = pteColors()) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    App()
                }
            }
        }
    }
}

private fun pteColors() = lightColorScheme(
    primary = Color(0xFF0B6E4F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEFE0),
    secondary = Color(0xFF1C5D99),
    background = Color(0xFFF6F8F7),
    surface = Color.White,
)

private sealed interface Screen {
    data object Home : Screen
    data object Test : Screen
    data object Search : Screen
}

@Composable
private fun App() {
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    when (screen) {
        Screen.Home -> HomeScreen(
            onTest = { screen = Screen.Test },
            onSearch = { screen = Screen.Search },
        )
        Screen.Test -> TestScreen(onBack = { screen = Screen.Home })
        Screen.Search -> SearchScreen(onBack = { screen = Screen.Home })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(onTest: () -> Unit, onSearch: () -> Unit) {
    val context = LocalContext.current
    val total = remember { QuestionRepository.load(context).size }
    val uniq = remember { QuestionRepository.unique(context).size }
    Scaffold(
        topBar = { TopAppBar(title = { Text("ПТЭ Тест") }, colors = barColors()) }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(20.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Аттестация по Правилам технической эксплуатации железных дорог РФ",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))
            ElevatedButton(
                onClick = onTest,
                modifier = Modifier.fillMaxWidth().height(64.dp),
            ) {
                Icon(Icons.Default.CheckCircle, null)
                Spacer(Modifier.width(10.dp))
                Text("Режим теста", fontSize = 18.sp)
            }
            Spacer(Modifier.height(14.dp))
            ElevatedButton(
                onClick = onSearch,
                modifier = Modifier.fillMaxWidth().height(64.dp),
            ) {
                Icon(Icons.Default.Search, null)
                Spacer(Modifier.width(10.dp))
                Text("Поиск ответа", fontSize = 18.sp)
            }
            Spacer(Modifier.weight(1f))
            Text(
                "Вопросов в базе: $total  •  уникальных: $uniq",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Ответы и ссылки на пункты ПТЭ/ИСИ/ИДП приведены для самоподготовки. " +
                    "Сверяйтесь с действующей редакцией (приказ Минтранса России № 250).",
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TestScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val questions = remember { QuestionRepository.load(context).shuffled() }
    var index by remember { mutableStateOf(0) }
    var selected by remember { mutableStateOf(-1) }
    var checked by remember { mutableStateOf(false) }
    var correctCount by remember { mutableStateOf(0) }
    var answeredCount by remember { mutableStateOf(0) }

    val q = questions[index]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Вопрос ${index + 1} из ${questions.size}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                colors = barColors(),
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Верно: $correctCount из $answeredCount",
                        fontWeight = FontWeight.Medium,
                    )
                    if (!checked) {
                        Button(
                            onClick = {
                                checked = true
                                answeredCount++
                                if (selected == q.correct) correctCount++
                            },
                            enabled = selected >= 0,
                        ) { Text("Проверить") }
                    } else {
                        Button(
                            onClick = {
                                index = (index + 1) % questions.size
                                selected = -1
                                checked = false
                            }
                        ) { Text(if (index + 1 < questions.size) "Дальше" else "Сначала") }
                    }
                }
            }
        },
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            LinearProgressIndicator(
                progress = { (index + 1f) / questions.size },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            Text(q.text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            q.image?.let { AssetImage(it) }
            Spacer(Modifier.height(16.dp))

            q.options.forEachIndexed { i, opt ->
                val state = when {
                    !checked && selected == i -> OptState.SELECTED
                    checked && i == q.correct -> OptState.CORRECT
                    checked && i == selected -> OptState.WRONG
                    else -> OptState.NORMAL
                }
                OptionCard(
                    text = opt,
                    state = state,
                    enabled = !checked,
                    onClick = { selected = i },
                )
                Spacer(Modifier.height(10.dp))
            }

            if (checked) {
                Spacer(Modifier.height(6.dp))
                val ok = selected == q.correct
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (ok) Color(0xFFE3F4EC) else Color(0xFFFBE6E6)
                    )
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            if (ok) "✓ Правильно" else "✗ Неправильно",
                            color = if (ok) Color(0xFF0B6E4F) else Color(0xFFB3261E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                        if (q.reference.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text("Основание: ${q.reference}", fontSize = 14.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private enum class OptState { NORMAL, SELECTED, CORRECT, WRONG }

@Composable
private fun OptionCard(text: String, state: OptState, enabled: Boolean, onClick: () -> Unit) {
    val bg = when (state) {
        OptState.CORRECT -> Color(0xFFD6F0E2)
        OptState.WRONG -> Color(0xFFF7D6D6)
        OptState.SELECTED -> MaterialTheme.colorScheme.primaryContainer
        OptState.NORMAL -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when (state) {
        OptState.CORRECT -> Color(0xFF0B6E4F)
        OptState.WRONG -> Color(0xFFB3261E)
        OptState.SELECTED -> MaterialTheme.colorScheme.primary
        OptState.NORMAL -> Color(0xFFD9DDDB)
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Text(
            text,
            Modifier.padding(14.dp),
            fontSize = 15.sp,
        )
    }
}

/** Запись поискового индекса: вопрос + нормализованные поля для поиска. */
private class SearchEntry(
    val q: Question,
    val title: String,
    val answers: String,
)

/** Приводит строку к нижнему регистру и унифицирует «ё» → «е» для поиска. */
private fun normalizeForSearch(s: String): String =
    s.lowercase().replace('ё', 'е')

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val all = remember { QuestionRepository.unique(context) }
    // Предварительно строим поисковый индекс: нормализованный текст вопроса и
    // отдельно весь текст вариантов ответа — чтобы искать контекстно по всему.
    val index = remember(all) {
        all.map { q ->
            SearchEntry(
                q = q,
                title = normalizeForSearch(q.text),
                answers = normalizeForSearch(q.options.joinToString(" ")),
            )
        }
    }
    var query by remember { mutableStateOf("") }
    var openQuestion by remember { mutableStateOf<Question?>(null) }

    val results = remember(query, index) {
        val raw = normalizeForSearch(query.trim())
        if (raw.length < 2) emptyList()
        else {
            // Контекстный поиск: каждое слово запроса должно встретиться где-то
            // в тексте вопроса или в любом из вариантов ответа (порядок не важен).
            val tokens = raw.split(Regex("[^\\p{L}\\p{N}]+")).filter { it.length >= 2 }
            if (tokens.isEmpty()) emptyList()
            else index
                .mapNotNull { e ->
                    var score = 0
                    var allMatched = true
                    for (t in tokens) {
                        val inTitle = e.title.contains(t)
                        val inAnswers = e.answers.contains(t)
                        if (!inTitle && !inAnswers) { allMatched = false; break }
                        if (inTitle) score += 3
                        if (inAnswers) score += 1
                    }
                    if (!allMatched) return@mapNotNull null
                    // Бонусы за точные совпадения целой фразы.
                    if (e.title.contains(raw)) score += 8
                    else if (e.answers.contains(raw)) score += 4
                    if (e.title.startsWith(raw)) score += 4
                    e.q to score
                }
                .sortedByDescending { it.second }
                .map { it.first }
                .take(60)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Поиск ответа") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                colors = barColors(),
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Введите вопрос или ключевые слова") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            )
            Spacer(Modifier.height(12.dp))
            if (query.trim().length < 2) {
                Text(
                    "Введите любые слова из вопроса или ответа — порядок не важен. " +
                        "Поиск идёт по всему тексту вопросов и вариантов ответов.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                )
            } else if (results.isEmpty()) {
                Text("Ничего не найдено.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn {
                    items(results) { q ->
                        Card(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable { openQuestion = q },
                        ) {
                            Text(q.text, Modifier.padding(14.dp), fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }

    openQuestion?.let { q ->
        AnswerDialog(q) { openQuestion = null }
    }
}

@Composable
private fun AnswerDialog(q: Question, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } },
        title = { Text("Ответ", fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(q.text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                q.image?.let { AssetImage(it) }
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = Color(0xFFD6F0E2),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF0B6E4F))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            q.options.getOrElse(q.correct) { "—" },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                if (q.reference.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text("Основание: ${q.reference}", fontSize = 13.sp)
                }
            }
        },
    )
}

@Composable
private fun AssetImage(name: String) {
    val context = LocalContext.current
    val bmp = remember(name) {
        runCatching {
            // Images are stored as base64 text (so they travel as plain text); decode at runtime.
            val b64 = context.assets.open("images/$name.b64")
                .bufferedReader(Charsets.US_ASCII).use { it.readText() }
            val bytes = android.util.Base64.decode(b64, android.util.Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull()
    }
    bmp?.let {
        Spacer(Modifier.height(12.dp))
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, Color(0xFFD9DDDB), RoundedCornerShape(8.dp))
                .padding(4.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun barColors() = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.primary,
    titleContentColor = MaterialTheme.colorScheme.onPrimary,
    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
)
