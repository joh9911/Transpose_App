package com.example.youtube_transpose

import android.app.SearchManager
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.SearchAutoComplete
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.BuildConfig
import com.example.youtube_transpose.databinding.MainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import okhttp3.ResponseBody
import retrofit2.*


class Activity: AppCompatActivity() {
    var mBinding: MainBinding? = null
    val binding get() = mBinding!!
    lateinit var transposePage: LinearLayout
    lateinit var floatButton: ExtendedFloatingActionButton
    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    lateinit var bottomNavigationView: BottomNavigationView
    lateinit var pitchSeekBar: SeekBar
    lateinit var tempoSeekBar: SeekBar

    private lateinit var popular100PlaylistAdapter: HomePopular100RecyclerViewAdapter
    private lateinit var searchAdapter: SearchSuggestionKeywordRecyclerViewAdapter
    private lateinit var thisYearPlaylistAdapter: HomePlaylistRecyclerViewAdapter
    private lateinit var latestMusicPlaylistAdapter: HomePlaylistRecyclerViewAdapter
    private lateinit var todayHotPlaylistAdapter: HomePlaylistRecyclerViewAdapter
    private lateinit var bestAtmospherePlaylistAdapter: HomePlaylistRecyclerViewAdapter
    private lateinit var bestSituationPlaylistAdapter: HomePlaylistRecyclerViewAdapter

    private val playerMotionLayout by lazy {
        findViewById<MotionLayout>(R.id.player_motion_layout)
    }


    val API_KEY = com.example.youtube_transpose.BuildConfig.API_KEY
    val suggestionKeywords = ArrayList<String>()
    val playListVideoData = ArrayList<VideoData>()
    val thisYearPlaylistData = ArrayList<PlayListData>()
    val todayHotPlaylistData = ArrayList<PlayListData>()
    val latestMusicPlaylistData = ArrayList<PlayListData>()
    val bestAtmospherePlaylistData = ArrayList<PlayListData>()
    val bestSituationPlaylistData = ArrayList<PlayListData>()
    val thisYearMusicUrl = arrayListOf("RDCLAK5uy_kFoxHZo_PEVrqVnwKkeucGn4ldSyKHD8A",
        "RDCLAK5uy_lZZ5gBJBR1AYziS_onNkE2m18Peg042fI","RDCLAK5uy_nwmWfUayX21xwr1VB72jjBqDPDxGm9zc0",
        "RDCLAK5uy_mOWD5gzPMEI5Ip97H4ivxpcxE7uglskHo","RDCLAK5uy_kaV_BsACArm2vCjj74y871plTk9F8RIDA",
        "RDCLAK5uy_kvnBKwOS6J1tBqsNmdISJEhJ2a-RhNliI","RDCLAK5uy_keUi-XamVp1RPNUoJ-BPIQy3_DusjC5Mg",
        "RDCLAK5uy_kV8LcjsV6_R-y5ncz_SOyoo1BfCRs79wM","RDCLAK5uy_l5AiprVpfhWBQvu-76GJFd1_T2HZoYZWs",
        "RDCLAK5uy_ln2vAJIQILond713LQLKfxSruLhFB1lIM","RDCLAK5uy_kcIyRk6eceGdn0Zjijw0ARVbHCubof1zM",
        "RDCLAK5uy_k4vBNV3dzeyoMlSagQydjsZ-uyo1x1fIU")

    val todayHotMusicUrl = arrayListOf("RDCLAK5uy_l7wbVbkC-dG5fyEQQsBfjm_z3dLAhYyvo",
        "RDCLAK5uy_m9ty3WvAucm7-5KsKdro9_HnocE8LSS9o","RDCLAK5uy_kRRqnSpfrRZ9OJyTB2IE5WsAqYluG0uYo",
        "RDCLAK5uy_lYPvoz4gPFnKMw_BFojpMk7xRSIqVBkEE","RDCLAK5uy_l6DCR35xfT9bfeUqP7-uw6kWApcfYeDPY",
        "RDCLAK5uy_k6pZ82Gha0sopanWffXo4iMBVaGR7jQaE","RDCLAK5uy_mMRkzfvFXzNQbSl3K-hE_FJ7g8TqMtSlo",
        "RDCLAK5uy_mjCKq8hnUQJqul0W6YW6x2Ep4P67jQ5Po","RDCLAK5uy_l0nFcbRh2kbs27gleqzu364A9rN-D8Ib8",
        "RDCLAK5uy_ky-kXJCA_i0Gf0k6iNxsRHBhAgugAN8-g","RDCLAK5uy_lBfTSdVdgXeN399Mxt2L3C6hLarC1aaN0")

