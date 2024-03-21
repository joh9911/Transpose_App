package com.myFile.transpose.view.Activity

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.myFile.transpose.R

class CustomBottomSheet<V : View>(context: Context, attrs: AttributeSet): BottomSheetBehavior<V>(context, attrs) {

    private val mContext: Context = context

    private lateinit var playerBottomSheet: ConstraintLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var bottomPlayerPauseButton: ImageView
    private lateinit var bottomPlayerCloseButton: ImageView
    private lateinit var bottomTitleTextView: TextView
    private lateinit var mainContainerLayout: View
    private lateinit var mainBackgroundView: View
    private lateinit var playerThumbnailView: ImageView
    private lateinit var bufferingProgressBar: ProgressBar
    private lateinit var playerView: PlayerView
    private lateinit var mainRecyclerView: RecyclerView
    private lateinit var playlistCoordinatorLayout: CoordinatorLayout
    private lateinit var playlistBackGroundView: View


    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: V,
        event: MotionEvent
    ): Boolean {
        val x = event.rawX.toInt()
        val y = event.rawY.toInt()

        val draggableArea = Rect()
        // 예: child.findViewById<View>(R.id.draggableArea)를 사용하여 뷰를 가져온다고 가정합니다.

        mainContainerLayout.getGlobalVisibleRect(draggableArea)

        return if (draggableArea.contains(x, y)) {
            super.onInterceptTouchEvent(parent, child, event)
        } else {
            false
        }
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        if (child is ConstraintLayout)
            playerBottomSheet = child
        bottomNavigationView = parent.findViewById(R.id.bottom_navigation_view)
        bottomPlayerCloseButton = child.findViewById(R.id.bottomPlayerCloseButton)
        bottomPlayerPauseButton = child.findViewById(R.id.bottomPlayerPauseButton)
        bottomTitleTextView = child.findViewById(R.id.bottomTitleTextView)
        mainBackgroundView = child.findViewById(R.id.main_background_view)
        playerThumbnailView = child.findViewById(R.id.playerThumbnailView)
        bufferingProgressBar = child.findViewById(R.id.bufferingProgressBar)
        playerView = child.findViewById(R.id.playerView)
        mainContainerLayout = child.findViewById(R.id.mainContainerLayout)
        mainRecyclerView = child.findViewById(R.id.mainRecyclerView)
        playlistCoordinatorLayout = child.findViewById(R.id.playlist_coordinator_layout)
        playlistBackGroundView = child.findViewById(R.id.playlist_background_view)

        return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun addBottomSheetCallback(callback: BottomSheetCallback) {
        super.addBottomSheetCallback(callback)
        super.addBottomSheetCallback(CustomBottomSheetCallback())
    }


    inner class CustomBottomSheetCallback: BottomSheetCallback(){
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when(newState){
                BottomSheetBehavior.STATE_EXPANDED -> {
                }
                BottomSheetBehavior.STATE_DRAGGING -> {
                }
                BottomSheetBehavior.STATE_COLLAPSED -> {
                }
                BottomSheetBehavior.STATE_HIDDEN -> {
                }
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            animateInPlayerView(slideOffset)
            animateInBottomSheetMargin(slideOffset)
            animateBottomNavigationView(slideOffset)
            animateInBottomViews(slideOffset)
            adjustBackgroundBrightness(slideOffset)
        }

    }



    private fun animateInBottomSheetMargin(slideOffset: Float){
        if (slideOffset >= 0){

            val scale = mContext.resources.displayMetrics.density
            val maxHeight = -56 * scale
            val transition = maxHeight * (1 - slideOffset)

            playerBottomSheet.translationY = transition
        }
    }

    private fun animateBottomNavigationView(slideOffset: Float){
        if (slideOffset >= 0){
            val translateYValue = bottomNavigationView.height * slideOffset
            bottomNavigationView.translationY = translateYValue
        }
    }

    private fun animateInBottomViews(slideOffset: Float){
        if (slideOffset >= 0){
            if (slideOffset < 0.2){
                val alphaValue = (0.2 - slideOffset) / 0.2 // 0에서 1 사이의 값
                bottomPlayerPauseButton.alpha = alphaValue.toFloat()
                bottomPlayerCloseButton.alpha = alphaValue.toFloat()
                bottomTitleTextView.alpha = alphaValue.toFloat()
            }else{
                bottomPlayerPauseButton.alpha = 0f
                bottomPlayerCloseButton.alpha = 0f
                bottomTitleTextView.alpha = 0f
            }
        }
    }

    private fun adjustBackgroundBrightness(slideOffset: Float){
        if (slideOffset >= 0){
//            mainBackgroundView.alpha = (1 - slideOffset * slideOffset * slideOffset).coerceAtLeast(0f)
            playlistBackGroundView.alpha = (1 - slideOffset * slideOffset * slideOffset).coerceAtLeast(0f)
        }
    }



    private fun animateInPlayerView(slideOffset: Float){
        val scale = mContext.resources.displayMetrics.density
        val peekHeightPx = 56 * scale
        val defaultHeight = mainContainerLayout.height

        val scaleFactor = peekHeightPx / defaultHeight + (1 - peekHeightPx / defaultHeight) * slideOffset

        // 내릴 때의 애니메이션
        if (slideOffset >= 0){

            mainContainerLayout.pivotY = 0f
            mainContainerLayout.scaleY = scaleFactor

            playerThumbnailView.pivotY = 0f
            playerThumbnailView.scaleY = scaleFactor

            bufferingProgressBar.pivotY = 0f
            bufferingProgressBar.scaleY = scaleFactor

            playerView.pivotY = 0f
            playerView.scaleY = scaleFactor

            val playerViewOriginalHeight = playerView.height
            val playerViewNewHeight = playerViewOriginalHeight * scaleFactor
            val translationDistance = playerViewOriginalHeight - playerViewNewHeight

            mainRecyclerView.translationY = -translationDistance
            mainBackgroundView.translationY = -translationDistance
            playlistCoordinatorLayout.translationY = -translationDistance
            playlistBackGroundView.translationY = -translationDistance
            if (slideOffset < 0.2){
                // scaleFactorX는 slideOffset이 0일 때 0.3가 되고 slideOffset이 0.2일 때 1이 됨
                val scaleFactorX = 1 - (0.2 - slideOffset) / 0.2 * 0.7

                playerThumbnailView.pivotX = 0f
                playerThumbnailView.scaleX = scaleFactorX.toFloat()

                bufferingProgressBar.pivotX = 0f
                bufferingProgressBar.scaleX = scaleFactorX.toFloat()

                playerView.pivotX = 0f
                playerView.scaleX = scaleFactorX.toFloat()
            }else{
                playerThumbnailView.scaleX = 1f
                bufferingProgressBar.scaleX = 1f
                playerView.scaleX = 1f
            }
        }
    }




}