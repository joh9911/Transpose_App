package com.myFile.transpose.fragment

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myFile.transpose.*
import com.myFile.transpose.retrofit.*
import com.myFile.transpose.adapter.HomePlaylistRecyclerViewAdapter
import com.myFile.transpose.adapter.HomePopular100RecyclerViewAdapter
import com.myFile.transpose.adapter.SearchSuggestionKeywordRecyclerViewAdapter
import com.myFile.transpose.dto.PlayListSearchData
import com.myFile.transpose.dto.PlayListVideoSearchData
import com.myFile.transpose.databinding.FragmentHomeBinding
import com.myFile.transpose.databinding.MainBinding
import com.myFile.transpose.dialog.DialogFragmentPopupAddPlaylist
import com.myFile.transpose.model.*
import com.myFile.transpose.repository.MusicCategoryRepository
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*
import kotlin.collections.ArrayList


class HomeFragment: Fragment() {
    lateinit var mainBinding: MainBinding
    var fbinding: FragmentHomeBinding? = null
    val binding get() = fbinding!!
    lateinit var activity: Activity

    lateinit var homeFragmentToolBar: androidx.appcompat.widget.Toolbar
    val suggestionKeywords = ArrayList<String>()


    val popularTop100Playlist = ArrayList<VideoData>()
    val thisYearPlaylistData = ArrayList<PlayListData>()
    val todayHotPlaylistData = ArrayList<PlayListData>()
    val latestMusicPlaylistData = ArrayList<PlayListData>()
    val bestAtmospherePlaylistData = ArrayList<PlayListData>()
    val bestSituationPlaylistData = ArrayList<PlayListData>()


    private lateinit var thisYearPlaylistAdapter: HomePlaylistRecyclerViewAdapter
    private lateinit var latestMusicPlaylistAdapter: HomePlaylistRecyclerViewAdapter
    private lateinit var todayHotPlaylistAdapter: HomePlaylistRecyclerViewAdapter
    private lateinit var bestAtmospherePlaylistAdapter: HomePlaylistRecyclerViewAdapter
    private lateinit var bestSituationPlaylistAdapter: HomePlaylistRecyclerViewAdapter
    private lateinit var popular100PlaylistAdapter: HomePopular100RecyclerViewAdapter

    private lateinit var searchSuggestionKeywordAdapter: SearchSuggestionKeywordRecyclerViewAdapter
    lateinit var searchView: SearchView
    lateinit var searchViewItem: MenuItem

    private lateinit var callback: OnBackPressedCallback
    lateinit var frameLayout: FrameLayout

