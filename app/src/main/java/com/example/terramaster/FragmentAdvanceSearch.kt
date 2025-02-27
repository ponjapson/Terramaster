package com.example.terramaster

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import android.Manifest


class FragmentAdvanceSearch : Fragment() {
    private lateinit var searchView: EditText
    private lateinit var searchButton: ImageButton
    private lateinit var suggestionRecyclerView: RecyclerView
    private lateinit var adapter: SuggestionAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val firestore = FirebaseFirestore.getInstance()

    private var userLatitude: Double = 0.0
    private var userLongitude: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_advance_search, container, false)

        // Initialize views
        searchView = view.findViewById(R.id.searchView)
        searchButton = view.findViewById(R.id.search_button) // Ensure this ID exists in your layout
        suggestionRecyclerView = view.findViewById(R.id.suggestions_recycler_view)
        suggestionRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = SuggestionAdapter(emptyList())
        suggestionRecyclerView.adapter = adapter

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Get user location on fragment creation (if already available)

        checkLocationPermission()

        // Set up the click listener on the search button
        searchButton.setOnClickListener {

                fetchSurveyors()

        }

        return view
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            // Request permission
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            // Permission already granted - Fetch location
            getUserLocation()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getUserLocation()
        } else {
            Log.e("Permission", "Location permission denied")
        }
    }


    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                userLatitude = it.latitude
                userLongitude = it.longitude
                Log.d("Location", "User Location: $userLatitude, $userLongitude")
                fetchSurveyors()
            }
        }.addOnFailureListener {
            Log.e("Location", "Failed to get user location", it)
        }
    }


    private fun fetchSurveyors() {
        val searchQuery = searchView.text.toString().trim().lowercase()

        Log.d("SearchQuery", "User searched: $searchQuery") // Debugging

        if (searchQuery in listOf("surveyor", "geodetic")) {
            firestore.collection("users")
                .whereEqualTo("user_type", "Surveyor")
                .get()
                .addOnSuccessListener { documents ->
                    val surveyorList = mutableListOf<Suggested>()

                    for (document in documents) {
                        val userId = document.getString("uid") ?: "Unknown"
                        val firstName = document.getString("first_name") ?: "Unknown"
                        val lastName = document.getString("last_name") ?: "Unknown"
                        val profilePic = document.getString("profile_picture") ?: "" // Profile picture URL

                        // ✅ Get latitude & longitude separately
                        val latitude = document.getDouble("latitude") ?: 0.0
                        val longitude = document.getDouble("longitude") ?: 0.0

                        // ✅ Calculate distance
                        val distance = calculateDistance(userLatitude, userLongitude, latitude, longitude)

                        // ✅ Add to list
                        surveyorList.add(Suggested(firstName, lastName, profilePic, latitude, longitude, distance, userId))
                    }

                    // ✅ Sort by nearest distance
                    surveyorList.sortBy { it.distance }

                    // ✅ Update RecyclerView
                    adapter.updateList(surveyorList)
                }
                .addOnFailureListener {
                    Log.e("Firestore", "Failed to fetch surveyors", it)
                }
        } else {
            Log.d("Search", "Search query did not match expected keywords.") // Debugging
        }
    }


    // Calculate the distance between two points (in kilometers)
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0] / 1000.0 // Convert meters to kilometers
    }
}
