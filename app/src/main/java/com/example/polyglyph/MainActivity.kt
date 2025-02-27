package com.example.polyglyph

import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.os.BatteryManager
import com.example.polyglyph.ui.theme.PolyGlyphTheme
import com.example.polyglyph.GlyphWrapper
import android.content.Intent
import android.content.BroadcastReceiver
import android.os.IBinder
import android.content.IntentFilter
import android.app.Service
import androidx.core.app.ServiceCompat
import androidx.core.app.NotificationCompat
import android.content.pm.ServiceInfo
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager

class MainActivity : ComponentActivity() {
    private var glyph: GlyphWrapper = GlyphWrapper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.startForegroundService(Intent(this, GlyphService::class.java))

        glyph.init(this)

        //enableEdgeToEdge()
        setContent {
            PolyGlyphTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    Column(modifier = Modifier.padding(paddingValues)) {
                        UI()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        // glyph.unInit()
        super.onDestroy()
    }

    @Composable
    private fun UI() {
        Row(
            modifier = Modifier
            .padding(24.dp, 0.dp)
        ) {
            Column(
                modifier = Modifier
                .verticalScroll(rememberScrollState())
            ) {
                for (i in 0 until glyph.state.size) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("glyph " + i)
                        Checkbox(
                            checked = glyph.state[i],
                            onCheckedChange = {
                                glyph.toggle(i)
                            }
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                .verticalScroll(rememberScrollState())
            ) {
                Button(onClick = {
                    glyph.reset()
                }) {
                    Text("Reset")
                }
                Button(onClick = {
                    for (i in 0 until 26) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            glyph.toggle(i)
                        }, (i.toLong() * 30))
                    }
                }) {
                    Text("Animate")
                }
                Button(onClick = {
                    val bm: BatteryManager = applicationContext.getSystemService(BATTERY_SERVICE) as BatteryManager
                    val bat: Int = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                    val p: Float = bat / 100.0f
                    val n: Int = (p * 24).toInt()

                    Log.d("bat", p.toString())

                    glyph.reset()
                    for (i in 0 until n) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            glyph.enable(i)
                        }, (i.toLong() * 20))
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        glyph.disable(n-1)
                    }, (n.toLong() * 20 + 500))
                    Handler(Looper.getMainLooper()).postDelayed({
                        glyph.enable(n-1)
                    }, (n.toLong() * 20 + 1000))
                    Handler(Looper.getMainLooper()).postDelayed({
                        glyph.disable(n-1)
                    }, (n.toLong() * 20 + 1500))
                    Handler(Looper.getMainLooper()).postDelayed({
                        glyph.enable(n-1)
                    }, (n.toLong() * 20 + 2000))
                    Handler(Looper.getMainLooper()).postDelayed({
                        glyph.reset()
                    }, (n.toLong() * 20 + 2500))
                }) {
                    Text("Battery")
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    private fun Preview() {
        PolyGlyphTheme {
            UI()
        }
    }
}

class GlyphService : Service() {
    private var glyph: GlyphWrapper = GlyphWrapper()

    private var receiver: BroadcastReceiver = 
        object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("GlyphService", intent.action ?: "null")

            when {
                intent.action == Intent.ACTION_POWER_CONNECTED ->
                    powerConnected()
                intent.action == Intent.ACTION_POWER_DISCONNECTED ->
                    powerDisconnected()
            }
        }
    }

    override fun onCreate() {
        Log.d("GlyphService", "created")

        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_POWER_CONNECTED)
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        registerReceiver(receiver, filter)
        glyph.init(this)
    }

    override fun onStartCommand(resultIntent: Intent, resultCode: Int, startId: Int): Int {
        Log.d("GlyphService", "started")

        val bm: BatteryManager = applicationContext.getSystemService(BATTERY_SERVICE) as BatteryManager
        val bat: Int = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val p: Float = bat / 100.0f
        val n: Int = (p * 24).toInt()

        val name = "myChannel"
        val descriptionText = "myChannelDesc"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel("myChannelId", name, importance)
        mChannel.description = descriptionText
        
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)

        val notification = NotificationCompat.Builder(this, "myChannelId")
            .setSmallIcon(R.drawable.star_on)
            .build()
        
        ServiceCompat.startForeground(this, 100, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)

        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("GlyphService", "destroyed")

        unregisterReceiver(receiver)
        glyph.unInit()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun powerConnected() {
        Log.d("GlyphService", "connected")

        val bm: BatteryManager = applicationContext.getSystemService(BATTERY_SERVICE) as BatteryManager
        val bat: Int = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val p: Float = bat / 100.0f
        val n: Int = (p * 24).toInt()

        Log.d("GlyphService", p.toString())

        glyph.reset()
        for (i in 0 until n) {
            Handler(Looper.getMainLooper()).postDelayed({
                glyph.enable(i)
            }, (i.toLong() * 20))
        }
        Handler(Looper.getMainLooper()).postDelayed({
            glyph.disable(n-1)
        }, (n.toLong() * 20 + 500))
        Handler(Looper.getMainLooper()).postDelayed({
            glyph.enable(n-1)
        }, (n.toLong() * 20 + 1000))
        Handler(Looper.getMainLooper()).postDelayed({
            glyph.disable(n-1)
        }, (n.toLong() * 20 + 1500))
        Handler(Looper.getMainLooper()).postDelayed({
            glyph.enable(n-1)
        }, (n.toLong() * 20 + 2000))
        Handler(Looper.getMainLooper()).postDelayed({
            glyph.reset()
        }, (n.toLong() * 20 + 2500))
    }

    private fun powerDisconnected() {
        Log.d("GlyphService", "disconnected")
    }
}