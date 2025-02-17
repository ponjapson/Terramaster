package com.example.terramaster

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AddGuideDialogFragment: DialogFragment(){
    private lateinit var etGuideTitle: EditText
    private lateinit var etStepTitle: EditText
    private lateinit var etStepDescription: EditText
    private lateinit var btnAddStep: Button
    private lateinit var btnSaveGuide: Button
    private lateinit var rvSteps: RecyclerView
    private lateinit var stepAdapter: StepAdapter

    private val stepList = mutableListOf<Step>()
    private var stepCount = 1


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_add_guide, container, false)

           etGuideTitle = view.findViewById(R.id.etGuideTitle)
           etStepTitle = view.findViewById(R.id.etStepTitle)
           etStepDescription = view.findViewById(R.id.etStepDescription)
           btnAddStep = view.findViewById(R.id.btnAddStep)
           btnSaveGuide = view.findViewById(R.id.btnSaveGuide)
           rvSteps = view.findViewById(R.id.rvSteps)

           stepAdapter = StepAdapter(stepList)
           rvSteps.layoutManager = LinearLayoutManager(requireContext())
           rvSteps.adapter = stepAdapter


           btnAddStep.setOnClickListener {
                 AddStep();
           }

           btnSaveGuide.setOnClickListener {
               SaveGuideToFirestore()

           }

           return view

    }

    private fun AddStep()
    {
        val etStepTitle1 = etStepTitle.text.toString().trim()
        val etStepDescription1 = etStepDescription.text.toString().trim()

        if(etStepTitle1.isNotEmpty() && etStepDescription1.isNotEmpty())
        {
            stepList.add(Step(etStepTitle1, etStepDescription1))
            stepAdapter.notifyDataSetChanged()
            etStepTitle.text.clear()
            etStepDescription.text.clear()
        }else
        {
            Toast.makeText(requireContext(), "Please enter step details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun SaveGuideToFirestore()
    {
        val guideTitle = etGuideTitle.text.toString().trim()

        if(guideTitle.isNotEmpty() && stepList.isNotEmpty())
        {
            val db = FirebaseFirestore.getInstance()
            val guide = Guide(guideTitle, stepList)

            db.collection("knowledge_guide").add(guide).addOnSuccessListener{
                Toast.makeText(requireContext(), "Guide Saved", Toast.LENGTH_SHORT).show()
                dismiss()
            }.addOnFailureListener{
                Toast.makeText(requireContext(), "Failed to save guide", Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(requireContext(), "Please enter a title and at least one step", Toast.LENGTH_SHORT).show()
        }

    }
}