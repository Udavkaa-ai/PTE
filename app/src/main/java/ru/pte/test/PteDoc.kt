package ru.pte.test

import android.content.Context
import org.json.JSONArray

/** Абзац нормативного документа. */
data class DocBlock(
    val part: String,      // ПТЭ / ИСИ / ИДП / ПРИЛОЖЕНИЯ / ПРЕАМБУЛА
    val clause: Int?,      // номер пункта, если абзац начинает пункт
    val text: String,
)

/** Куда открыть читалку: индекс целевого абзаца и конец подсветки пункта. */
data class DocTarget(val index: Int, val highlightEnd: Int, val label: String)

/** Полный текст ПТЭ (с приложениями ИСИ и ИДП) + резолвер ссылки на пункт. */
object PteDoc {
    @Volatile private var cache: List<DocBlock>? = null
    // (часть, номер пункта) -> индекс абзаца
    private var clauseIndex: Map<String, Int> = emptyMap()

    fun load(context: Context): List<DocBlock> {
        cache?.let { return it }
        synchronized(this) {
            cache?.let { return it }
            val json = context.assets.open("pte_doc.json")
                .bufferedReader(Charsets.UTF_8).use { it.readText() }
            val arr = JSONArray(json)
            val list = ArrayList<DocBlock>(arr.length())
            val idx = HashMap<String, Int>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val part = o.getString("p")
                val clause = if (o.isNull("c")) null else o.getInt("c")
                list.add(DocBlock(part, clause, o.getString("t")))
                if (clause != null) idx.putIfAbsent("$part#$clause", i)
            }
            clauseIndex = idx
            cache = list
            return list
        }
    }

    /** Определяет, к какому документу относится ссылка. */
    private fun partForRef(ref: String): String {
        val r = ref.lowercase()
        return when {
            r.contains("иси") || r.contains("прил. № 1") || r.contains("приложение № 1") -> "ИСИ"
            r.contains("идп") || r.contains("прил. № 2") || r.contains("приложение № 2") -> "ИДП"
            else -> "ПТЭ"
        }
    }

    private val clauseRegex = Regex("п\\.?\\s*(\\d+)")

    private fun normalize(s: String) = s.lowercase().replace('ё', 'е')

    /**
     * Находит место в документе для вопроса: сначала по явному номеру пункта из
     * ссылки, иначе — контекстным поиском по тексту вопроса и правильного ответа.
     */
    fun resolve(context: Context, q: Question): DocTarget {
        val blocks = load(context)
        val part = partForRef(q.reference)
        val partName = when (part) {
            "ИСИ" -> "ИСИ (прил. № 1)"
            "ИДП" -> "ИДП (прил. № 2)"
            else -> "ПТЭ"
        }

        // 1) Явный номер пункта в ссылке.
        clauseRegex.find(q.reference)?.let { m ->
            val n = m.groupValues[1].toIntOrNull()
            val hit = n?.let { clauseIndex["$part#$it"] }
            if (hit != null) {
                return DocTarget(hit, highlightEnd(blocks, hit), "$partName, п. $n")
            }
        }

        // 2) Контекстный поиск внутри нужной части документа.
        val correct = q.options.getOrElse(q.correct) { "" }
        val tokens = normalize("${q.text} $correct")
            .split(Regex("[^\\p{L}\\p{N}]+"))
            .filter { it.length >= 4 }
            .distinct()
        var bestIdx = -1
        var bestScore = 0
        blocks.forEachIndexed { i, b ->
            if (b.part != part) return@forEachIndexed
            if (b.text.length < 25) return@forEachIndexed
            val hay = normalize(b.text)
            var score = 0
            for (t in tokens) if (hay.contains(t)) score++
            // Небольшой бонус абзацу, начинающему пункт.
            if (b.clause != null) score += 1
            if (score > bestScore) { bestScore = score; bestIdx = i }
        }
        if (bestIdx >= 0) {
            // Привязываемся к началу пункта (с номером), а не к середине абзаца —
            // чтобы подсветка всегда была у заголовка пункта.
            val startIdx = clauseStart(blocks, bestIdx, part)
            val cl = blocks[startIdx].clause
            val label = if (cl != null) "$partName, п. $cl" else partName
            return DocTarget(startIdx, highlightEnd(blocks, startIdx), label)
        }

        // 3) Фолбэк — начало нужной части.
        val start = blocks.indexOfFirst { it.part == part }.coerceAtLeast(0)
        return DocTarget(start, start + 1, partName)
    }

    /** Возвращает индекс начала пункта (с номером), к которому относится абзац. */
    private fun clauseStart(blocks: List<DocBlock>, idx: Int, part: String): Int {
        var i = idx
        while (i > 0 && blocks[i].clause == null && blocks[i].part == part) i--
        return if (blocks[i].part == part && blocks[i].clause != null) i else idx
    }

    /** Конец подсветки — до следующего пункта в той же части. */
    private fun highlightEnd(blocks: List<DocBlock>, start: Int): Int {
        val part = blocks[start].part
        var i = start + 1
        while (i < blocks.size && blocks[i].part == part && blocks[i].clause == null) i++
        return i
    }
}
