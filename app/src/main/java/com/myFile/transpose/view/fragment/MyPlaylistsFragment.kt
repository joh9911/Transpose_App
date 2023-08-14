package com.myFile.transpose.view.fragment

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.myFile.transpose.*
import com.myFile.transpose.view.Activity.Activity
import com.myFile.transpose.view.adapter.MyPlaylistRecyclerViewAdapter
import com.myFile.transpose.view.adapter.SearchSuggestionKeywordRecyclerViewAdapter
import com.myFile.transpose.database.MyPlaylist
import com.myFile.transpose.databinding.FragmentMyPlaylistBinding
import com.myFile.transpose.databinding.MainBinding
import com.myFile.transpose.view.dialog.DialogCreatePlaylist
import com.myFile.transpose.viewModel.MyPlaylistsViewModel
import com.myFile.transpose.viewModel.MyPlaylistsViewModelFactory
import com.myFile.transpose.viewModel.SharedViewModel

class MyPlaylistsFragment: Fragment() {
    lateinit var mainBinding: MainBinding
    var fbinding: FragmentMyPlaylistBinding? = null
    val binding get() = fbinding!!
    lateinit var activity: Activity

    lateinit var playlistToolBar: androidx.appcompat.widget.Toolbar

    private lateinit var searchKeywordRecyclerAdapter: SearchSuggestionKeywordRecyclerViewAdapter
    private lateinit var myPlaylistRecyclerViewAdapter: MyPlaylistRecyclerViewAdapter
    lateinit var searchView: SearchView
    lateinit var searchViewItem: MenuItem
    private lateinit var callback: OnBackPressedCallback

    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var myPlaylistViewModel: MyPlaylistsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fbinding = FragmentMyPlaylistBinding.inflate(inflater, container, false)
        mainBinding = MainBinding.inflate(layoutInflater)
//        setHasOptionsMenu(true)
        initViewModel()
        val view = binding.root
        initRecyclerView()
        initToolbar()
        addPlaylistButtonEvent()
        initPlaylistRecyclerView()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        myPlaylistViewModel.getAllPlaylist()
    }
    private fun initViewModel(){
        val application = requireActivity().application as MyApplication
        val viewModelFactory = MyPlaylistsViewModelFactory(application)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        myPlaylistViewModel = ViewModelProvider(this, viewModelFactory)[MyPlaylistsViewModel::class.java]
    }

    private fun initObserver(){
        myPlaylistViewModel.myPlaylists.observe(viewLifecycleOwner){ myPlaylists ->
            myPlaylistRecyclerViewAdapter.submitList(myPlaylists.toMutableList())
        }
        myPlaylistViewModel.suggestionKeywords.observe(viewLifecycleOwner){ suggestionKeywords ->
            searchKeywordRecyclerAdapter.submitList(suggestionKeywords.toMutableList())
        }
    }

    private fun deleteMyPlaylistByPosition(position: Int){
        val myPlaylist = myPlaylistViewModel.myPlaylists.value?.get(position)!!
        myPlaylistViewModel.deleteMyPlaylist(myPlaylist)
    }

    private fun addMyPlaylist(myPlaylist: MyPlaylist){
        myPlaylistViewModel.addMyPlaylist(myPlaylist)
    }

    private fun initPlaylistRecyclerView(){
        binding.playlistRecyclerView.layoutManager = LinearLayoutManager(activity)
        myPlaylistRecyclerViewAdapter = MyPlaylistRecyclerViewAdapter()
        myPlaylistRecyclerViewAdapter.setItemClickListener(object: MyPlaylistRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val myPlaylists = myPlaylistViewModel.myPlaylists.value ?: return
                sharedViewModel.setMyPlaylistId(myPlaylists[position].uid)
                sharedViewModel.myPlaylistTitle = myPlaylists[position].playlistTitle
                childFragmentManager.beginTransaction()
                    .replace(binding.resultFrameLayout.id,
                        MyPlaylistItemsFragment()
                    )
                    .addToBackStack(null)
                    .commit()
            }

            override fun optionButtonClick(v: View, position: Int) {
                Log.d("버튼번호","$position")
                val popUp = PopupMenu(activity, v)
                popUp.menuInflater.inflate(R.menu.my_playlist_pop_up_menu, popUp.menu)
                popUp.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.delete_my_playlist -> {
                            deleteMyPlaylistByPosition(position)
                        }
                    }
                    true
                }
                popUp.show()
            }
        })
        binding.playlistRecyclerView.adapter = myPlaylistRecyclerViewAdapter
    }
    private fun initRecyclerView(){
        binding.searchSuggestionKeywordRecyclerView.layoutManager = LinearLayoutManager(activity)
        searchKeywordRecyclerAdapter = SearchSuggestionKeywordRecyclerViewAdapter()
        searchKeywordRecyclerAdapter.setItemClickListener(object: SearchSuggestionKeywordRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val searchWord = myPlaylistViewModel.suggestionKeywords.value?.get(position)
                if (searchWord != null && searchWord.isNotEmpty()){
                    sharedViewModel.setSearchKeywordData(searchWord)
                    myPlaylistViewModel.clearSuggestionKeywords()

                    searchView.setQuery(searchWord,false) // 검색한 키워드 텍스트 설정
                    searchView.clearFocus()
                    childFragmentManager.beginTransaction()
                        .add(binding.resultFrameLayout.id, SearchResultFragment())
                        .addToBackStack(null)
                        .commit()
                    binding.searchSuggestionKeywordRecyclerView.visibility = View.INVISIBLE
                }
            }
        })
        binding.searchSuggestionKeywordRecyclerView.adapter = searchKeywordRecyclerAdapter
    }

    private fun addPlaylistButtonEvent(){
        binding.addPlaylistLinearLayout.setOnClickListener {
            showNoticeDialog()
        }
    }
    private fun showNoticeDialog() {
        // Create an instance of the dialog fragment and show it
        val dialog = DialogCreatePlaylist()

        dialog.setListener(object: DialogCreatePlaylist.NoticeDialogListener {
            override fun onDialogPositiveClick(dialog: DialogFragment, text: Editable?) {
                addMyPlaylist(MyPlaylist(0, "$text"))
            }

            override fun onDialogNegativeClick(dialog: DialogFragment) {
                dialog.dismiss()}
        })
        dialog.show(activity.supportFragmentManager, "NoticeDialogFragment")
    }

    private fun initToolbar(){
        playlistToolBar = binding.playlistToolBar
        val menu = playlistToolBar.menu
        searchViewItem = menu.findItem(R.id.search_icon)
        searchView = searchViewItem.actionView as SearchView
        searchView.setIconifiedByDefault(true)
        val searchAutoComplete = searchView.findViewById<SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text)
        val searchViewCloseButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        val searchViewBackButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
        searchAutoComplete.setTextColor(resources.getColor(R.color.white))
        searchAutoComplete.setHintTextColor(resources.getColor(R.color.white))
        searchAutoComplete.hint = resources.getString(R.string.searchView_hint)
        searchViewCloseButton.setColorFilter(resources.getColor(R.color.white))
        searchViewBackButton.setColorFilter(resources.getColor(R.color.white))
        searchView.setOnQueryTextFocusChangeListener { p0, p1 -> // 서치뷰 검색창을 클릭할 때 이벤트
            if (p1){
                Log.d("눌림","ㄴㅇㄹ")
                binding.searchSuggestionKeywordRecyclerView.visibility = View.VISIBLE
            }
        }
        searchViewItem.setOnActionExpandListener(object: MenuItem.OnActionExpandListener{
            override fun onMenuItemActionExpand(p0: MenuItem): Boolean {
                activity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

                if (activity.transposePage.visibility == View.VISIBLE){
                    activity.transposePage.visibility = View.INVISIBLE
                    activity.binding.bottomNavigationView.menu.findItem(R.id.home_icon).isChecked =
                        true
                }
                Log.d("호갖","장")
                activity.binding.bottomNavigationView.visibility = View.GONE
                return true
            }
            override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
                Log.d("playlist","서치뷰닫힘")
                if (childFragmentManager.backStackEntryCount == 0){
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
//                if (activity.supportFragmentManager.findFragmentById(R.id.player_fragment) == null) {
//                    return if (activity.transposePage.visibility == View.VISIBLE) {
//                        Log.d("1","1")
//                        activity.transposePageInvisibleEvent()
//                        false
//                    } else{
//                        searchViewCollapseEvent()
//                        true
//                    }
//                } else {
//                    val playerFragment =
//                        activity.supportFragmentManager.findFragmentById(activity.binding.playerFragment.id) as PlayerFragment
//                    return if (playerFragment.binding.playerMotionLayout.currentState == R.id.end) {
//                        playerFragment.binding.playerMotionLayout.transitionToState(R.id.start)
//                        false
//                    } else {
//                        if (activity.transposePage.visibility == View.VISIBLE) {
//                            Log.d("1","2")
//                            activity.transposePageInvisibleEvent()
//                            false
//                        } else{
//                            searchViewCollapseEvent()
//                            true
//                        }
//                    }
//                }
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
                    .add(binding.resultFrameLayout.id, SearchResultFragment())
                    .addToBackStack(null)
                    .commit()
                binding.searchSuggestionKeywordRecyclerView.visibility = View.INVISIBLE
                return true
            }
            //SwipeRefreshLayout 새로고침
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null && newText.isNotBlank())
                    myPlaylistViewModel.getSuggestionKeyword(newText)
                else
                    myPlaylistViewModel.clearSuggestionKeywords()
                return false
            }
        })
    }

    private fun searchViewCollapseEvent(){
        Log.d("이게 실행이 됏잖아","왜")
//        binding.toolBar.setBackgroundColor(resources.getColor(R.color.black))
        activity.binding.bottomNavigationView.visibility = View.VISIBLE
        binding.searchSuggestionKeywordRecyclerView.visibility = View.INVISIBLE
        myPlaylistViewModel.clearSuggestionKeywords()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            callback.remove()
        } else {
            activity.onBackPressedDispatcher.addCallback(this, callback)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d("myPlaylistFragment","backPress")
                if (childFragmentManager.backStackEntryCount == 0) {
                    if (searchViewItem.isActionViewExpanded)
                        searchViewItem.collapseActionView()
                    else{
                        activity.supportFragmentManager.beginTransaction().show(activity.homeFragment).commit()
                        activity.supportFragmentManager.beginTransaction().hide(activity.myPlaylistFragment).commit()
                        activity.binding.bottomNavigationView.menu.findItem(R.id.home_icon).isChecked = true
                    }
                }
                else{
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
            }
            else
                activity.onBackPressedDispatcher.addCallback(this, callback)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fbinding = null
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }
}