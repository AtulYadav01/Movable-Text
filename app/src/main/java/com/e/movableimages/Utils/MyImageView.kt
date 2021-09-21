package com.e.movableimages.Utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import java.util.*

class MyImageView : AppCompatImageView {
    private var mHeight = 0
    private var mWidth = 0
    private var mRotate = 0f

    constructor(context: Context?) : super(context!!) {
        initRotate()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        initRotate()
    }

    private fun initRotate() {
        mRotate = (Random().nextFloat() - 0.5f) * 30
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mWidth = bottom - top
        mHeight = right - left
    }

    override fun onDraw(canvas: Canvas) {
        val borderWidth = 2
        canvas.save()
        canvas.rotate(mRotate, (mWidth / 2).toFloat(), (mHeight / 2).toFloat())
        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = -0x1
        canvas.drawRect(
            (paddingLeft - borderWidth).toFloat(),
            (paddingTop - borderWidth).toFloat(),
            (mWidth - (paddingRight - borderWidth)).toFloat(),
            (mHeight - (paddingBottom - borderWidth)).toFloat(),
            paint
        )
        super.onDraw(canvas)
        canvas.restore()
    }
}