package com.myFile.transpose

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class YoutubeDigitConverter {
    /** 현재시간 구하기 ["yyyy-MM-dd HH:mm:ss"] (*HH: 24시간)*/
    private fun getTime(): String {
        val now = System.currentTimeMillis()
        val date = Date(now)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return dateFormat.format(date)
    }
    /** 두 날짜 사이의 간격 계산해서 텍스트로 반환 */
    fun intervalBetweenDateText(beforeDate: String, dateArray: Array<String>): String {
        //현재 시간
        val nowFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(getTime())
        val beforeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(changeDateFormat(beforeDate))

        val diffSec     = (nowFormat.time - beforeFormat.time) / 1000                                           //초 차이
        val diffMin     = (nowFormat.time - beforeFormat.time) / (60*1000)                                      //분 차이
        val diffHor     = (nowFormat.time - beforeFormat.time) / (60 * 60 * 1000)                               //시 차이
        val diffDays    = diffSec / (24 * 60 * 60)                                                              //일 차이
        val diffMonths  = (nowFormat.year*12 + nowFormat.month) - (beforeFormat.year*12 + beforeFormat.month)   //월 차이
        val diffYears   = nowFormat.year - beforeFormat.year                                                    //연도 차이

        if(diffYears > 0){
            if (diffMonths >= 12)
                return String.format(dateArray[5],diffYears)
        }
        if(diffMonths > 0){
            return String.format(dateArray[4],diffMonths)
        }
        if (diffDays > 0){
            return String.format(dateArray[3],diffDays)
        }
        if(diffHor > 0){
            return String.format(dateArray[2],diffHor)
        }
        if(diffMin > 0){
            return String.format(dateArray[1],diffMin)
        }
        if(diffSec > 0){
            return String.format(dateArray[0],diffSec)
        }
        return ""
    }

    /** 날짜 형식변경 */
    private fun changeDateFormat(date: String): String{
        // ["yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"] -> ["yyyy-MM-dd HH:mm"]
        try{
            val oldFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'") // 받은 데이터 형식
            oldFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            val newFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss") // 바꿀 데이터 형식
            val oldDate = oldFormat.parse(date) //ex) "2016-11-01T15:25:31.000Z" // 000 - 밀리 세컨드
            return newFormat.format(oldDate)
        }catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
    fun viewCountCalculator(viewCountStringArray: Array<String>, viewCountString: String): String {
        val country = Locale.getDefault().language
        val viewCount = viewCountString.toLong()
        val df = DecimalFormat("#.#")
        var string = ""
        if (country == "ko"){
            if (viewCount < 1000)
                string = String.format(viewCountStringArray[0], viewCount.toString())
            else if (viewCount in 1000..9999){
                val convertedViewCount = df.format(viewCount/1000.0)
                string = String.format(viewCountStringArray[1], convertedViewCount)
            }
            else if (viewCount in 10000 .. 99999){
                val convertedViewCount = df.format(viewCount/10000.0)
                string = String.format(viewCountStringArray[2], convertedViewCount)
            }
            else if (viewCount in 100000.. 99999999){
                val convertedViewCount = (viewCount / 10000).toString()
                string = String.format(viewCountStringArray[2], convertedViewCount)
            }
            else{
                val convertedViewCount = df.format(viewCount/100000000.0)
                string = String.format(viewCountStringArray[4], convertedViewCount)
            }
        }
        else{
            if (viewCount < 1000)
                string = String.format(viewCountStringArray[0], viewCount)
            else if (viewCount in 1000..9999){
                val convertedViewCount = df.format(viewCount/1000.0)
                string = String.format(viewCountStringArray[1], convertedViewCount)
            }
            else if (viewCount in 10000..999999){
                val convertedViewCount = (viewCount/1000).toString()
                string = String.format(viewCountStringArray[1], convertedViewCount)
            }
            else if (viewCount in 1000000..9999999){
                val convertedViewCount = df.format(viewCount/1000000.0)
                string = String.format(viewCountStringArray[3], convertedViewCount)
            }
            else if (viewCount in 10000000..999999999){
                val convertedViewCount = (viewCount/1000000).toString()
                string = String.format(viewCountStringArray[3], convertedViewCount)
            }
            else{
                val convertedViewCount = df.format(viewCount/1000000000.0)
                string = String.format(viewCountStringArray[5], convertedViewCount)
            }
        }
        return string
    }

    fun subscriberCountConverter(subscriberCountString: String, subscriberArray: Array<String>): String {
        val country = Locale.getDefault().language
        val subscriberCount = subscriberCountString.toLong()
        val df = DecimalFormat("#.#")
        var string = ""
        if (country == "ko"){
            if (subscriberCount < 1000)
                string = String.format(subscriberArray[0], subscriberCount.toString())
            else if (subscriberCount in 1000..9999){
                val convertedViewCount = df.format(subscriberCount/1000.0)
                string = String.format(subscriberArray[1], convertedViewCount)
            }
            else if (subscriberCount in 10000 .. 99999){
                val convertedViewCount = df.format(subscriberCount/10000.0)
                string = String.format(subscriberArray[2], convertedViewCount)
            }
            else if (subscriberCount in 100000.. 99999999){
                val convertedViewCount = (subscriberCount / 10000).toString()
                string = String.format(subscriberArray[2], convertedViewCount)
            }
            else{
                val convertedViewCount = df.format(subscriberCount/100000000.0)
                string = String.format(subscriberArray[4], convertedViewCount)
            }
        }
        else{
            if (subscriberCount < 1000)
                string = String.format(subscriberArray[0], subscriberCount)
            else if (subscriberCount in 1000..9999){
                val convertedViewCount = df.format(subscriberCount/1000.0)
                string = String.format(subscriberArray[1], convertedViewCount)
            }
            else if (subscriberCount in 10000..999999){
                val convertedViewCount = (subscriberCount/1000).toString()
                string = String.format(subscriberArray[1], convertedViewCount)
            }
            else if (subscriberCount in 1000000..9999999){
                val convertedViewCount = df.format(subscriberCount/1000000.0)
                string = String.format(subscriberArray[3], convertedViewCount)
            }
            else if (subscriberCount in 10000000..999999999){
                val convertedViewCount = (subscriberCount/1000000).toString()
                string = String.format(subscriberArray[3], convertedViewCount)
            }
            else{
                val convertedViewCount = df.format(subscriberCount/1000000000.0)
                string = String.format(subscriberArray[5], convertedViewCount)
            }
        }
        return string
    }
}