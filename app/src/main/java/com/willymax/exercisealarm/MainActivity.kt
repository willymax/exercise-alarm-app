package com.willymax.exercisealarm

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.willymax.exercisealarm.databinding.ActivityMainBinding
import com.willymax.exercisealarm.utils.PermissionUtil

private const val REQUEST_CODE_ACTIVITY_RECOGNITION = 1000

private val activityPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    Manifest.permission.ACTIVITY_RECOGNITION
} else {
    "com.google.android.gms.permission.ACTIVITY_RECOGNITION"
}

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.addNewAlarm.setOnClickListener {
            navController.navigate(R.id.action_MainActivity_to_AddAlarmFragment)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()
        // request notifications permission
        PermissionUtil.checkAndRequestNotification(this)

        // request activity recognition permission
        if (ContextCompat.checkSelfPermission(
                this,
                activityPermission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val requestPermissionLauncher =
                registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->
                    if (isGranted) {
                        // Permission is granted. Continue the action or workflow in your
                        // app.
                    } else {
                        // Permission denied, disable features that require this permission
                        // Explain to the user that the feature is unavailable because the
                        // feature requires a permission that the user has denied. At the
                        // same time, respect the user's decision. Don't link to system
                        // settings in an effort to convince the user to change their
                        // decision.

                        // User selected "Don't ask again", disable features that require this permission
                        disableActivityTrackingFeatures()
                    }
                }
            Log.d(
                MainActivity::class.simpleName,
                "Permission is not granted $activityPermission"
            )
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    activityPermission
                )
            ) {
                // Show an explanation to the user asynchronously
                PermissionUtil.showConfirmDialog(
                    this,
                    "Activity Recognition Permission",
                    "This app needs activity recognition permission to track your activity",
                    { dialogInterface, i ->
                        run {
                            requestPermissionLauncher.launch(activityPermission)
                        }
                    }, { dialogInterface, i ->
                        run {
                            //
                        }
                    }
                )
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(activityPermission),
                    REQUEST_CODE_ACTIVITY_RECOGNITION
                )
            }
        } else {
            Log.d(
                MainActivity::class.simpleName,
                "Permission is granted $activityPermission"
            )
        }
    }

    private fun disableActivityTrackingFeatures() {
        // Disable features that require activity tracking

    }

    override fun onStart() {
        super.onStart()
        // We will start writing our code here.
    }

    private fun connected() {
        // Then we will write some more code here.
    }

    override fun onStop() {
        super.onStop()
        // Aaand we will finish off here.
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val uri: Uri? = intent?.data;
        if (uri != null) {
            val response: AuthorizationResponse = AuthorizationResponse . fromUri (uri);
            when(response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    // Response was successful and contains auth token
                    // Handle successful response
                    Log.d(MainActivity::class.simpleName, "AuthorizationResponse.Type.TOKEN ${response.accessToken}")
                }
                AuthorizationResponse.Type.ERROR -> {
                    // Auth flow returned an error
                    // Handle error response
                    Log.d(MainActivity::class.simpleName, "AuthorizationResponse.Type.ERROR ${response.error}")
                } else -> {
                // Auth flow returned an empty response
                // Most likely auth flow was cancelled
                    Log.d(MainActivity::class.simpleName, "AuthorizationResponse of unknown")
                }
                // Handle other cases
            }
        }
    }
}