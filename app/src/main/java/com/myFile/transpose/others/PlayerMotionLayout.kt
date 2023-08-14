package com.myFile.transpose.others

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.motion.widget.MotionLayout
import com.myFile.transpose.R

class PlayerMotionLayout(context: Context, attributeSet: AttributeSet? = null) : MotionLayout(context, attributeSet) {

    private var motionTouchStarted = false
    private var startX: Float? = null
    private var startY: Float? = null

    private val mainContainerView by lazy {
        findViewById<View>(R.id.mainContainerLayout)
    }

    private val bottomPlayerPlayButton by lazy{
        findViewById<ImageView>(R.id.bottomPlayerPauseButton)
    }
    private val bottomPlayerCloseButton by lazy{
        findViewById<ImageView>(R.id.bottomPlayerCloseButton)
    }

    private val hitRect = Rect()

    init {
        transitionToState(R.id.end)

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                motionTouchStarted = false

                return super.onTouchEvent(event)
            }
        }
        if (!motionTouchStarted) {
            mainContainerView.getHitRect(hitRect)
            motionTouchStarted = hitRect.contains(event.x.toInt(), event.y.toInt())
        }

        return super.onTouchEvent(event) && motionTouchStarted
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
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

                    if (currentState == R.id.start){
                        if (isAClick(startX!!, endX, startY!!, endY)){
                            transitionToState(R.id.end)
                            motionTouchStarted = false
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

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event!!)
    }
}