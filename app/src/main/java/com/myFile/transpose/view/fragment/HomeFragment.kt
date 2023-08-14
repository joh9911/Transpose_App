package com.myFile.transpose.view.fragment

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.*
import com.myFile.transpose.*
import com.myFile.transpose.view.Activity.Activity
import com.myFile.transpose.view.adapter.HomeNationalPlaylistRecyclerViewAdapter
import com.myFile.transpose.view.adapter.HomePlaylistRecyclerViewAdapter
import com.myFile.transpose.view.adapter.SearchSuggestionKeywordRecyclerViewAdapter
import com.myFile.transpose.databinding.FragmentHomeBinding
import com.myFile.transpose.databinding.MainBinding
import com.myFile.transpose.view.dialog.DialogFragmentPopupAddPlaylist
import com.myFile.transpose.model.model.VideoDataModel
import com.myFile.transpose.viewModel.HomeFragmentViewModelFactory
import com.myFile.transpose.viewModel.HomeViewModel
import com.myFile.transpose.viewModel.SharedViewModel
import java.net.SocketTimeoutException
import java.net.UnknownHostException


class HomeFragment: Fragment() {
    lateinit var mainBinding: MainBinding
    var fbinding: FragmentHomeBinding? = null
    val binding get() = fbinding!!
    lateinit var activity: Activity

    lateinit var homeFragmentToolBar: androidx.appcompat.widget.Toolbar

    private lateinit var nationRecyclerViewAdapter: HomeNationalPlaylistRecyclerViewAdapter
    private lateinit var recommendedPlaylistRecyclerViewAdapter: HomePlaylistRecyclerViewAdapter
    private lateinit var typedPlaylistRecyclerViewAdapter: HomePlaylistRecyclerViewAdapter

    private lateinit var searchSuggestionKeywordAdapter: SearchSuggestionKeywordRecyclerViewAdapter
    lateinit var searchView: SearchView
    lateinit var searchViewItem: MenuItem

    private lateinit var callback: OnBackPressedCallback
    lateinit var frameLayout: FrameLayout

