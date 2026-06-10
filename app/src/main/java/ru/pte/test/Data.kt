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

    /** Уникальные вопросы (без повторов) — для режима поиска. */
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
