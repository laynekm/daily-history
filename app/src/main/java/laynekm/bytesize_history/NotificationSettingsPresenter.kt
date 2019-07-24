package laynekm.bytesize_history

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast

class NotificationSettingsPresenter(val context: Context, val view: View) {

    private val preferencesKey: String = context.getString(R.string.preferences_key)
    private val notificationEnabledKey: String = context.getString(R.string.notification_enabled_pref_key)
    private val notificationTimeKey: String = context.getString(R.string.notification_time_pref_key)
    private val notificationTimeDefault: String = context.getString(R.string.notification_time_default)

    private val sharedPref: SharedPreferences = context.getSharedPreferences(preferencesKey, Context.MODE_PRIVATE)
    private var notificationEnabled: Boolean = sharedPref.getBoolean(notificationEnabledKey,  true)
    private var notificationTime: Time = stringToTime(sharedPref.getString(notificationTimeKey, notificationTimeDefault)!!)

    private val notificationManager = NotificationManager(context)

    init {
        ThemeManager(context).applyTheme()
    }

    fun onViewCreated() {
        view.updateUI(notificationEnabled, notificationTime)
    }

    fun setTime(time: Time) {
        notificationTime = time
        notificationManager.updateNotification(time)
        view.updateUI(notificationEnabled, notificationTime)
    }

    fun setNotification() {
        notificationEnabled = !notificationEnabled
        if (!notificationEnabled) notificationManager.disableNotification()
        view.updateUI(notificationEnabled, notificationTime)
    }

    fun showTimePickerDialog() {
        val hour = notificationTime.hour
        val minute = notificationTime.minute
        view.showTimePickerDialog(hour, minute)
    }

    interface View {
        fun updateUI(enabled: Boolean, time: Time)
        fun showTimePickerDialog(hour: Int, minute: Int)
    }
}