    private lateinit var viewModel: HomeViewModel
    private lateinit var sharedViewModel: SharedViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fbinding = FragmentHomeBinding.inflate(inflater, container, false)
        mainBinding = MainBinding.inflate(layoutInflater)
        frameLayout = binding.searchResultFrameLayout
        val view = binding.root
        showLoadingUI()
        initViewModel()
        initRecyclerView()
        initToolbar()
        initSearchSuggestionKeywordRecyclerView()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserve()
    }
    private fun initViewModel(){
        val application = requireActivity().application as MyApplication
        val viewModelFactory = HomeFragmentViewModelFactory(application)
        viewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
    }


    private fun showLoadingUI(){
        binding.mainScrollView.visibility = View.VISIBLE
        binding.errorLinearLayout.visibility = View.INVISIBLE

        binding.nationPlaylistProgressBar.visibility = View.VISIBLE
        binding.nationPlaylistRecyclerView.visibility = View.GONE

        binding.recommendedPlaylistProgressBar.visibility = View.VISIBLE
        binding.recommendedPlaylistRecyclerView.visibility = View.GONE

    }

    private fun initObserve(){

        viewModel.nationalPlaylist.observe(viewLifecycleOwner){
            binding.nationPlaylistProgressBar.visibility = View.GONE
            binding.nationPlaylistRecyclerView.visibility = View.VISIBLE
            nationRecyclerViewAdapter.submitList(it.toMutableList())
        }

        viewModel.recommendedPlaylists.observe(viewLifecycleOwner){ data ->
            binding.recommendedPlaylistProgressBar.visibility = View.GONE
            binding.recommendedPlaylistRecyclerView.visibility = View.VISIBLE
            recommendedPlaylistRecyclerViewAdapter.submitList(data.toMutableList())
        }

        viewModel.typedPlaylists.observe(viewLifecycleOwner){ data ->
            binding.typedPlaylistProgressBar.visibility = View.GONE
            binding.typedPlaylistRecyclerView.visibility = View.VISIBLE
            typedPlaylistRecyclerViewAdapter.submitList(data.toMutableList())
        }

        viewModel.severErrorMessage.observe(viewLifecycleOwner){
            handleServerError()
        }

        viewModel.errorException.observe(viewLifecycleOwner){ e ->
            handleExceptionError(e)
        }

        viewModel.suggestionKeywords.observe(viewLifecycleOwner){ data ->
            searchSuggestionKeywordAdapter.submitList(data.toMutableList())
        }


    }

    private fun loadAllData() {
        showLoadingUI()
        viewModel.loadAllData()
    }

    private fun handleServerError(){
        binding.mainScrollView.visibility = View.GONE
        binding.errorLinearLayout.visibility = View.VISIBLE
        binding.errorMessage.text = getString(R.string.quota_error_message)
    }

    private fun handleExceptionError(e: Exception){
        binding.mainScrollView.visibility = View.GONE
        binding.errorLinearLayout.visibility = View.VISIBLE
        when(e){
            is UnknownHostException -> binding.errorMessage.text = getString(R.string.network_error_text)
            is SocketTimeoutException -> binding.errorMessage.text = getString(R.string.load_fail_message)
        }
    }

    private fun initRecyclerView(){
        initNationPlaylistRecyclerView()
        initRecommendedPlaylistRecyclerView()
        initTypePlaylistRecyclerView()
        binding.refreshButton.setOnClickListener {
            viewModel.clearPlaylistData()
            loadAllData()
        }
    }

    private fun initNationPlaylistRecyclerView(){
        binding.nationPlaylistRecyclerView.layoutManager = LinearLayoutManager(activity,
            RecyclerView.HORIZONTAL,false)
        nationRecyclerViewAdapter = HomeNationalPlaylistRecyclerViewAdapter()
        nationRecyclerViewAdapter.setItemClickListener(object: HomeNationalPlaylistRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val playlistData = viewModel.nationalPlaylist.value ?: return
                sharedViewModel.setPlaylistsFragmentData(playlistData[position])
                childFragmentManager.beginTransaction()
                    .add(binding.searchResultFrameLayout.id,
                        PlaylistItemsFragment()
                    )
                    .addToBackStack(null)
                    .commit()
            }
        })
        binding.nationPlaylistRecyclerView.adapter = nationRecyclerViewAdapter
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.nationPlaylistRecyclerView)
        binding.nationPlaylistRecyclerView.addItemDecoration(CustomItemDecoration(0))
    }

    private fun initRecommendedPlaylistRecyclerView(){
        binding.recommendedPlaylistRecyclerView.layoutManager = LinearLayoutManager(activity,
            RecyclerView.HORIZONTAL,false)
        recommendedPlaylistRecyclerViewAdapter = HomePlaylistRecyclerViewAdapter()
        recommendedPlaylistRecyclerViewAdapter.setItemClickListener(object: HomePlaylistRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val playlistData = viewModel.recommendedPlaylists.value ?: return
                sharedViewModel.setPlaylistsFragmentData(playlistData[position])
                childFragmentManager.beginTransaction()
                    .add(binding.searchResultFrameLayout.id,
                        PlaylistItemsFragment()
                    )
                    .addToBackStack(null)
                    .commit()
            }
        })
        binding.recommendedPlaylistRecyclerView.adapter = recommendedPlaylistRecyclerViewAdapter
        binding.recommendedPlaylistRecyclerView.addItemDecoration(CustomItemDecoration(0))
    }

    private fun initTypePlaylistRecyclerView(){
        binding.typedPlaylistRecyclerView.layoutManager = LinearLayoutManager(activity,
            RecyclerView.HORIZONTAL,false)
        typedPlaylistRecyclerViewAdapter = HomePlaylistRecyclerViewAdapter()
        typedPlaylistRecyclerViewAdapter.setItemClickListener(object: HomePlaylistRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val playlistData = viewModel.typedPlaylists.value ?: return
                sharedViewModel.setPlaylistsFragmentData(playlistData[position])
                childFragmentManager.beginTransaction()
                    .add(binding.searchResultFrameLayout.id,
                        PlaylistItemsFragment()
                    )
                    .addToBackStack(null)
                    .commit()
            }
        })
        binding.typedPlaylistRecyclerView.adapter = typedPlaylistRecyclerViewAdapter
        binding.typedPlaylistRecyclerView.addItemDecoration(CustomItemDecoration(0))
    }

    inner class CustomItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect, view: View,
            parent: RecyclerView, state: RecyclerView.State
        ) {
            outRect.left = space
        }
    }


    fun showNoticeDialog(videoData: VideoDataModel) {
        // Create an instance of the dialog fragment and show it
        val dialog = DialogFragmentPopupAddPlaylist(videoData)
        dialog.show(activity.supportFragmentManager, "NoticeDialogFragment")
    }


    private fun initSearchSuggestionKeywordRecyclerView(){
        binding.searchSuggestionKeywordRecyclerView.layoutManager = LinearLayoutManager(activity)
        searchSuggestionKeywordAdapter = SearchSuggestionKeywordRecyclerViewAdapter()
        searchSuggestionKeywordAdapter.setItemClickListener(object: SearchSuggestionKeywordRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val searchWord = viewModel.suggestionKeywords.value?.get(position)
                if (searchWord != null && searchWord.isNotEmpty()){
                    sharedViewModel.setSearchKeywordData(searchWord)
                    viewModel.clearSuggestionKeywords()

                    searchView.setQuery(searchWord,false) // 검색한 키워드 텍스트 설정
                    searchView.clearFocus()
                    childFragmentManager.beginTransaction()
                        .add(binding.searchResultFrameLayout.id, SearchResultFragment())
                        .addToBackStack(null)
                        .commit()
                    binding.searchSuggestionKeywordRecyclerView.visibility = View.INVISIBLE
                }
            }
        })
        binding.searchSuggestionKeywordRecyclerView.adapter = searchSuggestionKeywordAdapter
    }

    private fun searchViewCollapseEvent(){
        activity.binding.bottomNavigationView.visibility = View.VISIBLE
        binding.searchSuggestionKeywordRecyclerView.visibility = View.INVISIBLE
        viewModel.clearSuggestionKeywords()
    }

    private fun initToolbar(){
        homeFragmentToolBar = binding.homeFragmentToolBar
        val menu = homeFragmentToolBar.menu
        searchViewItem = menu.findItem(R.id.search_icon)
        searchView = searchViewItem.actionView as SearchView
        val searchAutoComplete = searchView.findViewById<SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text)
        val searchViewCloseButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        searchAutoComplete.setTextColor(resources.getColor(R.color.white))
        searchAutoComplete.setHintTextColor(resources.getColor(R.color.white))
        searchAutoComplete.hint = resources.getString(R.string.searchView_hint)
        searchViewCloseButton.setColorFilter(resources.getColor(R.color.white))
        searchView.setOnQueryTextFocusChangeListener { p0, isClicked -> // 서치뷰 검색창을 클릭할 때 이벤트
            if (isClicked){
                binding.searchSuggestionKeywordRecyclerView.visibility = View.VISIBLE
            }
        }

        searchViewItem.setOnActionExpandListener(object: MenuItem.OnActionExpandListener{
            override fun onMenuItemActionExpand(p0: MenuItem): Boolean {
                // 이걸 꼭 넣어줘야함
                // 쌓인 카운트가 0이 되면 callback을 리무브 해줬음
                // 이 때 다시 검색창을 열 때, 뒤로가기를 누르면 그냥 앱이 꺼짐
                activity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
                if (activity.transposePage.visibility == View.VISIBLE){
                    activity.transposePage.visibility = View.INVISIBLE
                    activity.binding.bottomNavigationView.menu.findItem(R.id.home_icon).isChecked =
                        true
                }
                activity.binding.bottomNavigationView.visibility = View.GONE
                return true
            }
            override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
                Log.d("서치뷰","종료이벤트")
                if (childFragmentManager.backStackEntryCount == 0){
                    callback.remove()
                    searchViewCollapseEvent()
                    return true
                }
                else{
                    if (binding.searchSuggestionKeywordRecyclerView.isVisible)
                        searchViewCollapseEvent()
                    else
                        childFragmentManager.popBackStack()
                    searchView.clearFocus()
                    return false
                }
            }
        })
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query == null || query.isEmpty()) {
                    return false  // 키보드의 기본 동작을 유지하려면 false 반환
                }
                searchView.clearFocus()
                sharedViewModel.setSearchKeywordData(query)

                childFragmentManager.beginTransaction()
                    .add(binding.searchResultFrameLayout.id, SearchResultFragment())
                    .addToBackStack(null)
                    .commit()
                binding.searchSuggestionKeywordRecyclerView.visibility = View.INVISIBLE
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null && newText.isNotBlank())
                    viewModel.getSuggestionKeyword(newText)
                else
                    viewModel.clearSuggestionKeywords()
                return false
            }
        })
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden){
            callback.remove()
        }
        else{
            if (childFragmentManager.backStackEntryCount != 0)
                activity.onBackPressedDispatcher.addCallback(this, callback)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity

        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (childFragmentManager.backStackEntryCount == 0) {
                    if (searchViewItem.isActionViewExpanded){
                        searchViewItem.collapseActionView()
                    }
                }else{
                    if (binding.searchSuggestionKeywordRecyclerView.isVisible)
                        searchViewCollapseEvent()
                    else
                        childFragmentManager.popBackStack()
                }
            }
        }

        childFragmentManager.addOnBackStackChangedListener {
            if (childFragmentManager.backStackEntryCount == 0) {
                if (searchViewItem.isActionViewExpanded)
                    searchViewItem.collapseActionView()
                callback.remove()
            }
            else
                activity.onBackPressedDispatcher.addCallback(this, callback)
        }
    }


    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fbinding = null
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
    }
}