package com.glovo.test.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import com.glovo.test.R
import com.glovo.test.di.interactors.Response
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_select_city.*
import javax.inject.Inject

class SelectCityFragment : BottomSheetDialogFragment() {

    private lateinit var selectCityViewModel: SelectCityViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        selectCityViewModel = ViewModelProviders.of(this, viewModelFactory).get(SelectCityViewModel::class.java)
        selectCityViewModel.apply {

            citiesGroupedByCountriesResponse.observe(this@SelectCityFragment, Observer { response ->
                when (response.status) {
                    Response.Status.SUCCESS -> {
                        hideLoading()
                        response.data?.also { showItems(it) }
                    }
                    Response.Status.ERROR -> {
                        hideLoading()
                        Toast.makeText(context, R.string.error_loading_data, Toast.LENGTH_LONG).show()
                    }
                    Response.Status.LOADING -> {
                        showLoading()
                    }
                }
            })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_select_city, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectCityViewModel.getCitiesGroupedByCountries()
    }

    private fun showItems(items: List<CitiesByCountryAdapter.AdapterItem>) {
        headerTextView.visibility = View.VISIBLE
        val adapter = CitiesByCountryAdapter(items) { cityItem ->
            (activity as MainActivity?)?.onCitySelected(cityItem.code)
            dismissAllowingStateLoss()
        }
        citiesRecyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        citiesRecyclerView.adapter = adapter
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        progressBar.visibility = View.GONE
    }
}