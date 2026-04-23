package cz.losoos.calculator

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cz.losoos.calculator.databinding.ActivityCurrencyBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.URL
import java.util.Locale

data class CurrencyRate(val code: String, val rate: Double)

class CurrencyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCurrencyBinding
    private val currencyList = mutableListOf<CurrencyRate>()
    private var baseAmount = 1.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCurrencyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar and Drawer
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_calculator -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                R.id.nav_currency -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_home -> {
                    startActivity(Intent(this, MenuActivity::class.java))
                    finish()
                }
            }
            true
        }

        binding.currencyList.layoutManager = LinearLayoutManager(this)
        
        loadSavedRates()
        fetchRates()

        binding.amountInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                baseAmount = s.toString().toDoubleOrNull() ?: 1.0
                binding.currencyList.adapter?.notifyDataSetChanged()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchRates() {
        binding.loadingProgress.visibility = View.VISIBLE
        lifecycleScope.launch {
            val rates = withContext(Dispatchers.IO) {
                try {
                    val url = URL("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml")
                    val connection = url.openConnection()
                    val inputStream = connection.getInputStream()
                    
                    val factory = XmlPullParserFactory.newInstance()
                    val parser = factory.newPullParser()
                    parser.setInput(inputStream, null)

                    val fetchedRates = mutableListOf<CurrencyRate>()
                    var eventType = parser.eventType
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG && parser.name == "Cube") {
                            val currency = parser.getAttributeValue(null, "currency")
                            val rate = parser.getAttributeValue(null, "rate")
                            if (currency != null && rate != null) {
                                fetchedRates.add(CurrencyRate(currency, rate.toDouble()))
                            }
                        }
                        eventType = parser.next()
                    }
                    fetchedRates
                } catch (e: Exception) {
                    null
                }
            }

            binding.loadingProgress.visibility = View.GONE
            if (rates != null && rates.isNotEmpty()) {
                currencyList.clear()
                currencyList.addAll(rates)
                saveRates(rates)
                updateUI()
            }
        }
    }

    private fun saveRates(rates: List<CurrencyRate>) {
        val prefs = getSharedPreferences("currency_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        rates.forEach {
            editor.putFloat(it.code, it.rate.toFloat())
        }
        editor.putString("codes", rates.joinToString(",") { it.code })
        editor.apply()
    }

    private fun loadSavedRates() {
        val prefs = getSharedPreferences("currency_prefs", Context.MODE_PRIVATE)
        val codes = prefs.getString("codes", null)?.split(",") ?: return
        currencyList.clear()
        codes.forEach { code ->
            val rate = prefs.getFloat(code, 0f)
            if (rate > 0) {
                currencyList.add(CurrencyRate(code, rate.toDouble()))
            }
        }
        updateUI()
    }

    private fun updateUI() {
        binding.currencyList.adapter = CurrencyAdapter(currencyList) { baseAmount }
    }

    class CurrencyAdapter(private val rates: List<CurrencyRate>, private val getAmount: () -> Double) :
        RecyclerView.Adapter<CurrencyAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val code: TextView = view.findViewById(R.id.currencyCode)
            val value: TextView = view.findViewById(R.id.currencyValue)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_currency, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val rate = rates[position]
            holder.code.text = rate.code
            val converted = rate.rate * getAmount()
            holder.value.text = String.format(Locale.US, "%.2f", converted)
        }

        override fun getItemCount() = rates.size
    }
}
