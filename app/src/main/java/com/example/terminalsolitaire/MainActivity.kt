package com.example.terminalsolitaire

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var menuLayout: LinearLayout
    private lateinit var gameView: GameView
    private lateinit var suitsTextView: TextView
    private lateinit var titleTextView: TextView
    private lateinit var resumeOption: TextView
    private lateinit var startOption: TextView
    private lateinit var darkModeOption: TextView
    private lateinit var creditTextView: TextView

    private var isDarkMode = true

    // True once a game has been started (or restored) this install, so the
    // "RESUME GAME" menu option knows whether to show itself.
    private var hasActiveGame = false

    companion object {
        private const val PREFS_NAME = "solitaire_prefs"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_HAS_ACTIVE_GAME = "has_active_game"
        private const val KEY_ON_GAME_SCREEN = "on_game_screen"
        private const val KEY_GAME_STATE = "game_state"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val rootContainer = FrameLayout(this)

        // 1. Initialize GameView dynamically
        gameView = GameView(this).apply {
            visibility = View.GONE
        }

        // 2. Programmatically build Menu Layout
        menuLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(64, 64, 64, 320)
        }

        suitsTextView = TextView(this).apply {
            // U+FE0E forces "text presentation" rather than colored emoji glyphs,
            // so setTextColor() below actually applies to these symbols.
            text = "♠\uFE0E ♥\uFE0E ♦\uFE0E ♣\uFE0E"
            textSize = 18f
            typeface = Typeface.MONOSPACE
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }

        titleTextView = TextView(this).apply {
            text = "=== TERMINAL SOLITAIRE ==="
            textSize = 22f
            typeface = Typeface.MONOSPACE
            gravity = Gravity.CENTER
        }

        resumeOption = TextView(this).apply {
            textSize = 18f
            typeface = Typeface.MONOSPACE
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 16)
            visibility = View.GONE
            setOnClickListener {
                menuLayout.visibility = View.GONE
                creditTextView.visibility = View.GONE
                gameView.isDarkMode = isDarkMode
                gameView.visibility = View.VISIBLE
                gameView.invalidate()
            }
        }

        startOption = TextView(this).apply {
            textSize = 18f
            typeface = Typeface.MONOSPACE
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 16)
            setOnClickListener {
                menuLayout.visibility = View.GONE
                creditTextView.visibility = View.GONE
                gameView.isDarkMode = isDarkMode
                gameView.engine.startNewGame()
                gameView.visibility = View.VISIBLE
                gameView.invalidate()
                hasActiveGame = true
            }
        }

        darkModeOption = TextView(this).apply {
            textSize = 18f
            typeface = Typeface.MONOSPACE
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 16)
            setOnClickListener {
                isDarkMode = !isDarkMode
                applyTheme()
            }
        }

        menuLayout.addView(suitsTextView)
        menuLayout.addView(titleTextView)
        menuLayout.addView(resumeOption)
        menuLayout.addView(startOption)
        menuLayout.addView(darkModeOption)

        // Small credit line, anchored to the bottom of the screen
        creditTextView = TextView(this).apply {
            text = "A port of Brian Strauch's Solitaire-TUI"
            textSize = 12f
            typeface = Typeface.MONOSPACE
            gravity = Gravity.CENTER
        }

        rootContainer.addView(gameView)
        rootContainer.addView(menuLayout)
        rootContainer.addView(
            creditTextView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            ).apply {
                bottomMargin = 48
            }
        )

        setContentView(rootContainer)

        // Pressing the OS back button (or swiping back) while in a game
        // returns to the menu instead of quitting the app outright.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (gameView.visibility == View.VISIBLE) {
                    gameView.visibility = View.GONE
                    menuLayout.visibility = View.VISIBLE
                    creditTextView.visibility = View.VISIBLE
                    refreshMenuLabels()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        // Restore any in-progress game so backgrounding/reopening the app
        // (or the process being killed while backgrounded) doesn't lose it.
        restoreState()
    }

    override fun onPause() {
        super.onPause()
        saveState()
    }

    private fun saveState() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean(KEY_DARK_MODE, isDarkMode)
        editor.putBoolean(KEY_HAS_ACTIVE_GAME, hasActiveGame)
        editor.putBoolean(KEY_ON_GAME_SCREEN, gameView.visibility == View.VISIBLE)

        if (hasActiveGame) {
            editor.putString(KEY_GAME_STATE, gameView.engine.serialize())
        }
        editor.apply()
    }

    private fun restoreState() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        isDarkMode = prefs.getBoolean(KEY_DARK_MODE, true)
        hasActiveGame = prefs.getBoolean(KEY_HAS_ACTIVE_GAME, false)

        val onGameScreen = prefs.getBoolean(KEY_ON_GAME_SCREEN, false)
        val savedGameState = prefs.getString(KEY_GAME_STATE, null)

        if (hasActiveGame && savedGameState != null && gameView.engine.deserialize(savedGameState)) {
            gameView.isDarkMode = isDarkMode
            if (onGameScreen) {
                menuLayout.visibility = View.GONE
                creditTextView.visibility = View.GONE
                gameView.visibility = View.VISIBLE
            }
        } else {
            hasActiveGame = false
        }

        applyTheme()
    }

    private fun refreshMenuLabels() {
        var n = 1
        if (hasActiveGame) {
            resumeOption.visibility = View.VISIBLE
            resumeOption.text = "$n. RESUME GAME"
            resumeOption.setPadding(0, 64, 0, 16)
            startOption.setPadding(0, 16, 0, 16)
            n++
        } else {
            resumeOption.visibility = View.GONE
            startOption.setPadding(0, 64, 0, 16)
        }

        startOption.text = "$n. START NEW GAME"
        n++

        val checkMark = if (isDarkMode) "[X]" else "[ ]"
        darkModeOption.text = "$n. DARK MODE  $checkMark"
    }

    private fun applyTheme() {
        val bgColor = if (isDarkMode) Color.BLACK else Color.WHITE
        val fgColor = if (isDarkMode) Color.WHITE else Color.BLACK

        menuLayout.setBackgroundColor(bgColor)
        titleTextView.setTextColor(fgColor)
        resumeOption.setTextColor(fgColor)
        startOption.setTextColor(fgColor)
        darkModeOption.setTextColor(fgColor)
        creditTextView.setTextColor(fgColor)
        suitsTextView.setTextColor(fgColor)

        refreshMenuLabels()
    }
}
