package com.myFile.transpose.view.fragment

import android.app.Activity.RESULT_OK
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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myFile.transpose.MyApplication
import com.myFile.transpose.R
import com.myFile.transpose.data.model.NowPlaylistModel
import com.myFile.transpose.databinding.FragmentMyAudioFileItemBinding
import com.myFile.transpose.others.constants.Actions.TAG
import com.myFile.transpose.view.Activity.Activity
import com.myFile.transpose.view.adapter.MyAudioFileItemsRecyclerViewAdapter
import com.myFile.transpose.viewModel.MyAudioFileViewModel
import com.myFile.transpose.viewModel.MyAudioFileViewModelFactory
import com.myFile.transpose.viewModel.SharedViewModel

class MyAudioFileItemsFragment: Fragment()  {
    var fbinding: FragmentMyAudioFileItemBinding? = null
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
        fbinding = FragmentMyAudioFileItemBinding.inflate(inflater, container, false)
        val view = binding.root
        initViewModel()
        initRecyclerView()
        deletePermissionLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            Log.d(TAG, "붜든")
            if (result.resultCode == RESULT_OK) {
                deleteMusicFile()
            } else {
                Log.d(TAG,"권한을 거부함")
            }
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        sharedViewModel.fromChildFragmentInNavFragment.value = findNavController().currentDestination?.id
        if (!sharedViewModel.isMyAudioFileLoaded)
            sharedViewModel.fetchMusicFiles(requireActivity())
    }

    private fun initRecyclerView(){
        binding.myAudioFileItemRecyclerView.layoutManager = LinearLayoutManager(activity)
        myAudioFileItemsRecyclerViewAdapter = MyAudioFileItemsRecyclerViewAdapter()
        myAudioFileItemsRecyclerViewAdapter.setItemClickListener(object: MyAudioFileItemsRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val myPlaylistItems = sharedViewModel.myAudioFiles.value ?: return
                val myPlaylistTitle = "MyAudioFiles"
                val nowPlaylistModel = NowPlaylistModel(myPlaylistItems, position, myPlaylistTitle)
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
        binding.myAudioFileItemRecyclerView.adapter = myAudioFileItemsRecyclerViewAdapter
        val paddingInPixels = dpToPx(requireContext(), 56)
        binding.myAudioFileItemRecyclerView.addItemDecoration(CustomItemDecoration(paddingInPixels))
    }

    private fun deleteMusicFile(){
        if (currentId != null){
            if (sharedViewModel.deleteMusicFile(requireContext(), currentId!!)){
                deletePosition?.let {
                    sharedViewModel.deleteMusicFileFromList(it)
                    myAudioFileItemsRecyclerViewAdapter.submitList(sharedViewModel.myAudioFiles.value?.toMutableList())
                }

                Toast.makeText(requireContext(),requireContext().getString(R.string.my_file_delete_message),Toast.LENGTH_SHORT).show()
            }
        }
        else
            Toast.makeText(requireContext(),requireContext().getString(R.string.my_file_delete_message),Toast.LENGTH_SHORT).show()
    }

    private fun requestMediaWritePermission(id: Long?){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && id != null) {

            val mediaUri = listOf(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id))

            val writeRequest =
                MediaStore.createWriteRequest(requireContext().contentResolver, mediaUri)

            currentId = mediaUri.first()
            Log.d(TAG,"실행전")
            deletePermissionLauncher.launch(
                IntentSenderRequest.Builder(writeRequest.intentSender).build()
            )
            Log.d(TAG,"실행후")
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
    private fun showDialogForDeletionConfirm(id: Uri){
        AlertDialog.Builder(requireContext())
            .setTitle(resources.getString(R.string.delete_confirmation_dialog_title))
            .setMessage(resources.getString(R.string.delete_confirmation_dialog_message))
            .setPositiveButton(resources.getString(R.string.delete_confirmation_dialog_confirm_text)) { _, _ ->
                if (sharedViewModel.deleteMusicFile(requireContext(), id)){
                    Toast.makeText(requireContext(),"정상적으로 삭제됨",Toast.LENGTH_SHORT).show()
                }
                else
                    Toast.makeText(requireContext(),"정상적으로 삭제됨",Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(resources.getString(R.string.delete_confirmation_dialog_cancel_text), null)
            .show()
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
        val viewModelFactory = MyAudioFileViewModelFactory(application)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
    }


    fun initObserver(){
        sharedViewModel.myAudioFiles.observe(viewLifecycleOwner){
            myAudioFileItemsRecyclerViewAdapter.submitList(it)
            binding.myAudioFileItemProgressBar.visibility = View.GONE
            if (it.isEmpty())
                binding.emptyTextView.visibility = View.VISIBLE
        }
    }
}