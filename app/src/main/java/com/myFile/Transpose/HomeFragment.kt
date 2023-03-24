package com.myFile.Transpose

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myFile.Transpose.databinding.FragmentHomeBinding
import com.myFile.Transpose.databinding.MainBinding
import com.myFile.Transpose.model.*
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create
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
    val thisYearMusicId = arrayListOf("RDCLAK5uy_kFoxHZo_PEVrqVnwKkeucGn4ldSyKHD8A",
        "RDCLAK5uy_lZZ5gBJBR1AYziS_onNkE2m18Peg042fI","RDCLAK5uy_nwmWfUayX21xwr1VB72jjBqDPDxGm9zc0",
        "RDCLAK5uy_mOWD5gzPMEI5Ip97H4ivxpcxE7uglskHo","RDCLAK5uy_kaV_BsACArm2vCjj74y871plTk9F8RIDA",
        "RDCLAK5uy_kvnBKwOS6J1tBqsNmdISJEhJ2a-RhNliI","RDCLAK5uy_keUi-XamVp1RPNUoJ-BPIQy3_DusjC5Mg",
        "RDCLAK5uy_kV8LcjsV6_R-y5ncz_SOyoo1BfCRs79wM","RDCLAK5uy_l5AiprVpfhWBQvu-76GJFd1_T2HZoYZWs",
        "RDCLAK5uy_ln2vAJIQILond713LQLKfxSruLhFB1lIM","RDCLAK5uy_kcIyRk6eceGdn0Zjijw0ARVbHCubof1zM",
        "RDCLAK5uy_k4vBNV3dzeyoMlSagQydjsZ-uyo1x1fIU")

    val todayHotMusicId = arrayListOf("RDCLAK5uy_l7wbVbkC-dG5fyEQQsBfjm_z3dLAhYyvo",
        "RDCLAK5uy_m9ty3WvAucm7-5KsKdro9_HnocE8LSS9o","RDCLAK5uy_kRRqnSpfrRZ9OJyTB2IE5WsAqYluG0uYo",
        "RDCLAK5uy_lYPvoz4gPFnKMw_BFojpMk7xRSIqVBkEE","RDCLAK5uy_l6DCR35xfT9bfeUqP7-uw6kWApcfYeDPY",
        "RDCLAK5uy_k6pZ82Gha0sopanWffXo4iMBVaGR7jQaE","RDCLAK5uy_mMRkzfvFXzNQbSl3K-hE_FJ7g8TqMtSlo",
        "RDCLAK5uy_mjCKq8hnUQJqul0W6YW6x2Ep4P67jQ5Po","RDCLAK5uy_l0nFcbRh2kbs27gleqzu364A9rN-D8Ib8",
        "RDCLAK5uy_ky-kXJCA_i0Gf0k6iNxsRHBhAgugAN8-g","RDCLAK5uy_lBfTSdVdgXeN399Mxt2L3C6hLarC1aaN0")

    val latestMusicId = arrayListOf("RDCLAK5uy_lS4dqGRHszluFAbLsV-sHJCqULtBm2Gfw",
        "RDCLAK5uy_mVBAam6Saaqa_DeJRxGkawqqxwPTBrGXM","RDCLAK5uy_nkjcHIQK0Hgf7ihU25uJc0CEokeGkSNxA",
        "RDCLAK5uy_mWqhoadUUp9crhEkmZZkdExj7YpBuFBEQ","RDCLAK5uy_n0f4tLAkNM233wO0yiTEI7467ovnaGbR8",
        "RDCLAK5uy_lN9xj1RQGmBltmvrzTVHMg-vyVt594KYU","RDCLAK5uy_kITLp-IuXw_winp1mnN9PSNatPBiAK52A",
        "RDCLAK5uy_mn7OLm9QvyB230t7RtLWt0BvUmFVlQ-Hc","RDCLAK5uy_lz175mC_wAtZHK0hbDqLrxb5J28QbUznQ",
        "RDCLAK5uy_nppUVicPb1PRbUZmVEMhqgvyFz33Il4pE"
    )

    val bestAtmosphereMusicId = arrayListOf("RDCLAK5uy_mA88hxo-cmI0-WaaRH8Bb2k0x2NptOPqM",
        "RDCLAK5uy_meEBX-iIBwtXBhkeWzwX6njohWnpMijP8","RDCLAK5uy_kT-sIJz2O-hpkxwjosN2hMt9Y5xevcPYI",
        "RDCLAK5uy_lNJA7PB9DAQEdtTnfuKaC2XEOAE1OoX50","RDCLAK5uy_lv6V83HLaJMQDx8YFtfSAaZ6GGvSqI6PE",
        "RDCLAK5uy_mL0lDqxdKwRtBbzva3yVjVy-BZ9L7KX5I","RDCLAK5uy_nXDnxSmhez06eAnjfT2pWjSpp-p2VBv54",
        "RDCLAK5uy_ksGphJr7YduIL-vDvJBUJQ2_JCYnCkaYI","RDCLAK5uy_kDBL_tFOUos7q3SOifZrMHXKwuebdzf7I"
    )

    val bestSituationMusicId = arrayListOf("RDCLAK5uy_msV9Vc8q_guumIXgLkzYs58uBZHVVBPtE",
        "RDCLAK5uy_lFgjDM5dWvoq0_wkEqx4_M43Nk6wXviaM","RDCLAK5uy_nEcCeflWNpzQNRExtAKjKkkX96wjom9Nc",
        "RDCLAK5uy_kskrFUGb5Tnz3-x4wyK9Q5j8RgfwQvq4k","RDCLAK5uy_kjKtb_RC7LRbxiEmSIzZqJRVcYm8U9KMc",
        "RDCLAK5uy_mS7UhvWzUZdjauupjE5JO6VCn-CCwaRoI","RDCLAK5uy_krjFmKbzWzkGvhqkYvvNnUbdrHy0QN1S8",
        "RDCLAK5uy_kQ09S7a68znbjr7h26ur1RJb2tCXDlruY"
    )
    val API_KEY = BuildConfig.TOY_PROJECT

    val channelDataList = arrayListOf<ChannelData>() // 재생 프레그먼트에 넣기 위한 그냥 빈 리스트

    private lateinit var thisYearPlaylistAdapter: HomePlaylistRecyclerViewAdapter
    private lateinit var latestMusicPlaylistAdapter: HomePlaylistRecyclerViewAdapter
    private lateinit var todayHotPlaylistAdapter: HomePlaylistRecyclerViewAdapter
    private lateinit var bestAtmospherePlaylistAdapter: HomePlaylistRecyclerViewAdapter
    private lateinit var bestSituationPlaylistAdapter: HomePlaylistRecyclerViewAdapter
    private lateinit var popular100PlaylistAdapter: HomePopular100RecyclerViewAdapter

    private lateinit var searchSuggestionKeywordAdapter: SearchSuggestionKeywordRecyclerViewAdapter
    lateinit var searchView: SearchView
    lateinit var searchViewItem: MenuItem

    private lateinit var coroutineExceptionHandler: CoroutineExceptionHandler
    private lateinit var callback: OnBackPressedCallback
    lateinit var frameLayout: FrameLayout
    lateinit var channeld: ChannelData

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
        initExceptionHandler()
        getAllData()
        getDetailData("nh0xMF3wxG0")
        return view
    }

    private fun initRecyclerView(){
        initPopularTop100RecyclerView()
        initBestAtmospherePlaylistRecyclerView()
        initBestSituationPlaylistRecyclerView()
        initLatestMusicPlaylistRecyclerView()
        initThisYearPlaylistRecyclerView()
        initTodayHotListPlaylistRecyclerView()
    }

    private fun initThisYearPlaylistRecyclerView(){
        binding.thisYearPlaylistRecyclerView.layoutManager = LinearLayoutManager(activity,
            RecyclerView.HORIZONTAL,false)
        thisYearPlaylistAdapter = HomePlaylistRecyclerViewAdapter()
        thisYearPlaylistAdapter.setItemClickListener(object: HomePlaylistRecyclerViewAdapter.OnItemClickListener{
            var mLastClickTime = 0L
            override fun onClick(v: View, position: Int) {
                if (SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
                    childFragmentManager.beginTransaction().replace(binding.searchResultFrameLayout.id,PlaylistItemsFragment(thisYearPlaylistData[position]))
                        .addToBackStack(null)
                        .commit()
                }
                mLastClickTime = SystemClock.elapsedRealtime()
            }
        })
        thisYearPlaylistAdapter.submitList(thisYearPlaylistData)
        binding.thisYearPlaylistRecyclerView.adapter = thisYearPlaylistAdapter
    }

    private fun initTodayHotListPlaylistRecyclerView(){
        binding.todayHotListPlaylistRecyclerView.layoutManager = LinearLayoutManager(activity,
            RecyclerView.HORIZONTAL,false)
        todayHotPlaylistAdapter = HomePlaylistRecyclerViewAdapter()
        todayHotPlaylistAdapter.setItemClickListener(object: HomePlaylistRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                childFragmentManager.beginTransaction()
                    .replace(binding.searchResultFrameLayout.id,PlaylistItemsFragment(todayHotPlaylistData[position]))
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
                childFragmentManager.beginTransaction()
                    .replace(binding.searchResultFrameLayout.id,PlaylistItemsFragment(latestMusicPlaylistData[position]))
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
                childFragmentManager.beginTransaction()
                    .replace(binding.searchResultFrameLayout.id,PlaylistItemsFragment(bestAtmospherePlaylistData[position]))
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
                childFragmentManager.beginTransaction()
                    .replace(binding.searchResultFrameLayout.id,PlaylistItemsFragment(bestSituationPlaylistData[position]))
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
                val playlistModel = PlaylistModel(resources.getString(R.string.popular_Top100_playlist_text),popularTop100Playlist)
                parentFragmentManager.beginTransaction()
                    .replace(
                        activity.binding.playerFragment.id,
                        PlayerFragment(
                            popularTop100Playlist[position], playlistModel
                        )
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
    private fun getAllData(){
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch {
            async { getPopularTop100MusicData(null) }
            async { getPlaylistData(thisYearMusicId) }
            async { getPlaylistData(todayHotMusicId) }
            async { getPlaylistData(latestMusicId) }
            async { getPlaylistData(bestAtmosphereMusicId) }
            async { getPlaylistData(bestSituationMusicId) }
        }
    }

    private suspend fun getPlaylistData(musicUrls: ArrayList<String>) {
        for (index in musicUrls.indices) {
            val retrofit = RetrofitData.initRetrofit()
            val response = retrofit.create(RetrofitService::class.java)
                .getPlayLists(API_KEY, "snippet", musicUrls[index], "50")
            if (response.isSuccessful){
                if (response.body()?.items?.size != 0) {
                    withContext(Dispatchers.Main){
                        playlistDataMapping(musicUrls, response.body()!!)
                    }
                }
            }
        }
    }

    private fun playlistDataMapping(musicUrls: ArrayList<String>, responseData: PlayListSearchData){
        val thumbnail = responseData.items[0].snippet?.thumbnails?.medium?.url!!
        val title = responseData.items[0].snippet?.title!!
        val description = responseData.items[0].snippet?.description!!
        val playlistId = responseData.items[0].id!!
        when(musicUrls) {
            thisYearMusicId -> {
                thisYearPlaylistData.add(PlayListData(thumbnail, title, description, playlistId))
                thisYearPlaylistAdapter.submitList(thisYearPlaylistData.toMutableList())
                binding.thisYearPlaylistVideoProgressBar.visibility = View.GONE
                binding.thisYearPlaylistRecyclerView.visibility = View.VISIBLE
            }
            todayHotMusicId -> {
                todayHotPlaylistData.add(PlayListData(thumbnail, title, description, playlistId))
                todayHotPlaylistAdapter.submitList(todayHotPlaylistData.toMutableList())
                binding.todayHotListPlaylistProgressBar.visibility = View.GONE
                binding.todayHotListPlaylistRecyclerView.visibility = View.VISIBLE
            }
            latestMusicId -> {
                latestMusicPlaylistData.add(PlayListData(thumbnail, title, description, playlistId))
                latestMusicPlaylistAdapter.submitList(latestMusicPlaylistData.toMutableList())
                binding.latestMusicPlaylistProgressBar.visibility = View.GONE
                binding.latestMusicPlaylistRecyclerView.visibility = View.VISIBLE
            }
            bestAtmosphereMusicId -> {
                bestAtmospherePlaylistData.add(PlayListData(thumbnail, title, description, playlistId))
                bestAtmospherePlaylistAdapter.submitList(bestAtmospherePlaylistData.toMutableList())
                binding.bestAtmospherePlaylistProgressBar.visibility = View.GONE
                binding.bestAtmospherePlaylistRecyclerView.visibility = View.VISIBLE
            }
            bestSituationMusicId -> {
                bestSituationPlaylistData.add(PlayListData(thumbnail, title, description, playlistId))
                bestSituationPlaylistAdapter.submitList(bestSituationPlaylistData.toMutableList())
                binding.bestSituationPlaylistProgressBar.visibility = View.GONE
                binding.bestSituationPlaylistRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    private suspend fun getPopularTop100MusicData(
        nextPageToken: String?
    ) {
        val retrofit = RetrofitData.initRetrofit()
        val response =  retrofit.create(RetrofitService::class.java).getPlayListVideoItems(
            API_KEY,
            "snippet",
            "PL2HEDIx6Li8jGsqCiXUq9fzCqpH99qqHV",
            nextPageToken,
            "50"
        )
        if (response.isSuccessful){
            if (response.body()?.items?.size != 0){
                withContext(Dispatchers.Main){
                    popularTop100MusicDataMapping(response.body()!!)
                }
                if (response.body()?.nextPageToken != null)
                    getPopularTop100MusicData(response.body()?.nextPageToken)
            }
        }
    }

    private fun popularTop100MusicDataMapping(responseData: PlayListVideoSearchData){
        for (index in responseData.items.indices){
            val thumbnail = responseData.items[index].snippet?.thumbnails?.high?.url!!
            val date = responseData.items[index].snippet?.publishedAt!!
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

    private fun initExceptionHandler(){
        coroutineExceptionHandler = CoroutineExceptionHandler{ _, throwable ->
            Log.d("코루틴 에러","$throwable")
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(activity,resources.getString(R.string.network_error_message),
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun initSearchSuggestionKeywordRecyclerView(){
        binding.searchSuggestionKeywordRecyclerView.layoutManager = LinearLayoutManager(activity)
        searchSuggestionKeywordAdapter = SearchSuggestionKeywordRecyclerViewAdapter()
        searchSuggestionKeywordAdapter.setItemClickListener(object: SearchSuggestionKeywordRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val searchWord = suggestionKeywords[position]
                suggestionKeywords.clear()
                searchSuggestionKeywordAdapter.submitList(suggestionKeywords.toMutableList())
                searchView.setQuery(searchWord,false) // 검색한 키워드 텍스트 설정
                searchView.clearFocus()
                childFragmentManager.beginTransaction()
                    .replace(binding.searchResultFrameLayout.id,SearchResultFragment(searchWord))
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
                    val responseString = convertStringUnicodeToKorean(response.body()?.string()!!)
                    val splitBracketList = responseString.split('[')
                    val splitCommaList = splitBracketList[2].split(',')
                    if (splitCommaList[0] != "]]" && splitCommaList[0] != '"'.toString()){
                        addSubstringToSuggestionKeyword(splitCommaList)
                    }
                    searchSuggestionKeywordAdapter.submitList(suggestionKeywords.toMutableList())
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.d("실패!","!1")
                }
            })
    }
    /**
    문자열 정보가 이상하게 들어와 알맞게 나눠주고 리스트에 추가
     **/
    private fun addSubstringToSuggestionKeyword(splitList: List<String>){
        for (index in splitList.indices){
            if (splitList[index].isNotEmpty()){
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
        Log.d("이게 실행이 됏잖아","searchViewCollapseEvent")
//        binding.toolBar.setBackgroundColor(resources.getColor(R.color.black))
        activity.binding.bottomNavigationView.visibility = View.VISIBLE
        binding.searchSuggestionKeywordRecyclerView.visibility = View.INVISIBLE
        suggestionKeywords.clear()
        searchSuggestionKeywordAdapter.submitList(suggestionKeywords.toMutableList())

    }
    private fun getDetailData(videoId: String) {
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch {
            async {getVideoDetail(videoId)}
        }
    }

    private suspend fun getVideoDetail(videoId: String) {
        val retrofit = RetrofitData.initRetrofit()
        val response = retrofit.create(RetrofitService::class.java)
            .getVideoDetail(BuildConfig.TOY_PROJECT, "snippet, statistics",videoId)
        if (response.isSuccessful){
            if (response.body()?.items?.size != 0){
                getChannelData(response.body()!!)
            }
        }
    }

    private suspend fun getChannelData(videoDetailResponseData: VideoDetailData) {
        val retrofit = RetrofitData.initRetrofit()
        val response = retrofit.create(RetrofitService::class.java)
            .getChannelData(BuildConfig.TOY_PROJECT, "snippet, contentDetails, statistics, brandingSettings"
                ,videoDetailResponseData.items[0].snippet?.channelId)
        if (response.isSuccessful){
            if (response.body()?.items?.size != 0){
                withContext(Dispatchers.Main){
                    detailMapping(videoDetailResponseData, response.body()!!)
                }
            }
        }
    }

    private fun detailMapping(videoDetailResponseData: VideoDetailData, channelDetailResponseData: ChannelSearchData) {
        val currentChannelSearchData = channelDetailResponseData
        val currentVideoDetailData = videoDetailResponseData
        val channelThumbnail = currentChannelSearchData.items[0].snippet?.thumbnails?.default?.url!!
        val videoCount = currentChannelSearchData.items[0].statistics?.videoCount!!
        val subscriberCount = currentChannelSearchData.items[0].statistics?.subscriberCount!!
        val viewCount = currentChannelSearchData.items[0].statistics?.viewCount!!
        val channelBanner = currentChannelSearchData.items[0].brandingSettings?.image?.bannerExternalUrl
        val channelTitle = currentChannelSearchData.items[0].snippet?.title!!
        val channelDescription = currentChannelSearchData.items[0].snippet?.description!!
        val channelPlaylistId = currentChannelSearchData.items[0].contentDetails?.relatedPlaylists?.uploads!!
        channeld =  ChannelData(channelTitle, channelDescription, channelBanner, channelThumbnail, videoCount, viewCount, subscriberCount, channelPlaylistId)
    }



    fun initToolbar(){
        homeFragmentToolBar = binding.homeFragmentToolBar
        val menu = homeFragmentToolBar.menu
        menu.findItem(R.id.option_icon).setOnMenuItemClickListener {
            childFragmentManager.beginTransaction()
                .add(activity.homeFragment.binding.searchResultFrameLayout.id,
                    ChannelFragment(channeld))
                .addToBackStack(null)
                .commit()
            true
        }
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
                Log.d("뒤로가기","서치뷰")
                var count = 0
                childFragmentManager.addOnBackStackChangedListener {
                    Log.d("서치뷰 내","숫자")
                    count = childFragmentManager.backStackEntryCount
                }
                callback.remove()
                searchViewCollapseEvent()
                return childFragmentManager.backStackEntryCount == 0
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
                searchView.clearFocus()
                childFragmentManager.beginTransaction()
                    .replace(binding.searchResultFrameLayout.id,SearchResultFragment(
                        query!!
                    ))
                    .addToBackStack(null)
                    .commit()
                binding.searchSuggestionKeywordRecyclerView.visibility = View.INVISIBLE
                return false
            }
            //SwipeRefreshLayout 새로고침
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != ""){
                    Log.d("검색어를","불러옴")
                    getSuggestionKeyword(newText!!)
                }
                if (newText == ""){
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
                Log.d("homeFragment 백","버튼")

                if (childFragmentManager.backStackEntryCount == 0) {
                    if (searchViewItem.isActionViewExpanded){
                        Log.d("homeFragment 백","조건문")
                        searchViewItem.collapseActionView()
                    }
                }else{
                    if (binding.searchSuggestionKeywordRecyclerView.visibility == View.VISIBLE)
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
        fbinding = null
    }

    override fun onStop() {
        super.onStop()
        Log.d("homeFRagment","onStop")
    }

    override fun onResume() {
        super.onResume()
        Log.d("homeFragment","의 onResume")
    }
}