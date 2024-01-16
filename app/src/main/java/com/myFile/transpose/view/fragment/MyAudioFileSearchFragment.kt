package com.myFile.transpose.view.fragment

import android.content.ContentUris
import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myFile.transpose.MyApplication
import com.myFile.transpose.R
import com.myFile.transpose.data.model.NowPlaylistModel
import com.myFile.transpose.databinding.FragmentMyAudioFileSearchBinding
import com.myFile.transpose.others.constants.Actions
import com.myFile.transpose.view.Activity.Activity
import com.myFile.transpose.view.adapter.MyAudioFileItemsRecyclerViewAdapter
import com.myFile.transpose.viewModel.MyAudioFileViewModelFactory
import com.myFile.transpose.viewModel.SharedViewModel

class MyAudioFileSearchFragment: Fragment() {
    var fbinding: FragmentMyAudioFileSearchBinding? = null
    val binding get() = fbinding!!

    lateinit var activity: Activity


    lateinit var sharedViewModel: SharedViewModel

    private lateinit var myAudioFileItemsRecyclerViewAdapter: MyAudioFileItemsRecyclerViewAdapter

    private lateinit var deletePermissionLauncher: ActivityResultLauncher<IntentSenderRequest>

    // delete 시 필요
    private var currentId: Uri? = null
    private var deletePosition: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fbinding = FragmentMyAudioFileSearchBinding.inflate(inflater, container, false)
        initViewModel()
        initRecyclerView()

        deletePermissionLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            Log.d(Actions.TAG, "붜든")
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                deleteMusicFile()
            } else {
                Log.d(Actions.TAG,"권한을 거부함")
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

    inner class CustomItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect, view: View,
            parent: RecyclerView, state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            val totalItemCount = parent.adapter?.itemCount ?: 0


            if (position == totalItemCount - 1) {  // 마지막 아이템인 경우
                outRect.bottom = space
            }
        }
    }

    fun initViewModel(){
        val application = requireActivity().application as MyApplication
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
    }

    private fun initObserver(){
        sharedViewModel.mySearchedAudioFiles.observe(viewLifecycleOwner){
            val currentList = it.map{list ->
                list.first
            }
            myAudioFileItemsRecyclerViewAdapter.submitList(currentList.toMutableList())
        }
    }
    fun hideKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        // 현재 포커스를 가진 뷰를 찾습니다.
        val currentFocusedView = activity.currentFocus
        currentFocusedView?.let {
            // 키보드를 숨깁니다.
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }


    private fun initRecyclerView(){
        binding.myAudioFileSearchRecyclerView.layoutManager = LinearLayoutManager(activity)
        myAudioFileItemsRecyclerViewAdapter = MyAudioFileItemsRecyclerViewAdapter()
        myAudioFileItemsRecyclerViewAdapter.setItemClickListener(object: MyAudioFileItemsRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val myPlaylistItems = sharedViewModel.myAudioFiles.value ?: return
                val mySearchedAudioFiles = sharedViewModel.mySearchedAudioFiles.value ?: return

                val myPlaylistTitle = "MyAudioFiles"
                val nowPlaylistModel = NowPlaylistModel(myPlaylistItems, mySearchedAudioFiles[position].second, myPlaylistTitle)
                hideKeyboard(activity)
                activity.myFileSearchView.clearFocus()
                activity.activatePlayerInMyAudioFilesMode(nowPlaylistModel)
            }

            override fun optionButtonClick(v: View, position: Int) {

                val popUp = PopupMenu(requireContext(), v)
                popUp.menuInflater.inflate(R.menu.my_playlist_pop_up_menu, popUp.menu)
                popUp.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.delete_my_playlist -> {
                            sharedViewModel.myAudioFilesOrigin.value?.let { items ->
                                deletePosition = position
                                requestMediaWritePermission(items[position].id)

                            }
                        }
                    }
                    true
                }
                popUp.show()
            }
        })
        binding.myAudioFileSearchRecyclerView.adapter = myAudioFileItemsRecyclerViewAdapter
        val paddingInPixels = dpToPx(requireContext(), 56)
        binding.myAudioFileSearchRecyclerView.addItemDecoration(CustomItemDecoration(paddingInPixels))
    }

    private fun deleteMusicFile(){
        if (currentId != null){
            if (sharedViewModel.deleteMusicFile(requireContext(), currentId!!)){
                deletePosition?.let {
                    sharedViewModel.deleteMusicFileFromList(it)
                    myAudioFileItemsRecyclerViewAdapter.submitList(sharedViewModel.myAudioFiles.value?.toMutableList())
                }

                Toast.makeText(requireContext(),requireContext().getString(R.string.my_file_delete_message),
                    Toast.LENGTH_SHORT).show()
            }
        }
        else
            Toast.makeText(requireContext(),requireContext().getString(R.string.my_file_delete_message),
                Toast.LENGTH_SHORT).show()
    }

    private fun requestMediaWritePermission(id: Long?){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && id != null) {

            val mediaUri = listOf(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id))

            val writeRequest =
                MediaStore.createWriteRequest(requireContext().contentResolver, mediaUri)

            currentId = mediaUri.first()
            Log.d(Actions.TAG,"실행전")
            deletePermissionLauncher.launch(
                IntentSenderRequest.Builder(writeRequest.intentSender).build()
            )
            Log.d(Actions.TAG,"실행후")
        }
        else{
            if (activity.hasWriteExternalStoragePermission().not()){
                activity.requestWriteExternalStoragePermission()
            }
            else{
                deleteMusicFile()
            }
        }

    }

}