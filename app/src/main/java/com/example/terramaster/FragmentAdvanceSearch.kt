package com.example.terramaster

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yourapp.Suggested
import com.google.firebase.firestore.FirebaseFirestore

class FragmentAdvanceSearch : Fragment() {
    private lateinit var searchButton: ImageButton
    private lateinit var searchView: EditText
    private lateinit var locationInput: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SuggestionAdapter
    private val suggestionList = mutableListOf<Suggested>()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_advance_search, container, false)

        searchButton = view.findViewById(R.id.search_button)
        searchView = view.findViewById(R.id.searchView)
        locationInput = view.findViewById(R.id.location_input)
        recyclerView = view.findViewById(R.id.suggestions_recycler_view)

        adapter = SuggestionAdapter(suggestionList, requireActivity()){ userId ->
            val bundle = Bundle().apply {
                putString("userId", userId) // Pass userId to the profile fragment
            }

            val profileFragment = FragmentUserProfile() // Replace with actual profile fragment
            profileFragment.arguments = bundle

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, profileFragment) // Make sure R.id.fragment_container exists
                .addToBackStack(null)
                .commit()
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        searchButton.setOnClickListener {
            val location = locationInput.text.toString().trim()
            val searchQuery = searchView.text.toString().trim()

            if (location.isNotEmpty()) {
                convertLocationToCoordinates(location) { lat, lon ->
                    fetchNearestSurveyors(searchQuery, lat, lon)
                }
            } else {
                Toast.makeText(requireContext(), "Please enter a location", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun fetchNearestSurveyors(query: String, lat: Double, lon: Double) {
        firestore.collection("users")
            .whereEqualTo("user_type", "Surveyor")
            .get()
            .addOnSuccessListener { documents ->
                suggestionList.clear()
                val tempList = mutableListOf<Suggested>()

                for (doc in documents) {
                    val firstName = doc.getString("first_name") ?: ""
                    val lastName = doc.getString("last_name") ?: ""
                    val profileUrl = doc.getString("profile_picture") ?: ""
                    val userType = doc.getString("user_type") ?: ""
                    val surveyorLat = doc.getDouble("latitude") ?: 0.0
                    val surveyorLon = doc.getDouble("longitude") ?: 0.0
                    val userId = doc.getString("uid") ?: ""
                    val distance = calculateDistance(lat, lon, surveyorLat, surveyorLon)

                    if (distance <= 50.0) {
                        convertCoordinatesToAddress(surveyorLat, surveyorLon) { address ->
                            val surveyor = Suggested(firstName, lastName, userType, address, profileUrl, distance, surveyorLat, surveyorLon, userId)
                            tempList.add(surveyor)

                            // Update list after all conversions
                            if (tempList.size == documents.size()) {
                                suggestionList.clear()
                                suggestionList.addAll(tempList.sortedBy { it.distance })
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error fetching surveyors", Toast.LENGTH_SHORT).show()
            }
    }

    private fun convertLocationToCoordinates(locationName: String, callback: (Double, Double) -> Unit) {
        val geocoder = OpenStreetMapGeocoder(requireContext())
        geocoder.getCoordinatesFromAddress(locationName) { coordinates ->
            if (coordinates != null) {
                callback(coordinates.latitude, coordinates.longitude)
            } else {
                Toast.makeText(requireContext(), "Location not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun convertCoordinatesToAddress(lat: Double, lon: Double, callback: (String) -> Unit) {
        val geocoder = OpenStreetMapGeocoder(requireContext())
        geocoder.getAddressFromCoordinates(lat, lon) { address ->
            callback(address ?: "Unknown Address")
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }
}
