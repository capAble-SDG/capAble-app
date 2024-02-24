package com.example.workable

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.workable.databinding.JobDetailsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class JobDetails(private val jobPosition: JobPosition) : BottomSheetDialogFragment() {

    private var _binding: JobDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = JobDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)

        binding.jobDetailTitle.text = jobPosition.title
        binding.jobDetailCompany.text = jobPosition.company
        binding.jobDetailLocation.text = jobPosition.location
        binding.jobDetailExperience.text = if (jobPosition.experience.isNotEmpty()) "Experience: ${jobPosition.experience}" else "Experience: Not specified"
        binding.jobDetailPay.text = if (jobPosition.pay.isNotEmpty()) "Salary: ${jobPosition.pay}" else "Salary: Not disclosed"

        if (jobPosition.companyLogo.isNotEmpty()) {
            Glide.with(this)
                .load(jobPosition.companyLogo)
                .into(binding.jobDetailLogo)
        }

        binding.applyButton.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(jobPosition.jobPostingUrl))
            startActivity(browserIntent)
        }

    }

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val dialog = super.onCreateDialog(savedInstanceState)
//        dialog.setOnShowListener { dialogInterface ->
//            val bottomSheetDialog = dialogInterface as BottomSheetDialog
//            setupFullHeight(bottomSheetDialog)
//        }
//        return dialog
//    }
//
////    private fun setupFullHeight(bottomSheetDialog: BottomSheetDialog) {
////        val bottomSheet = bottomSheetDialog.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout?
//        val behavior = BottomSheetBehavior.from(bottomSheet!!)
//        val layoutParams = bottomSheet.layoutParams
//        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
//        bottomSheet.layoutParams = layoutParams
//        behavior.state = BottomSheetBehavior.STATE_EXPANDED
//    }
//

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(jobPosition: JobPosition): JobDetails {
            return JobDetails(jobPosition)
        }
    }
}
