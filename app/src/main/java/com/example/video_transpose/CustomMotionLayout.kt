package com.example.video_transpose

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.motion.widget.MotionLayout

class CustomMotionLayout(context: Context, attributeSet: AttributeSet? = null) : MotionLayout(context, attributeSet) {

    private var motionTouchStarted = false
    private var scrollViewTouchStarted = false

    private val mainContainerView by lazy {
        findViewById<View>(R.id.mainContainerLayout)
    }
//    private val playerFragmentScrollView by lazy {
//        findViewById<View>(R.id.fragment_scroll_view)
//    }

    private val playerMotionLayout by lazy {
        findViewById<MotionLayout>(R.id.player_motion_layout)
    }

    private val bottomCloseButton by lazy {
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

    private val gestureListener by lazy {
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                mainContainerView.getHitRect(hitRect)
                Log.d("onscroll","${hitRect.contains(e1.x.toInt(), e1.y.toInt())}")
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
        return gestureDetector.onTouchEvent(event)
    }
}