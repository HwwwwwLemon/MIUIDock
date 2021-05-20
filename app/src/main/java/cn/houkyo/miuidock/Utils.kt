package cn.houkyo.miuidock


import android.content.Context;
import android.content.res.Configuration
import android.util.TypedValue
import android.widget.Toast
import java.io.DataOutputStream


object Utils {

    fun dip2px(context: Context, dpValue: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun px2dip(context: Context, pxValue: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }


    fun killMIUIHomeProcess(): Int {
        val suProcess = Runtime.getRuntime().exec("su")
        val os = DataOutputStream(suProcess.outputStream)
        os.writeBytes("am force-stop com.miui.home;exit;")
        os.flush()
        os.close()
        return suProcess.waitFor()
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