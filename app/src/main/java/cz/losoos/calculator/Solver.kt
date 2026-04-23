/*
 * Copyright 2024 Losoos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cz.losoos.calculator

import android.content.Context
import java.util.Locale

object Solver {
    fun solve(input: String, context: Context): String {
        val locale = context.resources.configuration.locales[0]
        
        return try {
            var normalizedInput = input.lowercase(Locale.ROOT)
                .replace(",", ".")
                .replace("×", "*")
                .replace("÷", "/")
                .replace("π", "(${Math.PI})")
                .replace("e", "(${Math.E})")
                .replace("\\s".toRegex(), "")

            // Převod všech možných číslic (Arabské, Indické atd.) na 0-9
            normalizedInput = normalizeDigits(normalizedInput)

            val equations = normalizedInput.split(";").filter { it.isNotEmpty() }
            val allVars = normalizedInput.filter { it in 'a'..'z' }
                .map { it.toString() }
                .distinct()
                .sorted()

            if (!normalizedInput.contains("=")) {
                return formatResult(evaluateSimple(normalizedInput), locale, context)
            }

            if (allVars.isEmpty()) {
                val sides = equations[0].split("=")
                if (sides.size < 2) return formatResult(evaluateSimple(sides[0]), locale, context)
                val left = evaluateSimple(sides[0])
                val right = evaluateSimple(sides[1])
                return if (Math.abs(left - right) < 1e-10) context.getString(R.string.truth) else context.getString(R.string.falsehood)
            }

            val n = allVars.size
            val m = equations.size
            val matrix = Array(m) { DoubleArray(n + 1) }

            for (i in 0 until m) {
                val sides = equations[i].split("=")
                if (sides.size != 2) return context.getString(R.string.error_msg)
                val left = sides[0]
                val right = sides[1]

                for (j in 0 until n) {
                    matrix[i][j] = (evaluateWithVars(left, allVars, j, 1.0) - evaluateWithVars(left, allVars, j, 0.0)) -
                                   (evaluateWithVars(right, allVars, j, 1.0) - evaluateWithVars(right, allVars, j, 0.0))
                }
                matrix[i][n] = evaluateWithVars(right, allVars, -1, 0.0) - evaluateWithVars(left, allVars, -1, 0.0)
            }

            toRREF(matrix)

            for (i in 0 until m) {
                var allZeros = true
                for (j in 0 until n) if (Math.abs(matrix[i][j]) > 1e-10) allZeros = false
                if (allZeros && Math.abs(matrix[i][n]) > 1e-10) return context.getString(R.string.error_msg)
            }

            val results = mutableListOf<String>()
            var allResultsAreZero = true

            for (targetVarIdx in 0 until n) {
                var bestRow = -1
                for (i in 0 until m) {
                    if (Math.abs(matrix[i][targetVarIdx]) > 1e-10) {
                        var isPivot = true
                        for (k in 0 until targetVarIdx) if (Math.abs(matrix[i][k]) > 1e-10) isPivot = false
                        if (isPivot) { bestRow = i; break }
                        if (bestRow == -1) bestRow = i
                    }
                }

                if (bestRow != -1) {
                    val divisor = matrix[bestRow][targetVarIdx]
                    val constant = matrix[bestRow][n] / divisor
                    val hasOtherVars = (0 until n).any { k -> k != targetVarIdx && Math.abs(matrix[bestRow][k]) > 1e-10 }
                    if (Math.abs(constant) > 1e-10 || hasOtherVars) {
                        allResultsAreZero = false
                    }

                    val sb = StringBuilder("${allVars[targetVarIdx]} = ")
                    var hasTerms = false
                    if (Math.abs(constant) > 1e-10 || !hasOtherVars) {
                        sb.append(formatResult(constant, locale, context))
                        hasTerms = true
                    }
                    for (k in 0 until n) {
                        if (k == targetVarIdx) continue
                        val coeff = -matrix[bestRow][k] / divisor
                        if (Math.abs(coeff) > 1e-10) {
                            val sign = if (coeff > 0) (if (hasTerms) " + " else "") else (if (hasTerms) " - " else "-")
                            val absCoeff = Math.abs(coeff)
                            val coeffStr = if (Math.abs(absCoeff - 1.0) < 1e-10) "" else formatResult(absCoeff, locale, context)
                            sb.append("$sign$coeffStr${allVars[k]}")
                            hasTerms = true
                        }
                    }
                    results.add(sb.toString())
                } else {
                    results.add("${allVars[targetVarIdx]} = ${context.getString(R.string.any_value)}")
                    allResultsAreZero = false
                }
            }

            if (allResultsAreZero && results.isNotEmpty()) return context.getString(R.string.error_msg)
            if (results.isEmpty()) return context.getString(R.string.error_msg)
            results.joinToString(", ")
        } catch (e: Exception) {
            context.getString(R.string.error_msg)
        }
    }

    private fun normalizeDigits(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            val digit = Character.getNumericValue(ch)
            if (digit in 0..9 && !ch.isLetter()) {
                sb.append(digit)
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    private fun formatResult(v: Double, locale: Locale, context: Context): String {
        if (v.isNaN() || v.isInfinite()) return context.getString(R.string.error_msg)
        val formatted = if (Math.abs(v - Math.round(v)) < 1e-9) Math.round(v).toString()
                        else "%.4f".format(Locale.US, v).trimEnd('0').trimEnd('.')
        
        // Lokalizace oddělovače (čárka vs tečka) a číslic
        var result = if (locale.language in listOf("cs", "sk", "de", "es", "fr", "ru", "pl", "it", "pt")) {
            formatted.replace(".", ",")
        } else {
            formatted
        }

        // Lokalizace číslic pro jazyky jako arabština nebo hindština
        if (locale.language == "ar" || locale.language == "hi") {
            val sb = StringBuilder()
            val offset = if (locale.language == "ar") 0x0660 else 0x0966
            for (ch in result) {
                if (ch in '0'..'9') {
                    sb.append((ch.code - '0'.code + offset).toChar())
                } else {
                    sb.append(ch)
                }
            }
            result = sb.toString()
        }

        return result
    }

    private fun toRREF(matrix: Array<DoubleArray>) {
        val rowCount = matrix.size; val colCount = matrix[0].size; var pivotRow = 0
        for (pivotCol in 0 until colCount - 1) {
            if (pivotRow >= rowCount) break
            var i = pivotRow
            while (i < rowCount && Math.abs(matrix[i][pivotCol]) < 1e-10) i++
            if (i == rowCount) continue
            val temp = matrix[i]; matrix[i] = matrix[pivotRow]; matrix[pivotRow] = temp
            val divisor = matrix[pivotRow][pivotCol]
            for (j in pivotCol until colCount) matrix[pivotRow][j] /= divisor
            for (r in 0 until rowCount) {
                if (r != pivotRow) {
                    val factor = matrix[r][pivotCol]
                    for (j in pivotCol until colCount) matrix[r][j] -= factor * matrix[pivotRow][j]
                }
            }
            pivotRow++
        }
    }

    private fun evaluateWithVars(expr: String, vars: List<String>, activeVarIdx: Int, activeValue: Double): Double {
        val varMap = mutableMapOf<String, Double>()
        for (i in vars.indices) varMap[vars[i]] = if (i == activeVarIdx) activeValue else 0.0
        return evaluateSimple(expr, varMap)
    }

    private fun evaluateSimple(expr: String, varValues: Map<String, Double> = emptyMap()): Double {
        val prepared = expr.replace("π", Math.PI.toString()).replace("e", Math.E.toString())
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
                return (ch == '('.code || (ch >= '0'.code && ch <= '9'.code) || ch == '.'.code || (ch >= 'a'.code && ch <= 'z'.code) || ch == '√'.code)
            }
            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()
                var x: Double; val startPos = pos
                if (eat('('.code)) { x = parseExpression(); eat(')'.code) }
                else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) {
                    while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
                    x = prepared.substring(startPos, pos).toDouble()
                } else if (ch >= 'a'.code && ch <= 'z'.code) {
                    while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                    x = varValues[prepared.substring(startPos, pos)] ?: 0.0
                } else if (ch == '√'.code) { nextChar(); x = Math.sqrt(parseFactor()) }
                else return 0.0
                if (eat('^'.code)) x = Math.pow(x, parseFactor())
                return x
            }
        }.parse()
    }
}
