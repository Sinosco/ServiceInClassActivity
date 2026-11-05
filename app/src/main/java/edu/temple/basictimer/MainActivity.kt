package edu.temple.basictimer

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    val startButton : Button by lazy {
        findViewById(R.id.startButton)
    }

    val stopButton : Button by lazy {
        findViewById(R.id.stopButton)
    }

    val timerTextView : TextView by lazy {
        findViewById(R.id.textView)
    }

    var timerBinder: TimerService.TimerBinder? = null //can check and use as a gate, if have binder invoke if not then dont
                                                        // have binder when connected to service

    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            p0: ComponentName?,
            p1: IBinder?
        ) {
            timerBinder = p1 as TimerService.TimerBinder
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerBinder = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch { // with just this it might try to change a UI element that doesn't yet exist, need repeatOnLifecycle code
            repeatOnLifecycle(Lifecycle.State.RESUMED) { //activity in specified state or later the scoped code runs
                TimerInfo.currentTime.collect {
                    timerTextView.text = it.toString()
                }
            }
        }

        bindService(
            Intent(this, TimerService.TimerBinder::class.java),
            serviceConnection,
            BIND_AUTO_CREATE
        )

        startButton.setOnClickListener {
            timerBinder?.run{
                //start(100) //same as timerBinder?.start()
                if(!isRunning){
                    start(100)
                    startButton.text = "Pause"
                }
                else{
                    pause()
                    startButton.text = if (isPaused) "Resume" else "Pause"

                }
            }
        }
        
        stopButton.setOnClickListener {
            timerBinder?.stop()
            startButton.text = "Start"
        }
    }
}