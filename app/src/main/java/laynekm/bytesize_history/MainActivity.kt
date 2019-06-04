package laynekm.bytesize_history

import android.app.DatePickerDialog
import android.media.Image
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class HistoryViews(var views: MutableMap<Type, RecyclerView>)
class HistoryAdapters(var adapters: MutableMap<Type, HistoryItemAdapter>)

class MainActivity : AppCompatActivity()  {

    private lateinit var historyViews: HistoryViews
    private lateinit var historyAdapters: HistoryAdapters
    private lateinit var dateLabel: TextView
    private lateinit var dropdownFilter: ImageView
    private lateinit var dropdownView: View
    private lateinit var eventFilter: TextView
    private lateinit var birthFilter: TextView
    private lateinit var deathFilter: TextView
    private lateinit var progressBar: ProgressBar

    private val defaultOrder: Order = Order.ASCENDING
    private val defaultTypes: MutableList<Type> = mutableListOf(Type.EVENT, Type.BIRTH, Type.DEATH)
    private val defaultEras: MutableList<Era> = mutableListOf(Era.ANCIENT, Era.MEDIEVAL, Era.EARLYMODERN, Era.EIGHTEENS, Era.NINETEENS, Era.TWOTHOUSANDS)
    private var filterOptions: FilterOptions = FilterOptions(defaultOrder, defaultTypes, defaultEras)

    private val contentProvider: ContentProvider = ContentProvider()
    private var selectedDate: Date = getToday()
    private var selectedType: Type? = filterOptions.types[0]
    private var fetching: Boolean = false

    private val dateString = "selectedDate"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        dateLabel = findViewById(R.id.dateLabel)
        dropdownFilter = findViewById(R.id.dropdownFilter)
        dropdownView = findViewById(R.id.dropdownView)
        eventFilter = findViewById(R.id.eventBtn)
        birthFilter = findViewById(R.id.birthBtn)
        deathFilter = findViewById(R.id.deathBtn)
        progressBar = findViewById(R.id.progressBar)

        dropdownFilter.setOnClickListener { dropdownFilterOnClick() }
        eventFilter.setOnClickListener { setSelectedType(Type.EVENT) }
        birthFilter.setOnClickListener { setSelectedType(Type.BIRTH) }
        deathFilter.setOnClickListener { setSelectedType(Type.DEATH) }

        if (savedInstanceState !== null) {
            selectedDate = stringToDate(savedInstanceState.getString(dateString))
        }

        dateLabel.text = buildDateLabel(selectedDate)
        progressBar.visibility = View.VISIBLE

        initializeRecyclerViews()
        fetchHistoryItems()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.changeNotification -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Create RecyclerViews and their adapters, initialized as empty
    private fun initializeRecyclerViews() {
        historyViews = HistoryViews(mutableMapOf(
            Type.EVENT to findViewById(R.id.eventItems),
            Type.BIRTH to findViewById(R.id.birthItems),
            Type.DEATH to findViewById(R.id.deathItems)
        ))

        historyAdapters = HistoryAdapters(mutableMapOf(
            Type.EVENT to HistoryItemAdapter(this, mutableListOf()),
            Type.BIRTH to HistoryItemAdapter(this, mutableListOf()),
            Type.DEATH to HistoryItemAdapter(this, mutableListOf())
        ))

        for ((type, adapter) in historyViews.views) {
            adapter.adapter = historyAdapters.adapters[type]
            adapter.layoutManager = LinearLayoutManager(this)
        }
    }

    // Fetches history items from content provider
    private fun fetchHistoryItems() {
        fetching = true
        contentProvider.fetchHistoryItems(selectedDate, filterOptions, selectedType, ::updateRecyclerView)
    }

    // Filters history items without having to refetch
    private fun filterHistoryItems() {
        contentProvider.filterHistoryItems(filterOptions, ::updateRecyclerView)
    }

    // Callback function passed into fetchHistoryItems, updates views and other UI elements
    private fun updateRecyclerView(items: MutableMap<Type, MutableList<HistoryItem>>) {
        if (progressBar.visibility == View.VISIBLE) progressBar.visibility = View.GONE

        for ((type, adapter) in historyAdapters.adapters) {
            adapter.setItems(items[type]!!)
            adapter.notifyDataSetChanged()
        }

        fetching = false
    }

