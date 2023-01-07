package com.example.youtube_transpose

import android.app.SearchManager
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.SearchAutoComplete
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.youtube_transpose.databinding.MainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import okhttp3.ResponseBody
import retrofit2.*


class Activity: AppCompatActivity() {
    var mBinding: MainBinding? = null
    val binding get() = mBinding!!
    lateinit var page: LinearLayout
    lateinit var floatButton: ExtendedFloatingActionButton
    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    lateinit var bottomNavigationView: BottomNavigationView
    lateinit var text: TextView
    private lateinit var homeAdapter: HomeRecyclerViewAdapter
    private lateinit var searchAdapter: SearchRecyclerViewAdapter
    private val playerMotionLayout by lazy {
        findViewById<MotionLayout>(R.id.player_motion_layout)
    }

    val intentSearch = "omg" // 이거 검색한 텍스트
    val API_KEY = "AIzaSyBZlnQ_kRZ7mvs0wL31ezbBeEPYAoIM3EM"
    val videoData = ArrayList<VideoData>()
    val suggestionKeywords = ArrayList<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = MainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        floatButtonEvent()
//        getHomeVideo()
    }
    private fun initView(){
        page = binding.page
        toolbar = binding.toolBar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        floatButton = binding.floatButton
        bottomNavigationView = binding.bottomNavigationView
        binding.searchRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.homeRecyclerView.layoutManager = LinearLayoutManager(this)
        homeAdapter = HomeRecyclerViewAdapter(videoData)
        binding.homeRecyclerView.adapter = homeAdapter
        searchAdapter = SearchRecyclerViewAdapter(suggestionKeywords)
        binding.searchRecyclerView.adapter = searchAdapter
    }

    private fun getSuggestionKeyword(newText: String){
        val retrofit = RetrofitSuggestionKeyword.initRetrofit()
        retrofit.create(RetrofitService::class.java).getSuggestionKeyword("firefox","yt",newText)
            .enqueue(object : Callback<ResponseBody>{
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    suggestionKeywords.clear()
                    val responseString = convertStringUnicodeToKorean(response.body()?.string()!!)
                    val splitBracketList = responseString.split('[')
                    Log.d("스플릿 괄호","$splitBracketList")
                    val splitCommaList = splitBracketList[2].split(',')
                    Log.d("스플릿 콤마","$splitCommaList")
                    addSubstringToSuggestionKeyword(splitCommaList)
                    Log.d("서치 키워드","$suggestionKeywords")

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

    private fun getHomeVideo() {
        val retrofit = RetrofitVideo.initRetrofit()
        retrofit.create(RetrofitService::class.java).getVideoDetails(API_KEY,"snippet",intentSearch,"50","video")
            .enqueue(object : Callback<YoutubeSearchData> {
                override fun onResponse(call: Call<YoutubeSearchData>, response: Response<YoutubeSearchData>) {
                    for (index in 0 until response.body()?.items?.size!!){
                        val thumbnail = response?.body()?.items?.get(index)?.snippet?.thumbnails?.default?.url!!
                        val date = response?.body()?.items?.get(index)?.snippet?.publishedAt!!.substring(0, 10)
                        val account = response?.body()?.items?.get(index)?.snippet?.channelTitle!!
                        val title = stringToHtmlSign(response?.body()?.items?.get(index)?.snippet?.title!!)
                        val videoId = response?.body()?.items?.get(index)?.id?.videoId!!
                        videoData.add(VideoData(thumbnail, title, account, videoId, date))
                    }
                    Log.d("홈비디오 목록","$videoData")

                    homeAdapter.notifyDataSetChanged()
                    homeAdapter.setItemClickListener(object: HomeRecyclerViewAdapter.OnItemClickListener{
                        override fun onClick(v: View, position: Int) {
                            supportFragmentManager.beginTransaction().replace(binding.fragmentContainer.id,PlayerFragment(videoData[position])).commit()
                        }
                    })
                }
                override fun onFailure(call: Call<YoutubeSearchData>, t: Throwable) {
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
        var isDis = false
        floatButton.setOnClickListener{
            if (!isDis){
                floatButton.extend()
                isDis = true
            }
            else{
                floatButton.shrink()
                isDis = false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_item, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.youtube_search_icon).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            val searchAutoComplete = this.findViewById<SearchAutoComplete>(androidx.appcompat.R.id.search_src_text)
            searchAutoComplete.setTextColor(resources.getColor(R.color.white))
            searchAutoComplete.setHintTextColor(resources.getColor(R.color.black))
//            this.setOnSearchClickListener {
//                getSuggestionKeyword("omg")
//            }
            this.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
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
        super.onBackPressed()
        playerMotionLayout.transitionToStart()
    }

}