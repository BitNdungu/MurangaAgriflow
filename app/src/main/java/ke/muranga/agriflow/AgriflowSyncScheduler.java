package ke.muranga.agriflow;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/** Schedules immediate and periodic background synchronization. */
public final class AgriflowSyncScheduler {

    private static final String PERIODIC_WORK_NAME = "agriflow_periodic_sync";
    private static final String ONE_TIME_WORK_NAME = "agriflow_sync_pending";

    private AgriflowSyncScheduler() {
    }

    /** Ensures a connected-network retry cycle exists (minimum period: 15 minutes). */
    public static void schedulePeriodicSync(@NonNull Context context) {
        PeriodicWorkRequest request =
                new PeriodicWorkRequest.Builder(
                        AgriflowSyncWorker.class,
                        15,
                        TimeUnit.MINUTES
                )
                        .setConstraints(connectedNetworkConstraints())
                        .setBackoffCriteria(
                                BackoffPolicy.EXPONENTIAL,
                                30,
                                TimeUnit.SECONDS
                        )
                        .build();

        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniquePeriodicWork(
                        PERIODIC_WORK_NAME,
                        ExistingPeriodicWorkPolicy.KEEP,
                        request
                );
    }

    /** Enqueues work after a profile or report changes; appends behind active sync. */
    public static void enqueueSync(@NonNull Context context) {
        OneTimeWorkRequest request =
                new OneTimeWorkRequest.Builder(AgriflowSyncWorker.class)
                        .setConstraints(connectedNetworkConstraints())
                        .setBackoffCriteria(
                                BackoffPolicy.EXPONENTIAL,
                                30,
                                TimeUnit.SECONDS
                        )
                        .build();

        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniqueWork(
                        ONE_TIME_WORK_NAME,
                        ExistingWorkPolicy.APPEND_OR_REPLACE,
                        request
                );
    }

    /** Enqueues startup catch-up only if no one-time sync chain is unfinished. */
    public static void enqueueSyncIfIdle(@NonNull Context context) {
        OneTimeWorkRequest request =
                new OneTimeWorkRequest.Builder(AgriflowSyncWorker.class)
                        .setConstraints(connectedNetworkConstraints())
                        .setBackoffCriteria(
                                BackoffPolicy.EXPONENTIAL,
                                30,
                                TimeUnit.SECONDS
                        )
                        .build();

        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniqueWork(
                        ONE_TIME_WORK_NAME,
                        ExistingWorkPolicy.KEEP,
                        request
                );
    }

    private static Constraints connectedNetworkConstraints() {
        return new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
    }
}
