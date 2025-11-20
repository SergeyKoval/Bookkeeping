package by.bk.bookkeeper.android.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.sms.preferences.SharedPreferencesProvider

class SettingsFragment : Fragment() {

    private lateinit var debugPushSwitch: SwitchCompat
    private lateinit var pushDelaySeekBar: SeekBar
    private lateinit var pushDelayValueText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        debugPushSwitch = view.findViewById(R.id.switch_debug_push)

        // Load current preference value
        debugPushSwitch.isChecked = SharedPreferencesProvider.getDebugPushNotifications()

        // Save preference on change
        debugPushSwitch.setOnCheckedChangeListener { _, isChecked ->
            SharedPreferencesProvider.setDebugPushNotifications(isChecked)
        }

        // Push delay SeekBar setup
        pushDelaySeekBar = view.findViewById(R.id.seekbar_push_delay)
        pushDelayValueText = view.findViewById(R.id.text_push_delay_value)

        // Load current delay value
        val currentDelay = SharedPreferencesProvider.getPushProcessingDelaySeconds()
        pushDelaySeekBar.progress = currentDelay
        updateDelayLabel(currentDelay)

        // Update label and save on change
        pushDelaySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateDelayLabel(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.progress?.let { delay ->
                    SharedPreferencesProvider.setPushProcessingDelaySeconds(delay)
                }
            }
        })
    }

    private fun updateDelayLabel(seconds: Int) {
        pushDelayValueText.text = getString(R.string.settings_push_delay_label, seconds)
    }
}