    val latestMusicUrl = arrayListOf("RDCLAK5uy_lS4dqGRHszluFAbLsV-sHJCqULtBm2Gfw",
        "RDCLAK5uy_mVBAam6Saaqa_DeJRxGkawqqxwPTBrGXM","RDCLAK5uy_nkjcHIQK0Hgf7ihU25uJc0CEokeGkSNxA",
        "RDCLAK5uy_mWqhoadUUp9crhEkmZZkdExj7YpBuFBEQ","RDCLAK5uy_n0f4tLAkNM233wO0yiTEI7467ovnaGbR8",
        "RDCLAK5uy_lN9xj1RQGmBltmvrzTVHMg-vyVt594KYU","RDCLAK5uy_kITLp-IuXw_winp1mnN9PSNatPBiAK52A",
        "RDCLAK5uy_mn7OLm9QvyB230t7RtLWt0BvUmFVlQ-Hc","RDCLAK5uy_lz175mC_wAtZHK0hbDqLrxb5J28QbUznQ",
        "RDCLAK5uy_nppUVicPb1PRbUZmVEMhqgvyFz33Il4pE"
        )

    val bestAtmosphereMusicUrl = arrayListOf("RDCLAK5uy_mA88hxo-cmI0-WaaRH8Bb2k0x2NptOPqM",
        "RDCLAK5uy_meEBX-iIBwtXBhkeWzwX6njohWnpMijP8","RDCLAK5uy_kT-sIJz2O-hpkxwjosN2hMt9Y5xevcPYI",
        "RDCLAK5uy_lNJA7PB9DAQEdtTnfuKaC2XEOAE1OoX50","RDCLAK5uy_lv6V83HLaJMQDx8YFtfSAaZ6GGvSqI6PE",
        "RDCLAK5uy_mL0lDqxdKwRtBbzva3yVjVy-BZ9L7KX5I","RDCLAK5uy_nXDnxSmhez06eAnjfT2pWjSpp-p2VBv54",
        "RDCLAK5uy_ksGphJr7YduIL-vDvJBUJQ2_JCYnCkaYI","RDCLAK5uy_kDBL_tFOUos7q3SOifZrMHXKwuebdzf7I"
    )

