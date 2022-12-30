package com.example.youtube_transpose

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import com.example.youtube_transpose.databinding.MainBinding

class Activity: AppCompatActivity() {
    var mBinding: MainBinding? = null
    val binding get() = mBinding!!
    lateinit var page: LinearLayout
    lateinit var translateRight: Animation
    lateinit var translateLeft: Animation
    lateinit var translateUp: Animation
    lateinit var translateDown: Animation

    var isPageOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = MainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        page = binding.page
        val toolbar = binding.toolBar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        translateRight = AnimationUtils.loadAnimation(this,R.anim.translate_right)
        translateLeft = AnimationUtils.loadAnimation(this,R.anim.translate_left)
        translateLeft.duration = 500
        translateRight.duration = 500

        translateUp = AnimationUtils.loadAnimation(this,R.anim.translate_up)
        translateDown = AnimationUtils.loadAnimation(this,R.anim.translate_down)
        translateUp.duration = 500
        translateDown.duration = 500
        val listener = object: Animation.AnimationListener{
            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationEnd(p0: Animation?) {
                if (isPageOpen) {
                    page.visibility = View.INVISIBLE

                    isPageOpen = false
                }
                else{

                    isPageOpen = true
                }
            }
            override fun onAnimationRepeat(p0: Animation?) {}
        }

        translateRight.setAnimationListener(listener)
        translateLeft.setAnimationListener(listener)
        translateUp.setAnimationListener(listener)
        translateDown.setAnimationListener(listener)
        val button = binding.transposeBackButton
        button.setOnClickListener {
            page.startAnimation(translateDown)
        }

        val floatButton = binding.floatButton
        floatButton.setOnClickListener{
            page.visibility = View.VISIBLE
            page.startAnimation(translateUp)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item?.itemId){
            R.id.transpose_icon -> {
                    page.visibility = View.VISIBLE
                    page.startAnimation(translateLeft)
                    true
                }
            else -> super.onOptionsItemSelected(item)
        }
    }
}