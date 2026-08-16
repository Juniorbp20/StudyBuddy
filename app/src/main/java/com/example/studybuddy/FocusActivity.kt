package com.example.studybuddy

import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.studybuddy.databinding.ActivityFocusBinding
import com.example.studybuddy.util.FocusSessionStore
import java.util.Locale

class FocusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFocusBinding

    private var isWorkMode = true
    private var timer: CountDownTimer? = null
    private var remainingMillis = WORK_DURATION_MS
    private var isRunning = false

    companion object {
        private const val WORK_DURATION_MS = 25 * 60 * 1000L
        private const val BREAK_DURATION_MS = 5 * 60 * 1000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFocusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        updateSessionsLabel()
        updateTimerDisplay()

        binding.buttonToggle.setOnClickListener {
            if (isRunning) pauseTimer() else startTimer()
        }
        binding.buttonReset.setOnClickListener { resetTimer() }
    }

    private fun startTimer() {
        isRunning = true
        binding.buttonToggle.text = "Pausar"
        timer = object : CountDownTimer(remainingMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingMillis = millisUntilFinished
                updateTimerDisplay()
            }

            override fun onFinish() {
                onTimerFinished()
            }
        }.start()
    }

    private fun pauseTimer() {
        isRunning = false
        binding.buttonToggle.text = "Continuar"
        timer?.cancel()
    }

    private fun resetTimer() {
        pauseTimer()
        remainingMillis = if (isWorkMode) WORK_DURATION_MS else BREAK_DURATION_MS
        updateTimerDisplay()
    }

    private fun onTimerFinished() {
        vibrate()
        if (isWorkMode) {
            FocusSessionStore.recordSession(this)
            updateSessionsLabel()
            Toast.makeText(this, "¡Sesión completada! Tómate un descanso", Toast.LENGTH_LONG).show()
            isWorkMode = false
            remainingMillis = BREAK_DURATION_MS
            binding.textMode.text = "Descanso"
        } else {
            Toast.makeText(this, "Descanso terminado. ¡A concentrarse!", Toast.LENGTH_LONG).show()
            isWorkMode = true
            remainingMillis = WORK_DURATION_MS
            binding.textMode.text = "Enfoque"
        }
        isRunning = false
        binding.buttonToggle.text = "Comenzar"
        updateTimerDisplay()
    }

    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibrator = getSystemService(VibratorManager::class.java).defaultVibrator
            vibrator.vibrate(VibrationEffect.createOneShot(800, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = getSystemService(Vibrator::class.java)
            @Suppress("DEPRECATION")
            vibrator.vibrate(800)
        }
    }

    private fun updateTimerDisplay() {
        val totalSeconds = remainingMillis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        binding.textTimer.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        binding.progressTimer.max = 100
        val total = if (isWorkMode) WORK_DURATION_MS else BREAK_DURATION_MS
        binding.progressTimer.progress = ((total - remainingMillis) * 100 / total).toInt()
    }

    private fun updateSessionsLabel() {
        binding.textSessions.text = "Sesiones hoy: ${FocusSessionStore.sessionsToday(this)} · " +
            "Total: ${FocusSessionStore.totalSessions(this)}"
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}