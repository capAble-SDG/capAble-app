package com.example.workable

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.workable.R
import com.google.gson.Gson

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val username: TextView= findViewById(R.id.txtUserName)
        val sharedPref = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val fullName = sharedPref.getString("fullName", "")
        username.text = fullName

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
    }
    private fun inflateJobPositions() {
        val sharedPref = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val gson = Gson()
        val jobsContainer = findViewById<LinearLayout>(R.id.recommendedJobsContainer)
        jobsContainer.removeAllViews()
        val job1Json = sharedPref.getString("topJob1", null)
        val job2Json = sharedPref.getString("topJob2", null)

        listOf(job1Json, job2Json).forEach { jobJson ->
            jobJson?.let {
                val jobPosition = gson.fromJson(it, JobPosition::class.java)
                val jobView = LayoutInflater.from(this).inflate(R.layout.job_position, jobsContainer, false)
                bindJobView(jobView, jobPosition)
                jobsContainer.addView(jobView)
            }
        }
    }


    private fun bindJobView(view: View, jobPosition: JobPosition) {
        view.findViewById<TextView>(R.id.jobTitle).text = jobPosition.title
        view.findViewById<TextView>(R.id.jobCompany).text = jobPosition.company
        view.findViewById<TextView>(R.id.jobLocation).text = jobPosition.location
//        view.findViewById<TextView>(R.id.jobSalary).text = jobPosition.salaryRange

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