    private val coroutineExceptionHandler = CoroutineExceptionHandler{ _, throwable ->
        lifecycleScope.launch {
            onError(throwable)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fbinding = FragmentHomeBinding.inflate(inflater, container, false)
        mainBinding = MainBinding.inflate(layoutInflater)
        frameLayout = binding.searchResultFrameLayout
        val view = binding.root
        initRecyclerView()
        initToolbar()
        initSearchSuggestionKeywordRecyclerView()
        getAllData()
//        getTempData()
        return view
    }

    private fun initRecyclerView(){
        initPopularTop100RecyclerView()
        initBestAtmospherePlaylistRecyclerView()
        initBestSituationPlaylistRecyclerView()
        initLatestMusicPlaylistRecyclerView()
//        initThisYearPlaylistRecyclerView()
        initTodayHotListPlaylistRecyclerView()
        binding.refreshButton.setOnClickListener {
            getAllData()
        }
    }

//    private fun initThisYearPlaylistRecyclerView(){
//        binding.thisYearPlaylistRecyclerView.layoutManager = LinearLayoutManager(activity,
//            RecyclerView.HORIZONTAL,false)
//        thisYearPlaylistAdapter = HomePlaylistRecyclerViewAdapter()
//        thisYearPlaylistAdapter.setItemClickListener(object: HomePlaylistRecyclerViewAdapter.OnItemClickListener{
//            var mLastClickTime = 0L
//            override fun onClick(v: View, position: Int) {
//                val playlistData = thisYearPlaylistData[position]
//                val bundle = Bundle().apply {
//                    putParcelable("playlistItemsFragment", playlistData)
//                }
//                val playlistItemsFragment = PlaylistItemsFragment().apply {
//                    arguments = bundle
//                }
//                if (SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
//                    childFragmentManager.beginTransaction().add(binding.searchResultFrameLayout.id,
//                        playlistItemsFragment
//                    )
//                        .addToBackStack(null)
//                        .commit()
//                }
//                mLastClickTime = SystemClock.elapsedRealtime()
//            }
//        })
//        thisYearPlaylistAdapter.submitList(thisYearPlaylistData)
//        binding.thisYearPlaylistRecyclerView.adapter = thisYearPlaylistAdapter
//    }

    private fun initTodayHotListPlaylistRecyclerView(){
        binding.todayHotListPlaylistRecyclerView.layoutManager = LinearLayoutManager(activity,
            RecyclerView.HORIZONTAL,false)
        todayHotPlaylistAdapter = HomePlaylistRecyclerViewAdapter()
        todayHotPlaylistAdapter.setItemClickListener(object: HomePlaylistRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val playlistData = todayHotPlaylistData[position]

                val playlistItemsFragment = PlaylistItemsFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable("playlistItemsFragment", playlistData)
                    }
                }
                childFragmentManager.beginTransaction()
                    .add(binding.searchResultFrameLayout.id,
                        playlistItemsFragment
                    )
                    .addToBackStack(null)
                    .commit()
            }
        })
        todayHotPlaylistAdapter.submitList(todayHotPlaylistData)
        binding.todayHotListPlaylistRecyclerView.adapter = todayHotPlaylistAdapter
    }

    private fun initLatestMusicPlaylistRecyclerView(){
        binding.latestMusicPlaylistRecyclerView.layoutManager = LinearLayoutManager(activity,
            RecyclerView.HORIZONTAL,false)
        latestMusicPlaylistAdapter = HomePlaylistRecyclerViewAdapter()
        latestMusicPlaylistAdapter.setItemClickListener(object: HomePlaylistRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val playlistData = latestMusicPlaylistData[position]

                val playlistItemsFragment = PlaylistItemsFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable("playlistItemsFragment", playlistData)
                    }
                }
                childFragmentManager.beginTransaction()
                    .add(binding.searchResultFrameLayout.id,
                        playlistItemsFragment
                    )
                    .addToBackStack(null)
                    .commit()
            }
        })
        latestMusicPlaylistAdapter.submitList(latestMusicPlaylistData)
        binding.latestMusicPlaylistRecyclerView.adapter = latestMusicPlaylistAdapter
    }

    private fun initBestAtmospherePlaylistRecyclerView(){
        binding.bestAtmospherePlaylistRecyclerView.layoutManager = LinearLayoutManager(activity,
            RecyclerView.HORIZONTAL,false)
        bestAtmospherePlaylistAdapter = HomePlaylistRecyclerViewAdapter()
        bestAtmospherePlaylistAdapter.setItemClickListener(object: HomePlaylistRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val playlistData = bestAtmospherePlaylistData[position]

                val playlistItemsFragment = PlaylistItemsFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable("playlistItemsFragment", playlistData)
                    }
                }
                childFragmentManager.beginTransaction()
                    .add(binding.searchResultFrameLayout.id,
                        playlistItemsFragment
                    )
                    .addToBackStack(null)
                    .commit()
            }
        })
        bestAtmospherePlaylistAdapter.submitList(bestAtmospherePlaylistData)
        binding.bestAtmospherePlaylistRecyclerView.adapter = bestAtmospherePlaylistAdapter
    }

    private fun initBestSituationPlaylistRecyclerView(){
        binding.bestSituationPlaylistRecyclerView.layoutManager = LinearLayoutManager(activity,
            RecyclerView.HORIZONTAL,false)
        bestSituationPlaylistAdapter = HomePlaylistRecyclerViewAdapter()
        bestSituationPlaylistAdapter.setItemClickListener(object: HomePlaylistRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val playlistData = bestSituationPlaylistData[position]

                val playlistItemsFragment = PlaylistItemsFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable("playlistItemsFragment", playlistData)
                    }
                }

                childFragmentManager.beginTransaction()
                    .add(binding.searchResultFrameLayout.id,
                        playlistItemsFragment
                    )
                    .addToBackStack(null)
                    .commit()
            }
        })
        bestSituationPlaylistAdapter.submitList(bestSituationPlaylistData)
        binding.bestSituationPlaylistRecyclerView.adapter = bestSituationPlaylistAdapter
    }

    private fun initPopularTop100RecyclerView(){
        val gridLayoutManager = GridLayoutManager(activity,4, GridLayoutManager.HORIZONTAL,false)
        binding.popularTop100PlaylistVideoRecyclerView.layoutManager = gridLayoutManager
        popular100PlaylistAdapter = HomePopular100RecyclerViewAdapter()
        popular100PlaylistAdapter.setItemClickListener(object: HomePopular100RecyclerViewAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                val playlistModel = PlaylistModel(resources.getString(R.string.popular_Top100_playlist_text),popularTop100Playlist, position)
                val videoData = popularTop100Playlist[position]
                val playerFragmentBundle = PlayerFragmentBundle(videoData, playlistModel)

                val playerFragment = PlayerFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable("playerFragment", playerFragmentBundle)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(
                        activity.binding.playerFragment.id,
                        playerFragment
                    )
                    .commit()
            }
            override fun optionButtonClick(v: View, position: Int) {
                val popUp = PopupMenu(activity, v)
                popUp.menuInflater.inflate(R.menu.video_pop_up_menu, popUp.menu)
                popUp.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.add_my_playlist -> {
                            showNoticeDialog(popularTop100Playlist[position])
                        }
                    }
                    true
                })
                popUp.show()
            }
        })
        popular100PlaylistAdapter.submitList(popularTop100Playlist)
        binding.popularTop100PlaylistVideoRecyclerView.adapter = popular100PlaylistAdapter
    }

    fun showNoticeDialog(videoData: VideoData) {
        // Create an instance of the dialog fragment and show it
        val dialog = DialogFragmentPopupAddPlaylist(videoData)
        dialog.show(activity.supportFragmentManager, "NoticeDialogFragment")
    }

