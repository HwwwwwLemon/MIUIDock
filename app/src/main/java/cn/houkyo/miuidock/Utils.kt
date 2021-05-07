package cn.houkyo.miuidock

import android.annotation.SuppressLint
import android.content.Context;
import android.content.res.Configuration
import android.util.TypedValue
import android.widget.Toast

class Utils {
    val DATA_FILE_NAME = "MIUIDockConfig"

    fun dip2px(context: Context, dpValue: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun px2dip(context: Context, pxValue: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    @SuppressLint("SetWorldReadable")
    fun saveData(context: Context, key: String, value: Int) {
        try {
            val sharedPreferences = context.getSharedPreferences(DATA_FILE_NAME, Context.MODE_WORLD_READABLE)
            val editor = sharedPreferences.edit()
            editor.putInt(key, value)
            editor.apply()
        } catch (e: Throwable) {
            // 也许是模块尚未加载
        }
    }

    fun getData(context: Context, key: String, defValue: Int): Int {
        try {
            val sharedPreferences = context.getSharedPreferences(DATA_FILE_NAME, Context.MODE_WORLD_READABLE)
            return sharedPreferences.getInt(key, defValue)
        } catch (e: Throwable) {
            // 也许是模块尚未加载
        }
        return defValue
    }

    fun isModuleEnable(): Boolean {
        return false
    }

    fun isDarkMode(context: Context): Boolean {
        val mode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES;
    }

    fun getColorPrimary(context: Context): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
        return typedValue.data
    }

    fun showToast(context: Context, content: String) {
        val mToast = Toast(context)
        mToast.setText(content)
        mToast.show()
    }

    fun showToast(context: Context, resId: Int) {
        val mToast = Toast(context)
        mToast.setText(resId)
        mToast.show()
    }
}