    val bestSituationMusicUrl = arrayListOf("RDCLAK5uy_msV9Vc8q_guumIXgLkzYs58uBZHVVBPtE",
        "RDCLAK5uy_lFgjDM5dWvoq0_wkEqx4_M43Nk6wXviaM","RDCLAK5uy_nEcCeflWNpzQNRExtAKjKkkX96wjom9Nc",
        "RDCLAK5uy_kskrFUGb5Tnz3-x4wyK9Q5j8RgfwQvq4k","RDCLAK5uy_kjKtb_RC7LRbxiEmSIzZqJRVcYm8U9KMc",
        "RDCLAK5uy_mS7UhvWzUZdjauupjE5JO6VCn-CCwaRoI","RDCLAK5uy_krjFmKbzWzkGvhqkYvvNnUbdrHy0QN1S8",
        "RDCLAK5uy_kQ09S7a68znbjr7h26ur1RJb2tCXDlruY"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = MainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        initRecyclerView()
        floatButtonEvent()
        getPopularTop100MusicData(null)
        getPlaylistData(thisYearMusicUrl)
        getPlaylistData(todayHotMusicUrl)
        getPlaylistData(latestMusicUrl)
        getPlaylistData(bestAtmosphereMusicUrl)
        getPlaylistData(bestSituationMusicUrl)
        Log.d("온 크레이트","실행")
    }
    private fun initView() {

        transposePage = binding.transposePage
        pitchSeekBar = binding.pitchSeekBar
        pitchSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                binding.pitchValue.text = p1.toString()
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
            }
            override fun onStopTrackingTouch(p0: SeekBar?) {
                for(fragment in supportFragmentManager.fragments) {
                    if(fragment.isVisible && fragment is PlayerFragment) {
                        val playerFragment = supportFragmentManager.findFragmentById(binding.playerFragment.id) as PlayerFragment
                        playerFragment.setPitch(p0?.progress!!)
                    }
                }

            }
        })
        tempoSeekBar = binding.tempoSeekBar
        tempoSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                binding.tempoValue.text = p1.toString()
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
            }
            override fun onStopTrackingTouch(p0: SeekBar?) {
                for(fragment in supportFragmentManager.fragments) {
                    if(fragment.isVisible && fragment is PlayerFragment) {
                        val playerFragment = supportFragmentManager.findFragmentById(binding.playerFragment.id) as PlayerFragment
                        playerFragment.setTempo(p0?.progress!!)
                    }
                }

            }
        })
        toolbar = binding.toolBar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        floatButton = binding.floatButton
        bottomNavigationView = binding.bottomNavigationView
    }
    fun initRecyclerView(){
        initSearchRecyclerView()
        initPopularTop100RecyclerView()
        initBestAtmospherePlaylistRecyclerView()
        initBestSituationPlaylistRecyclerView()
        initLatestMusicPlaylistRecyclerView()
        initThisYearPlaylistRecyclerView()
        initTodayHotListPlaylistRecyclerView()
    }
    fun initSearchRecyclerView(){
        binding.searchRecyclerView.layoutManager = LinearLayoutManager(this)
        searchAdapter = SearchSuggestionKeywordRecyclerViewAdapter(suggestionKeywords)
        searchAdapter.setItemClickListener(object: SearchSuggestionKeywordRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                var mLastClickTime = 0L
                if (SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
                    val searchWord = suggestionKeywords[position]
                    suggestionKeywords.clear()
                    searchAdapter.notifyDataSetChanged()
                    supportFragmentManager.beginTransaction().replace(binding.searchResultFragment.id,SearchResultFragment(searchWord)).commit()
                }
                mLastClickTime = SystemClock.elapsedRealtime()
            }
        })
        binding.searchRecyclerView.adapter = searchAdapter
    }

    fun initThisYearPlaylistRecyclerView(){
        binding.thisYearPlaylistRecyclerView.layoutManager = LinearLayoutManager(this,RecyclerView.HORIZONTAL,false)
        thisYearPlaylistAdapter = HomePlaylistRecyclerViewAdapter(thisYearPlaylistData)
        thisYearPlaylistAdapter.setItemClickListener(object: HomePlaylistRecyclerViewAdapter.OnItemClickListener{
            var mLastClickTime = 0L
            override fun onClick(v: View, position: Int) {
                if (SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
                    supportFragmentManager.beginTransaction().replace(binding.playlistItemsFragment.id,PlaylistItemsFragment(thisYearPlaylistData[position]))
                        .addToBackStack(null)
                        .commit()
                }
                mLastClickTime = SystemClock.elapsedRealtime()
            }
        })
        binding.thisYearPlaylistRecyclerView.adapter = thisYearPlaylistAdapter
    }

    fun initTodayHotListPlaylistRecyclerView(){
        binding.todayHotListPlaylistRecyclerView.layoutManager = LinearLayoutManager(this,RecyclerView.HORIZONTAL,false)
        todayHotPlaylistAdapter = HomePlaylistRecyclerViewAdapter(todayHotPlaylistData)
        todayHotPlaylistAdapter.setItemClickListener(object: HomePlaylistRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                supportFragmentManager.beginTransaction().replace(binding.playlistItemsFragment.id,PlaylistItemsFragment(todayHotPlaylistData[position]))
                    .addToBackStack(null)
                    .commit()
            }

        })
        binding.todayHotListPlaylistRecyclerView.adapter = todayHotPlaylistAdapter
    }

    fun initLatestMusicPlaylistRecyclerView(){
        binding.latestMusicPlaylistRecyclerView.layoutManager = LinearLayoutManager(this,RecyclerView.HORIZONTAL,false)
        latestMusicPlaylistAdapter = HomePlaylistRecyclerViewAdapter(latestMusicPlaylistData)
        latestMusicPlaylistAdapter.setItemClickListener(object: HomePlaylistRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                supportFragmentManager.beginTransaction().replace(binding.playlistItemsFragment.id,PlaylistItemsFragment(latestMusicPlaylistData[position]))
                    .addToBackStack(null)
                    .commit()
            }

        })
        binding.latestMusicPlaylistRecyclerView.adapter = latestMusicPlaylistAdapter

    }

    fun initBestAtmospherePlaylistRecyclerView(){
        binding.bestAtmospherePlaylistRecyclerView.layoutManager = LinearLayoutManager(this,RecyclerView.HORIZONTAL,false)
        bestAtmospherePlaylistAdapter = HomePlaylistRecyclerViewAdapter(bestAtmospherePlaylistData)
        bestAtmospherePlaylistAdapter.setItemClickListener(object: HomePlaylistRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                supportFragmentManager.beginTransaction().replace(binding.playlistItemsFragment.id,PlaylistItemsFragment(bestAtmospherePlaylistData[position]))
                    .addToBackStack(null)
                    .commit()
            }

        })
        binding.bestAtmospherePlaylistRecyclerView.adapter = bestAtmospherePlaylistAdapter

    }

    fun initBestSituationPlaylistRecyclerView(){
        binding.bestSituationPlaylistRecyclerView.layoutManager = LinearLayoutManager(this,RecyclerView.HORIZONTAL,false)
        bestSituationPlaylistAdapter = HomePlaylistRecyclerViewAdapter(bestSituationPlaylistData)
        bestSituationPlaylistAdapter.setItemClickListener(object: HomePlaylistRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                supportFragmentManager.beginTransaction().replace(binding.playlistItemsFragment.id,PlaylistItemsFragment(bestSituationPlaylistData[position]))
                    .addToBackStack(null)
                    .commit()
            }

        })
        binding.bestSituationPlaylistRecyclerView.adapter = bestSituationPlaylistAdapter
    }


    fun initPopularTop100RecyclerView(){
        val gridLayoutManager = GridLayoutManager(this,4,GridLayoutManager.HORIZONTAL,false)
        binding.popularTop100PlaylistVideoRecyclerView.layoutManager = gridLayoutManager
        popular100PlaylistAdapter = HomePopular100RecyclerViewAdapter(playListVideoData)
        binding.popularTop100PlaylistVideoRecyclerView.adapter = popular100PlaylistAdapter

        popular100PlaylistAdapter.setItemClickListener(object: HomePopular100RecyclerViewAdapter.OnItemClickListener{
            var mLastClickTime = 0L
            override fun onClick(v: View, position: Int) {
                if (SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
                    replaceFragmentToPlayerFragment(playListVideoData[position])
                }
                mLastClickTime = SystemClock.elapsedRealtime()
            }
        })

    }

    fun replaceFragmentToPlayerFragment(videoData: VideoData){
        supportFragmentManager.beginTransaction().replace(binding.playerFragment.id,PlayerFragment(videoData),"playerFragment").commit()
    }

    private fun getSuggestionKeyword(newText: String){
        val retrofit = RetrofitSuggestionKeyword.initRetrofit()
        retrofit.create(RetrofitService::class.java).getSuggestionKeyword("firefox","yt",newText)
            .enqueue(object : Callback<ResponseBody>{
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    suggestionKeywords.clear()
                    val responseString = convertStringUnicodeToKorean(response.body()?.string()!!)
                    val splitBracketList = responseString.split('[')
                    val splitCommaList = splitBracketList[2].split(',')
                    if (splitCommaList[0] != "]]" && splitCommaList[0] != '"'.toString()){
                        addSubstringToSuggestionKeyword(splitCommaList)
                    }
                    searchAdapter.notifyDataSetChanged()
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
                Log.d("함수","${splitList[index]}")
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
    fun getPlaylistData(musicUrls: ArrayList<String>){
        for (index in musicUrls.indices){
            val retrofit = RetrofitVideo.initRetrofit()
            retrofit.create(RetrofitService::class.java).getPlayLists(API_KEY,"snippet",musicUrls[index],"50")
                .enqueue(object: Callback<PlayListSearchData>{
                    override fun onResponse(
                        call: Call<PlayListSearchData>,
                        response: Response<PlayListSearchData>
                    ) {
                        if (response.body()?.items?.size != 0){
                            val thumbnail = response.body()?.items?.get(0)?.snippet?.thumbnails?.medium?.url!!
                            val title = response.body()?.items?.get(0)?.snippet?.title!!
                            val description = response.body()?.items?.get(0)?.snippet?.description!!
                            Log.d("플레이리스트 가져오기","${response.body()?.items?.get(0)?.snippet?.title}")
                            when(musicUrls) {
                                thisYearMusicUrl -> {
                                    thisYearPlaylistData.add(
                                        PlayListData(
                                            thumbnail,
                                            title,
                                            description
                                        )
                                    )
                                    thisYearPlaylistAdapter.notifyDataSetChanged()
                                    binding.thisYearPlaylistVideoProgressBar.visibility = View.GONE
                                    binding.thisYearPlaylistRecyclerView.visibility = View.VISIBLE
                                }
                                todayHotMusicUrl -> {
                                    todayHotPlaylistData.add(
                                        PlayListData(
                                            thumbnail,
                                            title,
                                            description
                                        )
                                    )
                                    todayHotPlaylistAdapter.notifyDataSetChanged()
                                    binding.todayHotListPlaylistProgressBar.visibility = View.GONE
                                    binding.todayHotListPlaylistRecyclerView.visibility = View.VISIBLE
                                }
                                latestMusicUrl -> {
                                    latestMusicPlaylistData.add(
                                        PlayListData(
                                            thumbnail,
                                            title,
                                            description
                                        )
                                    )
                                    latestMusicPlaylistAdapter.notifyDataSetChanged()
                                    binding.latestMusicPlaylistProgressBar.visibility = View.GONE
                                    binding.latestMusicPlaylistRecyclerView.visibility = View.VISIBLE
                                }
                                bestAtmosphereMusicUrl -> {
                                    bestAtmospherePlaylistData.add(
                                        PlayListData(
                                            thumbnail,
                                            title,
                                            description
                                        )
                                    )
                                    bestAtmospherePlaylistAdapter.notifyDataSetChanged()
                                    binding.bestAtmospherePlaylistProgressBar.visibility = View.GONE
                                    binding.bestAtmospherePlaylistRecyclerView.visibility = View.VISIBLE
                                }
                                bestSituationMusicUrl -> {
                                    bestSituationPlaylistData.add(
                                        PlayListData(
                                            thumbnail,
                                            title,
                                            description
                                        )
                                    )
                                    bestSituationPlaylistAdapter.notifyDataSetChanged()
                                    binding.bestSituationPlaylistProgressBar.visibility = View.GONE
                                    binding.bestSituationPlaylistRecyclerView.visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                    override fun onFailure(call: Call<PlayListSearchData>, t: Throwable) {
                        Log.e(TAG, "onFailure: ${t.message}")
                    }
                })
        }

    }
    fun getPopularTop100MusicData(nextPageToken: String?){
        val retrofit = RetrofitVideo.initRetrofit()
        retrofit.create(RetrofitService::class.java).getPlayListVideoItems(API_KEY,"snippet","PLnlxKMP5GzKCXJ3Cw99qSWgC01cuhTFxG",nextPageToken,"50")
            .enqueue(object : Callback<PlayListVideoSearchData> {
                override fun onResponse(call: Call<PlayListVideoSearchData>, response: Response<PlayListVideoSearchData>) {
                    Log.d(TAG, "onSusses${response.body()?.pageInfo?.resultsPerPage}: ${response.body()?.items?.size}")
                    for (index in 0 until response.body()?.items?.size!!){
                        val thumbnail = response?.body()?.items?.get(index)?.snippet?.thumbnails?.default?.url!!
                        val date = response?.body()?.items?.get(index)?.snippet?.publishedAt!!.substring(0, 10)
                        val account = response.body()?.items?.get(index)?.snippet?.videoOwnerChannelTitle?.replace(" - Topic","")!!
                        val title = stringToHtmlSign(response?.body()?.items?.get(index)?.snippet?.title!!)
                        val videoId = response?.body()?.items?.get(index)?.snippet?.resourceId?.videoId!!
                        playListVideoData.add(VideoData(thumbnail, title, account, videoId, date))
                    }
                    popular100PlaylistAdapter.notifyDataSetChanged()
                    binding.popularTop100PlaylistVideoProgressBar.visibility = View.GONE
                    binding.popularTop100PlaylistVideoRecyclerView.visibility = View.VISIBLE
                    if (response.body()?.nextPageToken != null)
                        getPopularTop100MusicData(response.body()?.nextPageToken)
                }
                override fun onFailure(call: Call<PlayListVideoSearchData>, t: Throwable) {
                    Log.e(TAG, "onFailure: ${t.message}")
                }
            })
    }

    //영상 제목을 받아올때 &quot; &#39; 문자가 그대로 출력되기 때문에 다른 문자로 대체 해주기 위해 사용하는 메서드
    private fun stringToHtmlSign(str: String): String {
        return str.replace("&amp;".toRegex(), "[&]")
            .replace("[<]".toRegex(), "&lt;")
            .replace("[>]".toRegex(), "&gt;")
            .replace("&quot;".toRegex(), "'")
            .replace("&#39;".toRegex(), "'")
    }


    private fun floatButtonEvent(){
        floatButton.shrink()
        var isExtended = false
        floatButton.setOnClickListener{
            isExtended = if (!isExtended){
                transposePage.visibility = View.VISIBLE
                floatButton.extend()
                true
            } else{
                transposePage.visibility = View.INVISIBLE
                floatButton.shrink()
                false
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_item, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.youtube_search_icon).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            val searchView = this
            val searchAutoComplete = this.findViewById<SearchAutoComplete>(androidx.appcompat.R.id.search_src_text)
            searchAutoComplete.setTextColor(resources.getColor(R.color.white))
            searchAutoComplete.setHintTextColor(resources.getColor(R.color.description_color))
            searchAutoComplete.hint = "Youtube 검색"
            menu.findItem(R.id.youtube_search_icon).setOnActionExpandListener(object: MenuItem.OnActionExpandListener{
                override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
                    binding.bottomNavigationView.visibility = View.GONE
                    return true
                }

                override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                    for(fragment in supportFragmentManager.fragments) {
                        if(fragment.isVisible && fragment is SearchResultFragment) {
                            supportFragmentManager.beginTransaction().remove(fragment).commit()
                        }
                    }
                    binding.bottomNavigationView.visibility = View.VISIBLE
                    binding.searchRecyclerView.visibility = View.INVISIBLE
                    suggestionKeywords.clear()
                    return true
                }
            })
            this.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    searchView.clearFocus()
                    supportFragmentManager.beginTransaction().replace(binding.searchResultFragment.id,SearchResultFragment(query!!)).commit()
                    binding.searchRecyclerView.visibility = View.INVISIBLE
                    return false
                }
                //SwipeRefreshLayout 새로고침
                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText != ""){
                        getSuggestionKeyword(newText!!)
                    }
                    if (newText == ""){
                        Log.d("빈","칸")
                        suggestionKeywords.clear()
                        searchAdapter.notifyDataSetChanged()
                    }
                    Log.d("TAG", "SearchVies Text is changed : $newText")
                    return false
                }
            })

        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item?.itemId){
            R.id.transpose_icon -> {
                binding.searchRecyclerView.visibility = View.INVISIBLE

                true
            }
            R.id.youtube_search_icon -> {
                binding.searchRecyclerView.visibility = View.VISIBLE
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.findFragmentByTag("playerFragment") == null){
            return super.onBackPressed()
        }
        else{
            val playerFragment = supportFragmentManager.findFragmentById(binding.playerFragment.id) as PlayerFragment
            if (playerFragment.binding.playerMotionLayout.currentState == R.id.end)
                playerFragment.binding.playerMotionLayout.transitionToState(R.id.start)
            else
                return super.onBackPressed()
        }

    }

}