//    private fun getTempData(){
//        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch {
//            getPlaylistTitleData()
//        }
//    }
//
//    suspend fun getPlaylistTitleData(){
//        val retrofit = RetrofitData.initRetrofit()
//        val response = retrofit.create(RetrofitService::class.java)
//            .getPlayListsInChannel(BuildConfig.API_KEY, "snippet", "UCrKZcyOJVWnJ60zM1XWllNw", "50", token)
//        if (response.isSuccessful){
//            if (response.body()?.items?.size != 0) {
//                withContext(Dispatchers.Main){
//                    val items = response.body()?.items!!
//                    for (index in items.indices){
//                        Log.d("타이틀", items[index].snippet?.title!!)
//                    }
//                    token = response.body()?.nextPageToken
//                    if (token != null){
//                        getTempData()
//                    }
//                }
//            }
//        }
//    }

    private fun getAllData(){
        binding.mainScrollView.visibility = View.VISIBLE
        binding.errorLinearLayout.visibility = View.INVISIBLE
        val musicCategoryRepository = MusicCategoryRepository()
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch {
            async { getPopularTop100MusicData(null, musicCategoryRepository.getPopularTop100MusicId()) }
            async { getPlaylistData(musicCategoryRepository.getTodayHotMusicIdArray()) }
            async { getPlaylistData(musicCategoryRepository.getLatestMusicIdArray()) }
            async { getPlaylistData(musicCategoryRepository.getBestAtmosphereMusicIdArray()) }
            async { getPlaylistData(musicCategoryRepository.getBestSituationMusicIdArray()) }
        }
    }

    private suspend fun onError(throwable: Throwable){
        withContext(Dispatchers.Main){
            Log.d("코루틴 에러","$throwable")
            fbinding?.mainScrollView?.visibility = View.GONE
            fbinding?.errorLinearLayout?.visibility = View.VISIBLE
            when (throwable){
                is UnknownHostException -> fbinding?.errorMessage?.text = activity.getText(R.string.network_error_text)
                is SocketTimeoutException -> fbinding?.errorMessage?.text = activity.getText(R.string.load_fail_message)
                else -> fbinding?.errorMessage?.text = activity.getText(R.string.quota_error_message)
            }
        }
    }


    private suspend fun getPlaylistData(musicUrls: Array<String>) {
        for (musicId in musicUrls) {
            val random = Random()
            val keyArr = arrayListOf(BuildConfig.API_KEYjohtjdals9911, BuildConfig.API_KEY)
            val num = random.nextInt(keyArr.size)
            val retrofit = RetrofitData.initRetrofit()
            val response = retrofit.create(RetrofitService::class.java)
                .getPlayLists(keyArr[num], "snippet", musicId, "50")
            if (response.isSuccessful){
                if (response.body()?.items?.size != 0) {
                    withContext(Dispatchers.Main){
                        playlistDataMapping(musicId, response.body()!!)
                    }
                }
            }
        }
    }

    private fun playlistDataMapping(musicId: String, responseData: PlayListSearchData){
        val thumbnail = responseData.items[0].snippet?.thumbnails?.medium?.url!!
        val title = responseData.items[0].snippet?.title!!
        val description = responseData.items[0].snippet?.description!!
        val playlistId = responseData.items[0].id!!
        val musicCategoryRepository = MusicCategoryRepository()
        when(musicCategoryRepository.getMusicCategorySequence(musicId)){
            0 -> {
                todayHotPlaylistData.add(PlayListData(thumbnail, title, description, playlistId))
                todayHotPlaylistAdapter.submitList(todayHotPlaylistData.toMutableList())
                binding.todayHotListPlaylistProgressBar.visibility = View.GONE
                binding.todayHotListPlaylistRecyclerView.visibility = View.VISIBLE
            }
            1 -> {
                latestMusicPlaylistData.add(PlayListData(thumbnail, title, description, playlistId))
                latestMusicPlaylistAdapter.submitList(latestMusicPlaylistData.toMutableList())
                binding.latestMusicPlaylistProgressBar.visibility = View.GONE
                binding.latestMusicPlaylistRecyclerView.visibility = View.VISIBLE
            }
            2 -> {
                bestAtmospherePlaylistData.add(PlayListData(thumbnail, title, description, playlistId))
                bestAtmospherePlaylistAdapter.submitList(bestAtmospherePlaylistData.toMutableList())
                binding.bestAtmospherePlaylistProgressBar.visibility = View.GONE
                binding.bestAtmospherePlaylistRecyclerView.visibility = View.VISIBLE
            }
            3 -> {
                bestSituationPlaylistData.add(PlayListData(thumbnail, title, description, playlistId))
                bestSituationPlaylistAdapter.submitList(bestSituationPlaylistData.toMutableList())
                binding.bestSituationPlaylistProgressBar.visibility = View.GONE
                binding.bestSituationPlaylistRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    private suspend fun getPopularTop100MusicData(nextPageToken: String?, popularTop100MusicId: String) {
        val retrofit = RetrofitData.initRetrofit()
        val response =  retrofit.create(RetrofitService::class.java).getPlayListVideoItems(
            BuildConfig.API_KEY2,
            "snippet",
            popularTop100MusicId,
            nextPageToken,
            "50"
        )
        if (response.isSuccessful){
            if (response.body()?.items?.size != 0){
                withContext(Dispatchers.Main){
                    popularTop100MusicDataMapping(response.body()!!)
                }
                if (response.body()?.nextPageToken != null)
                    getPopularTop100MusicData(response.body()?.nextPageToken, popularTop100MusicId)
            }
        }
    }

    private fun popularTop100MusicDataMapping(responseData: PlayListVideoSearchData){
        val youtubeDigitConverter = YoutubeDigitConverter(activity)
        for (index in responseData.items.indices){
            val thumbnail = responseData.items[index].snippet?.thumbnails?.high?.url!!
            val rawDate = responseData.items[index].snippet?.publishedAt!!
            val date = youtubeDigitConverter.intervalBetweenDateText(rawDate)
            val channelTitle = responseData.items[index].snippet?.videoOwnerChannelTitle?.replace(" - Topic","")!!
            val title = stringToHtmlSign(responseData.items[index].snippet?.title!!)
            val videoId = responseData.items[index].snippet?.resourceId?.videoId!!
            val channelId = responseData.items[index].snippet?.channelId!!
            popularTop100Playlist.add(VideoData(thumbnail, title, channelTitle, channelId, videoId, date,  false))
        }
        popular100PlaylistAdapter.submitList(popularTop100Playlist.toMutableList())
        binding.popularTop100PlaylistVideoProgressBar.visibility = View.GONE
        binding.popularTop100PlaylistVideoRecyclerView.visibility = View.VISIBLE
    }

    //영상 제목을 받아올때 &quot; &#39; 문자가 그대로 출력되기 때문에 다른 문자로 대체 해주기 위해 사용하는 메서드
    private fun stringToHtmlSign(str: String): String {
        return str.replace("&amp;".toRegex(), "[&]")
            .replace("[<]".toRegex(), "&lt;")
            .replace("[>]".toRegex(), "&gt;")
            .replace("&quot;".toRegex(), "'")
            .replace("&#39;".toRegex(), "'")
    }

    private fun initSearchSuggestionKeywordRecyclerView(){
        binding.searchSuggestionKeywordRecyclerView.layoutManager = LinearLayoutManager(activity)
        searchSuggestionKeywordAdapter = SearchSuggestionKeywordRecyclerViewAdapter()
        searchSuggestionKeywordAdapter.setItemClickListener(object: SearchSuggestionKeywordRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val searchWord = suggestionKeywords[position]
                suggestionKeywords.clear()
                val searchResultFragment = SearchResultFragment().apply {
                    arguments = Bundle().apply {
                        putString("searchWord", searchWord)
                    }
                }
                searchSuggestionKeywordAdapter.submitList(suggestionKeywords.toMutableList())
                searchView.setQuery(searchWord,false) // 검색한 키워드 텍스트 설정
                searchView.clearFocus()
                childFragmentManager.beginTransaction()
                    .add(binding.searchResultFrameLayout.id, searchResultFragment)
                    .addToBackStack(null)
                    .commit()
                binding.searchSuggestionKeywordRecyclerView.visibility = View.INVISIBLE
            }
        })
        binding.searchSuggestionKeywordRecyclerView.adapter = searchSuggestionKeywordAdapter
    }

    private fun getSuggestionKeyword(newText: String){
        val retrofit = RetrofitSuggestionKeyword.initRetrofit()
        retrofit.create(RetrofitService::class.java).getSuggestionKeyword("firefox","yt",newText)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    suggestionKeywords.clear()
                    if (response.body() != null){
                        val responseString = convertStringUnicodeToKorean(response.body()?.string()!!)
                        val splitBracketList = responseString.split('[')
                        val splitCommaList = splitBracketList[2].split(',')
                        if (splitCommaList[0] != "]]" && splitCommaList[0] != '"'.toString()){
                            addSubstringToSuggestionKeyword(splitCommaList)
                        }
                    }
                    searchSuggestionKeywordAdapter.submitList(suggestionKeywords.toMutableList())
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    suggestionKeywords.clear()
                    searchSuggestionKeywordAdapter.submitList(suggestionKeywords.toMutableList())
                }
            })
    }
    /**
    문자열 정보가 이상하게 들어와 알맞게 나눠주고 리스트에 추가
     **/
    private fun addSubstringToSuggestionKeyword(splitList: List<String>){
        for (index in splitList.indices){
            if (splitList[index].length >= 3){
                if (splitList[index][splitList[index].length-1] == ']')
                    suggestionKeywords.add(splitList[index].substring(1, splitList[index].length-2))
                else
                    suggestionKeywords.add(splitList[index].substring(1, splitList[index].length-1))
            }
        }
    }


    private fun convertStringUnicodeToKorean(data: String): String {
        val sb = StringBuilder() // 단일 쓰레드이므로 StringBuilder 선언
        var i = 0
        /**
         * \uXXXX 로 된 아스키코드 변경
         * i+2 to i+6 을 16진수의 int 계산 후 char 타입으로 변환
         */
        while (i < data.length) {
            if (data[i] == '\\' && data[i + 1] == 'u') {
                val word = data.substring(i + 2, i + 6).toInt(16).toChar()
                sb.append(word)
                i += 5
            } else {
                sb.append(data[i])
            }
            i++
        }
        return sb.toString()
    }

    private fun searchViewCollapseEvent(){
        activity.binding.bottomNavigationView.visibility = View.VISIBLE
        binding.searchSuggestionKeywordRecyclerView.visibility = View.INVISIBLE
        suggestionKeywords.clear()
        searchSuggestionKeywordAdapter.submitList(suggestionKeywords.toMutableList())
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
                searchView.clearFocus()

                val searchResultFragment = SearchResultFragment().apply {
                    arguments = Bundle().apply {
                        putString("searchWord", query!!)
                    }
                }
                childFragmentManager.beginTransaction()
                    .add(binding.searchResultFrameLayout.id, searchResultFragment)
                    .addToBackStack(null)
                    .commit()
                binding.searchSuggestionKeywordRecyclerView.visibility = View.INVISIBLE
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null){
                    getSuggestionKeyword(newText)
                }
                else{
                    suggestionKeywords.clear()
                    searchSuggestionKeywordAdapter.submitList(suggestionKeywords.toMutableList())
                }
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