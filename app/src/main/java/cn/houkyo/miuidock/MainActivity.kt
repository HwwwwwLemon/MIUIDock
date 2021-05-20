package cn.houkyo.miuidock

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import cn.houkyo.miuidock.ui.CustomSeekBar
import com.leaf.library.StatusBarUtil


@SuppressLint("UseSwitchCompatOrMaterialCode")
class MainActivity : AppCompatActivity() {
    private var radius = DefaultValue.radius
    private var height = DefaultValue.height
    private var sideMargin = DefaultValue.sideMargin
    private var bottomMargin = DefaultValue.bottomMargin
    private var iconBottomMargin = DefaultValue.iconBottomMargin
    private var highLevel = DefaultValue.highLevel
    private var hideIcon = DefaultValue.hideIcon
    private var hideIconMenu: MenuItem? = null
    private var mToolbar: Toolbar? = null
    private fun init() {
        mToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.title = ""
        mToolbar?.background = if (!Utils.isDarkMode(this)) {
            ContextCompat.getDrawable(this, R.drawable.gradient_color)
        } else {
            ContextCompat.getDrawable(this, R.drawable.gradient_color_dark)
        }
        StatusBarUtil.setGradientColor(this, mToolbar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        if (!Utils.isModuleEnable()) {
            Utils.showToast(this, R.string.module_not_enable)
        }



        radius = MySharePreferences.getData(this, "DOCK_RADIUS", radius) as Int

        height = MySharePreferences.getData(this, "DOCK_HEIGHT", height) as Int

        sideMargin = MySharePreferences.getData(this, "DOCK_SIDE", sideMargin) as Int

        bottomMargin = MySharePreferences.getData(this, "DOCK_BOTTOM", bottomMargin) as Int

        iconBottomMargin = MySharePreferences.getData(this, "DOCK_ICON_BOTTOM", iconBottomMargin) as Int

        highLevel = MySharePreferences.getData(this, "HIGH_LEVEL", highLevel) as Int

        hideIcon = MySharePreferences.getData(this, "HIDE_ICON", hideIcon) as Int

        initData()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        hideIconMenu = menu.findItem(R.id.menu_hide_icon)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (hideIcon == 0) {
            hideIconMenu?.setTitle(R.string.hide_app_icon)
        } else {
            hideIconMenu?.setTitle(R.string.show_app_icon)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val hideIcon = R.id.menu_hide_icon
        val goToSetting = R.id.menu_to_setting
        val about = R.id.menu_about
        when (item.itemId) {
            hideIcon -> handleHideIcon()
            goToSetting -> killHome()
            about -> showAbout()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun initData() {
        val radiusSeekBar = findViewById<CustomSeekBar>(R.id.dockRadiusSeekBar)
        val heightSeekBar = findViewById<CustomSeekBar>(R.id.dockHeightSeekBar)
        val sideSeekBar = findViewById<CustomSeekBar>(R.id.dockSideSeekBar)
        val bottomSeekBar = findViewById<CustomSeekBar>(R.id.dockBottomSeekBar)
        val iconBottomSeekBar = findViewById<CustomSeekBar>(R.id.dockIconBottomSeekBar)
        val highLevelSwitch = findViewById<Switch>(R.id.highLevelSwitch)
        val saveButton = findViewById<Button>(R.id.saveButton)


        radiusSeekBar.setTitle(resources.getString(R.string.dock_radius_property))
        radiusSeekBar.setMinValue(0)
        radiusSeekBar.setMaxValue(height)
        radiusSeekBar.setValue(radius)

        heightSeekBar.setTitle(resources.getString(R.string.dock_height_property))
        heightSeekBar.setMinValue(30)
        heightSeekBar.setMaxValue(120)
        heightSeekBar.setValue(height)
        heightSeekBar.setOnValueChangeListener { value -> radiusSeekBar.setMaxValue(value) }

        val deviceWidth = Utils.px2dip(this, resources.displayMetrics.widthPixels)
        sideSeekBar.setTitle(resources.getString(R.string.dock_side_margin_property))
        sideSeekBar.setMinValue(0)
        sideSeekBar.setMaxValue((deviceWidth / 2) + 10)
        sideSeekBar.setValue(sideMargin)

        bottomSeekBar.setTitle(resources.getString(R.string.dock_bottom_margin_property))
        bottomSeekBar.setMinValue(0)
        bottomSeekBar.setMaxValue(200)
        bottomSeekBar.setValue(bottomMargin)

        iconBottomSeekBar.setTitle(resources.getString(R.string.dock_icon_bottom_margin_property))
        iconBottomSeekBar.setMinValue(0)
        iconBottomSeekBar.setMaxValue(200)
        iconBottomSeekBar.setValue(iconBottomMargin)

        highLevelSwitch.isChecked = highLevel == 1

        saveButton.setOnClickListener {
            radius = radiusSeekBar.getValue()
            height = heightSeekBar.getValue()
            sideMargin = sideSeekBar.getValue()
            bottomMargin = bottomSeekBar.getValue()
            iconBottomMargin = iconBottomSeekBar.getValue()
            highLevel = if (highLevelSwitch.isChecked) 1 else 0
            MySharePreferences.setData(this, "DOCK_RADIUS", radius)
            MySharePreferences.setData(this, "DOCK_RADIUS", radius)
            MySharePreferences.setData(this, "DOCK_HEIGHT", height)
            MySharePreferences.setData(this, "DOCK_SIDE", sideMargin)
            MySharePreferences.setData(this, "DOCK_BOTTOM", bottomMargin)
            MySharePreferences.setData(this, "DOCK_ICON_BOTTOM", iconBottomMargin)
            MySharePreferences.setData(this, "HIGH_LEVEL", highLevel)
            if (Utils.isModuleEnable()) {
                Utils.showToast(this, R.string.dock_save_tips)
                killHome()
            } else {
                Utils.showToast(this, R.string.module_not_enable)
            }
        }
    }

    private fun handleHideIcon() {
        var switch: Int = PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        if (hideIcon == 0) {
            // 图标显示时操作 -> 隐藏图标
            switch = PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            hideIcon = 1
            hideIconMenu?.setTitle(R.string.show_app_icon)
        } else {
            // 图标隐藏时操作 -> 显示图标
            hideIcon = 0
            hideIconMenu?.setTitle(R.string.hide_app_icon)
        }
        MySharePreferences.setData(this, "HIDE_ICON", hideIcon)
        this.packageManager.setComponentEnabledSetting(
            ComponentName(this, "cn.houkyo.miuidock.SplashActivityAlias"),
            switch, PackageManager.DONT_KILL_APP
        )
    }

    private fun killHome() {
        try {

            if (Utils.killMIUIHomeProcess() == 0) {
                Utils.showToast(this, R.string.restart_launcher_tips)
            } else {
                throw Exception()
            }
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.fromParts("package", "com.miui.home", null)
            startActivity(intent)
        }
    }

    private fun showAbout() {
        val alertDialogBuilder = AlertDialog.Builder(this, R.style.AlertDialog)
        alertDialogBuilder.setTitle(R.string.menu_about_title)
        alertDialogBuilder.setView(R.layout.about)
        alertDialogBuilder.setPositiveButton(
            "OK"
        ) { dialog, _ -> dialog.cancel() }
        alertDialogBuilder.setNegativeButton(
            "Github"
        ) { _, _ ->
            val github = "https://www.github.com/ouhoukyo/MIUIDock"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(github)))
        }
        val dialog = alertDialogBuilder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Utils.getColorPrimary(this))
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Utils.getColorPrimary(this))
    }


    override fun onBackPressed() {
        // super.onBackPressed(); 	不要调用父类的方法
        val intent = Intent(Intent.ACTION_MAIN)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addCategory(Intent.CATEGORY_HOME)
        startActivity(intent)
    }
}