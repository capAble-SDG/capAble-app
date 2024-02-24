package com.example.workable
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.ComponentActivity
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


class MainActivity : ComponentActivity() {

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
        //view.findViewById<TextView>(R.id.jobRating).text = "Rating: ${jobPosition.rating}"
        view.findViewById<TextView>(R.id.jobType).text = jobPosition.jobType
        view.findViewById<TextView>(R.id.jobLocation).text = "Location: ${jobPosition.location}"
//        view.findViewById<TextView>(R.id.jobSalary).text = "Salary: ${jobPosition.salaryRange}"
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

        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
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


}
