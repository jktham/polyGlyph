package com.example.polyglyph

import android.content.ComponentName
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.polyglyph.ui.theme.PolyGlyphTheme
import com.nothing.ketchum.Common
import com.nothing.ketchum.Glyph
import com.nothing.ketchum.GlyphException
import com.nothing.ketchum.GlyphFrame
import com.nothing.ketchum.GlyphManager


class MainActivity : ComponentActivity() {
    private var mGM: GlyphManager? = null
    private var mCallback: GlyphManager.Callback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        mGM = GlyphManager.getInstance(applicationContext)
        mGM?.init(mCallback)
        //enableEdgeToEdge()
        setContent {
            PolyGlyphTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    UI()
                }
            }
        }
    }

    override fun onDestroy() {
        try {
            mGM?.closeSession()
        } catch (e: GlyphException) {
            Log.e(TAG, e.message!!)
        }
        mGM?.unInit()
        super.onDestroy()
    }

    private fun init() {
        mCallback = object : GlyphManager.Callback {
            override fun onServiceConnected(componentName: ComponentName) {
                if (Common.is20111()) mGM?.register(Common.DEVICE_20111)
                if (Common.is22111()) mGM?.register(Common.DEVICE_22111)
                if (Common.is23111()) mGM?.register(Common.DEVICE_23111)
                if (Common.is23113()) mGM?.register(Common.DEVICE_23113)
                try {
                    mGM?.openSession()
                } catch (e: GlyphException) {
                    Log.e(TAG, e.message!!)
                }
            }

            override fun onServiceDisconnected(componentName: ComponentName) {
                mGM?.closeSession()
            }
        }
    }


    @Composable
    private fun UI() {
        Column(modifier = Modifier.padding(24.dp, 48.dp)) {
            Button(onClick = {
                val builder: GlyphFrame.Builder? = mGM?.glyphFrameBuilder
                val frame = builder
                    ?.buildChannel(Glyph.Code_23111.C_1)
                    ?.buildChannel(Glyph.Code_23111.C_24)
                    ?.build()
                mGM?.toggle(frame)
                Log.i("test", "toggle")
            }) {
                Text("Test Glyph")
            }
            Button(onClick = {
                mGM?.turnOff()
            }) {
                Text("Reset")
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
