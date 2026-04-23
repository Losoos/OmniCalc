package cz.losoos.calculator

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import cz.losoos.calculator.databinding.ActivityMainBinding
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLetterBar()

        binding.solveButton.setOnClickListener {
            val input = binding.equationInput.text.toString().trim()
            if (input.isNotEmpty()) {
                binding.resultText.text = Solver.solve(input, this)
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
