package cn.houkyo.miuidock

import android.content.Context
import android.content.res.Resources
import android.content.res.XResources
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import de.robv.android.xposed.callbacks.XC_LayoutInflated
import de.robv.android.xposed.callbacks.XC_LoadPackage

class MainHook : IXposedHookLoadPackage, IXposedHookInitPackageResources {
    companion object {
        const val SELF_PACKAGE_NAME = BuildConfig.APPLICATION_ID
        const val MIUI_HOME_LAUNCHER_PACKAGE_NAME = "com.miui.home"
        const val DEVICE_CONFIG_CLASSNAME = "$MIUI_HOME_LAUNCHER_PACKAGE_NAME.launcher.DeviceConfig"
        const val LAUNCHER_CLASSNAME = "$MIUI_HOME_LAUNCHER_PACKAGE_NAME.launcher.Launcher"
        const val BLUR_UTILS_CLASSNAME = "$MIUI_HOME_LAUNCHER_PACKAGE_NAME.launcher.common.BlurUtils"
        const val SEARCH_BAR_BLUR = "$MIUI_HOME_LAUNCHER_PACKAGE_NAME.launcher.SearchBarStyleData"
        const val DEVICE_LEVEL_UTILS_CLASSNAME =
            "$MIUI_HOME_LAUNCHER_PACKAGE_NAME.launcher.common.DeviceLevelUtils"
        const val CPU_LEVEL_UTILS_CLASSNAME =
            "$MIUI_HOME_LAUNCHER_PACKAGE_NAME.launcher.common.CpuLevelUtils"
        const val UTILITIES_CLASSNAME = "$MIUI_HOME_LAUNCHER_PACKAGE_NAME.launcher.common.Utilities"

        // 单位dip
        val DOCK_RADIUS = DefaultValue.radius
        val DOCK_HEIGHT = DefaultValue.height
        val DOCK_SIDE = DefaultValue.sideMargin
        val DOCK_BOTTOM = DefaultValue.bottomMargin
        val HIGH_LEVEL = DefaultValue.highLevel
        val DOCK_ICON_BOTTOM = DefaultValue.iconBottomMargin

        // 需要修改圆角的资源
        val drawableNameList = arrayOf(
            "bg_search_bar_white85_black5",
            "bg_search_bar_black20_white10",
            "bg_search_bar_black8_white11",
            "bg_search_bar_d9_15_non",
            "bg_search_bar_e3_25_non",
            "bg_search_bar_button_dark",
            "bg_search_bar_button_light",
            "bg_search_bar_dark",
            "bg_search_bar_light"
        )
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        when (lpparam.packageName) {
            SELF_PACKAGE_NAME -> {
                XposedHelpers.findAndHookMethod("${SELF_PACKAGE_NAME}.Utils",
                    lpparam.classLoader, "isModuleEnable", object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            param.result = true
                        }
                    })
            }
            MIUI_HOME_LAUNCHER_PACKAGE_NAME -> {
                launcherHook(lpparam)
                deviceConfigHook(lpparam)
                if (getData("HIGH_LEVEL", HIGH_LEVEL) == 1) {
                    deviceLevelHook(lpparam)
                }
            }

