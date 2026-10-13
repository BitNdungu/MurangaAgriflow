package ke.muranga.agriflow;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;

public class ProblemReportScreen extends ScrollView {

    private static final int REQUEST_CAMERA = 8101;
    private static final int REQUEST_GALLERY = 8102;

    private static final String PREF_PENDING_CAMERA_FILE =
            "problem_report_pending_camera_file";
    private static final String PREF_PENDING_CAMERA_URI =
            "problem_report_pending_camera_uri";

    private final MainActivity activity;
    private final LinearLayout content;
    private final ProblemReportDatabaseHelper database;
    private final SharedPreferences preferences;

    private Uri selectedPhotoUri;
    private Uri pendingCameraUri;
    private File pendingCameraFile;

    private ImageView formPhotoPreview;
    private TextView formPhotoStatus;
    private Button syncButton;
    private TextView syncStatusText;

    public ProblemReportScreen(MainActivity activity) {

        super(activity);

        this.activity = activity;

        database =
                new ProblemReportDatabaseHelper(activity);

        preferences =
                activity.getPreferencesStore();

        content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);

        setFillViewport(true);

        setBackgroundColor(
                activity.color(R.color.background)
        );

        addView(
                content,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        build();
    }

    // ============================================================
    // SCREEN
    // ============================================================

    private void build() {

        LinearLayout header =
                new LinearLayout(activity);

        header.setOrientation(LinearLayout.VERTICAL);

        header.setPadding(
                activity.dp(18),
                activity.dp(20),
                activity.dp(18),
                activity.dp(8)
        );

        header.addView(
                activity.text(
                        "FARM SUPPORT",
                        11,
                        R.color.brand_green,
                        Typeface.BOLD
                )
        );

        header.addView(
                activity.text(
                        "Farm problem reports",
                        25,
                        R.color.text_primary,
                        Typeface.BOLD
                )
        );

        content.addView(header);

        TextView introduction =
                activity.text(
                        "Record pests, suspected diseases, crop damage "
                                + "or other problems affecting your farm.",
                        14,
                        R.color.text_secondary,
                        Typeface.NORMAL
                );

        introduction.setPadding(
                activity.dp(18),
                activity.dp(8),
                activity.dp(18),
                activity.dp(12)
        );

        content.addView(introduction);

        LinearLayout locationCard = activity.card();

        locationCard.addView(
                activity.text(
                        "REPORTING LOCATION",
                        12,
                        R.color.brand_green,
                        Typeface.BOLD
                )
        );

        locationCard.addView(
                activity.text(
                        getReportingLocation(),
                        15,
                        R.color.text_primary,
                        Typeface.NORMAL
                )
        );

        content.addView(locationCard);

        Button newReportButton =
                activity.primaryButton(
                        "Report a farm problem"
                );

        newReportButton.setOnClickListener(
                v -> showNewReportDialog()
        );

        content.addView(
                newReportButton,
                activity.marginParams(
                        18,
                        14,
                        18,
                        14
                )
        );
        // Manual sync is a development aid; farmer builds sync in the background.
        if (BuildConfig.DEBUG) {
            syncButton = activity.secondaryButton("Sync with county");

            syncButton.setOnClickListener(
                    v -> syncWithCounty()
            );

            content.addView(
                    syncButton,
                    activity.marginParams(
                            18,
                            2,
                            18,
                            6
                    )
            );

            syncStatusText = activity.text(
                    preferences.getString(
                            "last_sync_message",
                            "Reports are saved on this phone until synced."
                    ),
                    13,
                    R.color.text_secondary,
                    Typeface.NORMAL
            );

            syncStatusText.setPadding(
                    activity.dp(18),
                    activity.dp(4),
                    activity.dp(18),
                    activity.dp(10)
            );

            content.addView(
                    syncStatusText,
                    activity.marginParams(
                            18,
                            0,
                            18,
                            8
                    )
            );
        }

        content.addView(
                activity.sectionTitle(
                        "MY PREVIOUS REPORTS"
                )
        );

        displayReports();

        LinearLayout note = activity.coloredCard(
                R.color.brand_green_light
        );

        note.addView(
                activity.text(
                        "Saved on this phone",
                        15,
                        R.color.brand_green_dark,
                        Typeface.BOLD
                )
        );

        note.addView(
                activity.text(
                        "Reports and attached photos are saved on this phone first "
                                + "and synchronized automatically when a connection "
                                + "is available. You can keep using the app while "
                                + "synchronization runs.",
                        13,
                        R.color.text_primary,
                        Typeface.NORMAL
                )
        );

        content.addView(
                note,
                activity.marginParams(
                        18,
                        12,
                        18,
                        24
                )
        );
    }

