package com.google.ar.sceneform.samples.gltf.library

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.google.mlkit.vision.text.Text

class TextOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var recognizedText: Text? = null
    var highlightedText: String? = null
    private val wordRects = mutableListOf<Pair<String, RectF>>()

    private val textPaint = Paint().apply {
        color = Color.RED
        textSize = 36f
    }

    private val highlightPaint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.FILL
    }

    var onWordTapped: ((String) -> Unit)? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        wordRects.clear()
        recognizedText?.let { visionText ->
            for (block in visionText.textBlocks) {
                for (line in block.lines) {
                    for (element in line.elements) {
                        val boundingBox = element.boundingBox
                        if (boundingBox != null) {
                            val rect = RectF(boundingBox)
                            wordRects.add(Pair(element.text, rect))
                            if (element.text == highlightedText) {
                                canvas.drawRect(rect, highlightPaint)
                            }
                            canvas.drawText(element.text, rect.left, rect.bottom, textPaint)
                        }
                    }
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val x = event.x
            val y = event.y
            for ((word, rect) in wordRects) {
                if (rect.contains(x, y)) {
                    highlightedText = word
                    onWordTapped?.invoke(word)
                    invalidate()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }
}