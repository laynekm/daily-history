package laynekm.bytesize_history

import android.app.DatePickerDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.widget.ProgressBar
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity()  {

    private lateinit var historyItemAdapter: HistoryItemAdapter
    private lateinit var progressBar: ProgressBar
    private val contentProvider: ContentProvider = ContentProvider()
    private var selectedDate: Date = getToday()

    private val dateString = "selectedDate"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        progressBar = findViewById(R.id.progressBar)

        if (savedInstanceState !== null) {
            selectedDate = stringToDate(savedInstanceState.getString(dateString))
        }

        initializeRecyclerView(ArrayList())
        var dateLabel: TextView = findViewById(R.id.dateLabel)
        dateLabel.text = buildDateLabel(selectedDate)
        progressBar.visibility = View.VISIBLE
        getHistoryItems(false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Populate recycler view with initial empty data
    private fun initializeRecyclerView(items: MutableList<HistoryItem>) {
        val historyItemView: RecyclerView = findViewById(R.id.historyItems)
        historyItemAdapter = HistoryItemAdapter(this, items)
        historyItemView.adapter = historyItemAdapter
        historyItemView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)) {
                    getHistoryItems(false)
                }
            }
        })
        historyItemView.layoutManager = LinearLayoutManager(this)
    }

    // Functions for adding
    private fun getHistoryItems(newDate: Boolean) {
        contentProvider.fetchHistoryItems(newDate, buildDateURL(selectedDate), ::updateRecyclerView)
    }

    // Populate recycler view with fetched data and hide progress bar
    private fun updateRecyclerView(items: MutableList<HistoryItem>) {
        if (progressBar.visibility === View.VISIBLE) progressBar.visibility = View.GONE
        historyItemAdapter.setItems(items)
        historyItemAdapter.notifyDataSetChanged()
    }

    private fun updateDate(date: Date) {
        if (!datesEqual(date, selectedDate)) {
            selectedDate = date
            var dateLabel: TextView = findViewById(R.id.dateLabel)
            dateLabel.text = buildDateLabel(selectedDate)
            progressBar.visibility = View.VISIBLE
            getHistoryItems(true)
        }
    }

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

    override fun onBackPressed() {
        val webView: WebView = findViewById(R.id.webView)
        if (webView.visibility === View.VISIBLE) {
            webView.visibility = View.GONE
            webView.loadUrl("about:blank")
        } else {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState);
        outState.putString(dateString, dateToString(selectedDate))
    }
}
