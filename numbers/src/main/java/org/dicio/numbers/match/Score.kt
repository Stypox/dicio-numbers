package org.dicio.numbers.match

import java.util.Stack

open class Score(
    val userMatched: Float,
    val userWeight: Float,
    val refMatched: Float,
    val refWeight: Float,

    val data: Stack<Any>,
) {
    fun score(): Float {
        return UM * userMatched +
                UW * userWeight +
                RM * refMatched +
                RW * refWeight
    }

    companion object {
        const val UM: Float = 2.0f;
        const val UW: Float = -1.1f;
        const val RM: Float = 2.0f;
        const val RW: Float = -1.1f;
    }
}
