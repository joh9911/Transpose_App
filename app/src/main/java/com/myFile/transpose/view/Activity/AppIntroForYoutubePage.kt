package com.myFile.transpose.view.Activity

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import com.myFile.transpose.R
import com.myFile.transpose.utils.AppUsageSharedPreferences

class AppIntroForYoutubePage : AppIntro() {

    private lateinit var appUsageSharedPreferences: AppUsageSharedPreferences

    @SuppressLint("PrivateResource")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appUsageSharedPreferences = AppUsageSharedPreferences(this)
        // Make sure you don't call setContentView!

        // Call addSlide passing your Fragments.
        // You can use AppIntroFragment to use a pre-built fragment

        setColorDoneText(getColor(R.color.blue_background))
        setColorSkipButton(getColor(R.color.blue_background))
        setNextArrowColor(getColor(R.color.blue_background))
        setIndicatorColor(
            selectedIndicatorColor = getColor(R.color.blue_background),
            unselectedIndicatorColor = getColor(R.color.black)
        )
        addSlide(
            AppIntroFragment.createInstance(
            title = getString(R.string.app_usage_guide),
            description = getString(R.string.patch_description_1),
            imageDrawable = R.drawable.description_1,
            titleColorRes = R.color.black,
            descriptionColorRes = R.color.black,
            titleTypefaceFontRes = androidx.media3.ui.R.font.roboto_medium_numbers,
            backgroundColorRes = R.color.white,
        ))

        addSlide(
            AppIntroFragment.createInstance(
            title = getString(R.string.app_usage_guide),
            description = getString(R.string.patch_description_2),
            imageDrawable = R.drawable.description_2,
            titleColorRes = R.color.black,
            descriptionColorRes = R.color.black,
            titleTypefaceFontRes = androidx.media3.ui.R.font.roboto_medium_numbers,
            backgroundColorRes = R.color.white,
        ))

        addSlide(AppIntroFragment.createInstance(
            title = getString(R.string.app_usage_guide),
            description = getString(R.string.patch_description_4),
            imageDrawable = R.drawable.description_4,
            titleColorRes = R.color.black,
            descriptionColorRes = R.color.black,
            titleTypefaceFontRes = androidx.media3.ui.R.font.roboto_medium_numbers,
            backgroundColorRes = R.color.white,
        ))


    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        appUsageSharedPreferences.saveDoNotShowAgain(true)
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        appUsageSharedPreferences.saveDoNotShowAgain(true)

        finish()
    }
}