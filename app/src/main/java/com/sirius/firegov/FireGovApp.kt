package com.sirius.firegov

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.sirius.firegov.worker.StationSyncWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

import android.util.Log

@HiltAndroidApp
class FireGovApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() {
            Log.d("FireGovApp", "workManagerConfiguration: called")
            return Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()
        }

    override fun onCreate() {
        super.onCreate()
        Log.d("FireGovApp", "onCreate: called")
        scheduleStationSync()
    }

    private fun scheduleStationSync() {
        Log.d("FireGovApp", "scheduleStationSync: called")
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<StationSyncWorker>()
            .setConstraints(constraints)
            .addTag("StationSyncWork")
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            "StationSyncWorkOneTime",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )

        val periodicSyncRequest = PeriodicWorkRequestBuilder<StationSyncWorker>(12, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "StationSyncWork",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSyncRequest
        )
    }
}
