package com.example.marketgreenapp.constant

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair

object TransitionHelper {

    fun navigateWithTransition(
        currentActivity: Activity,
        targetActivityClass: Class <out Activity>,
        transitionView: View,
        transitionName: String,
        enterAnimation: Int,
        exitAnimation: Int
    )
    {
        val intent = Intent(currentActivity,targetActivityClass)
        val pairs:Array<Pair<View,String>> = arrayOf(
                Pair(transitionView,transitionName)
        )
        val option = ActivityOptionsCompat.makeSceneTransitionAnimation(
            currentActivity,
            *pairs
        )

        option.update(
            ActivityOptionsCompat.makeCustomAnimation(
                currentActivity,
                enterAnimation,
                exitAnimation
            )
        )
        currentActivity.startActivity(intent,option.toBundle())
    }
    fun navigateWithTransition(
        currentActivity: Activity,
        targetActivityClass: Class<out Activity>,
        transitionView: View,
        transitionName: String,
        enterAnimation: Int,
        exitAnimation: Int,
        bundle: Bundle? = null // chứa dữ liệu gửi đi
    ) {
        val intent = Intent(currentActivity, targetActivityClass)
        if (bundle != null) {
            intent.putExtras(bundle)
        }

        val pairs: Array<Pair<View, String>> = arrayOf(
            Pair(transitionView, transitionName)
        )

        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            currentActivity,
            *pairs
        )

        options.update(
            ActivityOptionsCompat.makeCustomAnimation(
                currentActivity,
                enterAnimation,
                exitAnimation
            )
        )
        currentActivity.startActivity(intent, options.toBundle())
    }
    fun navigateWithTransition(
        activityResultLauncher: ActivityResultLauncher<Intent>,
        currentActivity: Activity,
        targetActivityClass: Class<out Activity>,
        transitionView: View,
        transitionName: String,
        enterAnimation: Int,
        exitAnimation: Int
    ) {
        val intent = Intent(currentActivity, targetActivityClass)
        val pairs: Array<Pair<View, String>> = arrayOf(
             Pair(transitionView, transitionName)
        )

        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            currentActivity,
            *pairs
        )

        options.update(
            ActivityOptionsCompat.makeCustomAnimation(
                currentActivity,
                enterAnimation,
                exitAnimation
            )
        )

        activityResultLauncher.launch(intent,options)
    }
}