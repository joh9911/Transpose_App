package com.myFile.transpose.fragment

interface PlayerServiceListener {
    fun onIsPlaying(type: Int)
    fun onStateEnded()
    fun playerViewInvisible()
    fun playerViewVisible()
}