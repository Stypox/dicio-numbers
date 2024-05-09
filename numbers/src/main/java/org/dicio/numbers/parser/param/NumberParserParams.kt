package org.dicio.numbers.parser.param

import org.dicio.numbers.parser.Parser
import org.dicio.numbers.parser.lexer.TokenStream

/**
 * TODO add documentation
 */
abstract class NumberParserParams<T> protected constructor(
    protected val parser: Parser, private val utterance: String
) {
    protected abstract fun getExtractorAtCurrentPosition(tokenStream: TokenStream): () -> T?


    val first: T?
        get() {
            val ts = parser.tokenize(utterance)
            val extractorAtCurrentPosition = getExtractorAtCurrentPosition(ts)

            while (!ts.finished()) {
                val result: T? = extractorAtCurrentPosition()
                if (result != null) {
                    // found the first match here, return it
                    return result
                }

                // nothing found here, try next position
                ts.movePositionForwardBy(1)
            }

            // nothing was found at any position
            return null
        }

    val mixedWithText: List<Any>
        get() {
            val ts = parser.tokenize(utterance)
            val extractorAtCurrentPosition = getExtractorAtCurrentPosition(ts)
            val textAndObjects: MutableList<Any> = ArrayList()
            val currentText = StringBuilder()

            while (!ts.finished()) {
                val o: Any? = extractorAtCurrentPosition()

                if (o == null) {
                    // no object here, add text and spaces of the current token to currentText instead
                    currentText.append(ts[0].value)
                    currentText.append(ts[0].spacesFollowing)
                    ts.movePositionForwardBy(1)
                } else {
                    if (currentText.isNotEmpty()) {
                        textAndObjects.add(currentText.toString()) // add the text before the object
                        currentText.setLength(0) // clear the string builder efficiently
                    }
                    textAndObjects.add(o)
                    currentText.append(ts[-1].spacesFollowing) // spaces after the object
                }
            }

            if (currentText.isNotEmpty()) {
                // add leftover text
                textAndObjects.add(currentText.toString())
            }

            return textAndObjects
        }
}
