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

    private static final int DATABASE_VERSION = 1;

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
                        " TEXT NOT NULL" +
                        ")";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {
        // Preserve existing reports.
        // Future schema changes must use explicit migrations.
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

                reports.add(
                        new FarmProblemReport(
                                id,
                                category,
                                crop,
                                subcounty,
                                ward,
                                description,
                                photoUri,
                                createdAt
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
}