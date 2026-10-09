package ke.muranga.agriflow;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class CropDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME =
            "agriflow.db";

    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_CROPS =
            "crops";

    private static final String COLUMN_ID =
            "_id";

    private static final String COLUMN_NAME =
            "name";

    private static final String COLUMN_ACREAGE =
            "acreage";

    private static final String COLUMN_PLANTING_DATE =
            "planting_date";

    private static final String COLUMN_EXPECTED_HARVEST =
            "expected_harvest_date";

    private static final String COLUMN_EXPECTED_QUANTITY =
            "expected_quantity";

    private static final String COLUMN_QUANTITY_UNIT =
            "quantity_unit";

    public CropDatabaseHelper(Context context) {

        super(
                context,
                DATABASE_NAME,
                null,
                DATABASE_VERSION
        );
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createTable =
                "CREATE TABLE " + TABLE_CROPS + " (" +
                        COLUMN_ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        COLUMN_NAME +
                        " TEXT NOT NULL, " +

                        COLUMN_ACREAGE +
                        " REAL NOT NULL, " +

                        COLUMN_PLANTING_DATE +
                        " TEXT, " +

                        COLUMN_EXPECTED_HARVEST +
                        " TEXT, " +

                        COLUMN_EXPECTED_QUANTITY +
                        " REAL, " +

                        COLUMN_QUANTITY_UNIT +
                        " TEXT" +
                        ")";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {

        db.execSQL(
                "DROP TABLE IF EXISTS " +
                        TABLE_CROPS
        );

        onCreate(db);
    }

    public long insertCrop(Crop crop) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                COLUMN_NAME,
                crop.getName()
        );

        values.put(
                COLUMN_ACREAGE,
                crop.getAcreage()
        );

        values.put(
                COLUMN_PLANTING_DATE,
                crop.getPlantingDate()
        );

        values.put(
                COLUMN_EXPECTED_HARVEST,
                crop.getExpectedHarvestDate()
        );

        values.put(
                COLUMN_EXPECTED_QUANTITY,
                crop.getExpectedQuantity()
        );

        values.put(
                COLUMN_QUANTITY_UNIT,
                crop.getQuantityUnit()
        );

        return db.insert(
                TABLE_CROPS,
                null,
                values
        );
    }
    public int updateCrop(Crop crop) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                COLUMN_NAME,
                crop.getName()
        );

        values.put(
                COLUMN_ACREAGE,
                crop.getAcreage()
        );

        values.put(
                COLUMN_PLANTING_DATE,
                crop.getPlantingDate()
        );

        values.put(
                COLUMN_EXPECTED_HARVEST,
                crop.getExpectedHarvestDate()
        );

        values.put(
                COLUMN_EXPECTED_QUANTITY,
                crop.getExpectedQuantity()
        );

        values.put(
                COLUMN_QUANTITY_UNIT,
                crop.getQuantityUnit()
        );

        return db.update(
                TABLE_CROPS,
                values,
                COLUMN_ID + " = ?",
                new String[]{
                        String.valueOf(crop.getId())
                }
        );
    }
    public List<Crop> getAllCrops() {

        List<Crop> crops =
                new ArrayList<>();

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_CROPS,
                null,
                null,
                null,
                null,
                null,
                COLUMN_ID + " DESC"
        );

        try {

            while (cursor.moveToNext()) {

                long id =
                        cursor.getLong(
                                cursor.getColumnIndexOrThrow(
                                        COLUMN_ID
                                )
                        );

                String name =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        COLUMN_NAME
                                )
                        );

                double acreage =
                        cursor.getDouble(
                                cursor.getColumnIndexOrThrow(
                                        COLUMN_ACREAGE
                                )
                        );

                String plantingDate =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        COLUMN_PLANTING_DATE
                                )
                        );

                String expectedHarvest =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        COLUMN_EXPECTED_HARVEST
                                )
                        );

                double expectedQuantity =
                        cursor.getDouble(
                                cursor.getColumnIndexOrThrow(
                                        COLUMN_EXPECTED_QUANTITY
                                )
                        );

                String quantityUnit =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        COLUMN_QUANTITY_UNIT
                                )
                        );

                crops.add(
                        new Crop(
                                id,
                                name,
                                acreage,
                                plantingDate,
                                expectedHarvest,
                                expectedQuantity,
                                quantityUnit
                        )
                );
            }

        } finally {
            cursor.close();
        }

        return crops;
    }

    public int getCropCount() {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor =
                db.rawQuery(
                        "SELECT COUNT(*) FROM " +
                                TABLE_CROPS,
                        null
                );

        try {

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }

            return 0;

        } finally {
            cursor.close();
        }
    }

    public void deleteCrop(long id) {

        SQLiteDatabase db =
                getWritableDatabase();

        db.delete(
                TABLE_CROPS,
                COLUMN_ID + " = ?",
                new String[]{
                        String.valueOf(id)
                }
        );
    }
}