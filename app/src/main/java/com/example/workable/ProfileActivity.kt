package com.example.workable

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.google.gson.Gson


class ProfileActivity : AppCompatActivity() {

    private lateinit var txtJobTitle: TextView
    private lateinit var txtCompanyName: TextView
    private lateinit var sharedPref: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sharedPref = getSharedPreferences("MyApp", Context.MODE_PRIVATE)

        val username: TextView = findViewById(R.id.txtUserName)
        txtJobTitle = findViewById(R.id.txtJobTitle)
        txtCompanyName = findViewById(R.id.txtCompanyName)

        findViewById<ImageButton>(R.id.back).setOnClickListener {
            finish()
        }
        findViewById<ImageButton>(R.id.briefcaseIcon).setOnClickListener {
            finish()
//            startActivity(Intent(this, MainActivity::class.java))
        }
        findViewById<ImageView>(R.id.imgEdit).setOnClickListener {
            showEditDialog()
        }

        val fullName = sharedPref.getString("fullName", "No Name") // Default value "No Name" for clarity
        username.text = fullName

        val savedJobTitle = sharedPref.getString("jobTitle", "Job Title") // Default value "Job Title"
        val savedCompanyName = sharedPref.getString("companyName", "Company Name") // Default value "Company Name"
        txtJobTitle.text = savedJobTitle
        txtCompanyName.text = savedCompanyName

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
                textSize = 20f

                setOnClickListener {
                    val intent = Intent(this@ProfileActivity, MainActivity::class.java)
                    intent.putExtra("searchQuery", search)
                    startActivity(intent)
                }
            }
            jobSearchesContainer.addView(searchView)
        }
    }

    private fun showEditDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_job, null)
        val editJobTitle = dialogView.findViewById<EditText>(R.id.editJobTitle)
        val editCompanyName = dialogView.findViewById<EditText>(R.id.editCompanyName)

        editJobTitle.setText(txtJobTitle.text)
        editCompanyName.setText(txtCompanyName.text)

        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.edit_details))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val newJobTitle = editJobTitle.text.toString()
                val newCompanyName = editCompanyName.text.toString()

                with(sharedPref.edit()) {
                    putString("jobTitle", newJobTitle)
                    putString("companyName", newCompanyName)
                    apply()
                }
                txtJobTitle.text = newJobTitle
                txtCompanyName.text = newCompanyName
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.show()
    }

}

