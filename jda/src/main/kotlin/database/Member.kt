@file:Suppress("ArrayInDataClass")
package database

import java.lang.Integer.max
import java.text.NumberFormat
import java.util.*

data class Member(
    val _id: Long,
    val guilds: List<Server>
)

data class Server(
    val id: Long,
    val level: Int,
    val xp: Int
) {
    private val formatter = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT)

    fun toSummary(): String {
        return "$level (${formatter.format(xp)} xp)"
    }

    fun toProgressBar(length: Int): String {
        if (xp == 0)
            return  "⬛".repeat(length) + " 0/100"

        val nextCap = 5 * level*level + 50*level + 100
        val nextTotal = 5.0/6 * (level+1) * (2*(level+1)*(level+1) + 27*(level+1) + 91)

        val completed = nextCap - nextTotal + xp
        val progress = (completed/nextCap)*length

        val intProgress = max(1, progress.toInt())
        val strCompleted = " ${formatter.format(completed)}/${formatter.format(nextCap)}"

        return "\uD83D\uDFE9".repeat(intProgress) + "⬛".repeat(length-intProgress) + strCompleted
    }
}