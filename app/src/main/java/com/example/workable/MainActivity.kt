package com.example.workable
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : ComponentActivity() {

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

        val topCompanies = listOf(
            Company("Facebook", logo = R.drawable.profile),
            Company("Apple", android.R.drawable.ic_btn_speak_now),
            Company("Amazon", R.drawable.ic_launcher_foreground)
        )

        val topCompaniesRecyclerView: RecyclerView = findViewById(R.id.topCompaniesRecyclerView)
        topCompaniesRecyclerView.adapter = CompaniesAdapter(topCompanies)

        val recommendedJobs = listOf(
            JobPosition("Junior Designer", "Airbit", 5.0f, "Full Time", "Remote", "$5000-$6000"),
            JobPosition("Senior Developer", "TechCorp", 4.8f, "Full Time", "On-site", "$8000-$10000"),
            JobPosition("UI/UX Designer", "DesignPro", 4.5f, "Part Time", "Remote", "$4000-$5000")
        )

        val recommendedJobsContainer: LinearLayout = findViewById(R.id.recommendedJobsContainer)
        recommendedJobs.forEach { jobPosition ->
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

class CompaniesAdapter(private val companies: List<Company>) :
    RecyclerView.Adapter<CompaniesAdapter.CompanyViewHolder>() {

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
    class JobPositionsAdapter(private val jobPositions: List<JobPosition>) :
        RecyclerView.Adapter<JobPositionsAdapter.JobPositionViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobPositionViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.job_position, parent, false)
            return JobPositionViewHolder(view)
        }

        override fun onBindViewHolder(holder: JobPositionViewHolder, position: Int) {
            holder.bind(jobPositions[position])
        }

        override fun getItemCount(): Int = jobPositions.size

        class JobPositionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val jobTitle: TextView = itemView.findViewById(R.id.jobTitle)
            private val jobCompany: TextView = itemView.findViewById(R.id.jobCompany)
            private val jobRating: TextView = itemView.findViewById(R.id.jobRating)
            private val jobType: TextView = itemView.findViewById(R.id.jobType)
            private val jobLocation: TextView = itemView.findViewById(R.id.jobLocation)
            private val jobSalary: TextView = itemView.findViewById(R.id.jobSalary)

            fun bind(jobPosition: JobPosition) {
                jobTitle.text = jobPosition.title
                jobCompany.text = jobPosition.company
                jobRating.text = "Rating: ${jobPosition.rating}"
                jobType.text = jobPosition.jobType
                jobLocation.text = "Location: ${jobPosition.location}"
                jobSalary.text = "Salary: ${jobPosition.salaryRange}"
            }
        }
    }



}
