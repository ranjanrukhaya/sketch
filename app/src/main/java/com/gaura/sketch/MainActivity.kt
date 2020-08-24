package com.gaura.sketch

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dailog_brush_size.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawing_view.setSizeForBrush(20.toFloat())
        ib_brush.setOnClickListener { showBrushDialog() }
    }

    fun showBrushDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dailog_brush_size)
        dialog.setTitle("Brush size: ")
        dialog.ib_small_brush.setOnClickListener {
            drawing_view.setSizeForBrush(10.toFloat())
            dialog.dismiss()
        }
        dialog.ib_medium_brush.setOnClickListener {
            drawing_view.setSizeForBrush(20.toFloat())
            dialog.dismiss()
        }
        dialog.ib_large_brush.setOnClickListener {
            drawing_view.setSizeForBrush(30.toFloat())
            dialog.dismiss()
        }
        dialog.show()
    }
}