package com.willymax.exercisealarm.alarm

import android.app.ForegroundServiceStartNotAllowedException
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.willymax.exercisealarm.AddAlarmFragment
import com.willymax.exercisealarm.R
import com.willymax.exercisealarm.receivers.AlarmReceiver
import com.willymax.exercisealarm.utils.AppConstants

private const val ACTION_PLAY_RINGTONE = "com.willymax.exercisealarm.alarm.action.PLAY_RINGTONE"
private const val ACTION_VIBRATE = "com.willymax.exercisealarm.alarm.action.VIBRATE"

// TODO: Rename parameters
private const val EXTRA_PARAM1 = "com.willymax.exercisealarm.alarm.extra.PARAM1"
private const val EXTRA_PARAM2 = "com.willymax.exercisealarm.alarm.extra.PARAM2"

/**
 * An subclass for handling asynchronous task requests in
 * a service on a separate handler thread.

 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.

 */
class RightOnPlayAndVibrateService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var vibrator: Vibrator
    private var spotifyAppRemote: SpotifyAppRemote? = null

    // onBind is called when the service is bound to an activity
    // this is not needed for this service
    // so we return null
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // onCreate is called when the service is created
    override fun onCreate() {
        super.onCreate()
        try {
            val channelId = AppConstants.ALARM_CHANNEL_ID
            val notification = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Exercise Alarm")
                .setContentText("It is time to {Add activity name here}")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .addAction(
                    R.drawable.ic_launcher_foreground, "Stop", PendingIntent.getBroadcast(
                        this, 0, Intent(this, AlarmReceiver::class.java).apply {
                            action = AppConstants.ACTION_STOP_ALARM
                        }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
                .build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Log.d("RightOnPlayAndVibrateService", "startForeground with foregroundServiceType")
                startForeground(
                    1, notification, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    } else {
                        0
                    }
                )
            } else {
                Log.d(
                    "RightOnPlayAndVibrateService", "startForeground without foregroundServiceType"
                )
                startForeground(1, notification)
            }
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && e is ForegroundServiceStartNotAllowedException) {
                // App not in a valid state to start foreground service
                // (e.g. started from bg)
                Log.e(
                    "RightOnPlayAndVibrateService",
                    "App not in a valid state to start foreground service ${e.message}"
                )
            } else {
                Log.e("RightOnPlayAndVibrateService", "startForegroundService failed ${e.message}")
            }
        }
    }

    // onStartCommand is called when the service is started
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val selectedRingtone = intent?.getStringExtra(AppConstants.SELECTED_RINGTONE)
        val selectedRingtoneFrom: AddAlarmFragment.Companion.RingtoneFrom = intent?.getStringExtra(AppConstants.SELECTED_RINGTONE_FROM)?.let {
            AddAlarmFragment.Companion.RingtoneFrom.valueOf(it)
        } ?: AddAlarmFragment.Companion.RingtoneFrom.DEFAULT

        // cast selectedRingtoneFrom to Enum
        Log.d("RightOnPlayAndVibrateService", "selectedRingtone: $selectedRingtone")
        Log.d("RightOnPlayAndVibrateService", "selectedRingtoneFrom: $selectedRingtoneFrom")

        when (selectedRingtoneFrom) {
            AddAlarmFragment.Companion.RingtoneFrom.DEFAULT -> {
                val defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                mediaPlayer = MediaPlayer.create(this, defaultRingtoneUri)
            }
            AddAlarmFragment.Companion.RingtoneFrom.SPOTIFY -> {
    //            spotifyAppRemote?.playerApi?.play(selectedRingtone)
            }
            else -> {
                mediaPlayer = MediaPlayer.create(this, Uri.parse(selectedRingtone))
            }
        }
        // if mediaPlayer is null, then use system default ringtone
        if (mediaPlayer == null) {
            val defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            mediaPlayer = MediaPlayer.create(this, defaultRingtoneUri)
        }
        mediaPlayer?.isLooping = true
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        when (intent?.action) {
            ACTION_PLAY_RINGTONE -> {
                handleActionPlayRingtone(selectedRingtone, selectedRingtoneFrom)
                handleActionVibrate(selectedRingtone, selectedRingtoneFrom)
            }

            ACTION_VIBRATE -> {
                handleActionVibrate(selectedRingtone, selectedRingtoneFrom)
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        vibrator.cancel()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        Log.d("RightOnPlayAndVibrateService", "onDestroy")
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionPlayRingtone(selectedRingtone: String?, selectedRingtoneFrom: AddAlarmFragment.Companion.RingtoneFrom) {
        // continue playing the ringtone until the user stops it
//        TODO: mediaPlayer?.start()
//        mediaPlayer?.start()
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionVibrate(selectedRingtone: String?, selectedRingtoneFrom: AddAlarmFragment.Companion.RingtoneFrom) {
        val vibrationEffect = VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)
        while (mediaPlayer?.isPlaying == true) {
            vibrator.vibrate(vibrationEffect)
            // For older devices without VibrationEffect API
            //vibrator.vibrate(1000)
        }
        // stop the vibration
    }

    companion object {
        /**
         * Starts this service to perform action Foo with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         */
        // TODO: Customize helper method
        @JvmStatic
        fun startActionPlayRingtone(context: Context, selectedRingtone: String, selectedRingtoneFrom: String) {
            val intent = Intent(context, RightOnPlayAndVibrateService::class.java).apply {
                action = ACTION_PLAY_RINGTONE
                putExtra(AppConstants.SELECTED_RINGTONE, selectedRingtone)
                putExtra(AppConstants.SELECTED_RINGTONE_FROM, selectedRingtoneFrom)
            }
            context.startService(intent)
        }

        /**
         * Starts this service to perform action Baz with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         */
        // TODO: Customize helper method
        @JvmStatic
        fun startActionVibrate(context: Context, param1: String, param2: String) {
            val intent = Intent(context, RightOnPlayAndVibrateService::class.java).apply {
                action = ACTION_VIBRATE
                putExtra(EXTRA_PARAM1, param1)
                putExtra(EXTRA_PARAM2, param2)
            }
            context.startService(intent)
        }
    }
}