package ru.pte.test

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

private val GREEN = Color(0xFF0B6E4F)
private val RED = Color(0xFFB3261E)

private sealed interface Screen {
    data object Splash : Screen
    data object Home : Screen
    data object TicketList : Screen
    data class Ticket(val number: Int) : Screen
    data object History : Screen
    data object Search : Screen
}

@Composable
private fun App() {
    var screen by remember { mutableStateOf<Screen>(Screen.Splash) }
    when (val s = screen) {
        Screen.Splash -> SplashScreen(onEnter = { screen = Screen.Home })
        Screen.Home -> HomeScreen(
            onTickets = { screen = Screen.TicketList },
            onHistory = { screen = Screen.History },
            onSearch = { screen = Screen.Search },
        )
        Screen.TicketList -> TicketListScreen(
            onBack = { screen = Screen.Home },
            onOpen = { screen = Screen.Ticket(it) },
        )
        is Screen.Ticket -> TicketScreen(
            ticket = s.number,
            onBack = { screen = Screen.TicketList },
            onHome = { screen = Screen.Home },
        )
        Screen.History -> HistoryScreen(onBack = { screen = Screen.Home })
        Screen.Search -> SearchScreen(onBack = { screen = Screen.Home })
    }
}

private val BOOK_BLUE = Color(0xFF1565C0)   // ПТЭ
private val BOOK_GREEN = Color(0xFF2E7D32)  // ИСИ
private val BOOK_ORANGE = Color(0xFFEF6C00) // ИДП

