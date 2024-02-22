package com.example.workable
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {

    private val db = Firebase.firestore
    private var topCompanies = mutableListOf<Company>()

    private var recommendedJobs = mutableListOf<JobPosition>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchView: SearchView = findViewById(R.id.searchView)

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


        if (DataCache.topCompanies.isNotEmpty()) {
            updateTopCompaniesUI(DataCache.topCompanies)
        }
        if (DataCache.recommendedJobs.isNotEmpty()) {
            updateRecommendedJobsUI(DataCache.recommendedJobs)
        } else {
            fetchOpportunities()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    filterTopCompanies(it)
                    filterRecommendedJobs(it)
                }
                return false
            }
        override fun onQueryTextChange(newText: String?): Boolean {
            newText?.let {
                filterTopCompanies(it)
                filterRecommendedJobs(it)
            }
            return false
        }
    })

    }


    private fun filterTopCompanies(query: String) {
        val filteredList = DataCache.topCompanies.filter {
            it.name.contains(query, ignoreCase = true) ||
                    (it.logo?.contains(query, ignoreCase = true) ?: false)
        }
        val topCompaniesRecyclerView: RecyclerView = findViewById(R.id.topCompaniesRecyclerView)
        (topCompaniesRecyclerView.adapter as CompaniesAdapter).updateData(filteredList)
    }

    private fun filterRecommendedJobs(query: String) {
        val filteredList = DataCache.recommendedJobs.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.company.contains(query, ignoreCase = true)
        }
        updateRecommendedJobsUI(filteredList)
    }

    private fun fetchOpportunities() {
        db.collection("opportunities")
            .get()
            .addOnSuccessListener { documents ->
                DataCache.topCompanies.clear()
                DataCache.recommendedJobs.clear()

                for (document in documents) {
                    val companyName = document.getString("Company") ?: "Unknown"
                    val companyLogo = document.getString("CompanyLogo")
                    val jobTitle = document.getString("Role") ?: "Unknown"

                    if (!DataCache.topCompanies.any { it.name == companyName }) {
                        val company = Company(name = companyName, logo = companyLogo)
                        DataCache.topCompanies.add(company)
                    }

                    val jobPosition = JobPosition(
                        title = jobTitle,
                        company = companyName,
                        rating = 5.0f,  // replace with actual logic to determine the rating
                        jobType = document.getString("EmploymentType") ?: "N/A",
                        location = document.getString("Location") ?: "N/A",
                        salaryRange = document.getString("Pay") ?: "N/A"
                    )
                    if (!DataCache.recommendedJobs.any { it.title == jobTitle && it.company == companyName }) {
                        DataCache.recommendedJobs.add(jobPosition)
                    }
                }

                if (DataCache.recommendedJobs.size >= 2) {
                    val sharedPref = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        val gson = Gson()
                        val job1Json = gson.toJson(DataCache.recommendedJobs[0])
                        val job2Json = gson.toJson(DataCache.recommendedJobs[1])
                        putString("topJob1", job1Json)
                        putString("topJob2", job2Json)
                        apply()
                    }
                }

                GlobalScope.launch(Dispatchers.IO) {
                    val recommendedJobs = DataCache.recommendedJobs
                    val topCompanies = DataCache.topCompanies

                    withContext(Dispatchers.Main) {
                        updateRecommendedJobsUI(recommendedJobs)
                        updateTopCompaniesUI(topCompanies)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    private fun updateTopCompaniesUI(companies: List<Company>) {
        val companiesWithLogos = companies.filter { !it.logo.isNullOrEmpty() }
        val sortedCompaniesWithLogos = companiesWithLogos.sortedBy { it.name }

        val topCompaniesRecyclerView: RecyclerView = findViewById(R.id.topCompaniesRecyclerView)
        (topCompaniesRecyclerView.adapter as CompaniesAdapter).updateData(sortedCompaniesWithLogos)
    }

    private fun updateRecommendedJobsUI(jobPositions: List<JobPosition>) {
        val recommendedJobsContainer: LinearLayout = findViewById(R.id.recommendedJobsContainer)
        recommendedJobsContainer.removeAllViews()
        jobPositions.forEach { jobPosition ->
            val jobView = LayoutInflater.from(this).inflate(R.layout.job_position, recommendedJobsContainer, false)
            bindJobView(jobView, jobPosition)
            recommendedJobsContainer.addView(jobView)
        }
    }


    private fun bindJobView(view: View, jobPosition: JobPosition) {
        view.findViewById<TextView>(R.id.jobTitle).text = jobPosition.title
        view.findViewById<TextView>(R.id.jobCompany).text = jobPosition.company
        //view.findViewById<TextView>(R.id.jobRating).text = "Rating: ${jobPosition.rating}"
        view.findViewById<TextView>(R.id.jobType).text = jobPosition.jobType
        view.findViewById<TextView>(R.id.jobLocation).text = "Location: ${jobPosition.location}"
        view.findViewById<TextView>(R.id.jobSalary).text = "Salary: ${jobPosition.salaryRange}"
    }
}