            else -> {
                return
            }
        }
    }

    override fun handleInitPackageResources(resparam: XC_InitPackageResources.InitPackageResourcesParam) {
        if (resparam.packageName != MIUI_HOME_LAUNCHER_PACKAGE_NAME) {
            return
        }

        resparam.res.hookLayout(
            MIUI_HOME_LAUNCHER_PACKAGE_NAME,
            "layout",
            "layout_search_bar",
            object : XC_LayoutInflated() {
                override fun handleLayoutInflated(liparam: LayoutInflatedParam) {
                    // 替换资源圆角
                    val targetView = liparam.view
                    drawableNameList.forEach { drawableName ->
                        resetDockRadius(
                            resparam.res,
                            targetView.context,
                            drawableName
                        )
                    }
                }
            })
    }

    private fun launcherHook(lpparam: XC_LoadPackage.LoadPackageParam) {
        val _LAUNCHER_CLASS = XposedHelpers.findClassIfExists(
            LAUNCHER_CLASSNAME,
            lpparam.classLoader
        ) ?: return
        try {
            XposedHelpers.findAndHookMethod(
                _LAUNCHER_CLASS,
                "onCreate",
                Bundle::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        super.afterHookedMethod(param)
                        val searchBarObject = XposedHelpers.callMethod(
                            param.thisObject,
                            "getSearchBar"
                        ) as FrameLayout
                        val searchBarDesktop = searchBarObject.getChildAt(0) as RelativeLayout
                        val searchBarDrawer = searchBarObject.getChildAt(1) as RelativeLayout
                        val searchBarContainer = searchBarObject.parent as FrameLayout
                        val searchEdgeLayout = searchBarContainer.parent as FrameLayout
                        // 重新给Searchbar容器排序
                        searchEdgeLayout.removeView(searchBarContainer)
                        searchEdgeLayout.addView(searchBarContainer, 0)
                        // 清空搜索图标和小爱同学
                        searchBarDesktop.removeAllViews()
                        // 修改高度
                        searchBarObject.layoutParams.height = Utils.dip2px(
                            searchBarDesktop.context,
                            getData("DOCK_HEIGHT", DOCK_HEIGHT)
                        )

                        // 修改应用列表搜索框
                        val mAllAppViewField = _LAUNCHER_CLASS.getDeclaredField("mAppsView")
                        mAllAppViewField.isAccessible = true
                        val mAllAppView = mAllAppViewField.get(param.thisObject) as RelativeLayout
                        val mAllAppSearchView =
                            mAllAppView.getChildAt(mAllAppView.childCount - 1) as FrameLayout
                        searchBarObject.removeView(searchBarDrawer)
                        mAllAppSearchView.addView(searchBarDrawer)
                        searchBarDrawer.bringToFront()
                        val layoutParams = searchBarDrawer.layoutParams as FrameLayout.LayoutParams
                        searchBarDrawer.layoutParams.height = Utils.dip2px(
                            searchBarDesktop.context,
                            45
                        )
                        layoutParams.leftMargin = Utils.dip2px(searchBarDesktop.context, 15)
                        layoutParams.rightMargin = Utils.dip2px(searchBarDesktop.context, 15)
                        searchBarDrawer.layoutParams = layoutParams
                    }
                })
        } catch (e: Throwable) {
            XposedBridge.log("[MIUIDock] LauncherHook Error:" + e.message)
        }
    }

    private fun deviceConfigHook(lpparam: XC_LoadPackage.LoadPackageParam) {
        val _DEVICE_CONFIG_CLASS = XposedHelpers.findClassIfExists(
            DEVICE_CONFIG_CLASSNAME,
            lpparam.classLoader
        ) ?: return
        try {
            // 图标区域顶部边距
            XposedHelpers.findAndHookMethod(
                _DEVICE_CONFIG_CLASS,
                "calcHotSeatsMarginTop",
                Context::class.java,
                Boolean::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        param.args[1] = false
                        super.beforeHookedMethod(param)
                    }
                })
            // 图标区域底部边距
            XposedHelpers.findAndHookMethod(
                _DEVICE_CONFIG_CLASS,
                "calcHotSeatsMarginBottom",
                Context::class.java,
                Boolean::class.java,
                Boolean::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        super.beforeHookedMethod(param)
                        val _context = param.args[0] as Context
                        param.result = Utils.dip2px(_context, getData("DOCK_ICON_BOTTOM", DOCK_ICON_BOTTOM))
                    }
                })
            // 搜索框宽度
            XposedHelpers.findAndHookMethod(
                _DEVICE_CONFIG_CLASS,
                "calcSearchBarWidth",
                Context::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        super.beforeHookedMethod(param)

                        val _context = param.args[0] as Context

                        val deviceWidth = Utils.px2dip(
                            _context,
                            _context.resources.displayMetrics.widthPixels
                        )
                        param.result =
                            Utils.dip2px(
                                _context,
                                deviceWidth - getData("DOCK_SIDE", DOCK_SIDE)
                            )
                    }
                })


            XposedHelpers.findAndHookMethod(
                _DEVICE_CONFIG_CLASS,
                "getSearchBarWidthDelta",
                XC_MethodReplacement.returnConstant(0)

            )

            XposedHelpers.findAndHookMethod(
                XposedHelpers.findClassIfExists(SEARCH_BAR_BLUR, lpparam.classLoader),
                "isUserBlur",
                XC_MethodReplacement.returnConstant(true)
            )

            // Dock底部边距
            XposedHelpers.findAndHookMethod(
                _DEVICE_CONFIG_CLASS,
                "calcSearchBarMarginBottom",
                Context::class.java,
                Boolean::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        super.beforeHookedMethod(param)
                        val _context = param.args[0] as Context
                        param.result =
                            Utils.dip2px(_context, getData("DOCK_BOTTOM", DOCK_BOTTOM))
                    }
                })
        } catch (e: Throwable) {
            XposedBridge.log("[MIUIDock] DeviceConfigHook Error:" + e.message)
        }
    }

    private fun deviceLevelHook(lpparam: XC_LoadPackage.LoadPackageParam) {
        val _BLUR_UTILS_CLASS = XposedHelpers.findClassIfExists(
            BLUR_UTILS_CLASSNAME,
            lpparam.classLoader
        ) ?: return
        val _DEVICE_LEVEL_UTILS_CLASS = XposedHelpers.findClassIfExists(
            DEVICE_LEVEL_UTILS_CLASSNAME,
            lpparam.classLoader
        ) ?: return
        val _CPU_LEVEL_UTILS_CLASS = XposedHelpers.findClassIfExists(
            CPU_LEVEL_UTILS_CLASSNAME,
            lpparam.classLoader
        ) ?: return
        val _UTILITIES_CLASS = XposedHelpers.findClassIfExists(
            UTILITIES_CLASSNAME,
            lpparam.classLoader
        ) ?: return
        // 高斯模糊类型
        replaceMethodResult(_BLUR_UTILS_CLASS, "getBlurType", 2)
        // 打开文件夹是否开启模糊
        replaceMethodResult(_BLUR_UTILS_CLASS, "isUserBlurWhenOpenFolder", true)
        // 设备等级
        replaceMethodResult(_DEVICE_LEVEL_UTILS_CLASS, "getDeviceLevel", 2)
        replaceMethodResult(_DEVICE_LEVEL_UTILS_CLASS, "isUseSimpleAnim", false)
        replaceMethodResult(_CPU_LEVEL_UTILS_CLASS, "getQualcommCpuLevel", 2, String::class.java)
        // 下载特效
        replaceMethodResult(_CPU_LEVEL_UTILS_CLASS, "needMamlDownload", true)
        // 平滑动画
        replaceMethodResult(_UTILITIES_CLASS, "isUseSmoothAnimationEffect", true)
    }

    private fun resetDockRadius(res: XResources, context: Context, drawableName: String) {
        try {
            res.setReplacement(
                MIUI_HOME_LAUNCHER_PACKAGE_NAME,
                "drawable",
                drawableName,
                object : XResources.DrawableLoader() {
                    override fun newDrawable(xres: XResources, id: Int): Drawable {
                        val background = ContextCompat.getDrawable(
                            context, xres.getIdentifier(
                                drawableName,
                                "drawable",
                                MIUI_HOME_LAUNCHER_PACKAGE_NAME
                            )
                        ) as RippleDrawable
                        val backgroundShape = background.getDrawable(0) as GradientDrawable
                        backgroundShape.cornerRadius =
                            Utils.dip2px(context, getData("DOCK_RADIUS", DOCK_RADIUS))
                                .toFloat()
                        background.setDrawable(0, backgroundShape)
                        return background
                    }
                })
        } catch (e: Throwable) {
            XposedBridge.log("[MIUIDock] ResourcesReplacement Error:" + e.message)
        }
    }

    private fun replaceMethodResult(clazz: Class<*>, methodName: String, result: Any, vararg args: Any?) {
        try {
            XposedHelpers.findAndHookMethod(clazz, methodName, *args, XC_MethodReplacement.returnConstant(result))
            XposedBridge.log("[MIUIDock] Replace Method Called $methodName Successfully!")
        } catch (e: Throwable) {
            XposedBridge.log("[MIUIDock] Replace Method Result Error:" + e.message)
        }
    }

    private fun getData(key: String, defValue: Int): Int {
        try {
            val pref = XSharedPreferences(SELF_PACKAGE_NAME, BuildConfig.APPLICATION_ID)
            return pref.getInt(key, defValue)
        } catch (e: Throwable) {
            XposedBridge.log("[MIUIDock] Can not get data:$key")
        }
        return defValue
    }
}
