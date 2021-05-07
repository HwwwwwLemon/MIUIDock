package cn.houkyo.miuidock.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import cn.houkyo.miuidock.R
import com.jem.rubberpicker.RubberSeekBar

class CustomSeekBar : LinearLayout {
    private var titleTextView: TextView? = null
    private var mainSeekBar: RubberSeekBar? = null
    private var minTextView: TextView? = null
    private var valueTextView: TextView? = null
    private var maxTextView: TextView? = null
    private lateinit var onValueChangeListener: (value: Int) -> Unit

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }


    private fun init(context: Context) {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_seek_bar, this)
        titleTextView = view.findViewById(R.id.titleTextView)
        mainSeekBar = view.findViewById(R.id.mainSeekBar)
        minTextView = view.findViewById(R.id.minTextView)
        valueTextView = view.findViewById(R.id.valueTextView)
        maxTextView = view.findViewById(R.id.maxTextView)
        fixScrollViewConflict()
        onValueChangeListener = { value: Int ->
            value
        }
    }

    fun setValue(i: Int) {
        mainSeekBar?.setCurrentValue(i)
        valueTextView?.text = i.toString()
        mainSeekBar?.setOnRubberSeekBarChangeListener(object : RubberSeekBar.OnRubberSeekBarChangeListener {
            override fun onProgressChanged(seekBar: RubberSeekBar, value: Int, fromUser: Boolean) {
                valueTextView?.text = value.toString()
            }
            override fun onStartTrackingTouch(seekBar: RubberSeekBar) {

            }
            override fun onStopTrackingTouch(seekBar: RubberSeekBar) {
                onValueChangeListener.invoke(seekBar.getCurrentValue())
            }

        })

    }


    fun setMinValue(i: Int) {
        mainSeekBar?.setMin(i)
        minTextView?.text = i.toString()
    }

    fun setMaxValue(i: Int) {
        mainSeekBar?.setMax(i)
        maxTextView?.text = i.toString()
    }

    fun getValue(): Int {
        if (mainSeekBar != null) {
            return mainSeekBar!!.getCurrentValue()
        }
        return 0
    }

    fun setTitle(str: String) {

        titleTextView?.text = str
    }

    fun setOnValueChangeListener(callback: (value: Int) -> Unit) {
        this.onValueChangeListener = callback
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun fixScrollViewConflict() {
        mainSeekBar?.setOnTouchListener { _, _ ->
            parent.parent.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
    }
}