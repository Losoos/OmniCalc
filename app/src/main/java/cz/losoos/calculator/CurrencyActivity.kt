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
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
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
    private val fullCurrencyList = mutableListOf<CurrencyRate>()
    private val filteredList = mutableListOf<CurrencyRate>()
    private var baseAmount = 1.0
    private var baseCurrency = "EUR"
    private var baseRate = 1.0
    private var adapter: CurrencyAdapter? = null

    private val continentMap = mapOf(
        "EUR" to "Europe", "CZK" to "Europe", "BGN" to "Europe", "DKK" to "Europe",
        "GBP" to "Europe", "HUF" to "Europe", "PLN" to "Europe", "RON" to "Europe",
        "SEK" to "Europe", "CHF" to "Europe", "ISK" to "Europe", "NOK" to "Europe",
        "TRY" to "Europe", "USD" to "America", "BRL" to "America", "CAD" to "America",
        "MXN" to "America", "JPY" to "Asia", "CNY" to "Asia", "HKD" to "Asia",
        "IDR" to "Asia", "ILS" to "Asia", "INR" to "Asia", "KRW" to "Asia",
        "MYR" to "Asia", "PHP" to "Asia", "SGD" to "Asia", "THB" to "Asia",
        "AUD" to "Others", "NZD" to "Others", "ZAR" to "Others"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCurrencyBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                R.id.nav_currency -> binding.drawerLayout.closeDrawer(GravityCompat.START)
                R.id.nav_home -> {
                    startActivity(Intent(this, MenuActivity::class.java))
                    finish()
                }
            }
            true
        }

        setupTabs()
        setupRecyclerView()
        loadSavedRates()
        fetchRates()

        binding.amountInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                baseAmount = s.toString().toDoubleOrNull() ?: 0.0
                adapter?.updateData(baseRate, baseAmount)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupTabs() {
        val tabs = listOf(
            getString(R.string.tab_all),
            getString(R.string.tab_europe),
            getString(R.string.tab_america),
            getString(R.string.tab_asia),
            getString(R.string.tab_others)
        )
        tabs.forEach { title ->
            binding.continentTabs.addTab(binding.continentTabs.newTab().setText(title))
        }

        binding.continentTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                filterByContinent(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun filterByContinent(position: Int) {
        filteredList.clear()
        when (position) {
            0 -> filteredList.addAll(fullCurrencyList)
            1 -> filteredList.addAll(fullCurrencyList.filter { continentMap[it.code] == "Europe" })
            2 -> filteredList.addAll(fullCurrencyList.filter { continentMap[it.code] == "America" })
            3 -> filteredList.addAll(fullCurrencyList.filter { continentMap[it.code] == "Asia" })
            4 -> filteredList.addAll(fullCurrencyList.filter { continentMap[it.code] == "Others" })
        }
        adapter?.notifyDataSetChanged()
    }

    private fun setupRecyclerView() {
        binding.currencyList.layoutManager = LinearLayoutManager(this)
        adapter = CurrencyAdapter(filteredList, baseRate, baseAmount) { selected ->
            baseCurrency = selected.code
            baseRate = selected.rate
            binding.baseCurrencyText.text = baseCurrency
            adapter?.updateData(baseRate, baseAmount)
        }
        binding.currencyList.adapter = adapter
    }

    private fun fetchRates() {
        binding.loadingProgress.visibility = View.VISIBLE
        lifecycleScope.launch {
            val rates = withContext(Dispatchers.IO) {
                try {
                    val url = URL("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml")
                    val connection = url.openConnection()
                    connection.connectTimeout = 5000
                    val factory = XmlPullParserFactory.newInstance()
                    factory.isNamespaceAware = false
                    val parser = factory.newPullParser()
                    parser.setInput(connection.getInputStream(), null)

                    val fetched = mutableListOf<CurrencyRate>()
                    fetched.add(CurrencyRate("EUR", 1.0))
                    
                    var eventType = parser.eventType
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG && parser.name == "Cube") {
                            val cur = parser.getAttributeValue(null, "currency")
                            val rt = parser.getAttributeValue(null, "rate")
                            if (cur != null && rt != null) {
                                fetched.add(CurrencyRate(cur, rt.toDouble()))
                            }
                        }
                        eventType = parser.next()
                    }
                    fetched.sortBy { it.code }
                    fetched
                } catch (e: Exception) {
                    null
                }
            }

            binding.loadingProgress.visibility = View.GONE
            if (rates != null && rates.size > 1) {
                fullCurrencyList.clear()
                fullCurrencyList.addAll(rates)
                saveRates(rates)
                
                if (baseCurrency == "EUR" && Locale.getDefault().language == "cs") {
                    rates.find { it.code == "CZK" }?.let {
                        baseCurrency = it.code
                        baseRate = it.rate
                        binding.baseCurrencyText.text = "CZK"
                    }
                }
                filterByContinent(binding.continentTabs.selectedTabPosition)
            }
        }
    }

    private fun saveRates(rates: List<CurrencyRate>) {
        val prefs = getSharedPreferences("currency_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        rates.forEach { editor.putFloat(it.code, it.rate.toFloat()) }
        editor.putString("codes", rates.joinToString(",") { it.code })
        editor.apply()
    }

    private fun loadSavedRates() {
        val prefs = getSharedPreferences("currency_prefs", Context.MODE_PRIVATE)
        val codes = prefs.getString("codes", null)?.split(",")
        
        fullCurrencyList.clear()
        if (codes != null) {
            codes.forEach { code ->
                val rate = prefs.getFloat(code, 0f)
                if (rate > 0) fullCurrencyList.add(CurrencyRate(code, rate.toDouble()))
            }
        }

        if (fullCurrencyList.isEmpty()) {
            val fallback = listOf(
                CurrencyRate("EUR", 1.0), CurrencyRate("CZK", 25.15), CurrencyRate("USD", 1.08),
                CurrencyRate("GBP", 0.85), CurrencyRate("PLN", 4.31), CurrencyRate("CHF", 0.96),
                CurrencyRate("AUD", 1.65), CurrencyRate("CAD", 1.48), CurrencyRate("JPY", 168.5),
                CurrencyRate("HUF", 394.0), CurrencyRate("BGN", 1.95), CurrencyRate("DKK", 7.45),
                CurrencyRate("RON", 4.97), CurrencyRate("SEK", 11.62), CurrencyRate("NOK", 11.55),
                CurrencyRate("TRY", 34.8), CurrencyRate("BRL", 5.92), CurrencyRate("CNY", 7.82),
                CurrencyRate("HKD", 8.44), CurrencyRate("IDR", 17450.0), CurrencyRate("ILS", 3.98),
                CurrencyRate("INR", 90.5), CurrencyRate("KRW", 1478.0), CurrencyRate("MXN", 18.25),
                CurrencyRate("MYR", 5.12), CurrencyRate("NZD", 1.82), CurrencyRate("PHP", 62.4),
                CurrencyRate("SGD", 1.46), CurrencyRate("THB", 39.2), CurrencyRate("ZAR", 19.8)
            )
            fullCurrencyList.addAll(fallback.sortedBy { it.code })
        }

        if (Locale.getDefault().language == "cs") {
            fullCurrencyList.find { it.code == "CZK" }?.let {
                baseCurrency = "CZK"
                baseRate = it.rate
                binding.baseCurrencyText.text = "CZK"
            }
        }
        filterByContinent(0)
    }

    class CurrencyAdapter(
        private val rates: List<CurrencyRate>,
        private var currentBaseRate: Double,
        private var amount: Double,
        private val onClick: (CurrencyRate) -> Unit
    ) : RecyclerView.Adapter<CurrencyAdapter.ViewHolder>() {

        fun updateData(newBaseRate: Double, newAmount: Double) {
            currentBaseRate = newBaseRate
            amount = newAmount
            notifyDataSetChanged()
        }

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
            val converted = (amount / currentBaseRate) * rate.rate
            holder.value.text = String.format(Locale.US, "%.2f", converted)
            holder.itemView.setOnClickListener { onClick(rate) }
        }

        override fun getItemCount() = rates.size
    }
}
