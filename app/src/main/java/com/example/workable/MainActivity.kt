package com.example.workable
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase



class MainActivity : ComponentActivity() {

    private val db = Firebase.firestore
    private var topCompanies = mutableListOf<Company>()

    private var recommendedJobs = mutableListOf<JobPosition>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val closeResumeBox: ImageButton = findViewById(R.id.closeResumeBox)
        val resumeBox: LinearLayout = findViewById(R.id.resumeBox)
        closeResumeBox.setOnClickListener {
            resumeBox.visibility = View.GONE
        }

        val profile: ImageButton = findViewById(R.id.profile)
        profile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }




        val topCompaniesRecyclerView: RecyclerView = findViewById(R.id.topCompaniesRecyclerView)

        val companiesAdapter = CompaniesAdapter(mutableListOf())

        topCompaniesRecyclerView.adapter = companiesAdapter

        topCompanies.forEach { company ->
            Glide.with(this)
                .load(company.logo)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .preload()
        }

        val recommendedJobsContainer: LinearLayout = findViewById(R.id.recommendedJobsContainer)

        recommendedJobs.forEach { jobPosition ->
            val jobView = LayoutInflater.from(this)
                .inflate(R.layout.job_position, recommendedJobsContainer, false)
            bindJobView(jobView, jobPosition)
            recommendedJobsContainer.addView(jobView)
        }



        fetchOpportunities()

    }

    private fun fetchOpportunities() {
        db.collection("opportunities1")
            .get()
            .addOnSuccessListener { documents ->
                //recommendedJobs.clear()

                val companiesSet = mutableSetOf<String>()
                val jobPositionsSet = mutableSetOf<String>()
                val companies = mutableListOf<Company>()
                val companyLogos = mutableListOf<String>()
                val jobPositions = mutableListOf<JobPosition>()


                for (document in documents) {
                    val companyName = document.getString("Company") ?: "Unknown"
                    val companyLogo = document.getString("CompanyLogo")
                    val jobTitle = document.getString("Job") ?: "Unknown"


                    val newJobPosition = JobPosition(
                        title = jobTitle,
                        company = companyName,
                        rating = 5.0f,  // replace with actual logic to determine the rating
                        jobType = document.getString("EmploymentType") ?: "N/A",
                        location = document.getString("Location") ?: "N/A",
                        salaryRange = document.getString("Pay") ?: "N/A"
                    )
                    if (newJobPosition !in recommendedJobs) {
                        recommendedJobs.add(newJobPosition)
                    }
                    if (!companiesSet.contains(companyName)) {
                        val company = Company(
                            name = companyName,
                            logo = companyLogo // replace with actual logic to determine the logo
                        )
                        companies.add(company)
                        companiesSet.add(companyName)
                        companyLogos.add(companyLogo!!)
                    }

                    if (!jobPositionsSet.contains(jobTitle)) {
                        val jobPosition = JobPosition(
                            title = jobTitle,
                            company = companyName,

                            rating = 5.0f,  // replace with actual logic to determine the rating
                            jobType = document.getString("EmploymentType") ?: "N/A",
                            location = document.getString("Location") ?: "N/A",
                            salaryRange = document.getString("Pay") ?: "N/A"
                        )
                        jobPositions.add(jobPosition)
                        jobPositionsSet.add(jobTitle)
                        //recommendedJobs.add(jobPosition)

                    }
                }

                runOnUiThread {
                    //recommendedJobs.addAll(jobPositions)
                    updateRecommendedJobsUI()
                }
                val sortedCompanies =
                    companies.sortedWith(compareBy<Company> { it.logo.isNullOrEmpty() }.thenBy { it.name })


                val recommendedJobsContainer: LinearLayout =
                    findViewById(R.id.recommendedJobsContainer)
                val topCompaniesRecyclerView: RecyclerView =
                    findViewById(R.id.topCompaniesRecyclerView)

                (topCompaniesRecyclerView.adapter as CompaniesAdapter).updateData(sortedCompanies)

                // adding new views
                jobPositions.forEach { jobPosition ->
                    val jobView = LayoutInflater.from(this)
                        .inflate(R.layout.job_position, recommendedJobsContainer, false)
                    bindJobView(jobView, jobPosition)
                    recommendedJobsContainer.addView(jobView)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }



    private fun updateRecommendedJobsUI() {
        val recommendedJobsContainer: LinearLayout = findViewById(R.id.recommendedJobsContainer)
        recommendedJobsContainer.removeAllViews() // Clear existing views

        for (jobPosition in recommendedJobs) {
            val jobView = LayoutInflater.from(this).inflate(R.layout.job_position, recommendedJobsContainer, false)
            bindJobView(jobView, jobPosition)
            recommendedJobsContainer.addView(jobView)
        }
    }

    private fun bindJobView(view: View, jobPosition: JobPosition) {
        view.findViewById<TextView>(R.id.jobTitle).text = jobPosition.title
        view.findViewById<TextView>(R.id.jobCompany).text = jobPosition.company
        view.findViewById<TextView>(R.id.jobRating).text = "Rating: ${jobPosition.rating}"
        view.findViewById<TextView>(R.id.jobType).text = jobPosition.jobType
        view.findViewById<TextView>(R.id.jobLocation).text = "Location: ${jobPosition.location}"
        view.findViewById<TextView>(R.id.jobSalary).text = "Salary: ${jobPosition.salaryRange}"
    }
}