    // Updates date using value selected in calendar, refetches history items if date changed
    private fun updateDate(date: Date) {
        if (!datesEqual(date, selectedDate)) {
            selectedDate = date
            dateLabel.text = buildDateLabel(selectedDate)
            progressBar.visibility = View.VISIBLE
            fetchHistoryItems()
        }
    }

    // Toggles dropdown filter, refetches history items when menu is closed if filters changed
    private fun dropdownFilterOnClick() {
        if (dropdownView.visibility == View.GONE) {
            dropdownView.visibility = View.VISIBLE
            filterOptions.setViewContent(dropdownView)
        }
        else {
            dropdownView.visibility = View.GONE
            val filtersChanged = filterOptions.setFilterOptions(dropdownView)
            if (filtersChanged) {
                if (!filterOptions.types.contains(selectedType)) {
                    if (filterOptions.types.size == 0) setSelectedType(null)
                    else setSelectedType(filterOptions.types[0])
                }
                updateTypeSelectors()
                filterHistoryItems()
            }
        }
    }

    // Shows and hides type selectors according to filter options
    private fun updateTypeSelectors() {
        if (filterOptions.types.contains(Type.EVENT)) eventFilter.visibility = View.VISIBLE
        else { eventFilter.visibility = View.GONE }
        if (filterOptions.types.contains(Type.BIRTH)) birthFilter.visibility = View.VISIBLE
        else { birthFilter.visibility = View.GONE }
        if (filterOptions.types.contains(Type.DEATH)) deathFilter.visibility = View.VISIBLE
        else { deathFilter.visibility = View.GONE }
    }

    // Sets current history item type and hides other views
    private fun setSelectedType(type: Type?) {
        selectedType = type
        if(historyAdapters.adapters[selectedType] != null && historyAdapters.adapters[selectedType]!!.itemCount == 0) {
            fetchHistoryItems()
        }

        when (selectedType) {
            Type.EVENT -> {
                findViewById<RecyclerView>(R.id.eventItems).visibility = View.VISIBLE
                findViewById<RecyclerView>(R.id.birthItems).visibility = View.GONE
                findViewById<RecyclerView>(R.id.deathItems).visibility = View.GONE
            }
            Type.BIRTH -> {
                findViewById<RecyclerView>(R.id.eventItems).visibility = View.GONE
                findViewById<RecyclerView>(R.id.birthItems).visibility = View.VISIBLE
                findViewById<RecyclerView>(R.id.deathItems).visibility = View.GONE
            }
            Type.DEATH -> {
                findViewById<RecyclerView>(R.id.eventItems).visibility = View.GONE
                findViewById<RecyclerView>(R.id.birthItems).visibility = View.GONE
                findViewById<RecyclerView>(R.id.deathItems).visibility = View.VISIBLE
            }
            else -> {
                findViewById<RecyclerView>(R.id.eventItems).visibility = View.GONE
                findViewById<RecyclerView>(R.id.birthItems).visibility = View.GONE
                findViewById<RecyclerView>(R.id.deathItems).visibility = View.GONE
            }
        }
    }

    // Displays DatePicker and handles calls updateDate with the new selected date
    fun showDatePickerDialog(view: View) {
        var date: Calendar = Calendar.getInstance()
        var selectedYear = date.get(Calendar.YEAR)
        var selectedMonth = selectedDate.month
        var selectedDay = selectedDate.day

        // TODO: Hide year label
        DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, _, month, day ->
            updateDate(Date(month, day))
        }, selectedYear, selectedMonth, selectedDay).show()
    }

    // If the WebView is open, back button should close the WebView; otherwise, it should function normally
    override fun onBackPressed() {
        val webView: WebView = findViewById(R.id.webView)
        if (webView.visibility == View.VISIBLE) {
            webView.visibility = View.GONE
            webView.loadUrl("about:blank")
        } else {
            super.onBackPressed()
        }
    }

    // Ensures state is consistent when activity is destroyed/recreated
    // TODO: Need to save a lot more than just the date
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState);
        outState.putString(dateString, dateToString(selectedDate))
    }
}
