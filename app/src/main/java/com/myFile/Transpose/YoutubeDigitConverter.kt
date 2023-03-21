package com.myFile.Transpose

import android.content.Context
import android.provider.Settings.Global.getString
import android.util.Log
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class YoutubeDigitConverter(val context: Context) {
    /** 현재시간 구하기 ["yyyy-MM-dd HH:mm:ss"] (*HH: 24시간)*/
    fun getTime(): String {
        var now = System.currentTimeMillis()
        var date = Date(now)

        var dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var getTime = dateFormat.format(date)

        return getTime
    }
    /** 두 날짜 사이의 간격 계산해서 텍스트로 반환 */
    fun intervalBetweenDateText(beforeDate: String): String {
        //현재 시간
        val nowFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(getTime())
        val beforeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(changeDateFormat(beforeDate))
        Log.d("beforFormat","$beforeFormat")

        val diffSec     = (nowFormat.time - beforeFormat.time) / 1000                                           //초 차이
        val diffMin     = (nowFormat.time - beforeFormat.time) / (60*1000)                                      //분 차이
        val diffHor     = (nowFormat.time - beforeFormat.time) / (60 * 60 * 1000)                               //시 차이
        val diffDays    = diffSec / (24 * 60 * 60)                                                              //일 차이
        val diffMonths  = (nowFormat.year*12 + nowFormat.month) - (beforeFormat.year*12 + beforeFormat.month)   //월 차이
        val diffYears   = nowFormat.year - beforeFormat.year                                                    //연도 차이

        if(diffYears > 0){
            if (diffMonths >= 12)
                return String.format(context.getString(R.string.publish_date_year),diffYears)
        }
        if(diffMonths > 0){
            return String.format(context.getString(R.string.publish_date_month),diffMonths)
        }
        if (diffDays > 0){
            return String.format(context.getString(R.string.publish_date_day),diffDays)
        }
        if(diffHor > 0){
            return String.format(context.getString(R.string.publish_date_hour),diffHor)
        }
        if(diffMin > 0){
            return String.format(context.getString(R.string.publish_date_minute),diffMin)
        }
        if(diffSec > 0){
            return String.format(context.getString(R.string.publish_date_second),diffSec)
        }
        return ""
    }

    /** 날짜 형식변경 */
    fun changeDateFormat(date: String): String{
        // ["yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"] -> ["yyyy-MM-dd HH:mm"]
        try{
            val old_format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'") // 받은 데이터 형식
            old_format.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            val new_format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss") // 바꿀 데이터 형식
            val old_date = old_format.parse(date) //ex) "2016-11-01T15:25:31.000Z" // 000 - 밀리 세컨드
            return new_format.format(old_date)
        }catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
    fun viewCountCalculator(viewCountString: String): String {
        val country = Locale.getDefault().language
        val viewCount = viewCountString.toInt()
        val df = DecimalFormat("#.#")
        var string = ""
        if (country == "ko"){
            if (viewCount < 1000)
                string = String.format(context.resources.getString(R.string.view_count_under_thousand), viewCount.toString())
            else if (viewCount in 1000..9999){
                val convertedViewCount = df.format(viewCount/1000.0)
                string = String.format(context.resources.getString(R.string.view_count_over_thousand), convertedViewCount)
            }
            else if (viewCount in 10000 .. 99999){
                val convertedViewCount = df.format(viewCount/10000.0)
                string = String.format(context.resources.getString(R.string.view_count_over_hundred_thousand), convertedViewCount)
            }
            else if (viewCount in 100000.. 99999999){
                val convertedViewCount = (viewCount / 10000).toString()
                string = String.format(context.resources.getString(R.string.view_count_over_hundred_thousand), convertedViewCount)
            }
            else{
                val convertedViewCount = df.format(viewCount/100000000.0)
                string = String.format(context.resources.getString(R.string.view_count_over_hundred_million), convertedViewCount)
            }
        }
        else{
            if (viewCount < 1000)
                string = String.format(context.resources.getString(R.string.view_count_under_thousand), viewCount)
            else if (viewCount in 1000..9999){
                val convertedViewCount = df.format(viewCount/1000.0)
                string = String.format(context.resources.getString(R.string.view_count_over_thousand), convertedViewCount)
            }
            else if (viewCount in 10000..999999){
                val convertedViewCount = (viewCount/1000).toString()
                string = String.format(context.resources.getString(R.string.view_count_over_thousand), convertedViewCount)
            }
            else if (viewCount in 1000000..9999999){
                val convertedViewCount = df.format(viewCount/1000000.0)
                string = String.format(context.resources.getString(R.string.view_count_over_million), convertedViewCount)
            }
            else if (viewCount in 10000000..999999999){
                val convertedViewCount = (viewCount/1000000).toString()
                string = String.format(context.resources.getString(R.string.view_count_over_million), convertedViewCount)
            }
            else{
                val convertedViewCount = df.format(viewCount/1000000000.0)
                string = String.format(context.resources.getString(R.string.view_count_over_billion), convertedViewCount)
            }
        }
        return string
    }

    fun subscriberCountConverter(subscriberCountString: String): String {
        val country = Locale.getDefault().language
        val subscriberCount = subscriberCountString.toInt()
        val df = DecimalFormat("#.#")
        var string = ""
        if (country == "ko"){
            if (subscriberCount < 1000)
                string = String.format(context.resources.getString(R.string.subscriber_count_under_thousand), subscriberCount.toString())
            else if (subscriberCount in 1000..9999){
                val convertedViewCount = df.format(subscriberCount/1000.0)
                string = String.format(context.resources.getString(R.string.subscriber_count_over_thousand), convertedViewCount)
            }
            else if (subscriberCount in 10000 .. 99999){
                val convertedViewCount = df.format(subscriberCount/10000.0)
                string = String.format(context.resources.getString(R.string.subscriber_count_over_hundred_thousand), convertedViewCount)
            }
            else if (subscriberCount in 100000.. 99999999){
                val convertedViewCount = (subscriberCount / 10000).toString()
                string = String.format(context.resources.getString(R.string.subscriber_count_over_hundred_thousand), convertedViewCount)
            }
            else{
                val convertedViewCount = df.format(subscriberCount/100000000.0)
                string = String.format(context.resources.getString(R.string.view_count_over_hundred_million), convertedViewCount)
            }
        }
        else{
            if (subscriberCount < 1000)
                string = String.format(context.resources.getString(R.string.subscriber_count_under_thousand), subscriberCount)
            else if (subscriberCount in 1000..9999){
                val convertedViewCount = df.format(subscriberCount/1000.0)
                string = String.format(context.resources.getString(R.string.subscriber_count_over_thousand), convertedViewCount)
            }
            else if (subscriberCount in 10000..999999){
                val convertedViewCount = (subscriberCount/1000).toString()
                string = String.format(context.resources.getString(R.string.subscriber_count_over_thousand), convertedViewCount)
            }
            else if (subscriberCount in 1000000..9999999){
                val convertedViewCount = df.format(subscriberCount/1000000.0)
                string = String.format(context.resources.getString(R.string.subscriber_count_over_million), convertedViewCount)
            }
            else if (subscriberCount in 10000000..999999999){
                val convertedViewCount = (subscriberCount/1000000).toString()
                string = String.format(context.resources.getString(R.string.subscriber_count_over_million), convertedViewCount)
            }
            else{
                val convertedViewCount = df.format(subscriberCount/1000000000.0)
                string = String.format(context.resources.getString(R.string.view_count_over_billion), convertedViewCount)
            }
        }
        return string
    }
}