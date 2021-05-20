package cn.houkyo.miuidock

import android.content.Context

object MySharePreferences {

    fun getData(ctx: Context?, key: String, any: Any): Any? {
        val myShare = ctx!!.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_WORLD_READABLE)
        val v = when (any) {
            is Int -> myShare.getInt(key, any)
            is Float -> myShare.getFloat(key, any)
            is String -> myShare.getString(key, any)
            is Boolean -> myShare.getBoolean(key, any)
            is Long -> myShare.getLong(key, any)
            else -> any
        }
        updateDefaultValue(key, any)
        return v
    }

    fun setData(ctx: Context?, key: String, any: Any) {
        val myShare = ctx!!.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_WORLD_READABLE)
        val editor = myShare.edit()
        when (any) {
            is Int -> editor.putInt(key, any)
            is Float -> editor.putFloat(key, any)
            is String -> editor.putString(key, any)
            is Boolean -> editor.putBoolean(key, any)
            is Long -> editor.putLong(key, any)
        }

        editor.apply()
        updateDefaultValue(key, any)
    }

    private fun updateDefaultValue(key: String, value: Any) {
        value as Int
        when (key) {
            "DOCK_RADIUS" -> DefaultValue.radius = value
            "DOCK_HEIGHT" -> DefaultValue.height = value
            "DOCK_SIDE" -> DefaultValue.sideMargin = value
            "DOCK_BOTTOM" -> DefaultValue.bottomMargin = value
            "DOCK_ICON_BOTTOM" -> DefaultValue.iconBottomMargin = value
            "HIGH_LEVEL" -> DefaultValue.highLevel = value
            "HIDE_ICON" -> DefaultValue.hideIcon = value
        }
    }

}