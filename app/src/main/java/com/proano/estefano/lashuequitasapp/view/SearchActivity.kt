package com.proano.estefano.lashuequitasapp.view

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.proano.estefano.lashuequitasapp.R
import com.proano.estefano.lashuequitasapp.model.businesslogic.ResenaRepository
import com.proano.estefano.lashuequitasapp.model.entities.Restaurant

class SearchActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var recyclerViewSearchResults: RecyclerView
    private lateinit var noResultsMessage: TextView
    private lateinit var resenaRepository: ResenaRepository
    private lateinit var searchResultsAdapter: RestaurantSearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_search_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        resenaRepository = ResenaRepository(this)

        initViews()
        setupListeners()
        setupRecyclerView()
    }

    private fun initViews() {
        searchEditText = findViewById(R.id.searchEditText)
        recyclerViewSearchResults = findViewById(R.id.recyclerViewSearchResults)
        noResultsMessage = findViewById(R.id.noResultsMessage)
        val btnBack: ImageButton = findViewById(R.id.btnBack)
        btnBack.setOnClickListener { finish() }
    }

    private fun setupListeners() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                performSearch(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupRecyclerView() {
        // Pasar resenaRepository al adapter
        searchResultsAdapter = RestaurantSearchAdapter(emptyList(), resenaRepository)
        recyclerViewSearchResults.layoutManager = LinearLayoutManager(this)
        recyclerViewSearchResults.adapter = searchResultsAdapter
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) {
            searchResultsAdapter.updateData(emptyList())
            noResultsMessage.visibility = View.GONE
            return
        }

        // Use buscarRestaurantsPorNombre from ResenaRepository for direct restaurant search
        val results = resenaRepository.buscarRestaurantsPorNombre(query)

        if (results.isEmpty()) {
            noResultsMessage.visibility = View.VISIBLE
        } else {
            noResultsMessage.visibility = View.GONE
        }
        searchResultsAdapter.updateData(results)
    }
}