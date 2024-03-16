package com.myFile.transpose.view.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.myFile.transpose.MyApplication
import com.myFile.transpose.R
import com.myFile.transpose.databinding.FragmentHomeBinding
import com.myFile.transpose.databinding.FragmentSuggestionKeywordBinding
import com.myFile.transpose.others.constants.Actions.TAG
import com.myFile.transpose.view.Activity.Activity
import com.myFile.transpose.view.adapter.SearchSuggestionKeywordRecyclerViewAdapter
import com.myFile.transpose.viewModel.HomeFragmentViewModelFactory
import com.myFile.transpose.viewModel.HomeViewModel
import com.myFile.transpose.viewModel.SharedViewModel
import com.myFile.transpose.viewModel.SuggestionKeywordViewModel
import com.myFile.transpose.viewModel.SuggestionKeywordViewModelFactory

class SuggestionKeywordFragment: Fragment() {
    var fbinding: FragmentSuggestionKeywordBinding? = null
    val binding get() = fbinding!!
    lateinit var activity: Activity
    private lateinit var searchSuggestionKeywordAdapter: SearchSuggestionKeywordRecyclerViewAdapter

    private lateinit var viewModel: SuggestionKeywordViewModel
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fbinding = FragmentSuggestionKeywordBinding.inflate(inflater, container, false)
        initViewModel()
        initSearchSuggestionKeywordRecyclerView()
        Log.d(TAG,"suggestionkeyword, oncreate")
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initObserve()
        sharedViewModel.fromChildFragmentInNavFragment.value = findNavController().currentDestination?.id
        super.onViewCreated(view, savedInstanceState)
    }


    private fun initViewModel(){
        val application = requireActivity().application as MyApplication
        val viewModelFactory = SuggestionKeywordViewModelFactory(application)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory)[SuggestionKeywordViewModel::class.java]
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
    }

    private fun initObserve(){
        sharedViewModel.suggestionKeywords.observe(viewLifecycleOwner){ data ->
            val items = mutableListOf<SearchSuggestionKeywordRecyclerViewAdapter.SuggestionKeywordViewType>()
//            items.add(SearchSuggestionKeywordRecyclerViewAdapter.SuggestionKeywordViewType.SavedSuggestionKeywordItem)
            items.addAll(data.map{SearchSuggestionKeywordRecyclerViewAdapter.SuggestionKeywordViewType.SuggestionKeywordItem(it)})
            searchSuggestionKeywordAdapter.submitList(items)
        }
    }
    private fun initSearchSuggestionKeywordRecyclerView(){
        binding.searchSuggestionKeywordRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
        searchSuggestionKeywordAdapter = SearchSuggestionKeywordRecyclerViewAdapter()
        searchSuggestionKeywordAdapter.setItemClickListener(object: SearchSuggestionKeywordRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val suggestionKeywords = sharedViewModel.suggestionKeywords.value
                if (!suggestionKeywords.isNullOrEmpty()){
                    val searchWord = suggestionKeywords[position]

                    sharedViewModel.searchKeyword = searchWord
                    sharedViewModel.clearSuggestionKeywords()

                    activity.collapseSearchView()

                    findNavController().navigate(R.id.searchResultFragment)
                }

            }

            override fun savedKeywordClick(v: View, position: Int) {
                Log.d(TAG,"저장된 검색어 클릭함")
            }
        })
        binding.searchSuggestionKeywordRecyclerView.adapter = searchSuggestionKeywordAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG,"suggestionkeyword, ondestoryview")
    }
}