@file:Suppress("DEPRECATION")

package com.example.workable
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.activity.ComponentActivity
import org.json.JSONArray
import org.json.JSONException
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        // we cannot perform operns like fetching data from a server on the main thread
        // bec that may freeze the ui, app could crash. so we use AsyncTask
        // Parsing().execute("https://jsonplaceholder.typicode.com/users") // this line starts the async task
    }

//
//    private inner class Parsing : AsyncTask<String, Void, String>() {
//
//        // the doInBackground() fn executes in the bg, not on mainthread
//        // so we can perform net operns here, asynchronously
//        override fun doInBackground(vararg params: String): String {
//            return getJsonDataFromUrl(params[0])
//        }
//
//
//        // this fn is executed after the async (doInBackground) fn, so it executes on the main thread i.e.
//        // the json fetched in the bg is now available to use on the main thread (for parsing)
//        override fun onPostExecute(result: String) {
//            try {
//                // parsing the JSON array, not a single object
//                // bec the json in the url is in array format
//                val jsonArray = JSONArray(result)
//
//                // looping through each JSON object in the array
//                for (i in 0 until jsonArray.length()) {
//                    val jsonObject = jsonArray.getJSONObject(i)
//                    Log.d("MainActivity", jsonObject.toString())
//                }
//
//            } catch (e: JSONException) {
//                e.printStackTrace()
//            }
//        }
//
//
//
//    }
//
//
//    // this fn fetches the json data from the url (in the bg)
//    private fun getJsonDataFromUrl(url: String): String {
//        val connection = URL(url).openConnection()
//        val reader = BufferedReader(InputStreamReader(connection.getInputStream()))
//        val jsonData = StringBuilder()
//
//        var line: String?
//        while (reader.readLine().also { line = it } != null) {
//            jsonData.append(line)
//        }
//        reader.close()
//
//        return jsonData.toString()
//    }
}
