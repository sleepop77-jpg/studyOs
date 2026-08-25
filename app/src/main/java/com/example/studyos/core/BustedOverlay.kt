package com.example.studyos.core

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.studyos.MainActivity

object BustedOverlay {
    private var view: View? = null

    fun canShow(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)

    @Suppress("DEPRECATION")
    fun show(context: Context, appName: String, penalty: Int): Boolean {
        if (view != null) return true
        if (!canShow(context)) return false
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.CENTER
        val overlay = build(context, appName, penalty)
        view = overlay
        return try {
            wm.addView(overlay, params)
            true
        } catch (_: Exception) {
            view = null
            false
        }
    }

    fun hide() {
        view?.let { v ->
            try {
                (v.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).removeView(v)
            } catch (_: Exception) { }
        }
        view = null
    }

    private fun build(context: Context, appName: String, penalty: Int): View {
        val dp = context.resources.displayMetrics.density
        fun p(v: Int) = (v * dp).toInt()
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#FF1A0505"))
            setPadding(p(28), p(28), p(28), p(28))
            isClickable = true
            isFocusable = true
        }
        val title = TextView(context).apply {
            text = "BUSTED."
            setTextColor(Color.parseColor("#FFFFD700"))
            textSize = 42f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        val msg = TextView(context).apply {
            text = "You opened $appName during Lockdown.\n+$penalty Shame and -10 Fame. The mascot is disappointed."
            setTextColor(Color.WHITE)
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(0, p(16), 0, p(32))
        }
        val btn = Button(context).apply {
            text = "RETURN TO STUDYOS"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#FFD9534F"))
            setPadding(p(16), p(14), p(16), p(14))
        }
        btn.setOnClickListener {
            hide()
            val i = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            try { context.startActivity(i) } catch (_: Exception) { }
        }
        val full = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        root.addView(title, full)
        root.addView(msg, full)
        root.addView(btn, full)
        return root
    }
}
