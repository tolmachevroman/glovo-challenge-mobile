package com.glovo.test.ui.fragments

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
import com.glovo.test.common.rx.ResponseObserver
import com.glovo.test.di.api.Response
import com.glovo.test.ui.activities.MainActivity
import com.glovo.test.ui.adapters.CitiesByCountryAdapter
import com.glovo.test.ui.viewmodels.SelectCityViewModel
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

            /**
             * Observes list of adapter items
             */
            citiesGroupedByCountriesResponse.observe(this@SelectCityFragment,
                ResponseObserver(this@SelectCityFragment.javaClass.name,
                    onSuccess = { adapterItems ->
                        showItems(adapterItems)
                    },
                    onError = {
                        showErrorToast()
                    },
                    showLoading = {
                        showLoading()
                    },
                    hideLoading = {
                        hideLoading()
                    }
                ))
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

    private fun showErrorToast() {
        Toast.makeText(context, R.string.error_loading_data, Toast.LENGTH_LONG).show()
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        progressBar.visibility = View.GONE
    }
}