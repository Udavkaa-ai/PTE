package ru.pte.test

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Одна запись истории прохождения билета. */
data class Attempt(
    val ticket: Int,
    val correct: Int,
    val total: Int,
    val timestamp: Long,
) {
    val percent: Int get() = if (total == 0) 0 else correct * 100 / total
}

/** История прохождений, хранится локально в SharedPreferences. */
object History {
    private const val PREFS = "pte_prefs"
    private const val KEY = "history"

    fun load(context: Context): List<Attempt> {
        val raw = prefs(context).getString(KEY, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            val list = ArrayList<Attempt>(arr.length())
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    Attempt(
                        ticket = o.getInt("ticket"),
                        correct = o.getInt("correct"),
                        total = o.getInt("total"),
                        timestamp = o.getLong("ts"),
                    )
                )
            }
            // Свежие — сверху.
            list.sortedByDescending { it.timestamp }
        }.getOrDefault(emptyList())
    }

    fun add(context: Context, attempt: Attempt) {
        val current = load(context).toMutableList()
        current.add(0, attempt)
        val arr = JSONArray()
        for (a in current.take(200)) {
            arr.put(
                JSONObject()
                    .put("ticket", a.ticket)
                    .put("correct", a.correct)
                    .put("total", a.total)
                    .put("ts", a.timestamp)
            )
        }
        prefs(context).edit().putString(KEY, arr.toString()).apply()
    }

    fun clear(context: Context) {
        prefs(context).edit().remove(KEY).apply()
    }

    /** Лучший результат (% правильных) по конкретному билету, или null. */
    fun bestPercent(context: Context, ticket: Int): Int? =
        load(context).filter { it.ticket == ticket }.maxOfOrNull { it.percent }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
