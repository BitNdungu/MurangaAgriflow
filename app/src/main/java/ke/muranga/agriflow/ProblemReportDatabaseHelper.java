package ke.muranga.agriflow;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class ProblemReportDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME =
            "agriflow_problem_reports.db";

    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_REPORTS =
            "problem_reports";

    private static final String COLUMN_ID =
            "_id";

    private static final String COLUMN_CATEGORY =
            "category";

    private static final String COLUMN_CROP =
            "crop_affected";

    private static final String COLUMN_SUBCOUNTY =
            "subcounty";

    private static final String COLUMN_WARD =
            "ward";

    private static final String COLUMN_DESCRIPTION =
            "description";

    private static final String COLUMN_PHOTO_URI =
            "photo_uri";

    private static final String COLUMN_CREATED_AT =
            "created_at";

    public ProblemReportDatabaseHelper(Context context) {
        super(
                context,
                DATABASE_NAME,
                null,
                DATABASE_VERSION
        );
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql =
                "CREATE TABLE " + TABLE_REPORTS + " (" +
                        COLUMN_ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        COLUMN_CATEGORY +
                        " TEXT NOT NULL, " +

                        COLUMN_CROP +
                        " TEXT NOT NULL, " +

                        COLUMN_SUBCOUNTY +
                        " TEXT NOT NULL, " +

                        COLUMN_WARD +
                        " TEXT, " +

                        COLUMN_DESCRIPTION +
                        " TEXT NOT NULL, " +

                        COLUMN_PHOTO_URI +
                        " TEXT, " +

                        COLUMN_CREATED_AT +
                        " TEXT NOT NULL, " +

                        COLUMN_SERVER_REPORT_ID +
                        " INTEGER, " +

                        COLUMN_SYNC_STATUS +
                        " TEXT NOT NULL DEFAULT 'pending', " +

                        COLUMN_SYNC_ERROR +
                        " TEXT NOT NULL DEFAULT '', " +

                        COLUMN_PHOTO_SYNC_STATUS +
                        " TEXT NOT NULL DEFAULT 'not_needed', " +

                        COLUMN_PHOTO_SYNC_ERROR +
                        " TEXT NOT NULL DEFAULT ''" +
                        ")";

        db.execSQL(sql);
    }
	private static final String COLUMN_SERVER_REPORT_ID =
        "server_report_id";

    private static final String COLUMN_SYNC_STATUS =
        "sync_status";

    private static final String COLUMN_SYNC_ERROR =
        "sync_error";

    private static final String COLUMN_PHOTO_SYNC_STATUS =
        "photo_sync_status";

    private static final String COLUMN_PHOTO_SYNC_ERROR =
        "photo_sync_error";

    @Override
    public void onUpgrade(
        SQLiteDatabase db,
        int oldVersion,
        int newVersion
    ) {
    if (oldVersion < 2) {
        db.execSQL(
                "ALTER TABLE " + TABLE_REPORTS
                        + " ADD COLUMN "
                        + COLUMN_SERVER_REPORT_ID
                        + " INTEGER"
        );

        db.execSQL(
                "ALTER TABLE " + TABLE_REPORTS
                        + " ADD COLUMN "
                        + COLUMN_SYNC_STATUS
                        + " TEXT NOT NULL DEFAULT 'pending'"
        );

        db.execSQL(
                "ALTER TABLE " + TABLE_REPORTS
                        + " ADD COLUMN "
                        + COLUMN_SYNC_ERROR
                        + " TEXT NOT NULL DEFAULT ''"
        );

        db.execSQL(
                "ALTER TABLE " + TABLE_REPORTS
                        + " ADD COLUMN "
                        + COLUMN_PHOTO_SYNC_STATUS
                        + " TEXT NOT NULL DEFAULT 'not_needed'"
        );

        db.execSQL(
                "ALTER TABLE " + TABLE_REPORTS
                        + " ADD COLUMN "
                        + COLUMN_PHOTO_SYNC_ERROR
                        + " TEXT NOT NULL DEFAULT ''"
        );

        // Existing reports with local photos need uploading.
        db.execSQL(
                "UPDATE " + TABLE_REPORTS
                        + " SET " + COLUMN_PHOTO_SYNC_STATUS
                        + " = 'pending'"
                        + " WHERE " + COLUMN_PHOTO_URI
                        + " IS NOT NULL AND TRIM("
                        + COLUMN_PHOTO_URI + ") <> ''"
        );
    }
}

    public long insertReport(FarmProblemReport report) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(
                COLUMN_CATEGORY,
                report.getCategory()
        );

        values.put(
                COLUMN_CROP,
                report.getCropAffected()
        );

        values.put(
                COLUMN_SUBCOUNTY,
                report.getSubcounty()
        );

        values.put(
                COLUMN_WARD,
                report.getWard()
        );

        values.put(
                COLUMN_DESCRIPTION,
                report.getDescription()
        );

        values.put(
                COLUMN_PHOTO_URI,
                report.getPhotoUri()
        );

        values.put(
                COLUMN_CREATED_AT,
                report.getCreatedAt()
        );

        values.put(
                COLUMN_SYNC_STATUS,
                report.getSyncStatus()
        );

        values.put(
                COLUMN_SYNC_ERROR,
                report.getSyncError()
        );

        values.put(
                COLUMN_PHOTO_SYNC_STATUS,
                report.getPhotoSyncStatus()
        );

        values.put(
                COLUMN_PHOTO_SYNC_ERROR,
                report.getPhotoSyncError()
        );

        return db.insert(
                TABLE_REPORTS,
                null,
                values
        );
    }

    public List<FarmProblemReport> getAllReports() {

        List<FarmProblemReport> reports =
                new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_REPORTS,
                null,
                null,
                null,
                null,
                null,
                COLUMN_ID + " DESC"
        )) {

            while (cursor.moveToNext()) {

                long id = cursor.getLong(
                        cursor.getColumnIndexOrThrow(
                                COLUMN_ID
                        )
                );

                String category = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                COLUMN_CATEGORY
                        )
                );

                String crop = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                COLUMN_CROP
                        )
                );

                String subcounty = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                COLUMN_SUBCOUNTY
                        )
                );

                String ward = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                COLUMN_WARD
                        )
                );

                String description = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                COLUMN_DESCRIPTION
                        )
                );

                String photoUri = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                COLUMN_PHOTO_URI
                        )
                );

                String createdAt = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                COLUMN_CREATED_AT
                        )
                );

                Long serverReportId = null;

                int serverIdColumn =
                        cursor.getColumnIndexOrThrow(
                                COLUMN_SERVER_REPORT_ID
                        );

                if (!cursor.isNull(serverIdColumn)) {
                    serverReportId = cursor.getLong(serverIdColumn);
                }

                String syncStatus = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                COLUMN_SYNC_STATUS
                        )
                );

                String syncError = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                COLUMN_SYNC_ERROR
                        )
                );

                String photoSyncStatus = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                COLUMN_PHOTO_SYNC_STATUS
                        )
                );

                String photoSyncError = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                COLUMN_PHOTO_SYNC_ERROR
                        )
                );

                reports.add(
                        new FarmProblemReport(
                                id,
                                category,
                                crop,
                                subcounty,
                                ward,
                                description,
                                photoUri,
                                createdAt,
                                serverReportId,
                                syncStatus,
                                syncError,
                                photoSyncStatus,
                                photoSyncError
                        )
                );
            }
        }

        return reports;
    }

    public void deleteReport(long id) {

        SQLiteDatabase db = getWritableDatabase();

        db.delete(
                TABLE_REPORTS,
                COLUMN_ID + " = ?",
                new String[]{
                        String.valueOf(id)
                }
        );
    }

    public boolean markReportSynced(
            long localReportId,
            long serverReportId
    ) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_SERVER_REPORT_ID, serverReportId);
        values.put(COLUMN_SYNC_STATUS, "synced");
        values.put(COLUMN_SYNC_ERROR, "");

        return db.update(
                TABLE_REPORTS,
                values,
                COLUMN_ID + " = ?",
                new String[]{
                        String.valueOf(localReportId)
                }
        ) > 0;
    }

    public boolean updateReportSyncState(
            long localReportId,
            String status,
            String error
    ) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_SYNC_STATUS, status);
        values.put(
                COLUMN_SYNC_ERROR,
                error == null ? "" : error
        );

        return db.update(
                TABLE_REPORTS,
                values,
                COLUMN_ID + " = ?",
                new String[]{
                        String.valueOf(localReportId)
                }
        ) > 0;
    }

    public boolean updatePhotoSyncState(
            long localReportId,
            String status,
            String error
    ) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_PHOTO_SYNC_STATUS, status);
        values.put(
                COLUMN_PHOTO_SYNC_ERROR,
                error == null ? "" : error
        );

        return db.update(
                TABLE_REPORTS,
                values,
                COLUMN_ID + " = ?",
                new String[]{
                        String.valueOf(localReportId)
                }
        ) > 0;
    }

}