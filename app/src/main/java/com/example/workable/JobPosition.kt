package com.example.workable

data class JobPosition(
    val title: String,
    val company: String,
    val rating: Float,
    val jobType: String, // e.g., "Full Time", "Part Time"
    val location: String, // e.g., "Remote", "On-site"
    val salaryRange: String // e.g., "$5000-$6000"
)
