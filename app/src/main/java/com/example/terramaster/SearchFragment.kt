package com.example.terramaster

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class SearchFragment : Fragment() {

    private lateinit var searchView: EditText
    private lateinit var recentListView: ListView
    private lateinit var displayedListView: ListView
    private lateinit var recentHeadingTextView: TextView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var recentAdapter: RecentSearchAdapter
    private lateinit var displayedAdapter: DisplayedSearchAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var recentItems: ArrayList<SearchItem>
    private lateinit var displayedItems: ArrayList<SearchItem>
    private var currentSearchText: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        var advanceSearch: Button = view.findViewById(R.id.advanceSearch)

        advanceSearch.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FragmentAdvanceSearch())
                .addToBackStack(null)
                .commit()
        }
        // Initialize views
        searchView = view.findViewById(R.id.searchView)
        recentListView = view.findViewById(R.id.recentListView)
        displayedListView = view.findViewById(R.id.displayedListView)
        recentHeadingTextView = view.findViewById(R.id.recentHeadingTextView)

        // Initialize Firestore and SharedPreferences
        firestore = FirebaseFirestore.getInstance()
        sharedPreferences = requireActivity().getSharedPreferences("search_prefs", Context.MODE_PRIVATE)

        // Initialize lists and adapters
        recentItems = ArrayList()
        displayedItems = ArrayList()


        recentAdapter = RecentSearchAdapter(requireContext(), recentItems)

        displayedAdapter = DisplayedSearchAdapter(requireContext(), displayedItems, object : OnItemClickListener {
            override fun onItemClick(userId: String) {
                navigateToProfileFragment(userId)
            }
        })

        recentListView.adapter = recentAdapter
        displayedListView.adapter = displayedAdapter



        // Initially hide the displayed ListView
        displayedListView.visibility = View.GONE

        loadRecentSearches()

        // Show keyboard when search field is focused
        searchView.requestFocus()
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT)

        // Add TextWatcher for search input
        searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val searchText = s.toString().trim().lowercase()
                currentSearchText = searchText
                Log.d("Search", "Search text: $searchText")

                if (searchText.isNotEmpty()) {
                    Log.d("Search", "Showing Displayed ListView")
                    recentListView.visibility = View.GONE
                    recentHeadingTextView.visibility = View.GONE
                    advanceSearch.visibility = View.GONE
                    displayedListView.visibility = View.VISIBLE
                    filterNames(searchText)
                } else {
                    Log.d("Search", "Showing Recent ListView")
                    recentListView.visibility = View.VISIBLE
                    recentHeadingTextView.visibility = View.VISIBLE
                    displayedListView.visibility = View.GONE
                    advanceSearch.visibility = View.VISIBLE
                }
            }
        })

        // Handle long click to remove recent search
        recentListView.setOnItemLongClickListener { _, _, position, _ ->
            val searchTextToRemove = recentItems[position].name
            removeRecentSearch(searchTextToRemove)
            true
        }

        if (searchView.text.isEmpty()) {
            recentListView.visibility = View.VISIBLE
            recentHeadingTextView.visibility = View.VISIBLE
        }

        return view
    }



    private fun filterNames(searchText: String) {
        displayedItems.clear()
        val uniqueNames = LinkedHashSet<String>()

        firestore.collection("users").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val firstName = document.getString("first_name") ?: ""
                    val lastName = document.getString("last_name") ?: ""
                    val fullName = "$firstName $lastName".trim()

                    if (fullName.lowercase().startsWith(searchText) && uniqueNames.add(fullName)) {
                        val profilePicUrl = document.getString("profile_picture")
                        val userId = document.getString("uid") ?: ""

                        displayedItems.add(SearchItem(userId, profilePicUrl ?: "", fullName))

                        if (displayedItems.size >= 5) break
                    }
                }
                displayedAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                displayedItems.clear()
                displayedAdapter.notifyDataSetChanged()
            }
    }

    private fun loadRecentSearches() {
        val recentSearches = sharedPreferences.getStringSet("recent_searches", LinkedHashSet()) ?: emptySet()
        recentItems.clear()
        val uniqueItems = LinkedHashSet<String>()

        recentSearches.forEach { searchText ->
            val profilePicUrl = sharedPreferences.getString("${searchText}_profile_pic", "")
            val fullName = sharedPreferences.getString("${searchText}_full_name", "")

            if (!profilePicUrl.isNullOrEmpty() && !fullName.isNullOrEmpty()) {
                uniqueItems.add(searchText)
            }
        }

        uniqueItems.take(5).forEach { searchText ->
            val profilePicUrl = sharedPreferences.getString("${searchText}_profile_pic", "")
            val fullName = sharedPreferences.getString("${searchText}_full_name", "")
            recentItems.add(SearchItem("", profilePicUrl ?: "", fullName ?: ""))
        }

        Log.d("RecentSearches", "Recent searches: $recentSearches")


        recentAdapter.notifyDataSetChanged()
    }

    private fun removeRecentSearch(searchText: String) {
        val recentSearches = sharedPreferences.getStringSet("recent_searches", HashSet())?.toMutableSet()
        recentSearches?.remove(searchText)
        with(sharedPreferences.edit()) {
            putStringSet("recent_searches", recentSearches)
            remove("${searchText}_profile_pic")
            remove("${searchText}_full_name")
            apply()
        }
        recentItems.removeAll { it.name == searchText }
        recentAdapter.notifyDataSetChanged()
    }

    private var listenerRegistration: ListenerRegistration? = null



    override fun onPause() {
        super.onPause()
        listenerRegistration?.remove() // Detach the listener to prevent memory leaks
    }

    private fun navigateToProfileFragment(userId: String){

            val currentUser = FirebaseAuth.getInstance().currentUser
            val fragment = if (currentUser != null && userId == currentUser.uid) {
                FragmentProfile()
            } else {
                FragmentUserProfile()
            }
            val bundle = Bundle().apply { putString("userId", userId) }
            fragment.arguments = bundle

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()

            (requireActivity() as MainActivity).showBottomNavigationBar()

    }
}
