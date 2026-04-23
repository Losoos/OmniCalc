package cz.losoos.calculator

import kotlin.math.*

object SharedSolver {
    fun solve(input: String): Double? {
        return try {
            val normalizedInput = input.lowercase()
                .replace(",", ".")
                .replace("×", "*")
                .replace("÷", "/")
                .replace("π", "(${PI})")
                .replace("e", "(${E})")
                .replace("\\s".toRegex(), "")
            
            evaluateSimple(normalizedInput)
        } catch (e: Exception) {
            null
        }
    }

    private fun evaluateSimple(expr: String): Double {
        val prepared = expr
        return object : Any() {
            var pos = -1; var ch = 0
            fun nextChar() { ch = if (++pos < prepared.length) prepared[pos].code else -1 }
            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) { nextChar(); return true }
                return false
            }
            fun parse(): Double { nextChar(); return parseExpression() }
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x += parseTerm()
                    else if (eat('-'.code)) x -= parseTerm()
                    else return x
                }
            }
            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code)) x *= parseFactor()
                    else if (eat('/'.code)) x /= parseFactor()
                    else if (isNextFactorStart()) x *= parseFactor()
                    else return x
                }
            }
            fun isNextFactorStart(): Boolean {
                return (ch == '('.code || (ch >= '0'.code && ch <= '9'.code) || ch == '.'.code || ch == '√'.code)
            }
            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()
                var x: Double; val startPos = pos
                if (eat('('.code)) { x = parseExpression(); eat(')'.code) }
                else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) {
                    while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
                    x = prepared.substring(startPos, pos).toDouble()
                } else if (ch == '√'.code) { nextChar(); x = sqrt(parseFactor()) }
                else return 0.0
                if (eat('^'.code)) x = x.pow(parseFactor())
                return x
            }
        }.parse()
    }
}
