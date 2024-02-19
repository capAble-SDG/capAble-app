package com.example.workable
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase



class MainActivity : ComponentActivity() {

    val db = Firebase.firestore


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

//        val nestedScrollView: NestedScrollView = findViewById(R.id.scrollableView)
//
//        val whiteBackgroundCard: CardView = findViewById(R.id.whiteBackgroundCard)
//
//
//        nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
//            // Get the height of the grey card
//            val greyCardHeight = whiteBackgroundCard.height
//            if (scrollY > greyCardHeight) {
//                // Scrolled past the grey card, set the background to white
//                nestedScrollView.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
//            } else {
//                // Still within the grey card's height, set the background to the initial color
//                nestedScrollView.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
//            }
//        })


        val topCompanies = listOf(
            Company("Facebook", logo = R.drawable.profile),
            Company("Apple", android.R.drawable.ic_btn_speak_now),
            Company("Amazon", R.drawable.ic_launcher_foreground)
        )

        val topCompaniesRecyclerView: RecyclerView = findViewById(R.id.topCompaniesRecyclerView)
        val companiesAdapter = CompaniesAdapter(topCompanies)
        topCompaniesRecyclerView.adapter = companiesAdapter



        val recommendedJobs = listOf(
            JobPosition("Junior Designer", "Airbit", 5.0f, "Full Time", "Remote", "$5000-$6000"),
            JobPosition("Senior Developer", "TechCorp", 4.8f, "Full Time", "On-site", "$8000-$10000"),
            JobPosition("UI/UX Designer", "DesignPro", 4.5f, "Part Time", "Remote", "$4000-$5000")
        )

        JobPositionsAdapter(listOf())
        topCompaniesRecyclerView.adapter = companiesAdapter
        val recommendedJobsContainer: LinearLayout = findViewById(R.id.recommendedJobsContainer)
        // Assume you have a method to add views to your LinearLayout for each job
        recommendedJobs.forEach { jobPosition ->
            val jobView = LayoutInflater.from(this).inflate(R.layout.job_position, recommendedJobsContainer, false)
            bindJobView(jobView, jobPosition)
            recommendedJobsContainer.addView(jobView)
        }

        // Fetch opportunities from Firestore and update your adapters
        fetchOpportunities()


    }

    private fun fetchOpportunities() {
        db.collection("opportunities")
            .get()
            .addOnSuccessListener { documents ->
                val companiesSet = mutableSetOf<String>()
                val jobPositionsSet = mutableSetOf<String>()
                val companies = mutableListOf<Company>()
                val jobPositions = mutableListOf<JobPosition>()

                for (document in documents) {
                    val companyName = document.getString("Company") ?: "Unknown"
                    val jobTitle = document.getString("Job") ?: "N/A"

                    if (!companiesSet.contains(companyName)) {
                        val company = Company(
                            name = companyName,
                            logo = R.drawable.pfp // replace with actual logic to determine the logo
                        )
                        companies.add(company)
                        companiesSet.add(companyName)
                    }

                    if (!jobPositionsSet.contains(jobTitle)) {
                        val jobPosition = JobPosition(
                            title = jobTitle,
                            company = companyName,
                            rating = 5.0f, // replace with actual logic to get the rating
                            jobType = document.getString("EmploymentType") ?: "N/A",
                            location = document.getString("Location") ?: "N/A",
                            salaryRange = document.getString("Pay") ?: "N/A"
                        )
                        jobPositions.add(jobPosition)
                        jobPositionsSet.add(jobTitle)
                    }
                }

                val recommendedJobsContainer: LinearLayout = findViewById(R.id.recommendedJobsContainer)
                val topCompaniesRecyclerView: RecyclerView = findViewById(R.id.topCompaniesRecyclerView)

                (topCompaniesRecyclerView.adapter as CompaniesAdapter).updateData(companies)

                //clearing hardcoded jobs
                recommendedJobsContainer.removeAllViews()
                // adding new views
                jobPositions.forEach { jobPosition ->
                    val jobView = LayoutInflater.from(this).inflate(R.layout.job_position, recommendedJobsContainer, false)
                    bindJobView(jobView, jobPosition)
                    recommendedJobsContainer.addView(jobView)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
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

class CompaniesAdapter(private var companies: List<Company>) :
    RecyclerView.Adapter<CompaniesAdapter.CompanyViewHolder>() {

    fun updateData(newCompanies: List<Company>) {
        companies = newCompanies
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_company, parent, false)
        return CompanyViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompanyViewHolder, position: Int) {
        val company = companies[position]
        holder.bind(company)
    }

    override fun getItemCount(): Int = companies.size

    class CompanyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val companyLogo: ImageView = itemView.findViewById(R.id.companyLogo)

        fun bind(company: Company) {
            companyLogo.setImageResource(company.logo)
        }
    }
}

    class JobPositionsAdapter(private var jobPositions: List<JobPosition>) :
        RecyclerView.Adapter<JobPositionsAdapter.JobPositionViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobPositionViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.job_position, parent, false)
            return JobPositionViewHolder(view)
        }

        override fun onBindViewHolder(holder: JobPositionViewHolder, position: Int) {
            holder.bind(jobPositions[position])
        }

        override fun getItemCount() = jobPositions.size

        class JobPositionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(jobPosition: JobPosition) {
                itemView.findViewById<TextView>(R.id.jobTitle).text = jobPosition.title
                itemView.findViewById<TextView>(R.id.jobCompany).text = jobPosition.company
                // other views to bind
            }
        }
    }



}
