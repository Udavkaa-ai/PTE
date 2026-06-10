package ru.pte.test

import android.content.Context
import org.json.JSONArray

/** Один вопрос теста. */
data class Question(
    val id: Int,
    val text: String,
    val options: List<String>,
    val correct: Int,
    val reference: String,
    val image: String?,
    val confidence: String,
)

object QuestionRepository {
    @Volatile private var cache: List<Question>? = null

    /** Загружает вопросы из assets/questions.json (однократно, с кэшированием). */
    fun load(context: Context): List<Question> {
        cache?.let { return it }
        synchronized(this) {
            cache?.let { return it }
            val json = context.assets.open("questions.json")
                .bufferedReader(Charsets.UTF_8).use { it.readText() }
            val arr = JSONArray(json)
            val list = ArrayList<Question>(arr.length())
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val optsArr = o.getJSONArray("options")
                val opts = ArrayList<String>(optsArr.length())
                for (j in 0 until optsArr.length()) opts.add(optsArr.getString(j))
                list.add(
                    Question(
                        id = o.getInt("id"),
                        text = o.getString("question"),
                        options = opts,
                        correct = o.getInt("correct"),
                        reference = o.optString("reference", ""),
                        image = if (o.has("image") && !o.isNull("image")) o.getString("image") else null,
                        confidence = o.optString("confidence", "med"),
                    )
                )
            }
            cache = list
            return list
        }
    }

    /** Уникальные вопросы (без повторов) — для режима поиска и билетов. */
    fun unique(context: Context): List<Question> {
        val seen = HashSet<String>()
        val result = ArrayList<Question>()
        for (q in load(context)) {
            val key = q.text.trim().lowercase()
            if (seen.add(key)) result.add(q)
        }
        return result
    }
}

/** Размер одного билета. */
const val TICKET_SIZE = 20

/** Генерация экзаменационных билетов из уникальных вопросов. */
object Tickets {
    /** Сколько билетов доступно (каждый по [TICKET_SIZE] вопросов). */
    fun count(context: Context): Int {
        val n = QuestionRepository.unique(context).size
        if (n == 0) return 0
        return ((n + TICKET_SIZE - 1) / TICKET_SIZE).coerceAtLeast(1)
    }

    /**
     * Вопросы билета [ticket] (нумерация с 1). Набор фиксирован (seed = номер
     * билета), вопросы внутри билета не повторяются. При нехватке уникальных
     * вопросов билет дополняется другими, но без повторов внутри билета.
     */
    fun questions(context: Context, ticket: Int): List<Question> {
        val pool = QuestionRepository.unique(context)
        if (pool.isEmpty()) return emptyList()
        val rnd = java.util.Random(ticket.toLong() * 1000003L + 17L)
        val shuffled = pool.toMutableList().apply { shuffle(rnd) }
        return shuffled.take(TICKET_SIZE.coerceAtMost(shuffled.size))
    }
}
