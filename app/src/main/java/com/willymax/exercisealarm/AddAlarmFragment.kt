package com.willymax.exercisealarm

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.Track
import com.spotify.sdk.android.auth.AccountsQueryParameters.CLIENT_ID
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.willymax.exercisealarm.alarm.AlarmItem
import com.willymax.exercisealarm.alarm.AlarmSchedulerImpl
import com.willymax.exercisealarm.databinding.FragmentAddFragmentBinding
import com.willymax.exercisealarm.utils.AlarmActivities
import com.willymax.exercisealarm.utils.AppUtils
import com.willymax.exercisealarm.utils.PermissionUtil
import com.willymax.exercisealarm.utils.SharedPreferencesHelper
import com.willymax.exercisealarm.utils.TimeUtils
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale


const val REQUEST_CODE_READ_EXTERNAL_STORAGE = 1001

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class AddAlarmFragment : Fragment() {

    private lateinit var registerForActivityResultRingtone: ActivityResultLauncher<Int>
    private var _binding: FragmentAddFragmentBinding? = null
    private var sensorManager: SensorManager? = null
    private var alarmManager: AlarmManager? = null
    private var spotifyClientId: String = BuildConfig.SPOTIFY_CLIENT_ID
    private var spotifyRedirectUri: String = BuildConfig.SPOTIFY_REDIRECT_URI
    private var spotifyAppRemote: SpotifyAppRemote? = null

    // a variable to hold the selected ringtone and from where
    private var selectedRingtone: String? = null
    private var selectedRingtoneFrom: RingtoneFrom = RingtoneFrom.DEFAULT


    private val readMediaAudioPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // PERMISSION GRANTED
            openFileExplorer()
        } else {
            // PERMISSION NOT GRANTED
        }
    }

    companion object {
        enum class RingtoneFrom {
            DEFAULT, CUSTOM, SPOTIFY
        }
    }


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        alarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager


        try {
            class PickRingtone : ActivityResultContract<Int, Uri?>() {
                override fun createIntent(context: Context, ringtoneType: Int) =
                    Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, ringtoneType)
                    }

                override fun parseResult(resultCode: Int, result: Intent?): Uri? {
                    if (resultCode != Activity.RESULT_OK) {
                        return null
                    }
                    return result?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                }
            }

            registerForActivityResultRingtone = registerForActivityResult(PickRingtone()) {
                // do something with the result
                Log.d(
                    AddAlarmFragment::class.java.name,
                    "Ringtone Selected: $it"
                )
                selectedRingtone = it?.toString()
            }
        } catch (ex: ActivityNotFoundException) {
            // Potentially direct the user to the Market with a Dialog
            AppUtils.showMessageDialog(
                requireActivity(),
                "File Manager Not Found",
                "Please install a File Manager.",
                "Install",
                "Cancel",
                { dialogInterface, i ->
                    run {
                        // redirect to play store
                        val branchLink =
                            Uri.encode("https://spotify.link/content_linking?~campaign=" + requireContext().packageName);
                        val appPackageName = "com.spotify.music";
                        val referrer = "_branch_link=$branchLink";
                        AppUtils.redirectUserToPlayStore(
                            requireActivity(),
                            appPackageName,
                            referrer
                        )
                    }
                },
                { dialogInterface, i ->
                    // do nothing
                }
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddFragmentBinding.inflate(inflater, container, false)
        binding.alarmTimePicker.setIs24HourView(true)
        binding.alarmTimePicker.setOnTimeChangedListener { _, _, _ -> updateRemainingTime() }
        binding.alarmRepeatSpinner.setSelection(0)
        binding.alarmRepeatSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if ("Custom" == parent?.getItemAtPosition(position)) {
                        binding.customWeekdays.visibility = View.VISIBLE
                    } else {
                        binding.customWeekdays.visibility = View.GONE
                    }
                    updateRemainingTime()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }
        binding.alarmRingtoneSongSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    onRingtoneSongSelected()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // do nothing
                }
            }
        updateRemainingTime()
        return binding.root
    }

    private fun onRingtoneSongSelected() {
        Log.d(
            AddAlarmFragment::class.java.name,
            "Ringtone Song Type Selected: ${binding.alarmRingtoneSongSpinner.selectedItem}"
        )

        when (binding.alarmRingtoneSongSpinner.selectedItem.toString()) {
            "Select From Device" -> {
                selectedRingtoneFrom = RingtoneFrom.CUSTOM
                // play custom music
                Log.d(
                    AddAlarmFragment::class.java.name,
                    "Select From Device Selected"
                )
                selectCustomAlarmRingtoneFromDevice()
            }

            "Select From Spotify" -> {
                Log.d(
                    AddAlarmFragment::class.java.name,
                    "Select From Spotify Selected"
                )
                selectedRingtoneFrom = RingtoneFrom.SPOTIFY
                spotifySelected();
            }

            else -> {
                selectedRingtoneFrom = RingtoneFrom.DEFAULT
                // play default music
            }
        }
    }

    /**
     * give me pseudocode to select custom music from device
     * 1. open file explorer
     * 2. once file is selected, then set alarm ringtone to custom music
     * 3. once alarm rings, then play custom music
     * 4. once alarm is dismissed, then stop playing custom music
     */
    private fun selectCustomAlarmRingtoneFromDevice() {
        // check if user has granted permission to read external storage
        if (PermissionUtil.isGranted(requireContext(), readMediaAudioPermission)) {
            // open file explorer
            openFileExplorer()
        } else {
            Log.d(
                MainActivity::class.simpleName,
                "Permission is not granted $readMediaAudioPermission"
            )
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    readMediaAudioPermission
                )
            ) {
                Manifest.permission.READ_EXTERNAL_STORAGE
                Log.d(
                    MainActivity::class.simpleName,
                    "Should show request permission rationale $readMediaAudioPermission"
                )
                // Show an explanation to the user asynchronously
                PermissionUtil.showConfirmDialog(
                    requireActivity(),
                    "Read External Storage Permission",
                    "This app needs read external storage permission to select custom alarm ringtone",
                    { dialogInterface, i ->
                        run {
                            requestPermissionLauncher.launch(readMediaAudioPermission)
                        }
                    }
                ) { dialogInterface, i ->
                    run {

                    }
                }
            } else {
                Log.d(
                    MainActivity::class.simpleName,
                    "Requesting permission $readMediaAudioPermission"
                )
                // No explanation needed, we can request the permission.
                requestPermissionLauncher.launch(readMediaAudioPermission)
            }
        }
    }

    private fun openFileExplorer() {
//        val intent = Intent(Intent.ACTION_GET_CONTENT)
//        intent.type = "audio/*"
//        intent.addCategory(Intent.CATEGORY_OPENABLE)
        registerForActivityResultRingtone.launch(RingtoneManager.TYPE_ALARM)
    }

    /**
     * give me pseudocode to set alarm ringtone to play spotify music
     * 1. check if spotify is installed
     * 2. if spotify is installed, then authorize app
     * 3. if spotify is not installed, then redirect user to play store
     * 4. once spotify is authorized, then play music
     * 5. once music is playing, then set alarm ringtone to play spotify music
     * 6. once alarm rings, then play spotify music
     * 7. once alarm is dismissed, then stop playing spotify music
     * 8. once alarm is snoozed, then stop playing spotify music
     * 9. once alarm is turned off, then stop playing spotify music
     * 10. once alarm is turned on, then play spotify music
     * 11. once alarm is deleted, then stop playing spotify music
     * 12. once alarm is edited, then stop playing spotify music
     * 13. once alarm is saved, then stop playing spotify music
     * 14. once alarm is cancelled, then stop playing spotify music
     * 15. once alarm is set, then stop playing spotify music
     */


    private fun spotifySelected() {
        // play spotify music

        // confirm if user has spotify installed
        val packageManager = requireContext().packageManager;
        try {
            val info = packageManager.getPackageInfo("com.spotify.music", 0)
            if (info != null) {
                // if spotify is installed, then authorize app
                authorizeSpotify()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            // if not, then redirect to play store
            AppUtils.showMessageDialog(
                requireActivity(),
                "Spotify Not Installed",
                "Please install Spotify from the Play Store",
                "Install",
                "Cancel",
                { dialogInterface, i ->
                    run {
                        // redirect to play store
                        val branchLink =
                            Uri.encode("https://spotify.link/content_linking?~campaign=" + requireContext().packageName);
                        val appPackageName = "com.spotify.music";
                        val referrer = "_branch_link=$branchLink";
                        AppUtils.redirectUserToPlayStore(
                            requireActivity(),
                            appPackageName,
                            referrer
                        )
                    }
                },
                { dialogInterface, i ->
                    // do nothing
                }
            )
        }
    }

    private fun authorizeSpotify() {
        // authorize app
        // Set the connection parameters
//        val connectionParams = ConnectionParams.Builder(spotifyClientId)
//            .setRedirectUri(spotifyRedirectUri)
//            .showAuthView(true)
//            .build()
//        // Authorizing user with Single Sign-On library
//        SpotifyAppRemote.connect(
//            requireContext(),
//            connectionParams,
//            object : Connector.ConnectionListener {
//                override fun onConnected(p0: SpotifyAppRemote?) {
//                    spotifyAppRemote = p0
//                    Log.d(AddAlarmFragment::class.java.name, "Connected Spotify! Yay!")
//                    // Now you can start interacting with App Remote
//                    spotifyConnected()
//                }
//
//                override fun onFailure(p0: Throwable?) {
//                    Log.d(AddAlarmFragment::class.java.name, "Failed Spotify! Yay!")
//                    Log.e(AddAlarmFragment::class.java.name, p0?.message, p0)
//                    // Something went wrong when attempting to connect! Handle errors here
//                }
//            })

        val REQUEST_CODE = 1337
        val REDIRECT_URI = "http://com.willymax.exercisealarm/callback"

        val builder =
            AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI)

        builder.setScopes(arrayOf("streaming"))
        val request = builder.build()

        AuthorizationClient.openLoginActivity(requireActivity(), REQUEST_CODE, request)
    }

    private fun spotifyConnected() {
        selectedRingtone = "spotify:playlist:37i9dQZF1DXcBWIGoYBM5M"
        // play music
        spotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback { playerState ->
            val track: Track? = playerState.track
            if (track != null) {
                Log.d(AddAlarmFragment::class.java.name, track.name + " by " + track.artist.name)
            }
        }
    }

    /**
     * Updates the remaining time text view based on the time picker
     * Note: an alarm can repeat on multiple days depending on the repeat spinner. Calculate the remaining time based on the next day the alarm will ring
     */
    private fun updateRemainingTime() {
        var remainingTime: Triple<Long, Long, Long> = Triple(0, 0, 0)
        val hourOfDay = binding.alarmTimePicker.hour
        val minute = binding.alarmTimePicker.minute
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        Log.d(
            AddAlarmFragment::class.java.name,
            "Alarm Time: ${
                SimpleDateFormat(
                    "HH:mm",
                    Locale.US
                ).format(calendar.time)
            } Millis: ${calendar.timeInMillis}"
        )
        // determine the next day the alarm will ring given that an alarm can repeat once, daily, or weekly
        val selectedItem = binding.alarmRepeatSpinner.selectedItem
        when (selectedItem) {
            "Once" -> {
                remainingTime =
                    TimeUtils.getDaysHoursMinutes(TimeUtils.getTimeRemainingInMilliseconds(calendar.timeInMillis))
            }

            "Everyday" -> {
                remainingTime =
                    TimeUtils.getDaysHoursMinutes(TimeUtils.getTimeRemainingInMilliseconds(calendar.timeInMillis))
            }

            "Weekdays" -> {
                // if the alarm is set to repeat on weekdays, then calculate the remaining time based on the next weekday
                val currentDayOfWeek = LocalDate.now().dayOfWeek
                val daysUntilNextWeekday = when (currentDayOfWeek) {
                    DayOfWeek.MONDAY -> 1
                    DayOfWeek.TUESDAY -> 1
                    DayOfWeek.WEDNESDAY -> 1
                    DayOfWeek.THURSDAY -> 1
                    DayOfWeek.FRIDAY -> 1
                    DayOfWeek.SATURDAY -> 2
                    DayOfWeek.SUNDAY -> 1
                    else -> 1
                }
                calendar.add(Calendar.DAY_OF_MONTH, daysUntilNextWeekday)
                remainingTime =
                    TimeUtils.getDaysHoursMinutes(TimeUtils.getTimeRemainingInMilliseconds(calendar.timeInMillis))
            }

            "Weekends" -> {
                // if the alarm is set to repeat on weekends, then calculate the remaining time based on the next weekend
                val currentDayOfWeek = LocalDate.now().dayOfWeek
                val daysUntilNextWeekend = when (currentDayOfWeek) {
                    DayOfWeek.MONDAY -> 5
                    DayOfWeek.TUESDAY -> 4
                    DayOfWeek.WEDNESDAY -> 3
                    DayOfWeek.THURSDAY -> 2
                    DayOfWeek.FRIDAY -> 1
                    DayOfWeek.SATURDAY -> 1
                    DayOfWeek.SUNDAY -> 7
                    else -> 1
                }
                if (currentDayOfWeek == DayOfWeek.SATURDAY || currentDayOfWeek == DayOfWeek.SUNDAY) {
                    calendar.add(Calendar.DAY_OF_MONTH, 0)
                } else {
                    calendar.add(Calendar.DAY_OF_MONTH, daysUntilNextWeekend)
                }

                remainingTime =
                    TimeUtils.getDaysHoursMinutes(TimeUtils.getTimeRemainingInMilliseconds(calendar.timeInMillis))
            }

            "Custom" -> {
                // is there a way to group checkboxes together? if so, then get the selected checkboxes and calculate the remaining time based on the next day the alarm will ring

                val selectedDaysOfWeek = binding.monday.isChecked
                remainingTime =
                    TimeUtils.getDaysHoursMinutes(TimeUtils.getTimeRemainingInMilliseconds(calendar.timeInMillis))
            }
        }

        if (remainingTime.first > 0) {
            binding.alarmAfter.text =
                getString(R.string.alarm_will_ring_in, "${remainingTime.first} Days")
        } else if (remainingTime.second > 0) {
            binding.alarmAfter.text =
                getString(
                    R.string.alarm_will_ring_in,
                    "${remainingTime.second} Hours ${remainingTime.third} Minutes"
                )
        } else {
            binding.alarmAfter.text =
                getString(R.string.alarm_will_ring_in, "${remainingTime.third} Minutes")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.saveAlarmButton.setOnClickListener {
            saveExactAlarm()
        }
    }

    private fun saveExactAlarm() {
        val newAlarmItem = AlarmItem(
            Date().time.toString(),
            binding.alarmTimePicker.hour,
            binding.alarmTimePicker.minute,
            arrayListOf(LocalDate.now().dayOfWeek),
            binding.alarmLabelText.text.toString(),
            AlarmActivities.WALKING,
            repeats = true,
            isOn = true,
            selectedRingtone,
            selectedRingtoneFrom,
        )
        Log.d(
            AddAlarmFragment::class.java.name,
            "Save Alarm Button Clicked newAlarmItem: $newAlarmItem"
        )

        val sharedPreferencesHelper = SharedPreferencesHelper(requireContext(), "AlarmList")

        sharedPreferencesHelper.updateList("AlarmList") { list ->
            list + newAlarmItem
        }
        Log.d(
            AddAlarmFragment::class.java.name,
            "Alarm List: ${sharedPreferencesHelper.retrieveList("AlarmList")}"
        )

        AlarmSchedulerImpl(requireActivity()).scheduleAlarm(newAlarmItem)
        findNavController().popBackStack(R.id.AlarmFragment, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}