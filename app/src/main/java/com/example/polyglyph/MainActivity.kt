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
import com.example.polyglyph.ui.theme.PolyGlyphTheme
import com.example.polyglyph.GlyphWrapper


class MainActivity : ComponentActivity() {
    private var glyph: GlyphWrapper = GlyphWrapper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glyph.init(applicationContext)

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
        glyph.unInit()
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
