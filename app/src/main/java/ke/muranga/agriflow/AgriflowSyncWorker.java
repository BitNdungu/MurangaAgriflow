package ke.muranga.agriflow;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Automatically synchronizes the farmer profile, pending problem reports,
 * and attached photos. All work runs away from the UI thread.
 */
public final class AgriflowSyncWorker extends Worker {

    private static final String PREFERENCES_NAME = "agriflow_profile";
    private static final long CALLBACK_TIMEOUT_SECONDS = 60L;

    // Periodic and one-time requests can both exist. Serialize them within
    // this process so they cannot create the same report concurrently.
    private static final Object SYNC_LOCK = new Object();
    private static final Object CLIENT_LOCK = new Object();
    private static volatile AgriflowApiClient sharedApiClient;

    public AgriflowSyncWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParameters
    ) {
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        synchronized (SYNC_LOCK) {
            return synchronizePendingItems();
        }
    }

    private Result synchronizePendingItems() {
        Context context = getApplicationContext();
        SharedPreferences preferences = context.getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
        );

        String fullName = preferences.getString("full_name", "").trim();
        String phone = preferences.getString("phone", "").trim();
        String subcounty = preferences.getString("subcounty", "").trim();

        // The app also schedules work at startup, before a profile may exist.
        // Profile saving and report creation enqueue it again when ready.
        if (fullName.isEmpty() || phone.isEmpty() || subcounty.isEmpty()) {
            return Result.success();
        }

        ProblemReportDatabaseHelper database =
                new ProblemReportDatabaseHelper(context);

        try {
            AgriflowApiClient apiClient = getSharedApiClient(context);

            ApiResult health = awaitApi(apiClient::checkHealth);
            if (!health.isSuccess()
                    || !"ok".equals(health.response.optString("status"))) {
                return Result.retry();
            }

            JSONObject farmerPayload = createFarmerPayload(preferences);
            long existingFarmerId = preferences.getLong(
                    "backend_farmer_id",
                    0L
            );

            ApiResult farmerResult = awaitApi(
                    callback -> apiClient.syncFarmer(
                            existingFarmerId,
                            farmerPayload,
                            callback
                    )
            );

            if (!farmerResult.isSuccess()) {
                return Result.retry();
            }

            JSONObject remoteFarmer =
                    farmerResult.response.optJSONObject("farmer");
            long serverFarmerId = remoteFarmer == null
                    ? 0L
                    : remoteFarmer.optLong("id", 0L);

            if (serverFarmerId <= 0L) {
                return Result.retry();
            }

            preferences.edit()
                    .putLong("backend_farmer_id", serverFarmerId)
                    .apply();

            List<FarmProblemReport> reports = database.getAllReports();
            boolean anyFailure = false;

            for (FarmProblemReport report : reports) {
                if (isStopped()) {
                    return Result.retry();
                }

                long serverReportId = 0L;
                Long knownReportId = report.getServerReportId();
                if (knownReportId != null && knownReportId > 0L) {
                    serverReportId = knownReportId;
                } else {
                    database.updateReportSyncState(
                            report.getId(),
                            "syncing",
                            ""
                    );

                    JSONObject reportPayload = createReportPayload(report);
                    ApiResult reportResult = awaitApi(
                            callback -> apiClient.createProblemReport(
                                    serverFarmerId,
                                    reportPayload,
                                    callback
                            )
                    );

                    if (!reportResult.isSuccess()) {
                        database.updateReportSyncState(
                                report.getId(),
                                "failed",
                                reportResult.error
                        );
                        anyFailure = true;
                        // A failed report has no server ID yet. Continue with
                        // other reports; WorkManager will retry this one.
                        continue;
                    }

                    JSONObject remoteReport =
                            reportResult.response.optJSONObject("report");
                    serverReportId = remoteReport == null
                            ? 0L
                            : remoteReport.optLong("id", 0L);

                    if (serverReportId <= 0L) {
                        database.updateReportSyncState(
                                report.getId(),
                                "failed",
                                "The server returned no report ID."
                        );
                        anyFailure = true;
                        continue;
                    }

                    if (!database.markReportSynced(
                            report.getId(),
                            serverReportId
                    )) {
                        // Don't attempt its photo unless the server ID is
                        // persisted locally. The next run can retry it.
                        database.updateReportSyncState(
                                report.getId(),
                                "failed",
                                "Could not save the server report ID locally."
                        );
                        anyFailure = true;
                        continue;
                    }
                }

                String photoUriString = report.getPhotoUri();
                if (photoUriString == null || photoUriString.trim().isEmpty()) {
                    continue;
                }

                if ("synced".equalsIgnoreCase(report.getPhotoSyncStatus())) {
                    continue;
                }

                database.updatePhotoSyncState(
                        report.getId(),
                        "syncing",
                        ""
                );

                ApiResult photoResult;
                final long reportIdForPhoto = serverReportId;
                try {
                    photoResult = awaitApi(
                            callback -> apiClient.uploadProblemReportPhoto(
                                    serverFarmerId,
                                    reportIdForPhoto,
                                    Uri.parse(photoUriString),
                                    callback
                            )
                    );
                } catch (RuntimeException exception) {
                    database.updatePhotoSyncState(
                            report.getId(),
                            "failed",
                            safeMessage(exception)
                    );
                    anyFailure = true;
                    continue;
                }

                if (!photoResult.isSuccess()
                        || photoResult.response.optJSONObject("photo") == null) {
                    String error = photoResult.error == null
                            ? "The server returned an unexpected photo response."
                            : photoResult.error;
                    database.updatePhotoSyncState(
                            report.getId(),
                            "failed",
                            error
                    );
                    anyFailure = true;
                    continue;
                }

                if (!database.updatePhotoSyncState(
                        report.getId(),
                        "synced",
                        ""
                )) {
                    anyFailure = true;
                }
            }

            return anyFailure ? Result.retry() : Result.success();

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return Result.retry();
        } catch (Exception exception) {
            return Result.retry();
        } finally {
            database.close();
        }
    }

    private JSONObject createFarmerPayload(
            SharedPreferences preferences
    ) throws JSONException {
        JSONObject payload = new JSONObject();
        payload.put("full_name", preferences.getString("full_name", "").trim());
        payload.put("phone", preferences.getString("phone", "").trim());
        payload.put("county", "Murang'a County");
        payload.put("subcounty", preferences.getString("subcounty", "").trim());
        payload.put("ward", preferences.getString("ward", "").trim());
        payload.put(
                "main_enterprise",
                preferences.getString("main_enterprise", "").trim()
        );

        // Farm size is optional. Ignore a malformed optional value rather than
        // blocking report delivery; the profile can be corrected independently.
        String farmSize = preferences.getString("farm_size", "").trim();
        if (!farmSize.isEmpty()) {
            try {
                double acres = Double.parseDouble(farmSize);
                if (!Double.isNaN(acres)
                        && !Double.isInfinite(acres)
                        && acres > 0) {
                    payload.put("farm_size_acres", acres);
                }
            } catch (NumberFormatException ignored) {
                // Omit an invalid optional number.
            }
        }
        return payload;
    }

    private JSONObject createReportPayload(
            FarmProblemReport report
    ) throws JSONException {
        JSONObject payload = new JSONObject();
        payload.put("category", report.getCategory());
        payload.put("crop_affected", report.getCropAffected());
        payload.put("subcounty", report.getSubcounty());
        payload.put("ward", report.getWard() == null ? "" : report.getWard());
        payload.put("description", report.getDescription());
        return payload;
    }

    private static AgriflowApiClient getSharedApiClient(Context context) {
        AgriflowApiClient client = sharedApiClient;
        if (client == null) {
            synchronized (CLIENT_LOCK) {
                client = sharedApiClient;
                if (client == null) {
                    client = new AgriflowApiClient(
                            context.getApplicationContext()
                    );
                    sharedApiClient = client;
                }
            }
        }
        return client;
    }

    private ApiResult awaitApi(ApiCall apiCall) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        JSONObject[] response = new JSONObject[1];
        String[] error = new String[1];

        apiCall.start(new AgriflowApiClient.Callback() {
            @Override
            public void onSuccess(JSONObject result) {
                response[0] = result;
                latch.countDown();
            }

            @Override
            public void onError(String message) {
                error[0] = message == null
                        ? "The county server request failed."
                        : message;
                latch.countDown();
            }
        });

        if (!latch.await(CALLBACK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            return new ApiResult(
                    null,
                    "The county server request timed out."
            );
        }

        return new ApiResult(response[0], error[0]);
    }

    private static String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.trim().isEmpty()
                ? "The photo could not be uploaded."
                : message;
    }

    private interface ApiCall {
        void start(AgriflowApiClient.Callback callback);
    }

    private static final class ApiResult {
        final JSONObject response;
        final String error;

        ApiResult(JSONObject response, String error) {
            this.response = response;
            this.error = error;
        }

        boolean isSuccess() {
            return response != null && error == null;
        }
    }
}
