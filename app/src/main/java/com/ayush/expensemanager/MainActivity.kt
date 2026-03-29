package com.ayush.expensemanager

import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.ayush.expensemanager.databinding.ActivityMainBinding
import com.ayush.expensemanager.utils.NotificationHelper
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NotificationHelper.createNotificationChannel(this)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        setupGestureDetector()
    }

    private fun setupGestureDetector() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            navigateTab(-1) // Swipe right -> Previous tab
                        } else {
                            navigateTab(1)  // Swipe left -> Next tab
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun navigateTab(direction: Int) {
        val menu = binding.bottomNavigation.menu
        val currentItemId = binding.bottomNavigation.selectedItemId
        var currentIndex = -1
        for (i in 0 until menu.size()) {
            if (menu.getItem(i).itemId == currentItemId) {
                currentIndex = i
                break
            }
        }

        if (currentIndex != -1) {
            val newIndex = currentIndex + direction
            if (newIndex in 0 until menu.size()) {
                val nextItemId = menu.getItem(newIndex).itemId
                binding.bottomNavigation.selectedItemId = nextItemId
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }
}
