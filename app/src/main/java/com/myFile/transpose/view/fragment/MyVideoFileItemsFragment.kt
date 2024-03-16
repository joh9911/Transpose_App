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
import com.myFile.transpose.databinding.FragmentMyVideoFileItemBinding
import com.myFile.transpose.others.constants.Actions
import com.myFile.transpose.view.Activity.Activity
import com.myFile.transpose.view.adapter.MyVideoFileItemsRecyclerViewAdapter
import com.myFile.transpose.viewModel.MyAudioFileViewModel
import com.myFile.transpose.viewModel.MyAudioFileViewModelFactory
import com.myFile.transpose.viewModel.MyVideoFileViewModel
import com.myFile.transpose.viewModel.MyVideoFileViewModelFactory
import com.myFile.transpose.viewModel.SharedViewModel

class MyVideoFileItemsFragment: Fragment() {
    var fbinding: FragmentMyVideoFileItemBinding? = null
    val binding get() = fbinding!!

    lateinit var activity: Activity

    lateinit var sharedViewModel: SharedViewModel

    private lateinit var myVideoFileItemsRecyclerViewAdapter: MyVideoFileItemsRecyclerViewAdapter

    private lateinit var deletePermissionLauncher: ActivityResultLauncher<IntentSenderRequest>

    // delete 시 필요
    private var currentId: Uri? = null
    private var deletePosition: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fbinding = FragmentMyVideoFileItemBinding.inflate(inflater, container, false)
        initViewModel()
        initRecyclerView()

        deletePermissionLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            Log.d(Actions.TAG, "붜든")
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                deleteVideoFile()
            } else {
                Log.d(Actions.TAG,"권한을 거부함")
            }
        }

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        sharedViewModel.fromChildFragmentInNavFragment.value = findNavController().currentDestination?.id
        if (!sharedViewModel.isMyVideoFileLoaded)
            sharedViewModel.fetchVideoFiles(requireContext())
    }

    private fun initRecyclerView(){
        binding.myVideoFileItemRecyclerView.layoutManager = LinearLayoutManager(activity)
        myVideoFileItemsRecyclerViewAdapter = MyVideoFileItemsRecyclerViewAdapter()
        myVideoFileItemsRecyclerViewAdapter.setItemClickListener(object: MyVideoFileItemsRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val myPlaylistItems = sharedViewModel.myVideoFiles.value ?: return
                val myPlaylistTitle = "MyVideoFiles"
                val nowPlaylistModel = NowPlaylistModel(myPlaylistItems, position, myPlaylistTitle, null)
                activity.activatePlayerInMyVideoFilesMode(nowPlaylistModel)
            }

            override fun optionButtonClick(v: View, position: Int) {

                val popUp = PopupMenu(requireContext(), v)
                popUp.menuInflater.inflate(R.menu.delete_video_from_device_pop_up_menu_text, popUp.menu)
                popUp.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.delete_video_from_device -> {
                            sharedViewModel.myVideoFilesOrigin.value?.let { items ->
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
        binding.myVideoFileItemRecyclerView.adapter = myVideoFileItemsRecyclerViewAdapter
        val paddingInPixels = dpToPx(requireContext(), 56)
        binding.myVideoFileItemRecyclerView.addItemDecoration(CustomItemDecoration(paddingInPixels))
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

    private fun requestMediaWritePermission(id: Long?){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && id != null) {

            val mediaUri = listOf(ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id))

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
                deleteVideoFile()
            }
        }
    }

    private fun deleteVideoFile(){
        if (currentId != null){
            if (sharedViewModel.deleteVideoFile(requireContext(), currentId!!)){
                deletePosition?.let {
                    sharedViewModel.deleteVideoFileFromList(it)
                    myVideoFileItemsRecyclerViewAdapter.submitList(sharedViewModel.myVideoFiles.value?.toMutableList())
                }

                Toast.makeText(requireContext(),requireContext().getString(R.string.my_file_delete_message), Toast.LENGTH_SHORT).show()
            }
        }
        else
            Toast.makeText(requireContext(),requireContext().getString(R.string.my_file_delete_message), Toast.LENGTH_SHORT).show()
    }


    fun initViewModel(){
        val application = requireActivity().application as MyApplication
        val viewModelFactory = MyVideoFileViewModelFactory(application)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
    }


    fun initObserver(){
        sharedViewModel.myVideoFiles.observe(viewLifecycleOwner){
            myVideoFileItemsRecyclerViewAdapter.submitList(it)
            binding.myVideoFileItemProgressBar.visibility = View.GONE
            if (it.isEmpty())
                binding.emptyTextView.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        Log.d("로그 확인","비디오 아이템 온데스트로이뷰")
        super.onDestroyView()
    }

}