package com.myFile.transpose.viewModel

import android.content.Context

import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.github.mikephil.charting.data.Entry
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.myFile.transpose.MyApplication
import com.myFile.transpose.data.model.*
import com.myFile.transpose.utils.YoutubeDataMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class SharedViewModel(application: MyApplication): ViewModel() {
    private val youtubeDataRepository = application.youtubeDataRepository
    private val youtubeDataMapper = YoutubeDataMapper(application.applicationContext)
    private val suggestionKeywordRepository = application.suggestionKeywordRepository


    /*
    VideoPlayerFragment 를 위한 playbackMode.
    PlaybackMode 가 같을 경우 현재 실행중인 VideoPlayerFragment 의 뷰만 바꿔줌
    다를 경우 replace
     */
    enum class PlaybackMode {
        SINGLE_VIDEO,
        PLAYLIST,
        MYAUDIOFILES,
    }
    var playbackMode: PlaybackMode = PlaybackMode.SINGLE_VIDEO

    // VideoPlayerFragment 를 위한 변수


    // 현재 재생곡
    private val _currentPlayingVideo: MutableLiveData<CurrentVideoDataModel?> = MutableLiveData()
    val currentPlayingVideo: LiveData<CurrentVideoDataModel?> get() = _currentPlayingVideo

    var nowPlaylistModel: NowPlaylistModel? = null

    private val _currentVideoDetailData: MutableLiveData<VideoDetailDataModel?> = MutableLiveData()
    val currentVideoDetailData: LiveData<VideoDetailDataModel?> get() = _currentVideoDetailData

    private val _currentChannelDetailData: MutableLiveData<ChannelDataModel?> = MutableLiveData()
    val currentChannelDetailData: LiveData<ChannelDataModel?> get() = _currentChannelDetailData

    private val _currentCommentThreadData: MutableLiveData<List<CommentDataModel>?> = MutableLiveData()
    val currentCommentThreadData: LiveData<List<CommentDataModel>?> get() = _currentCommentThreadData

    // Single Mode 일 때
    private val _singleModeVideoId: MutableLiveData<String> = MutableLiveData()
    val singleModeVideoId: LiveData<String> get() = _singleModeVideoId

    private val _currentVideoDataModel: MutableLiveData<VideoDataModel?> = MutableLiveData()
    val currentVideoDataModel: LiveData<VideoDataModel?> get() = _currentVideoDataModel


    // PlaylistsFragment 를 위한 변수
    private val _playlistData: MutableLiveData<PlaylistDataModel> = MutableLiveData()
    val playlistData: LiveData<PlaylistDataModel> get() = _playlistData

    // SearchResultFragment 를 위한 변수
    enum class SearchResultMode{
        SearchKeyword,
        SharedLink
    }
    var searchResultMode = SearchResultMode.SearchKeyword

    var searchKeyword = ""

    var sharedLink = ""


    // suggestionKeyword를 위한 변수

    private val _suggestionKeywords: MutableLiveData<ArrayList<String>> = MutableLiveData()
    val suggestionKeywords: LiveData<ArrayList<String>> get() = _suggestionKeywords

    // searchView 상태
    var isSearchViewExpanded: Boolean = false

    var aboutToCollapsedBySearchButton: Boolean = false


    // MyPlaylistItemFragment 를 위한 변수
    private val _myPlaylistId: MutableLiveData<Int> = MutableLiveData(0)
    val myPlaylistId: LiveData<Int> get() = _myPlaylistId

    var myPlaylistTitle: String? = null

    // PlayerBottomSheet State 공유
    private val _bottomSheetState: MutableLiveData<Int> = MutableLiveData()
    val bottomSheetState: LiveData<Int> get() = _bottomSheetState

    var playlistBottomSheetState: Int = BottomSheetBehavior.STATE_HIDDEN

    var isFullScreenMode: Boolean = false
    var isManualOrientationChange = false

    // HomeNavFragment 를 위한 변수

    var fromFragmentIdInHomeNavFragment: Int? = null

    var fromFragmentIdInConvertNavFragment: Int? = null

    var fromFragmentIdInLibraryNavFragment: Int? = null

    // MyAudio, MyVideo 검색창을 위한 방법
    // 각 childFragment 의 onViewCreate 에 현재 id 설정하게 해둠
    // observer를 통해, myfileItem 프레그먼트 이동시 toobar icon 보이게 해둠
    // id에 따라 오디오 검색, 비디오 검색으로 나눔
    val fromChildFragmentInNavFragment: MutableLiveData<Int> = MutableLiveData()

    // AudioEditFragment 연동을 위한 피치 템포 값

    var audioEditFragmentScrollValue = 0

    private val _pitchValue: MutableLiveData<Int> = MutableLiveData()
    val pitchValue get() = _pitchValue

    private val _tempoValue: MutableLiveData<Int> = MutableLiveData()
    val tempoValue get() = _tempoValue

    private val _bassBoostValue = MutableLiveData(0)
    val bassBoostValue: LiveData<Int> get() = _bassBoostValue

    private val _loudnessEnhancerValue = MutableLiveData(0)
    val loudnessEnhancerValue: LiveData<Int> get() = _loudnessEnhancerValue

    private val _virtualizerValue = MutableLiveData(0)
    val virtualizerValue: LiveData<Int> get() = _virtualizerValue

    private val _presetReverbIndexValue = MutableLiveData(0)
    val presetReverbIndexValue: LiveData<Int> get() = _presetReverbIndexValue

    private val _presetReverbSendLevel = MutableLiveData(0)
    val presetReverbSendLevel: LiveData<Int> get() = _presetReverbSendLevel

    private val _equalizerIndexValue = MutableLiveData(3)
    val equalizerIndexValue: LiveData<Int> get() = _equalizerIndexValue

    private val _isEqualizerEnabled = MutableLiveData(false)
    val isEqualizerEnabled: LiveData<Boolean> get() = _isEqualizerEnabled

    private val _isPresetReverbEnabled = MutableLiveData(false)
    val isPresetReverbEnabled: LiveData<Boolean> get() = _isPresetReverbEnabled

    fun setBassBoostValue(value: Int) {
        _bassBoostValue.value = value
    }

    fun setLoudnessEnhancerValue(value: Int) {
        _loudnessEnhancerValue.value = value
    }

    fun setVirtualizerValue(value: Int) {
        _virtualizerValue.value = value
    }

    fun setPresetReverbIndexValue(value: Int) {
        _presetReverbIndexValue.value = value
    }

    fun setPresetReverbSendLevel(value: Int) {
        _presetReverbSendLevel.value = value
    }

    fun setEqualizerIndexValue(value: Int) {
        _equalizerIndexValue.value = value
    }

    fun setIsPresetReverbEnabled(value: Boolean) {
        Log.d("플레이리스트 추가","${value}  setIsPresetReverbEnabled")
        _isPresetReverbEnabled.value = value
    }

    fun setIsEqualizerEnabled(value: Boolean) {
        _isEqualizerEnabled.value = value
    }

    var isEqualizerViewFolded: Boolean = true

    var equalizerChartValueList: ArrayList<Entry>? = null

    var isPresetReverbViewFolded: Boolean = true

    fun setAudioEffectValues(audioEffectsDataModel: AudioEffectsDataModel){
        setPitchValue(audioEffectsDataModel.pitchValue)
        setTempoValue(audioEffectsDataModel.tempoValue)
        setBassBoostValue(audioEffectsDataModel.bassBoostValue)
        setLoudnessEnhancerValue(audioEffectsDataModel.loudnessEnhancerValue)
        setVirtualizerValue(audioEffectsDataModel.virtualizerValue)
        setPresetReverbIndexValue(audioEffectsDataModel.presetReverbIndexValue)
        setPresetReverbSendLevel(audioEffectsDataModel.presetReverbSendLevel)
        setEqualizerIndexValue(audioEffectsDataModel.equalizerIndexValue)
        setIsPresetReverbEnabled(audioEffectsDataModel.isPresetReverbEnabled)
        setIsEqualizerEnabled(audioEffectsDataModel.isEqualizerEnabled)

    }

    // MyFile 들을 위한 변수

    // MyAudioFile
    private val _myAudioFilesOrigin:MutableLiveData<List<MusicFileDataModel>> = MutableLiveData()
    val myAudioFilesOrigin:LiveData<List<MusicFileDataModel>> get() = _myAudioFilesOrigin

    private val _myAudioFiles: MutableLiveData<List<VideoDataModel>> = MutableLiveData()
    val myAudioFiles: LiveData<List<VideoDataModel>> get() = _myAudioFiles

    private val _mySearchedAudioFiles: MutableLiveData<List<Pair<VideoDataModel,Int>>> = MutableLiveData()
    val mySearchedAudioFiles: LiveData<List<Pair<VideoDataModel,Int>>> get() = _mySearchedAudioFiles

    var isMyAudioFileLoaded: Boolean = false

    var myMusicFetchJob: Job? = null

    fun deleteMusicFile(context: Context, contentUri: Uri): Boolean {
        return try {
            val deletedRows = context.contentResolver.delete(contentUri, null, null)
            deletedRows > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    fun deleteMusicFileFromList(position: Int){
        val currentList = myAudioFiles.value?.toMutableList() ?: return
        currentList.removeAt(position)
        _myAudioFiles.value = currentList
    }

    fun fetchMusicFiles(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        val musicFilesList = mutableListOf<MusicFileDataModel>()
        val contentResolver = context.contentResolver
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATE_ADDED
        )

        val musicCursor = contentResolver.query(musicUri, projection, selection, null, null)
        try{
            musicCursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID) // 추가
                val pathColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val dateColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn) // 추가
                    val path = it.getString(pathColumn)
                    val title = it.getString(titleColumn)
                    val artist = it.getString(artistColumn)
                    val albumId = it.getLong(albumIdColumn)

                    val dateAddedInSeconds = it.getLong(dateColumn)


                    musicFilesList.add(MusicFileDataModel(id, path, title, artist, albumId, dateAddedInSeconds))
                }
            }
            isMyAudioFileLoaded = true

            _myAudioFilesOrigin.postValue(musicFilesList)

            _myAudioFiles.postValue(youtubeDataMapper.mapMyAudioFileToVideoDataModel(musicFilesList))
        }catch (e: Exception){
            Log.d("로그 확인","fetchMusicFiles ${e.message}")
        }
    }

    fun getAudioFileDataByIntent(context: Context, audioUri: Uri): List<VideoDataModel>? {
        val musicFilesList = mutableListOf<MusicFileDataModel>()
        val contentResolver = context.contentResolver

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATE_ADDED
        )
        try{
            contentResolver.query(audioUri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                    val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                    val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

                    val id = cursor.getLong(idColumn)
                    val path = cursor.getString(pathColumn)
                    val title = cursor.getString(titleColumn)
                    val artist = cursor.getString(artistColumn)
                    val albumId = cursor.getLong(albumIdColumn)
                    val date = cursor.getLong(dateColumn)


                    musicFilesList.add(MusicFileDataModel(id, path, title, artist, albumId, date))

                    return youtubeDataMapper.mapMyAudioFileToVideoDataModel(musicFilesList)
                }
            }
        }catch (e: Exception){
            Toast.makeText(context,"getAudioFileDataByIntent Error", Toast.LENGTH_SHORT).show()
            Log.d("로그 확인","getAudioFileDataByIntent ${e.message}")
            return null
        }

        return null
    }

    fun searchMyAudioFilesByKeyword(keyword: String) = viewModelScope.launch(Dispatchers.IO) {
        val currentList = myAudioFiles.value ?: return@launch

        val filteredAndIndexed = currentList
            .withIndex()
            .filter { it.value.title.contains(keyword, ignoreCase = true) }
            .map { Pair(it.value, it.index) }

        _mySearchedAudioFiles.postValue(filteredAndIndexed)
    }


    fun clearMySearchedAudioFiles(){
        _mySearchedAudioFiles.value = listOf()
    }

    // MyVideoFile
    private val _myVideoFilesOrigin: MutableLiveData<List<VideoFileDataModel>> = MutableLiveData()
    val myVideoFilesOrigin: LiveData<List<VideoFileDataModel>> get() = _myVideoFilesOrigin

    private val _myVideoFiles: MutableLiveData<List<VideoDataModel>> = MutableLiveData()
    val myVideoFiles: LiveData<List<VideoDataModel>> get() = _myVideoFiles

    private val _mySearchedVideoFiles: MutableLiveData<List<Pair<VideoDataModel,Int>>> = MutableLiveData()
    val mySearchedVideoFiles: LiveData<List<Pair<VideoDataModel,Int>>> get() = _mySearchedVideoFiles


    var isMyVideoFileLoaded: Boolean = false

    fun searchVideoFilesByKeyword(keyword: String) = viewModelScope.launch(Dispatchers.IO) {
        val currentList = myVideoFiles.value ?: return@launch

        val filteredAndIndexed = currentList
            .withIndex()
            .filter { it.value.title.contains(keyword, ignoreCase = true) }
            .map { Pair(it.value, it.index) }

        _mySearchedVideoFiles.postValue(filteredAndIndexed)
    }


    fun clearMySearchedVideoFiles(){
        _mySearchedVideoFiles.value = listOf()
    }

    fun deleteVideoFile(context: Context, contentUri: Uri): Boolean {
        return try {
            val deletedRows = context.contentResolver.delete(contentUri, null, null)
            deletedRows > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteVideoFileFromList(position: Int){
        val currentList = myVideoFiles.value?.toMutableList() ?: return
        currentList.removeAt(position)
        _myVideoFiles.value = currentList
    }


    fun fetchVideoFiles(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        val videoFilesList = mutableListOf<VideoFileDataModel>()
        val contentResolver = context.contentResolver
        val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATE_ADDED,
        )

        val videoCursor = contentResolver.query(videoUri, projection, null, null, null)

        try{
            videoCursor?.use {

                val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val pathColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                val titleColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                val durationColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val dateColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)


                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val path = it.getString(pathColumn)
                    val title = it.getString(titleColumn)
                    val thumbnail = null
                    val dateAddedInSeconds = it.getLong(dateColumn)

                    videoFilesList.add(
                        VideoFileDataModel(
                            id,
                            path,
                            title,
                            thumbnail,
                            dateAddedInSeconds
                        )
                    )

                }
            }
            isMyVideoFileLoaded = true
            _myVideoFilesOrigin.postValue(videoFilesList)
            _myVideoFiles.postValue(youtubeDataMapper.mapMyVideoFileToVideoDataModel(videoFilesList))
        }catch (e: Exception){
            Log.d("로그 확인","fetchVideoFiles ${e.message}")
        }

    }

    fun getVideoFileDataByIntent(context: Context, videoUri: Uri): List<VideoDataModel>? {
        val videoFilesList = mutableListOf<VideoFileDataModel>()
        val contentResolver = context.contentResolver

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATE_ADDED
        )
        try{
            contentResolver.query(videoUri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                    val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                    val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                    val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                    val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

                    val id = cursor.getLong(idColumn)
                    val path = cursor.getString(pathColumn)
                    val title = cursor.getString(titleColumn)
                    val thumbnail = null
                    val dateAddedInSeconds = cursor.getLong(dateAddedColumn)

                    videoFilesList.add(VideoFileDataModel(
                        id,
                        path,
                        title,
                        thumbnail,
                        dateAddedInSeconds
                    ))

                }
                return youtubeDataMapper.mapMyVideoFileToVideoDataModel(videoFilesList)
            }
        }catch (e: Exception){
            Toast.makeText(context,"getVideoFileDataByIntent Error", Toast.LENGTH_SHORT).show()
            Log.d("로그 확인","getVideoFileDataByIntent ${e.message}")
            return null
        }

        return null
    }


    fun setPitchValue(value: Int){
        _pitchValue.value = value
    }

    fun setTempoValue(value: Int){
        _tempoValue.value = value
    }

    fun setBottomSheetState(state: Int){
        _bottomSheetState.value = state
    }

    fun setMyPlaylistId(myPlaylistId: Int){
        _myPlaylistId.value = myPlaylistId
    }


    fun setSingleModeVideoId(videoId: String){
        _singleModeVideoId.postValue(videoId)
        _currentPlayingVideo.postValue(null)
        nowPlaylistModel = null
    }


    fun setPlaylistsFragmentData(playListData: PlaylistDataModel){
        _playlistData.value = playListData
    }

    fun clearCurrentVideoData(){
        _currentPlayingVideo.value = null
        _currentVideoDataModel.value = null
        _currentChannelDetailData.value = null
        _currentCommentThreadData.value = null
    }

    fun updateVideoByPosition(position: Int) {
        nowPlaylistModel ?: return

        nowPlaylistModel?.updateCurrentPosition(position)
        nowPlaylistModel?.refreshPlaylist()
        val nowPlaylistCurrentVideo = nowPlaylistModel?.currentMusicModel() ?: return
        // 피치 조절 뷰가 두번 나오는거 방지
        val currentVideo = currentPlayingVideo.value
        if (currentVideo?.videoDataModel != nowPlaylistCurrentVideo){
            _currentPlayingVideo.value = CurrentVideoDataModel(nowPlaylistCurrentVideo, System.currentTimeMillis())
        }

    }


    fun fetchAllData(videoId: String) = viewModelScope.launch(Dispatchers.IO){
        fetchDetailData(videoId)
        fetchCommentThreadData(videoId)
    }

    private suspend fun fetchDetailData(videoId: String){
        Log.d("패치 데이타","비디오아이디 $videoId")
        try {
            val videoDetailResponse = youtubeDataRepository.fetchVideoDetailData(videoId)
            val videoDetailBody = videoDetailResponse.body()
            Log.d("비디오 디테일 ","시도")

            if (videoDetailResponse.isSuccessful && videoDetailBody != null){
                Log.d("비디오 디테일","성공")
                val currentVideoDetailData = youtubeDataMapper.mapVideoDetailDataModel(videoDetailBody)
//                _currentVideoDetailData.postValue(null)
                _currentVideoDetailData.postValue(currentVideoDetailData)


                val currentVideoDataModel = youtubeDataMapper.mapVideoDataModelByVideoDetailResponse(videoDetailBody)
//                _currentVideoDataModel.postValue(null)
                _currentVideoDataModel.postValue(currentVideoDataModel)

                val channelDetailResponse = youtubeDataRepository.fetchChannelDetailData(currentVideoDetailData.channelId)
                val channelDetailBody = channelDetailResponse.body()
                Log.d("채널 디테일","ㅅ도")
                if (channelDetailResponse.isSuccessful && channelDetailBody != null){
                    Log.d("채널 디테일","성공")
                    val channelDataModel = youtubeDataMapper.mapChannelDataModel(channelDetailBody)
//                    _currentChannelDetailData.postValue(null)
                    _currentChannelDetailData.postValue(channelDataModel)
                }
            }
            else{
                _currentVideoDetailData.postValue(null)
                _currentVideoDataModel.postValue(null)
                _currentChannelDetailData.postValue(null)
            }
        }catch (e: Exception){
            Log.d("detailData오류", "$e")
            _currentVideoDetailData.postValue(null)
            _currentVideoDataModel.postValue(null)
            _currentChannelDetailData.postValue(null)
        }
    }


    private suspend fun fetchCommentThreadData(videoId: String){
        try {
            val response = youtubeDataRepository.fetchVideoCommentThreadData(videoId)

            val body = response.body()
            if (response.isSuccessful && body != null){
                val currentCommentDataModel = youtubeDataMapper.mapCommentThreadData(body)
                _currentCommentThreadData.postValue(currentCommentDataModel)
            }
            else{
                Log.d("왜 성공이지 않았지?","${response.message()}")
            }
        }catch (e: Exception){
            Log.d("코멘트 데이타","$e")
        }
    }

    fun clearSuggestionKeywords(){
        _suggestionKeywords.value = arrayListOf()
    }

    fun getSuggestionKeyword(newText: String) = viewModelScope.launch{
        try {
            val response = suggestionKeywordRepository.getSuggestionKeyword(newText)
            if (response.isSuccessful){
                response.body()?.string()?.let{
                    val responseString = convertStringUnicodeToKorean(it)
                    val splitBracketList = responseString.split('[')
                    val splitCommaList = splitBracketList[2].split(',')
                    if (splitCommaList[0] != "]]" && splitCommaList[0] != '"'.toString()) {
                        addSubstringToSuggestionKeyword(splitCommaList)
                    }
                }
            }
            else{
                clearSuggestionKeywords()
            }

        }catch (e: Exception){
            Log.d("suggestionQuery","$e")
        }
    }

    /**
    문자열 정보가 이상하게 들어와 알맞게 나눠주고 리스트에 추가
     **/
    private fun addSubstringToSuggestionKeyword(splitList: List<String>){
        val currentList = splitList.filter { it.length >= 3 }
            .map { if (it.last() == ']') it.substring(1, it.length - 2) else it.substring(1, it.length - 1) }
        _suggestionKeywords.value = ArrayList(currentList)
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

}

class SharedViewModelModelFactory(private val application: MyApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SharedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SharedViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}