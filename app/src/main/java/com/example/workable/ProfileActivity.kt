package com.example.workable

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.workable.R
import com.google.gson.Gson


class ProfileActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val username: TextView= findViewById(R.id.txtUserName)
        val sharedPref = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val fullName = sharedPref.getString("fullName", "")
        username.text = fullName
        val lastSearchQuery = sharedPref.getString("lastSearchQuery", "")
        val txtYourJobSearches: TextView = findViewById(R.id.txtYourJobSearches)
        txtYourJobSearches.text = "Your Job Searches: $lastSearchQuery"

        val back: ImageButton = findViewById(R.id.back)
        back.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        val home: ImageButton = findViewById(R.id.briefcaseIcon)
        home.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        val imgEdit: ImageView = findViewById(R.id.imgEdit)
        imgEdit.setOnClickListener {
            showEditDialog()
        }
        inflateJobPositions()
        showLastSearches()

    }


    private fun inflateJobPositions() {
        val jobsContainer = findViewById<LinearLayout>(R.id.recommendedJobsContainer)
        jobsContainer.removeAllViews()

        val jobCount = DataCache.recommendedJobs.size
        Log.d("ProfileActivity", "Inflating job positions, count: $jobCount")

        if (jobCount > 0) {
            val randomJobPosition = DataCache.recommendedJobs.random()
            Log.d("ProfileActivity", "Inflating random job position: ${randomJobPosition.title}")
            val jobView = LayoutInflater.from(this).inflate(R.layout.job_position, jobsContainer, false)
            bindJobView(jobView, randomJobPosition)
            jobsContainer.addView(jobView)
        } else {
            Log.d("ProfileActivity", "No job positions available to display.")
        }
    }



    private fun bindJobView(view: View, jobPosition: JobPosition) {
        view.findViewById<TextView>(R.id.jobTitle).text = jobPosition.title
        view.findViewById<TextView>(R.id.jobCompany).text = jobPosition.company
        view.findViewById<TextView>(R.id.jobLocation).text = jobPosition.location
        view.setOnClickListener {
            openJobDetail(jobPosition)
        }
        }

    private fun openJobDetail(jobPosition: JobPosition) {
        val detailView = LayoutInflater.from(this).inflate(R.layout.job_details, null)
        val imageViewLogo = detailView.findViewById<ImageView>(R.id.jobDetailLogo)

        if (jobPosition.companyLogo.isNotEmpty()) {
            Glide.with(this)
                .load(jobPosition.companyLogo)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(imageViewLogo)
        } else {
            imageViewLogo.setImageResource(R.drawable.ic_launcher_background)
        }
        detailView.findViewById<TextView>(R.id.jobDetailTitle).text = jobPosition.title
        detailView.findViewById<TextView>(R.id.jobDetailCompany).text = jobPosition.company
        detailView.findViewById<TextView>(R.id.jobDetailLocation).text = jobPosition.location

        val experienceTextView = detailView.findViewById<TextView>(R.id.jobDetailExperience)
        if (jobPosition.experience.isNotEmpty()) {
            experienceTextView.text = "Experience: "+ jobPosition.experience
        } else {
            experienceTextView.visibility = View.GONE
        }

        val payTextView = detailView.findViewById<TextView>(R.id.jobDetailPay)
        payTextView.text = if (jobPosition.pay.isNotEmpty()) "Salary: " + jobPosition.pay else "Salary Not Disclosed"

        detailView.findViewById<Button>(R.id.applyButton).setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(jobPosition.jobPostingUrl))
            startActivity(browserIntent)
        }

        val dialog = android.app.AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setView(detailView)
            .create()

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.apply {
            val params = WindowManager.LayoutParams()
            params.copyFrom(dialog.window?.attributes)
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            dialog.window?.attributes = params
            setLayout((resources.displayMetrics.widthPixels * 0.9).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.CENTER)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(0.2f)
        }
        dialog.show()
    }

    private fun showLastSearches() {
        val sharedPref = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val json = sharedPref.getString("searchQueries", "[]")
        val searches = Gson().fromJson(json, Array<String>::class.java).toList()

        val jobSearchesContainer = findViewById<LinearLayout>(R.id.jobSearchesContainer)
        jobSearchesContainer.removeAllViews()

        searches.forEach { search ->
            val searchView = TextView(this).apply {
                text = search
                setTextColor(Color.BLACK)
                textSize = 18f
                setPadding(12, 5, 0, 5)

            }
            jobSearchesContainer.addView(searchView)
        }
    }

    private fun showEditDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_job, null)
        val editJobTitle = dialogView.findViewById<EditText>(R.id.editJobTitle)
        val editCompanyName = dialogView.findViewById<EditText>(R.id.editCompanyName)

        val currentJobTitle = findViewById<TextView>(R.id.txtJobTitle)
        val currentCompanyName = findViewById<TextView>(R.id.txtCompanyName)

        editJobTitle.setText(currentJobTitle.text)
        editCompanyName.setText(currentCompanyName.text)

        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.edit_details))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                currentJobTitle.text = editJobTitle.text.toString()
                currentCompanyName.text = editCompanyName.text.toString()

                val sharedPref = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("jobTitle", currentJobTitle.text.toString())
                    putString("companyName", currentCompanyName.text.toString())
                    apply()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.show()
    }

}

