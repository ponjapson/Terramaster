package com.example.terramaster

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.terramaster.databinding.ActivityBookingBinding.inflate
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects

class FragmentHome: Fragment() {
    private lateinit var btnAddGuide: Button
    private lateinit var rvGuide: RecyclerView
    private lateinit var guideAdapter: GuideAdapter
    private val guideList = mutableListOf<Guide>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val searchEditText = view.findViewById<EditText>(R.id.searchEditText)

        btnAddGuide = view.findViewById(R.id.addButton)
        rvGuide = view.findViewById(R.id.recyclerViewKnowledge)

        guideAdapter = GuideAdapter(guideList)
        rvGuide.adapter = guideAdapter
        rvGuide.layoutManager = LinearLayoutManager(requireContext())

        btnAddGuide.setOnClickListener {
            val dialog = AddGuideDialogFragment()
            dialog.show(parentFragmentManager, "AddGuideDialogFragment")
        }

        searchEditText.setOnClickListener {
            val fragment = SearchFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, fragment)
            transaction.addToBackStack(null)
            transaction.commit()

            (requireActivity() as MainActivity).showBottomNavigationBar()
        }

        LoadGuideFromFirestore()


        return view
    }

    private fun LoadGuideFromFirestore(){
        var db = FirebaseFirestore.getInstance()

        db.collection("knowledge_guide").addSnapshotListener {snapshots, error ->
            if(error != null)
            {
                return@addSnapshotListener
            }
            if(snapshots != null)
            {
                guideList.clear()
                for(document in snapshots.documents)
                {
                    val guide = document.toObject(Guide::class.java)
                    if(guide != null) {
                        guideList.add(guide)
                    }
                }
            }


            guideAdapter.notifyDataSetChanged()
        }
    }
}