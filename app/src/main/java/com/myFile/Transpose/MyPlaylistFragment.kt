package com.myFile.Transpose

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.myFile.Transpose.databinding.FragmentChannelBinding
import com.myFile.Transpose.databinding.FragmentMyPlaylistListBinding
import com.myFile.Transpose.databinding.MainBinding
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyPlaylistFragment: Fragment() {
    lateinit var mainBinding: MainBinding
    var fbinding: FragmentMyPlaylistListBinding? = null
    val binding get() = fbinding!!
    lateinit var activity: Activity

    lateinit var playlistToolBar: androidx.appcompat.widget.Toolbar
    val suggestionKeywords = ArrayList<String>()

    private lateinit var searchAdapter: SearchSuggestionKeywordRecyclerViewAdapter
    lateinit var searchView: SearchView
    private lateinit var callback: OnBackPressedCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fbinding = FragmentMyPlaylistListBinding.inflate(inflater, container, false)
        mainBinding = MainBinding.inflate(layoutInflater)
        setHasOptionsMenu(true)
        val view = binding.root
        initRecyclerView()
        initToolbar()
        Log.d("실행됨","ㄴㅇㄹㄴㅇㄹ")
        return view
    }
    fun initRecyclerView(){
        binding.searchRecyclerView.layoutManager = LinearLayoutManager(activity)
        searchAdapter = SearchSuggestionKeywordRecyclerViewAdapter()
        searchAdapter.setItemClickListener(object: SearchSuggestionKeywordRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val searchWord = suggestionKeywords[position]
                suggestionKeywords.clear()
                searchAdapter.submitList(suggestionKeywords.toMutableList())
                searchView.setQuery(searchWord,false) // 검색한 키워드 텍스트 설정
                searchView.clearFocus()
                activity.supportFragmentManager.beginTransaction()
                    .replace(binding.resultFrameLayout.id,SearchResultFragment(searchWord))
                    .addToBackStack(null)
                    .commit()
                binding.searchRecyclerView.visibility = View.INVISIBLE
            }
        })
        binding.searchRecyclerView.adapter = searchAdapter
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.transpose_icon -> {
                true
            }
            R.id.youtube_search_icon -> {
                Log.d("서치 버튼이1","눌림")
//                binding.searchRecyclerView.visibility = View.VISIBLE
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



    fun initToolbar(){
        playlistToolBar = binding.playlistToolBar
        activity.setSupportActionBar(playlistToolBar)
        activity.supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu,inflater)
        inflater.inflate(R.menu.toolbar_item, menu)
        val searchManager = activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.youtube_search_icon).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(activity.componentName))
            searchView = this
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
                    binding.searchRecyclerView.visibility = View.VISIBLE
                }
            }
            menu.findItem(R.id.youtube_search_icon).setOnActionExpandListener(object: MenuItem.OnActionExpandListener{
                override fun onMenuItemActionExpand(p0: MenuItem): Boolean {
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
                    if (activity.supportFragmentManager.findFragmentById(R.id.player_fragment) == null) {
                        return if (activity.transposePage.visibility == View.VISIBLE) {
                            Log.d("1","1")
                            activity.transposePageInvisibleEvent()
                            false
                        } else{
                            searchViewCollapseEvent()
                            true
                        }
                    } else {
                        val playerFragment =
                            activity.supportFragmentManager.findFragmentById(activity.binding.playerFragment.id) as PlayerFragment
                        return if (playerFragment.binding.playerMotionLayout.currentState == R.id.end) {
                            playerFragment.binding.playerMotionLayout.transitionToState(R.id.start)
                            false
                        } else {
                            if (activity.transposePage.visibility == View.VISIBLE) {
                                Log.d("1","2")
                                activity.transposePageInvisibleEvent()
                                false
                            } else{
                                searchViewCollapseEvent()
                                true
                            }
                        }
                    }
                }
            })
            this.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    Log.d("난 분명","쳤다123131")
                    searchView.clearFocus()
                    childFragmentManager.beginTransaction()
                        .replace(binding.resultFrameLayout.id,SearchResultFragment(query!!))
                        .addToBackStack(null)
                        .commit()
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
                        searchAdapter.submitList(suggestionKeywords.toMutableList())
                    }
                    return false
                }
            })
        }
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
                    searchAdapter.submitList(suggestionKeywords.toMutableList())
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
        Log.d("이게 실행이 됏잖아","왜")
        activity.supportFragmentManager.popBackStackImmediate()
//        binding.toolBar.setBackgroundColor(resources.getColor(R.color.black))
        activity.binding.bottomNavigationView.visibility = View.VISIBLE
        binding.searchRecyclerView.visibility = View.INVISIBLE
        suggestionKeywords.clear()
        searchAdapter.submitList(suggestionKeywords.toMutableList())

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
                Log.d("myPlaylistFragment","backPress")
                if (playlistToolBar.menu.findItem(R.id.youtube_search_icon).isActionViewExpanded)
                    playlistToolBar.collapseActionView()
                else
                    childFragmentManager.popBackStack()

            }
        }
        childFragmentManager.addOnBackStackChangedListener {
            if (childFragmentManager.backStackEntryCount == 0) {
                Log.d("콜백 ", "해제")
                callback.remove()
            }
            else
                activity.onBackPressedDispatcher.addCallback(this, callback)
        }
    }
}