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

import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import cz.losoos.calculator.databinding.ActivityMainBinding
import com.google.android.material.button.MaterialButton
import cz.losoos.calculator.SharedSolver

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_calculator -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_currency -> {
                    startActivity(Intent(this, CurrencyActivity::class.java))
                    finish()
                }
                R.id.nav_home -> {
                    startActivity(Intent(this, MenuActivity::class.java))
                    finish()
                }
            }
            true
        }

        setupLetterBar()

        binding.solveButton.setOnClickListener {
            val input = binding.equationInput.text.toString().trim()
            if (input.isNotEmpty()) {
                // Zkusíme nejdříve sdílený solver pro jednoduché výpočty
                val sharedResult = if (!input.contains("=")) SharedSolver.solve(input) else null
                
                if (sharedResult != null) {
                    binding.resultText.text = sharedResult.toString()
                } else {
                    binding.resultText.text = Solver.solve(input, this)
                }
            } else {
                binding.resultText.text = getString(R.string.error_msg)
            }
        }

        val locale = resources.configuration.locales[0]
        if (locale.language == "cs") {
            binding.decimalButton.text = ","
        } else {
            binding.decimalButton.text = "."
        }

        binding.btnVariableX.setOnLongClickListener {
            if (binding.letterBar.visibility == View.GONE) {
                binding.letterBar.visibility = View.VISIBLE
            } else {
                binding.letterBar.visibility = View.GONE
            }
            true
        }
    }

    private fun setupLetterBar() {
        val alphabet = ('a'..'z').toList()
        for (char in alphabet) {
            val btn = MaterialButton(ContextThemeWrapper(this, R.style.CalcLetterButton), null, 0)
            btn.text = char.toString()
            val params = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.letter_button_size),
                resources.getDimensionPixelSize(R.dimen.letter_button_size)
            )
            params.setMargins(8, 8, 8, 8)
            btn.layoutParams = params

            btn.setOnClickListener {
                appendAtCursor(char.toString())
                binding.letterBar.visibility = View.GONE
            }
            binding.letterContainer.addView(btn)
        }
    }

    fun onKeyClick(view: View) {
        val button = view as Button
        val textToAppend = when(val txt = button.text.toString()) {
            "xʸ" -> "^"
            "√" -> "√"
            "π" -> "π"
            "e" -> "e"
            "×" -> "×"
            "÷" -> "÷"
            else -> txt
        }
        appendAtCursor(textToAppend)
    }

    private fun appendAtCursor(text: String) {
        val start = binding.equationInput.selectionStart
        val end = binding.equationInput.selectionEnd
        binding.equationInput.text?.replace(Math.min(start, end), Math.max(start, end), text)
    }

    fun onClearClick(view: View) {
        binding.equationInput.setText("")
        binding.resultText.text = ""
        binding.letterBar.visibility = View.GONE
    }

    fun onDeleteClick(view: View) {
        val text = binding.equationInput.text.toString()
        if (text.isNotEmpty()) {
            val start = binding.equationInput.selectionStart
            if (start > 0) {
                binding.equationInput.text?.delete(start - 1, start)
            }
        }
    }
}