@Composable
private fun SplashScreen(onEnter: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "Подготовка к аттестации",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "ПТЭ · ИСИ · ИДП",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(40.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom,
        ) {
            Book("ПТЭ", "Правила\nтехнической\nэксплуатации", BOOK_BLUE, height = 168.dp, delayMs = 0)
            Spacer(Modifier.width(14.dp))
            Book("ИСИ", "Инструкция\nпо сигнализации", BOOK_GREEN, height = 188.dp, delayMs = 140)
            Spacer(Modifier.width(14.dp))
            Book("ИДП", "Инструкция\nпо движению\nпоездов", BOOK_ORANGE, height = 150.dp, delayMs = 280)
        }
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onEnter,
            modifier = Modifier.fillMaxWidth(0.8f).height(56.dp),
        ) {
            Text("Начать подготовку", fontSize = 17.sp)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Самоподготовка по нормативам железнодорожного транспорта",
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Книга-«корешок» на стартовом экране: цветной том с тиснёной аббревиатурой. */
@Composable
private fun Book(abbr: String, title: String, color: Color, height: Dp, delayMs: Int) {
    // Появление: книга «вырастает» снизу с лёгким отскоком.
    val grow = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        grow.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .width(78.dp)
                .height(height * grow.value)
                .shadow(8.dp, RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp,
                    topEnd = 10.dp, bottomEnd = 10.dp))
                .background(color, RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp,
                    topEnd = 10.dp, bottomEnd = 10.dp)),
        ) {
            // Корешок книги — более тёмная полоса слева.
            Box(
                Modifier
                    .fillMaxHeight()
                    .width(12.dp)
                    .background(color.darker(),
                        RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)),
            )
            // Срез страниц справа.
            Box(
                Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(6.dp)
                    .padding(vertical = 6.dp)
                    .background(Color(0xFFF1ECE0)),
            )
            // Декоративная рамка тиснения по высоте обложки.
            Box(
                Modifier
                    .align(Alignment.Center)
                    .padding(start = 16.dp, end = 10.dp, top = 12.dp, bottom = 12.dp)
                    .fillMaxSize()
                    .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(4.dp)),
            )
            // Аббревиатура на обложке.
            if (grow.value > 0.7f) {
                Text(
                    abbr,
                    Modifier.align(Alignment.Center),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            title,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Затемнённый вариант цвета — для корешка книги. */
private fun Color.darker(factor: Float = 0.75f) =
    Color(red * factor, green * factor, blue * factor, alpha)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(onTickets: () -> Unit, onHistory: () -> Unit, onSearch: () -> Unit) {
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
                onClick = onTickets,
                modifier = Modifier.fillMaxWidth().height(64.dp),
            ) {
                Icon(Icons.Default.CheckCircle, null)
                Spacer(Modifier.width(10.dp))
                Text("Билеты ($TICKET_SIZE вопросов)", fontSize = 18.sp)
            }
            Spacer(Modifier.height(14.dp))
            ElevatedButton(
                onClick = onHistory,
                modifier = Modifier.fillMaxWidth().height(64.dp),
            ) {
                Icon(Icons.Default.History, null)
                Spacer(Modifier.width(10.dp))
                Text("История прохождения", fontSize = 18.sp)
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
private fun TicketListScreen(onBack: () -> Unit, onOpen: (Int) -> Unit) {
    val context = LocalContext.current
    val count = remember { Tickets.count(context) }
    // Перечитываем историю при каждом входе, чтобы обновить «лучший результат».
    val best = remember { (1..count).associateWith { History.bestPercent(context, it) } }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Билеты") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                colors = barColors(),
            )
        }
    ) { pad ->
        LazyColumn(
            Modifier.padding(pad).fillMaxSize().padding(horizontal = 16.dp),
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            items((1..count).toList()) { n ->
                val b = best[n]
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onOpen(n) },
                ) {
                    Row(
                        Modifier.padding(18.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Билет № $n",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                        )
                        if (b != null) {
                            Surface(
                                color = if (b >= 80) Color(0xFFD6F0E2) else Color(0xFFFBE6E6),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(
                                    "лучший: $b%",
                                    Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    fontSize = 13.sp,
                                    color = if (b >= 80) GREEN else RED,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        } else {
                            Text("не пройден", fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TicketScreen(ticket: Int, onBack: () -> Unit, onHome: () -> Unit) {
    val context = LocalContext.current
    val questions = remember(ticket) { Tickets.questions(context, ticket) }
    var index by remember { mutableStateOf(0) }
    var selected by remember { mutableStateOf(-1) }
    var checked by remember { mutableStateOf(false) }
    var showPopup by remember { mutableStateOf(false) }
    var correctCount by remember { mutableStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    // Завершение билета — сохраняем результат в историю один раз.
    LaunchedEffect(finished) {
        if (finished) {
            History.add(
                context,
                Attempt(ticket, correctCount, questions.size, System.currentTimeMillis()),
            )
        }
    }

    if (finished) {
        TicketResultScreen(
            ticket = ticket,
            correct = correctCount,
            total = questions.size,
            onHome = onHome,
            onRetry = {
                index = 0; selected = -1; checked = false
                showPopup = false; correctCount = 0; finished = false
            },
        )
        return
    }

    val q = questions[index]
    val isLast = index + 1 >= questions.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Билет № $ticket · ${index + 1}/${questions.size}") },
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
                    Text("Верно: $correctCount", fontWeight = FontWeight.Medium)
                    if (!checked) {
                        Button(
                            onClick = {
                                checked = true
                                if (selected == q.correct) correctCount++
                                showPopup = true
                            },
                            enabled = selected >= 0,
                        ) { Text("Проверить") }
                    } else {
                        Button(onClick = {
                            showPopup = false
                            if (isLast) finished = true
                            else { index++; selected = -1; checked = false }
                        }) { Text(if (isLast) "Завершить" else "Дальше") }
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
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showPopup) {
        ResultPopup(
            correct = selected == q.correct,
            reference = q.reference,
            correctText = q.options.getOrElse(q.correct) { "—" },
            isLast = isLast,
            onNext = {
                if (isLast) finished = true
                else { index++; selected = -1; checked = false }
                showPopup = false
            },
            onDismiss = { showPopup = false },
        )
    }
}

@Composable
private fun ResultPopup(
    correct: Boolean,
    reference: String,
    correctText: String,
    isLast: Boolean,
    onNext: () -> Unit,
    onDismiss: () -> Unit,
) {
    // Пульс иконки при появлении попапа.
    val scale = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onNext) { Text(if (isLast) "Завершить" else "Следующий вопрос") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Посмотреть") }
        },
        icon = {
            Icon(
                if (correct) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (correct) GREEN else RED,
                modifier = Modifier.size(56.dp).scale(scale.value),
            )
        },
        title = {
            Text(
                if (correct) "Правильно!" else "Неправильно",
                color = if (correct) GREEN else RED,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                if (!correct) {
                    Text("Правильный ответ:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(correctText, fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                }
                Surface(
                    color = Color(0xFFEFF4FB),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "Где искать в нормативах:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color(0xFF1C5D99),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (reference.isNotBlank()) reference else "пункт уточняется",
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun TicketResultScreen(
    ticket: Int,
    correct: Int,
    total: Int,
    onHome: () -> Unit,
    onRetry: () -> Unit,
) {
    val percent = if (total == 0) 0 else correct * 100 / total
    val passed = percent >= 80
    Column(
        Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val scale = remember { Animatable(0.5f) }
        LaunchedEffect(Unit) {
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
        Icon(
            if (passed) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (passed) GREEN else RED,
            modifier = Modifier.size(96.dp).scale(scale.value),
        )
        Spacer(Modifier.height(16.dp))
        Text("Билет № $ticket пройден", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Text(
            "$correct из $total  ·  $percent%",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = if (passed) GREEN else RED,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            if (passed) "Сдано" else "Не сдано (нужно ≥ 80%)",
            fontSize = 15.sp,
            color = if (passed) GREEN else RED,
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onRetry, Modifier.fillMaxWidth().height(54.dp)) {
            Text("Пройти заново", fontSize = 16.sp)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onHome, Modifier.fillMaxWidth().height(54.dp)) {
            Text("На главную", fontSize = 16.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var items by remember { mutableStateOf(History.load(context)) }
    val fmt = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru")) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История прохождения") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    if (items.isNotEmpty()) {
                        TextButton(onClick = {
                            History.clear(context); items = emptyList()
                        }) { Text("Очистить", color = Color.White) }
                    }
                },
                colors = barColors(),
            )
        }
    ) { pad ->
        if (items.isEmpty()) {
            Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Пока нет пройденных билетов.\nПройдите билет — результат появится здесь.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(Modifier.padding(pad).fillMaxSize().padding(horizontal = 16.dp)) {
                item { Spacer(Modifier.height(8.dp)) }
                items(items) { a ->
                    val passed = a.percent >= 80
                    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Row(
                            Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Билет № ${a.ticket}", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                Spacer(Modifier.height(2.dp))
                                Text(fmt.format(Date(a.timestamp)), fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                "${a.correct}/${a.total} · ${a.percent}%",
                                fontWeight = FontWeight.Bold,
                                color = if (passed) GREEN else RED,
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

private enum class OptState { NORMAL, SELECTED, CORRECT, WRONG }

@Composable
private fun OptionCard(text: String, state: OptState, enabled: Boolean, onClick: () -> Unit) {
    val targetBg = when (state) {
        OptState.CORRECT -> Color(0xFFD6F0E2)
        OptState.WRONG -> Color(0xFFF7D6D6)
        OptState.SELECTED -> MaterialTheme.colorScheme.primaryContainer
        OptState.NORMAL -> MaterialTheme.colorScheme.surface
    }
    val targetBorder = when (state) {
        OptState.CORRECT -> GREEN
        OptState.WRONG -> RED
        OptState.SELECTED -> MaterialTheme.colorScheme.primary
        OptState.NORMAL -> Color(0xFFD9DDDB)
    }
    // Плавная заливка цветом при проверке ответа.
    val bg by animateColorAsState(targetBg, tween(300), label = "bg")
    val border by animateColorAsState(targetBorder, tween(300), label = "border")

    // «Пульс» правильного/неправильного варианта.
    val scale = remember { Animatable(1f) }
    LaunchedEffect(state) {
        if (state == OptState.CORRECT || state == OptState.WRONG) {
            scale.snapTo(0.92f)
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale.value)
            .border(1.5.dp, border, RoundedCornerShape(12.dp))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text, Modifier.weight(1f), fontSize = 15.sp)
            AnimatedVisibility(
                visible = state == OptState.CORRECT || state == OptState.WRONG,
                enter = fadeIn() + scaleIn(),
            ) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    if (state == OptState.CORRECT) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (state == OptState.CORRECT) GREEN else RED,
                )
            }
        }
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
                        Icon(Icons.Default.CheckCircle, null, tint = GREEN)
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
    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
)
