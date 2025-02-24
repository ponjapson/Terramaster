package com.example.terramaster

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.terramaster.databinding.ActivityBookingBinding.inflate
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects

class FragmentHome: Fragment() {
    private lateinit var rvGuide: RecyclerView
    private lateinit var guideAdapter: GuideAdapter
    private val guideList = mutableListOf<Guide>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as MainActivity).setSupportActionBar(toolbar)

        // Enable options menu in Fragment
        setHasOptionsMenu(true)

        rvGuide = view.findViewById(R.id.recyclerViewKnowledge)

        guideAdapter = GuideAdapter(guideList) { guideId, guideType ->
            navigateToGuide(guideId, guideType)
        }
        rvGuide.adapter = guideAdapter
        rvGuide.layoutManager = LinearLayoutManager(requireContext())


        loadGuidesFromFirestore()


        return view
    }

    private fun navigateToGuide(guideId: String, guideType: String) {
        val bundle = Bundle().apply {
            putString("guideId", guideId)
        }

        val fragment = if (guideType == "StepByStep") {
            FragmentDisplayStepByStepGuide()
        } else {
            FragmentDisplayPDF()
        }

        fragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }


    private fun loadGuidesFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        db.collection("knowledge_guide")
            .get()
            .addOnSuccessListener { documents ->
                guideList.clear()
                for (document in documents) {
                    val id = document.id
                    val title = document.getString("title") ?: ""  // Fetch title only
                    guideList.add(Guide(id, title, mutableListOf())) // Pass empty steps list
                }
                guideAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Handle error if needed
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.knowledge_guide_tool_bar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                // Navigate using FragmentTransaction
                val fragment =  AddGuideDialogFragment()
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_container, fragment)
                transaction.addToBackStack(null)
                transaction.commit()

                // Show bottom navigation bar (if needed)
                (requireActivity() as MainActivity).showBottomNavigationBar()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}