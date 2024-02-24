package com.example.workable
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View

import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ProfileActivity"
    }

    private lateinit var loadingContainer: LinearLayout

    private val db = Firebase.firestore
    private var topCompanies = mutableListOf<Company>()

    private var recommendedJobs = mutableListOf<JobPosition>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        val searchView: SearchView = findViewById(R.id.searchView)

        val closeResumeBox: ImageButton = findViewById(R.id.closeResumeBox)
        val resumeBox: LinearLayout = findViewById(R.id.resumeBox)
        closeResumeBox.setOnClickListener {
            resumeBox.visibility = View.GONE
        }

        intent.getStringExtra("searchQuery")?.let { searchQuery ->
            searchView.setQuery(searchQuery, true) // true to submit the query
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
        val nestedScrollView: NestedScrollView = findViewById(R.id.scrollableView)
        val mainLayout: ConstraintLayout = findViewById(R.id.mainLayout)

        nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            if (scrollY > 500) {
                mainLayout.setBackgroundColor(Color.parseColor("#FFFFFF"))
            } else {
                mainLayout.setBackgroundColor(Color.parseColor("#ECEFEF"))
            }
        })


        val recommendedJobsContainer: LinearLayout = findViewById(R.id.recommendedJobsContainer)

        recommendedJobs.forEach { jobPosition ->
            val jobView = LayoutInflater.from(this)
                .inflate(R.layout.job_position, recommendedJobsContainer, false)
            bindJobView(jobView, jobPosition)
            recommendedJobsContainer.addView(jobView)
        }


        loadingContainer = findViewById(R.id.loadingContainer)

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
                    // saves the search query
                    val sharedPref = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
                    val searches = getSearchQueries(sharedPref) + it
                    val limitedSearches = searches.takeLast(2) // keep only the last two searches
                    with(sharedPref.edit()) {
                        putString("searchQueries", Gson().toJson(limitedSearches))
                        apply()
                    }

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

    private fun getSearchQueries(sharedPref: SharedPreferences): List<String> {
        val json = sharedPref.getString("searchQueries", "[]")
        return Gson().fromJson(json, Array<String>::class.java).toList()
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
        showLoading(true)

        db.collection("opportunities")
            .get()
            .addOnSuccessListener { documents ->
                DataCache.topCompanies.clear()
                DataCache.recommendedJobs.clear()


                for (document in documents) {
                    val companyName = document.getString("Company") ?: "Unknown"
                    val companyLogo = document.getString("CompanyLogo")
                    val jobTitle = document.getString("Role") ?: "Unknown"
                    val jobDescription = document.getString("Description") ?: "Description not available"
                    val jobExperience = document.getString("Experience") ?: "Experience not available"
                    val jobPay = document.getString("Pay") ?: "Salary not disclosed"
                    val jobPostingUrl = document.getString("JobPostingUrl") ?: ""

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
                            salaryRange = document.getString("Pay") ?: "N/A",
                            description = jobDescription,
                            experience = jobExperience,
                            pay = jobPay,
                            jobPostingUrl = jobPostingUrl,
                            companyLogo = companyLogo ?: ""

                        )
                    Log.d(TAG, "Fetched job: $jobTitle at $companyName")

                    if (!DataCache.recommendedJobs.any { it.title == jobTitle && it.company == companyName }) {
                        DataCache.recommendedJobs.add(jobPosition)
                    }
                }

                lifecycleScope.launch(Dispatchers.IO) {
                    val recommendedJobs = DataCache.recommendedJobs
                    val topCompanies = DataCache.topCompanies

                    withContext(Dispatchers.Main) {
                        updateRecommendedJobsUI(recommendedJobs)
                        updateTopCompaniesUI(topCompanies)
                    }
                    showLoading(false)

                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
                showLoading(false)

            }
    }


    private fun showLoading(isLoading: Boolean) {
        loadingContainer.visibility = if (isLoading) View.VISIBLE else View.GONE
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
        Log.d(TAG, "Updating UI with ${jobPositions.size} jobs")

        jobPositions.forEach { jobPosition ->
            val jobView = LayoutInflater.from(this).inflate(R.layout.job_position, recommendedJobsContainer, false)
            bindJobView(jobView, jobPosition)
            recommendedJobsContainer.addView(jobView)
        }
    }


    private fun bindJobView(view: View, jobPosition: JobPosition) {
        view.findViewById<TextView>(R.id.jobTitle).text = jobPosition.title
        view.findViewById<TextView>(R.id.jobCompany).text = jobPosition.company
        view.findViewById<TextView>(R.id.jobType).text = jobPosition.jobType
        view.findViewById<TextView>(R.id.jobLocation).text = "Location: ${jobPosition.location}"
//        view.findViewById<TextView>(R.id.jobSalary).text = "Salary: ${jobPosition.salaryRange}"
        view.setOnClickListener {
            openJobDetail(jobPosition)
        }
    }
    private fun openJobDetail(jobPosition: JobPosition) {
        val jobDetailBottomSheetFragment = JobDetails.newInstance(jobPosition)
        jobDetailBottomSheetFragment.show(supportFragmentManager, "JobDetailsTag")
    }


}
