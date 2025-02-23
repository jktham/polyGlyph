package com.example.polyglyph

import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateListOf
import com.nothing.ketchum.Common
import com.nothing.ketchum.Glyph
import com.nothing.ketchum.GlyphException
import com.nothing.ketchum.GlyphFrame
import com.nothing.ketchum.GlyphManager


class GlyphWrapper() {
    public var glyphManager: GlyphManager? = null
    public var glyphCallback: GlyphManager.Callback? = null

    public var state = mutableStateListOf<Boolean>(*(Array<Boolean>(26){false}))
    
    public fun init(applicationContext: Context) {
        glyphCallback = object : GlyphManager.Callback {
            override fun onServiceConnected(componentName: ComponentName) {
                if (Common.is20111()) glyphManager?.register(Common.DEVICE_20111)
                if (Common.is22111()) glyphManager?.register(Common.DEVICE_22111)
                if (Common.is23111()) glyphManager?.register(Common.DEVICE_23111)
                if (Common.is23113()) glyphManager?.register(Common.DEVICE_23113)
                try {
                    glyphManager?.openSession()
                } catch (e: GlyphException) {
                    Log.e(TAG, e.message!!)
                }
            }

            override fun onServiceDisconnected(componentName: ComponentName) {
                glyphManager?.closeSession()
            }
        }

        glyphManager = GlyphManager.getInstance(applicationContext)
        glyphManager?.init(glyphCallback)
    }

    public fun unInit() {
        try {
            glyphManager?.closeSession()
        } catch (e: GlyphException) {
            Log.e(TAG, e.message!!)
        }
        glyphManager?.unInit()
    }

    public fun enable(index: Int) {
        state[index] = true
        update()
    }
    
    public fun disable(index: Int) {
        state[index] = false
        update()
    }

    public fun toggle(index: Int) {
        state[index] = !state[index]
        update()
    }

    public fun reset() {
        for (i in 0 until state.size) {
            state[i] = false
        }
        update()
    }

    public fun update() {
        glyphManager?.turnOff()
        var builder: GlyphFrame.Builder? = glyphManager?.glyphFrameBuilder
        for (i in 0 until state.size) {
            if (state[i] == true) {
                builder = builder?.buildChannel(i)
            }
        }
        val frame = builder?.build()
        glyphManager?.toggle(frame)
    }
}