    private void syncWithCounty() {
        String fullName = preferences.getString(
                "full_name", ""
        ).trim();

        String phone = preferences.getString(
                "phone", ""
        ).trim();

        String subcounty = preferences.getString(
                "subcounty", ""
        ).trim();

        String ward = preferences.getString(
                "ward", ""
        ).trim();

        String farmSize = preferences.getString(
                "farm_size", ""
        ).trim();

        String enterprise = preferences.getString(
                "main_enterprise", ""
        ).trim();

        if (fullName.isEmpty()
                || phone.isEmpty()
                || subcounty.isEmpty()) {
            syncStatusText.setText(
                    "Complete your name, phone and sub-county "
                            + "in Farmer Profile before syncing."
            );
            return;
        }

        JSONObject farmerPayload = new JSONObject();

        try {
            farmerPayload.put("full_name", fullName);
            farmerPayload.put("phone", phone);
            farmerPayload.put("county", "Murang'a County");
            farmerPayload.put("subcounty", subcounty);
            farmerPayload.put("ward", ward);
            farmerPayload.put("main_enterprise", enterprise);

            if (!farmSize.isEmpty()) {
                double acres = Double.parseDouble(farmSize);

                if (Double.isNaN(acres)
                        || Double.isInfinite(acres)
                        || acres <= 0) {
                    syncStatusText.setText(
                            "Correct your farm size in Farmer Profile."
                    );
                    return;
                }

                farmerPayload.put("farm_size_acres", acres);
            }

        } catch (JSONException | NumberFormatException exception) {
            syncStatusText.setText(
                    "Check your farmer profile details and try again."
            );
            return;
        }

        syncButton.setEnabled(false);
        syncStatusText.setText("Checking connection to the county server...");

        activity.getApiClient().checkHealth(
                new AgriflowApiClient.Callback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        if (!"ok".equals(
                                response.optString("status")
                        )) {
                            finishSynchronizationWithError(
                                    "The county server is not ready."
                            );
                            return;
                        }

                        syncFarmerProfile(farmerPayload);
                    }

                    @Override
                    public void onError(String message) {
                        finishSynchronizationWithError(
                                "Cannot reach the county server. "
                                        + "Check the connection and try again."
                        );
                    }
                }
        );
    }

    private void syncFarmerProfile(JSONObject payload) {
        long existingServerFarmerId = preferences.getLong(
                "backend_farmer_id",
                0L
        );

        syncStatusText.setText("Synchronizing farmer profile...");

        activity.getApiClient().syncFarmer(
                existingServerFarmerId,
                payload,
                new AgriflowApiClient.Callback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        JSONObject farmer =
                                response.optJSONObject("farmer");

                        if (farmer == null) {
                            finishSynchronizationWithError(
                                    "The server returned an unexpected "
                                            + "farmer response."
                            );
                            return;
                        }

                        long serverFarmerId =
                                farmer.optLong("id", 0L);

                        if (serverFarmerId <= 0) {
                            finishSynchronizationWithError(
                                    "The server did not return a valid "
                                            + "farmer ID."
                            );
                            return;
                        }

                        // Remember the ID assigned by the backend.
                        preferences.edit()
                                .putLong(
                                        "backend_farmer_id",
                                        serverFarmerId
                                )
                                .apply();

                        List<FarmProblemReport> reports =
                                database.getAllReports();

                        syncReportsSequentially(
                                reports,
                                0,
                                serverFarmerId,
                                0,
                                0,
                                0,
                                0
                        );
                    }

                    @Override
                    public void onError(String message) {
                        finishSynchronizationWithError(
                                "Farmer synchronization failed: "
                                        + message
                        );
                    }
                }
        );
    }


    private void syncReportsSequentially(
            List<FarmProblemReport> reports,
            int index,
            long serverFarmerId,
            int submitted,
            int failed,
            int photosUploaded,
            int photosFailed
    ) {
        if (index >= reports.size()) {
            finishSynchronization(
                    reports.size(),
                    submitted,
                    failed,
                    photosUploaded,
                    photosFailed
            );
            return;
        }

        FarmProblemReport report = reports.get(index);
        Long existingServerReportId = report.getServerReportId();

        // A report already on the server must not be created again.
        if (existingServerReportId != null
                && existingServerReportId > 0L) {
            syncPhotoThenContinue(
                    reports,
                    index,
                    serverFarmerId,
                    existingServerReportId,
                    submitted,
                    failed,
                    photosUploaded,
                    photosFailed
            );
            return;
        }

        database.updateReportSyncState(
                report.getId(),
                "syncing",
                ""
        );

        syncStatusText.setText(
                "Submitting report "
                        + (index + 1)
                        + " of "
                        + reports.size()
                        + "..."
        );

        JSONObject reportPayload = new JSONObject();

        try {
            reportPayload.put("category", report.getCategory());
            reportPayload.put(
                    "crop_affected",
                    report.getCropAffected()
            );
            reportPayload.put(
                    "subcounty",
                    report.getSubcounty()
            );
            reportPayload.put(
                    "ward",
                    report.getWard() == null ? "" : report.getWard()
            );
            reportPayload.put(
                    "description",
                    report.getDescription()
            );

        } catch (JSONException exception) {
            database.updateReportSyncState(
                    report.getId(),
                    "failed",
                    exception.getMessage()
            );

            syncReportsSequentially(
                    reports,
                    index + 1,
                    serverFarmerId,
                    submitted,
                    failed + 1,
                    photosUploaded,
                    photosFailed
            );
            return;
        }

        activity.getApiClient().createProblemReport(
                serverFarmerId,
                reportPayload,
                new AgriflowApiClient.Callback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        JSONObject remoteReport =
                                response.optJSONObject("report");

                        long serverReportId =
                                remoteReport == null
                                        ? 0L
                                        : remoteReport.optLong("id", 0L);

                        if (serverReportId <= 0) {
                            database.updateReportSyncState(
                                    report.getId(),
                                    "failed",
                                    "The server returned no report ID."
                            );

                            syncReportsSequentially(
                                    reports,
                                    index + 1,
                                    serverFarmerId,
                                    submitted,
                                    failed + 1,
                                    photosUploaded,
                                    photosFailed
                            );
                            return;
                        }

                        boolean saved = database.markReportSynced(
                                report.getId(),
                                serverReportId
                        );

                        if (!saved) {
                            syncReportsSequentially(
                                    reports,
                                    index + 1,
                                    serverFarmerId,
                                    submitted,
                                    failed + 1,
                                    photosUploaded,
                                    photosFailed
                            );
                            return;
                        }

                        // Upload this report's photo after saving its server ID.
                        syncPhotoThenContinue(
                                reports,
                                index,
                                serverFarmerId,
                                serverReportId,
                                submitted + 1,
                                failed,
                                photosUploaded,
                                photosFailed
                        );
                    }

                    @Override
                    public void onError(String message) {
                        database.updateReportSyncState(
                                report.getId(),
                                "failed",
                                message
                        );

                        syncReportsSequentially(
                                reports,
                                index + 1,
                                serverFarmerId,
                                submitted,
                                failed + 1,
                                photosUploaded,
                                photosFailed
                        );
                    }
                }
        );
    }

    private void syncPhotoThenContinue(
            List<FarmProblemReport> reports,
            int index,
            long serverFarmerId,
            long serverReportId,
            int submitted,
            int failed,
            int photosUploaded,
            int photosFailed
    ) {
        FarmProblemReport report = reports.get(index);
        String photoUriString = report.getPhotoUri();
        String photoStatus = report.getPhotoSyncStatus();

        // No photo, or one already uploaded successfully: move on.
        if (photoUriString == null
                || photoUriString.trim().isEmpty()
                || "synced".equalsIgnoreCase(photoStatus)) {
            syncReportsSequentially(
                    reports,
                    index + 1,
                    serverFarmerId,
                    submitted,
                    failed,
                    photosUploaded,
                    photosFailed
            );
            return;
        }

        database.updatePhotoSyncState(
                report.getId(),
                "syncing",
                ""
        );

        syncStatusText.setText(
                "Uploading photo for report "
                        + (index + 1)
                        + " of "
                        + reports.size()
                        + "..."
        );

        activity.getApiClient().uploadProblemReportPhoto(
                serverFarmerId,
                serverReportId,
                Uri.parse(photoUriString),
                new AgriflowApiClient.Callback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        JSONObject remotePhoto =
                                response.optJSONObject("photo");

                        if (remotePhoto == null) {
                            database.updatePhotoSyncState(
                                    report.getId(),
                                    "failed",
                                    "The server returned an unexpected photo response."
                            );

                            syncReportsSequentially(
                                    reports,
                                    index + 1,
                                    serverFarmerId,
                                    submitted,
                                    failed,
                                    photosUploaded,
                                    photosFailed + 1
                            );
                            return;
                        }

                        boolean saved = database.updatePhotoSyncState(
                                report.getId(),
                                "synced",
                                ""
                        );

                        syncReportsSequentially(
                                reports,
                                index + 1,
                                serverFarmerId,
                                submitted,
                                failed,
                                saved ? photosUploaded + 1 : photosUploaded,
                                saved ? photosFailed : photosFailed + 1
                        );
                    }

                    @Override
                    public void onError(String message) {
                        database.updatePhotoSyncState(
                                report.getId(),
                                "failed",
                                message
                        );

                        syncReportsSequentially(
                                reports,
                                index + 1,
                                serverFarmerId,
                                submitted,
                                failed,
                                photosUploaded,
                                photosFailed + 1
                        );
                    }
                }
        );
    }

    private void finishSynchronization(
            int total,
            int submitted,
            int failed,
            int photosUploaded,
            int photosFailed
    ) {
        String message;

        if (total == 0) {
            message = "Farmer profile synced. No local reports to submit.";
        } else if (failed == 0 && submitted == 0) {
            message = "Sync complete. All reports were already submitted.";
        } else {
            message = "Sync finished: "
                    + submitted
                    + " report(s) submitted; "
                    + failed
                    + " report(s) failed.";
        }

        message += " Photos uploaded: "
                + photosUploaded
                + "; photo uploads failed: "
                + photosFailed
                + ".";

        if (failed > 0 || photosFailed > 0) {
            message += " Failed items remain on this phone for retry.";
        }

        preferences.edit()
                .putString("last_sync_message", message)
                .apply();

        Toast.makeText(
                activity,
                message,
                Toast.LENGTH_LONG
        ).show();

        activity.showScreen(MainActivity.PROBLEM_REPORT);
    }

    private void finishSynchronizationWithError(String message) {
        preferences.edit()
                .putString("last_sync_message", message)
                .apply();

        Toast.makeText(
                activity,
                message,
                Toast.LENGTH_LONG
        ).show();

        activity.showScreen(MainActivity.PROBLEM_REPORT);
    }


    private String getReportingLocation() {

        String subcounty =
                preferences.getString("subcounty", "");

        String ward =
                preferences.getString("ward", "");

        if (subcounty.trim().isEmpty()) {
            return "Sub-county not set. Complete your farmer profile "
                    + "before submitting a report.";
        }

        String location =
                "Murang'a County • " + subcounty;

        if (!ward.trim().isEmpty()) {
            location += " • " + ward;
        }

        return location;
    }

    // ============================================================
    // NEW REPORT
    // ============================================================

    private void showNewReportDialog() {

        String subcounty =
                preferences.getString("subcounty", "").trim();

        if (subcounty.isEmpty()) {

            new AlertDialog.Builder(activity)
                    .setTitle("Farm location required")
                    .setMessage(
                            "Please select your sub-county in your "
                                    + "farmer profile before reporting a problem."
                    )
                    .setNegativeButton(
                            "Cancel",
                            null
                    )
                    .setPositiveButton(
                            "Open profile",
                            (dialog, which) ->
                                    activity.showScreen(
                                            MainActivity.PROFILE
                                    )
                    )
                    .show();

            return;
        }

        selectedPhotoUri = null;
        pendingCameraUri = null;
        pendingCameraFile = null;

        LinearLayout form = new LinearLayout(activity);
        form.setOrientation(LinearLayout.VERTICAL);

        // Problem category
        form.addView(
                activity.fieldLabel("Problem type")
        );

        Spinner categorySpinner = new Spinner(
                activity,
                Spinner.MODE_DROPDOWN
        );

        ArrayAdapter<CharSequence> categoryAdapter =
                ArrayAdapter.createFromResource(
                        activity,
                        R.array.problem_types,
                        android.R.layout.simple_spinner_item
                );

        categoryAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        categorySpinner.setAdapter(categoryAdapter);

        LinearLayout.LayoutParams spinnerParams =
                activity.marginParams(
                        18,
                        0,
                        18,
                        8
                );

        spinnerParams.height = activity.dp(52);

        form.addView(
                categorySpinner,
                spinnerParams
        );

        // Affected crop
        form.addView(
                activity.fieldLabel("Affected crop / enterprise")
        );

        EditText cropField = activity.inputField(
                "e.g. avocado, coffee or tea",
                "",
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        );

        form.addView(cropField);

        // Description
        form.addView(
                activity.fieldLabel("Describe the problem")
        );

        EditText descriptionField = new EditText(activity);

        descriptionField.setHint(
                "What have you noticed? When did it start? "
                        + "How much of the crop is affected?"
        );

        descriptionField.setTextSize(15);

        descriptionField.setTextColor(
                activity.color(R.color.text_primary)
        );

        descriptionField.setHintTextColor(
                activity.color(R.color.text_secondary)
        );

        descriptionField.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                        | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        );

        descriptionField.setSingleLine(false);
        descriptionField.setMinLines(3);
        descriptionField.setMaxLines(6);
        descriptionField.setGravity(
                Gravity.TOP | Gravity.START
        );

        descriptionField.setPadding(
                activity.dp(14),
                activity.dp(12),
                activity.dp(14),
                activity.dp(12)
        );

        descriptionField.setBackground(
                activity.roundedBorderBackground(
                        activity.color(R.color.surface),
                        activity.color(R.color.border),
                        12
                )
        );

        form.addView(
                descriptionField,
                activity.marginParams(
                        18,
                        0,
                        18,
                        10
                )
        );

        // Photo controls
        form.addView(
                activity.fieldLabel("Photo (optional)")
        );

        TextView photoExplanation =
                activity.text(
                        "A photo can help describe the problem, but "
                                + "you can submit without one.",
                        13,
                        R.color.text_secondary,
                        Typeface.NORMAL
                );

        photoExplanation.setPadding(
                activity.dp(18),
                0,
                activity.dp(18),
                activity.dp(8)
        );

        form.addView(photoExplanation);

        LinearLayout photoButtons =
                new LinearLayout(activity);

        photoButtons.setOrientation(LinearLayout.HORIZONTAL);

        Button cameraButton =
                activity.secondaryButton("Take photo");

        Button galleryButton =
                activity.secondaryButton("Choose photo");

        LinearLayout.LayoutParams photoButtonParams =
                new LinearLayout.LayoutParams(
                        0,
                        activity.dp(48),
                        1
                );

        photoButtonParams.setMargins(
                activity.dp(18),
                0,
                activity.dp(4),
                0
        );

        photoButtons.addView(
                cameraButton,
                photoButtonParams
        );

        LinearLayout.LayoutParams galleryParams =
                new LinearLayout.LayoutParams(
                        0,
                        activity.dp(48),
                        1
                );

        galleryParams.setMargins(
                activity.dp(4),
                0,
                activity.dp(18),
                0
        );

        photoButtons.addView(
                galleryButton,
                galleryParams
        );

        form.addView(photoButtons);

        formPhotoStatus =
                activity.text(
                        "No photo attached",
                        13,
                        R.color.text_secondary,
                        Typeface.NORMAL
                );

        formPhotoStatus.setPadding(
                activity.dp(18),
                activity.dp(8),
                activity.dp(18),
                activity.dp(6)
        );

        form.addView(formPhotoStatus);

        formPhotoPreview = new ImageView(activity);

        formPhotoPreview.setScaleType(
                ImageView.ScaleType.CENTER_CROP
        );

        formPhotoPreview.setVisibility(View.GONE);

        form.addView(
                formPhotoPreview,
                activity.marginParams(
                        18,
                        0,
                        18,
                        12
                )
        );

        // Scrollable form
        ScrollView scrollForm = new ScrollView(activity);

        scrollForm.setFillViewport(false);

        scrollForm.addView(
                form,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        AlertDialog dialog =
                new AlertDialog.Builder(activity)
                        .setTitle("Report farm problem")
                        .setView(scrollForm)
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Save report",
                                null
                        )
                        .create();

        cameraButton.setOnClickListener(
                v -> launchCamera()
        );

        galleryButton.setOnClickListener(
                v -> launchGallery()
        );

        dialog.setOnShowListener(d -> {

            Button saveButton =
                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    );

            saveButton.setOnClickListener(v -> {

                if (categorySpinner.getSelectedItemPosition() == 0) {

                    Toast.makeText(
                            activity,
                            "Please select a problem type",
                            Toast.LENGTH_SHORT
                    ).show();

                    categorySpinner.performClick();
                    return;
                }

                String crop =
                        cropField.getText()
                                .toString()
                                .trim();

                if (crop.isEmpty()) {

                    cropField.setError(
                            "Enter the affected crop"
                    );

                    return;
                }

                String description =
                        descriptionField.getText()
                                .toString()
                                .trim();

                if (description.isEmpty()) {

                    descriptionField.setError(
                            "Describe the problem"
                    );

                    return;
                }

                String ward =
                        preferences.getString("ward", "").trim();

                String photo =
                        selectedPhotoUri == null
                                ? ""
                                : selectedPhotoUri.toString();

                String createdAt =
                        new SimpleDateFormat(
                                "yyyy-MM-dd HH:mm",
                                Locale.getDefault()
                        ).format(new Date());

                FarmProblemReport report =
                        new FarmProblemReport(
                                0,
                                categorySpinner.getSelectedItem().toString(),
                                crop,
                                subcounty,
                                ward,
                                description,
                                photo,
                                createdAt
                        );

                long id = database.insertReport(report);

                if (id <= 0) {

                    Toast.makeText(
                            activity,
                            "Could not save the report",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                // Queue background synchronization only after the local save succeeds.
                AgriflowSyncScheduler.enqueueSync(activity);

                dialog.dismiss();

                Toast.makeText(
                        activity,
                        "Problem report saved on this phone",
                        Toast.LENGTH_SHORT
                ).show();

                activity.showScreen(
                        MainActivity.PROBLEM_REPORT
                );
            });
        });

        dialog.setOnDismissListener(d -> {
            formPhotoPreview = null;
            formPhotoStatus = null;
        });

        dialog.show();
    }

    // ============================================================
    // CAMERA AND GALLERY
    // ============================================================

    private void launchCamera() {

        try {

            File directory = new File(
                    activity.getFilesDir(),
                    "problem_photos"
            );

            if (!directory.exists() && !directory.mkdirs()) {

                Toast.makeText(
                        activity,
                        "Could not prepare photo storage",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            pendingCameraFile =
                    File.createTempFile(
                            "problem_",
                            ".jpg",
                            directory
                    );

            pendingCameraUri =
                    FileProvider.getUriForFile(
                            activity,
                            activity.getPackageName()
                                    + ".fileprovider",
                            pendingCameraFile
                    );

            Intent intent =
                    new Intent(
                            MediaStore.ACTION_IMAGE_CAPTURE
                    );

            intent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    pendingCameraUri
            );

            intent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            );

            intent.setClipData(
                    ClipData.newRawUri(
                            "Problem photo",
                            pendingCameraUri
                    )
            );

            // Keep the output location recoverable if Android recreates this screen.
            preferences.edit()
                    .putString(
                            PREF_PENDING_CAMERA_FILE,
                            pendingCameraFile.getAbsolutePath()
                    )
                    .putString(
                            PREF_PENDING_CAMERA_URI,
                            pendingCameraUri.toString()
                    )
                    .apply();

            activity.startActivityForResult(
                    intent,
                    REQUEST_CAMERA
            );

        } catch (Exception e) {

            clearPendingCameraCapture(true);

            Toast.makeText(
                    activity,
                    "Could not open the camera. You can choose a photo instead.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void launchGallery() {

        try {

            Intent intent =
                    new Intent(Intent.ACTION_OPEN_DOCUMENT);

            intent.addCategory(
                    Intent.CATEGORY_OPENABLE
            );

            intent.setType("image/*");

            intent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            );

            activity.startActivityForResult(
                    intent,
                    REQUEST_GALLERY
            );

        } catch (Exception e) {

            Toast.makeText(
                    activity,
                    "Could not open the image picker",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    public void handleActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        if (requestCode != REQUEST_CAMERA
                && requestCode != REQUEST_GALLERY) {
            return;
        }

        // Some camera apps return RESULT_CANCELED even after writing the
        // EXTRA_OUTPUT file. Trust a valid image file rather than resultCode alone.
        if (requestCode == REQUEST_CAMERA) {
            restorePendingCameraCapture();

            boolean usablePhoto = hasUsableCameraFile();

            // A few camera implementations provide only a thumbnail in the
            // returned Intent. Save that thumbnail as a fallback if needed.
            if (!usablePhoto && saveCameraThumbnailFallback(data)) {
                usablePhoto = hasUsableCameraFile();
            }

            if (usablePhoto && pendingCameraUri != null) {
                Uri capturedUri = pendingCameraUri;
                selectedPhotoUri = capturedUri;
                clearPendingCameraCapture(false);
                showPhotoPreview(capturedUri);
            } else {
                clearPendingCameraCapture(true);
                showPhotoPreview(selectedPhotoUri);

                Toast.makeText(
                        activity,
                        "The camera did not provide a usable image. "
                                + "Please try again or choose a photo.",
                        Toast.LENGTH_LONG
                ).show();
            }

            return;
        }

        // A canceled gallery picker should not change the current selection.
        if (resultCode != Activity.RESULT_OK
                || data == null
                || data.getData() == null) {
            return;
        }

        Uri chosenUri = data.getData();

        int grantedFlags = data.getFlags()
                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        if (grantedFlags != 0) {
            try {
                activity.getContentResolver()
                        .takePersistableUriPermission(
                                chosenUri,
                                grantedFlags
                        );
            } catch (SecurityException ignored) {
                // Providers that do not support persistent grants may still
                // allow access for the current session.
            }
        }

        selectedPhotoUri = chosenUri;
        showPhotoPreview(selectedPhotoUri);
    }

    private void restorePendingCameraCapture() {
        if (pendingCameraFile == null) {
            String savedPath = preferences.getString(
                    PREF_PENDING_CAMERA_FILE,
                    ""
            );

            if (!savedPath.trim().isEmpty()) {
                pendingCameraFile = new File(savedPath);
            }
        }

        if (pendingCameraUri == null) {
            String savedUri = preferences.getString(
                    PREF_PENDING_CAMERA_URI,
                    ""
            );

            if (!savedUri.trim().isEmpty()) {
                pendingCameraUri = Uri.parse(savedUri);
            } else if (pendingCameraFile != null) {
                try {
                    pendingCameraUri = FileProvider.getUriForFile(
                            activity,
                            activity.getPackageName() + ".fileprovider",
                            pendingCameraFile
                    );
                } catch (IllegalArgumentException ignored) {
                    // The path may no longer be covered by FileProvider.
                }
            }
        }
    }

    private boolean hasUsableCameraFile() {
        if (pendingCameraFile == null
                || !pendingCameraFile.isFile()
                || pendingCameraFile.length() <= 0) {
            return false;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(
                pendingCameraFile.getAbsolutePath(),
                options
        );

        return options.outWidth > 0 && options.outHeight > 0;
    }

    private boolean saveCameraThumbnailFallback(Intent data) {
        if (data == null
                || data.getExtras() == null
                || pendingCameraFile == null) {
            return false;
        }

        Object thumbnailValue = data.getExtras().get("data");
        if (!(thumbnailValue instanceof Bitmap)) {
            return false;
        }

        Bitmap thumbnail = (Bitmap) thumbnailValue;

        try (FileOutputStream output =
                     new FileOutputStream(pendingCameraFile, false)) {
            return thumbnail.compress(
                    Bitmap.CompressFormat.JPEG,
                    90,
                    output
            );
        } catch (IOException exception) {
            return false;
        }
    }

    private void clearPendingCameraCapture(boolean deleteFile) {
        File fileToClear = pendingCameraFile;

        if (fileToClear == null) {
            String savedPath = preferences.getString(
                    PREF_PENDING_CAMERA_FILE,
                    ""
            );

            if (!savedPath.trim().isEmpty()) {
                fileToClear = new File(savedPath);
            }
        }

        if (deleteFile && fileToClear != null && fileToClear.exists()) {
            // Never delete a selected photo that has been accepted by the form.
            String selectedUri = selectedPhotoUri == null
                    ? ""
                    : selectedPhotoUri.toString();
            String pendingUri = pendingCameraUri == null
                    ? preferences.getString(PREF_PENDING_CAMERA_URI, "")
                    : pendingCameraUri.toString();

            if (!pendingUri.equals(selectedUri)) {
                // This file is only a temporary camera capture.
                fileToClear.delete();
            }
        }

        pendingCameraFile = null;
        pendingCameraUri = null;

        preferences.edit()
                .remove(PREF_PENDING_CAMERA_FILE)
                .remove(PREF_PENDING_CAMERA_URI)
                .apply();
    }

    private void showPhotoPreview(Uri uri) {

        if (formPhotoPreview == null
                || formPhotoStatus == null) {
            return;
        }

        if (uri == null) {

            formPhotoPreview.setVisibility(View.GONE);

            formPhotoStatus.setText(
                    "No photo attached"
            );

            return;
        }

        Bitmap bitmap =
                decodeSampledBitmap(uri, 480, 320);

        if (bitmap == null) {

            formPhotoPreview.setVisibility(View.GONE);

            formPhotoStatus.setText(
                    "Photo selected; preview unavailable"
            );

            return;
        }

        formPhotoPreview.setImageBitmap(bitmap);

        formPhotoPreview.setVisibility(View.VISIBLE);

        formPhotoStatus.setText(
                "Photo attached"
        );
    }

    // Scale image previews to protect memory on low-end phones.
    private Bitmap decodeSampledBitmap(
            Uri uri,
            int requestedWidth,
            int requestedHeight
    ) {

        BitmapFactory.Options bounds =
                new BitmapFactory.Options();

        bounds.inJustDecodeBounds = true;

        try (InputStream input =
                     activity.getContentResolver()
                             .openInputStream(uri)) {

            if (input == null) {
                return null;
            }

            BitmapFactory.decodeStream(
                    input,
                    null,
                    bounds
            );

        } catch (Exception e) {
            return null;
        }

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null;
        }

        int sampleSize = 1;

        while (
                bounds.outWidth / sampleSize > requestedWidth * 2
                        || bounds.outHeight / sampleSize > requestedHeight * 2
        ) {

            sampleSize *= 2;
        }

        BitmapFactory.Options options =
                new BitmapFactory.Options();

        options.inSampleSize = sampleSize;

        try (InputStream input =
                     activity.getContentResolver()
                             .openInputStream(uri)) {

            if (input == null) {
                return null;
            }

            return BitmapFactory.decodeStream(
                    input,
                    null,
                    options
            );

        } catch (Exception e) {
            return null;
        }
    }

    // ============================================================
    // SAVED REPORTS
    // ============================================================

    private void displayReports() {

        List<FarmProblemReport> reports =
                database.getAllReports();

        if (reports.isEmpty()) {

            LinearLayout emptyCard = activity.card();

            emptyCard.addView(
                    activity.text(
                            "No reports recorded",
                            17,
                            R.color.text_primary,
                            Typeface.BOLD
                    )
            );

            emptyCard.addView(
                    activity.text(
                            "Your saved farm problem reports will appear here.",
                            14,
                            R.color.text_secondary,
                            Typeface.NORMAL
                    )
            );

            content.addView(emptyCard);

            return;
        }

        for (FarmProblemReport report : reports) {

            addReportCard(report);
        }
    }

    private void addReportCard(FarmProblemReport report) {

        LinearLayout card = activity.card();

        card.addView(
                activity.text(
                        report.getCategory(),
                        18,
                        R.color.text_primary,
                        Typeface.BOLD
                )
        );

        card.addView(
                activity.detailRow(
                        "Affected crop",
                        report.getCropAffected()
                )
        );

        String location =
                "Murang'a County • " + report.getSubcounty();

        if (report.getWard() != null
                && !report.getWard().trim().isEmpty()) {

            location += " • " + report.getWard();
        }

        card.addView(
                activity.detailRow(
                        "Location",
                        location
                )
        );

        card.addView(
                activity.detailRow(
                        "Date reported",
                        report.getCreatedAt()
                )
        );

        TextView description =
                activity.text(
                        report.getDescription(),
                        14,
                        R.color.text_primary,
                        Typeface.NORMAL
                );

        description.setPadding(
                0,
                0,
                0,
                activity.dp(10)
        );

        card.addView(description);

        String savedPhoto = report.getPhotoUri();

        if (savedPhoto != null && !savedPhoto.trim().isEmpty()) {

            Uri uri = Uri.parse(savedPhoto);

            Bitmap bitmap =
                    decodeSampledBitmap(uri, 480, 320);

            if (bitmap != null) {

                ImageView image = new ImageView(activity);

                image.setImageBitmap(bitmap);

                image.setScaleType(
                        ImageView.ScaleType.CENTER_CROP
                );

                card.addView(
                        image,
                        new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                activity.dp(160)
                        )
                );

            } else {

                TextView photoNote =
                        activity.text(
                                "A photo was attached to this report.",
                                13,
                                R.color.text_secondary,
                                Typeface.NORMAL
                        );

                card.addView(photoNote);
            }

            String photoStatusMessage;
            int photoStatusColor = R.color.text_secondary;
            String photoSyncStatus = report.getPhotoSyncStatus();

            if ("synced".equalsIgnoreCase(photoSyncStatus)) {
                photoStatusMessage = "Photo uploaded to county";
                photoStatusColor = R.color.brand_green;
            } else if ("failed".equalsIgnoreCase(photoSyncStatus)) {
                photoStatusMessage =
                        "Photo upload failed • Will retry when a connection is available.";
            } else if ("syncing".equalsIgnoreCase(photoSyncStatus)) {
                photoStatusMessage = "Uploading photo...";
            } else {
                photoStatusMessage =
                        "Photo saved locally • Awaiting upload";
            }

            TextView photoStatus = activity.text(
                    photoStatusMessage,
                    12,
                    photoStatusColor,
                    Typeface.BOLD
            );

            photoStatus.setPadding(
                    0,
                    activity.dp(6),
                    0,
                    activity.dp(8)
            );

            card.addView(photoStatus);
        }

        String statusMessage;
        int statusColor = R.color.text_secondary;

        if (report.getServerReportId() != null
                && report.getServerReportId() > 0) {
            statusMessage = "Submitted to county • Report #"
                    + report.getServerReportId();
            statusColor = R.color.brand_green;
        } else if ("failed".equals(report.getSyncStatus())) {
            statusMessage = "Sync failed • Will retry when a connection is available.";
        } else if ("syncing".equals(report.getSyncStatus())) {
            statusMessage = "Synchronization in progress...";
        } else {
            statusMessage = "Saved locally • Awaiting synchronization";
        }

        TextView localStatus =
                activity.text(
                        statusMessage,
                        12,
                        statusColor,
                        Typeface.BOLD
                );


        localStatus.setPadding(
                0,
                activity.dp(8),
                0,
                activity.dp(8)
        );

        card.addView(localStatus);

        Button removeButton =
                activity.secondaryButton(
                        "Delete report"
                );

        removeButton.setOnClickListener(
                v -> new AlertDialog.Builder(activity)
                        .setTitle("Delete report?")
                        .setMessage(
                                "This will permanently remove this report "
                                        + "from this phone."
                        )
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Delete",
                                (dialog, which) -> {

                                    database.deleteReport(
                                            report.getId()
                                    );

                                    activity.showScreen(
                                            MainActivity.PROBLEM_REPORT
                                    );
                                }
                        )
                        .show()
        );

        card.addView(removeButton);

        content.addView(card);
    }
}
