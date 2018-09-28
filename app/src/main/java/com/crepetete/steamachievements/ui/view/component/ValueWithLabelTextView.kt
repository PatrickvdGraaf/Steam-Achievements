package com.crepetete.steamachievements.ui.view.component

import android.content.Context
import androidx.annotation.ColorInt
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.crepetete.steamachievements.R
import kotlinx.android.synthetic.main.component_text_value.view.*

class ValueWithLabelTextView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyle, defStyleRes) {
    init {
        LayoutInflater.from(context)
                .inflate(R.layout.component_text_value, this, true)
        orientation = VERTICAL

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it,
                    R.styleable.ValueWithLabelTextView, 0, 0)
            val label = resources.getText(typedArray
                    .getResourceId(R.styleable.ValueWithLabelTextView_label,
                            R.string.label_placeholder))
            val value = resources.getText(typedArray
                    .getResourceId(R.styleable.ValueWithLabelTextView_value,
                            R.string.value_placeholder))

            setValueText(value.toString())
            setLabel(label.toString())

            typedArray.recycle()
        }
    }

    /**
     * Public access point for updating the value.
     */
    fun setText(text: String?) {
        setValueText(text)
    }

    private fun setValueText(text: String?) {
        if (text.isNullOrBlank() || text == context.getString(R.string.value_placeholder)) {
            visibility = View.GONE
        } else {
            textview_value.text = text
            visibility = View.VISIBLE
        }
    }

    /**
     * Public access point for whenever you want to programmatically change the label.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun setLabel(text: String?) {
        if (text.isNullOrBlank() || text == context.getString(R.string.label_placeholder)) {
            textview_label.visibility = View.GONE
        } else {
            textview_label.text = text
            textview_label.visibility = View.VISIBLE
        }
    }

    fun setTextColor(@ColorInt color: Int) {
        textview_value.setTextColor(color)
    }
}