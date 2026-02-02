package com.example.emotionsai.ui.hr.analytics.transforms

import com.example.emotionsai.data.remote.Feedback
import kotlin.math.roundToInt

fun List<Feedback>.toEmotionPiePercent(
    emotionsOrder: List<String>
): List<Pair<String, Float>> {
    if (isEmpty()) return emotionsOrder.map { it to 0f }

    val total = size.toFloat()
    val counts = groupingBy { it.emotion.lowercase() }.eachCount()

    return emotionsOrder.map { emo ->
        val c = counts[emo.lowercase()] ?: 0
        emo to (c / total) // 0..1
    }
}

fun List<Feedback>.toDepartmentEmotionCounts(
    departments: List<Pair<Int, String>>,
    emotionsOrder: List<String>
): List<DepartmentEmotionCount> {
    val byDept = groupBy { it.department ?: -1 }
    return departments.map { (deptId, deptName) ->
        val list = byDept[deptId].orEmpty()
        val counts = list.groupingBy { it.emotion.lowercase() }.eachCount()
        val emoCounts = emotionsOrder.associateWith { e -> counts[e.lowercase()] ?: 0 }
        DepartmentEmotionCount(deptId, deptName, emoCounts)
    }
}

fun List<Feedback>.toKpis(): Kpis {
    val total = size
    if (total == 0) return Kpis(total = "0", topEmotion = "—", withEvent = "0", withoutEvent = "0")

    val counts = groupingBy { it.emotion.lowercase() }.eachCount()
    val top = counts.maxByOrNull { it.value }?.key ?: "—"

    val withEvent = count { it.event != null }
    val withoutEvent = total - withEvent

    return Kpis(
        total = total.toString(),
        topEmotion = top,
        withEvent = withEvent.toString(),
        withoutEvent = withoutEvent.toString()
    )
}

data class DepartmentEmotionCount(
    val departmentId: Int,
    val departmentName: String,
    val emotionCounts: Map<String, Int>
)

data class Kpis(
    val total: String,
    val topEmotion: String,
    val withEvent: String,
    val withoutEvent: String
)
