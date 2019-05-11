package laynekm.dailyhistory

import android.net.Uri
import android.util.Log
import com.google.gson.JsonParser
import org.jetbrains.anko.doAsyncResult
import java.net.URL

class ContentProvider {

    fun getHistoryData() {
        doAsyncResult {
            val url = buildURL("May_11")
            val result = url.readText()
            parseContent(result)
//          activityUiThread {
//              longToast(result)
//          }
        }
    }

    // /w/api.php?action=query&format=json&prop=revisions&titles=May_11&formatversion=2&rvprop=content&rvslots=main&rvlimit=1
    private fun buildURL(searchParam: String): URL {
        val uri: Uri = Uri.parse("https://en.wikipedia.org/w/api.php").buildUpon()
            .appendQueryParameter("action", "query")
            .appendQueryParameter("prop", "revisions")
            .appendQueryParameter("rvprop", "content")
            .appendQueryParameter("rvslots", "main")
            .appendQueryParameter("rvlimit", "1")
            .appendQueryParameter("format", "json")
            .appendQueryParameter("formatversion" , "2")
            .appendQueryParameter("titles", searchParam)
            .build()

        Log.wtf("URL", uri.toString())
        return URL(uri.toString())
    }

    // Builds HistoryItem objects from json string input
    // TODO: Add error handling in case content does not exist or API call fails
    private fun parseContent(json: String) {
        Log.wtf("json", json)

        // extract content property from json
        val content = JsonParser().parse(json)
            .asJsonObject.get("query")
            .asJsonObject.getAsJsonArray("pages").get(0)
            .asJsonObject.getAsJsonArray("revisions").get(0)
            .asJsonObject.get("slots")
            .asJsonObject.get("main")
            .asJsonObject.get("content")
            .toString()

        // content itself is not in json format but can be split into an array
        val lines = content.split("\\n").toTypedArray()
        lines.forEach{ Log.wtf("lines", it) }

        // Split array into events, births, and deaths
        // Only care about strings starting with an asterisk
        // Assumes events, births, deaths proceed each other
        var historyItems = mutableListOf<HistoryItem>()
        var type: Type? = null
        for (line in lines) {
            if (line.contains("==Events==")) type = Type.EVENT
            if (line.contains("==Births==")) type = Type.BIRTH
            if (line.contains("==Deaths==")) type = Type.DEATH
            if (line.contains("==Holidays and observances==")) break
            if (type != null && line.contains("*")) historyItems!!.add(buildHistoryItem(line, type!!))
        }

        historyItems.forEach {
            Log.wtf("HistoryItems", it.toString())
        }
    }

    private fun buildHistoryItem(line: String, type: Type): HistoryItem {
        val year = parseYear(line)
        val desc = parseDescription(line)
        return HistoryItem(type, year, desc)
    }

    // Return year with numbers only
    private fun parseYear(line: String): String {
        val numsOnly = Regex("[^0-9]")
        return numsOnly.replace(line.substringBefore(" &ndash; "), "")
    }

    // Return description only (ie. remove links)
    private fun parseDescription(line: String): String {
        return line.substringAfter(" &ndash; ")
    }

    // Return links
//    private fun parseLinks(line: String): Link {
//
//    }
}