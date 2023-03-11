package com.myFile.Transpose

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.motion.widget.MotionLayout

class PlayerMotionLayout(context: Context, attributeSet: AttributeSet? = null) : MotionLayout(context, attributeSet) {

    private var motionTouchStarted = false
    private var startX: Float? = null
    private var startY: Float? = null

    private val mainContainerView by lazy {
        findViewById<View>(R.id.mainContainerLayout)
    }

    private val playerMotionLayout by lazy {
        findViewById<MotionLayout>(R.id.player_motion_layout)
    }
    private val bottomPlayerPlayButton by lazy{
        findViewById<ImageView>(R.id.bottomPlayerPauseButton)
    }
    private val bottomPlayerCloseButton by lazy{
        findViewById<ImageView>(R.id.bottomPlayerCloseButton)
    }

    private val hitRect = Rect()

    init {
        playerMotionLayout.transitionToState(R.id.end)
        setTransitionListener(object : TransitionListener {
            override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {
            }

            override fun onTransitionChange(motionLayout: MotionLayout?, startId: Int, endId: Int, progress: Float) {
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                motionTouchStarted = false
            }
            override fun onTransitionTrigger(motionLayout: MotionLayout?, triggerId: Int, positive: Boolean, progress: Float) {}
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // ACTION_UP 과 ACTION_CANCEL 은 필요 없어서 when 문으로 motionTouchStarted 값을 false로 주었다.
        when (event.actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                motionTouchStarted = false
                return super.onTouchEvent(event)
            }
        }
        if (!motionTouchStarted) {
            mainContainerView.getHitRect(hitRect) // 해당 뷰의 클릭 영역 hitRect에 저장
            motionTouchStarted = hitRect.contains(event.x.toInt(), event.y.toInt())
        }

        return super.onTouchEvent(event) && motionTouchStarted
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        Log.d("idsafsdf","Adfsafsaff")
        bottomPlayerCloseButton.getHitRect(hitRect)
        if (hitRect.contains(ev.x.toInt(), ev.y.toInt()))
            return super.dispatchTouchEvent(ev)
        bottomPlayerPlayButton.getHitRect(hitRect)
        if (hitRect.contains(ev.x.toInt(), ev.y.toInt()))
            return super.dispatchTouchEvent(ev)
        mainContainerView.getHitRect(hitRect)
        if (hitRect.contains(ev.x.toInt(), ev.y.toInt())){
            when (ev.action){
                MotionEvent.ACTION_DOWN -> {
                    startX = ev.x
                    startY = ev.y
                }
                MotionEvent.ACTION_UP -> {
                    val endX = ev.x
                    val endY = ev.y

                    if (playerMotionLayout.currentState == R.id.start){
                        if (isAClick(startX!!, endX, startY!!, endY)){
                            playerMotionLayout.transitionToEnd()
                            return true
                        }
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
    private fun isAClick(startX: Float, endX: Float, startY: Float, endY: Float): Boolean {
        val differenceX = Math.abs(startX - endX)
        val differenceY = Math.abs(startY - endY)
        return !(differenceX > 200 || differenceY > 200)
    }

    private val gestureListener by lazy {
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                mainContainerView.getHitRect(hitRect)
                return hitRect.contains(e1.x.toInt(), e1.y.toInt())
            }

        }
    }

    private val gestureDetector by lazy {
        GestureDetector(context, gestureListener)
    }

    //이게 true면 커스텀모션 레이아웃이라는 전체 틀에서만 터치 이벤트가 실행됨
    //따라서 안의 뷰는 터치가 안먹음
    //이거 로그로 gestureDetector.onTouchEvent(event) 이거 하면 작동안함 왤까?
    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event!!)
    }
}