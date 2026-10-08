package ke.muranga.agriflow;

import android.app.DatePickerDialog;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CropScreen extends ScrollView {

    private final MainActivity activity;
    private final LinearLayout content;
    private final CropDatabaseHelper database;

    private final SimpleDateFormat storageFormat =
            new SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.US
            );

    private final SimpleDateFormat displayFormat =
            new SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.US
            );

    public CropScreen(MainActivity activity) {

        super(activity);

        this.activity = activity;
        this.database =
                new CropDatabaseHelper(activity);

        setFillViewport(true);

        setBackgroundColor(
                activity.color(R.color.background)
        );

        content =
                new LinearLayout(activity);

        content.setOrientation(
                LinearLayout.VERTICAL
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

    private void build() {

        addHeader();

        TextView introduction =
                activity.text(
                        "Record the crops you grow and the "
                                + "production you expect from them.",
                        14,
                        R.color.text_secondary,
                        Typeface.NORMAL
                );

        introduction.setPadding(
                activity.dp(18),
                activity.dp(12),
                activity.dp(18),
                activity.dp(12)
        );

        content.addView(introduction);

        Button addCrop =
                activity.primaryButton(
                        "Add crop"
                );

        addCrop.setOnClickListener(
                v -> showAddCropDialog()
        );

        content.addView(
                addCrop,
                activity.marginParams(
                        18,
                        4,
                        18,
                        14
                )
        );

        content.addView(
                activity.sectionTitle(
                        "MY CROPS"
                )
        );

        displayCrops();
    }

    private void addHeader() {

        LinearLayout header =
                new LinearLayout(activity);

        header.setOrientation(
                LinearLayout.VERTICAL
        );

        header.setPadding(
                activity.dp(18),
                activity.dp(20),
                activity.dp(18),
                activity.dp(8)
        );

        header.addView(
                activity.text(
                        "MY FARM",
                        11,
                        R.color.brand_green,
                        Typeface.BOLD
                )
        );

        header.addView(
                activity.text(
                        "Crops & production",
                        27,
                        R.color.text_primary,
                        Typeface.BOLD
                )
        );

        content.addView(header);
    }

    private void displayCrops() {

        List<Crop> crops =
                database.getAllCrops();

        if (crops.isEmpty()) {

            LinearLayout emptyCard =
                    activity.card();

            TextView title =
                    activity.text(
                            "No crops added yet",
                            18,
                            R.color.text_primary,
                            Typeface.BOLD
                    );

            title.setPadding(
                    0,
                    0,
                    0,
                    activity.dp(6)
            );

            TextView message =
                    activity.text(
                            "Start by adding your first crop. "
                                    + "The information will later help "
                                    + "AGRIFLOW estimate production and "
                                    + "understand market supply.",
                            14,
                            R.color.text_secondary,
                            Typeface.NORMAL
                    );

            emptyCard.addView(title);
            emptyCard.addView(message);

            content.addView(emptyCard);

            return;
        }

        for (Crop crop : crops) {

            addCropCard(crop);
        }
    }

    private void addCropCard(Crop crop) {

        LinearLayout cropCard =
                activity.card();

        TextView name =
                activity.text(
                        crop.getName(),
                        19,
                        R.color.text_primary,
                        Typeface.BOLD
                );

        name.setPadding(
                0,
                0,
                0,
                activity.dp(8)
        );

        cropCard.addView(name);

        cropCard.addView(
                activity.detailRow(
                        "Area",
                        formatNumber(
                                crop.getAcreage()
                        ) + " acres"
                )
        );

        String planting =
                crop.getPlantingDate();

        if (planting != null &&
                !planting.isEmpty()) {

            cropCard.addView(
                    activity.detailRow(
                            "Planting date",
                            formatStoredDate(
                                    planting
                            )
                    )
            );
        }

        String harvest =
                crop.getExpectedHarvestDate();

        if (harvest != null &&
                !harvest.isEmpty()) {

            cropCard.addView(
                    activity.detailRow(
                            "Expected harvest",
                            formatStoredDate(
                                    harvest
                            )
                    )
            );
        }

        if (crop.getExpectedQuantity() > 0) {

            String quantity =
                    formatNumber(
                            crop.getExpectedQuantity()
                    );

            String unit =
                    crop.getQuantityUnit();

            if (unit == null ||
                    unit.trim().isEmpty()) {

                unit = "units";
            }

            cropCard.addView(
                    activity.detailRow(
                            "Expected production",
                            quantity + " " + unit
                    )
            );
        }

        Button deleteButton =
                activity.secondaryButton(
                        "Remove crop"
                );

        deleteButton.setOnClickListener(
                v -> confirmDelete(crop)
        );

        cropCard.addView(
                deleteButton
        );

        content.addView(cropCard);
    }

    private void showAddCropDialog() {

        LinearLayout form =
                new LinearLayout(activity);

        form.setOrientation(
                LinearLayout.VERTICAL
        );

        form.setPadding(
                activity.dp(4),
                0,
                activity.dp(4),
                0
        );

        TextView nameLabel =
                activity.fieldLabel(
                        "Crop / enterprise"
                );

        EditText nameField =
                activity.inputField(
                        "e.g. avocado",
                        "",
                        InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                );

        TextView areaLabel =
                activity.fieldLabel(
                        "Area (acres)"
                );

        EditText areaField =
                activity.inputField(
                        "e.g. 2.5",
                        "",
                        InputType.TYPE_CLASS_NUMBER |
                                InputType.TYPE_NUMBER_FLAG_DECIMAL
                );

        TextView plantingLabel =
                activity.fieldLabel(
                        "Planting date"
                );

        EditText plantingField =
                activity.inputField(
                        "Select planting date",
                        "",
                        InputType.TYPE_CLASS_TEXT
                );

        plantingField.setFocusable(false);

        TextView harvestLabel =
                activity.fieldLabel(
                        "Expected harvest date"
                );

        EditText harvestField =
                activity.inputField(
                        "Select expected harvest date",
                        "",
                        InputType.TYPE_CLASS_TEXT
                );

        harvestField.setFocusable(false);

        TextView quantityLabel =
                activity.fieldLabel(
                        "Expected production"
                );

        EditText quantityField =
                activity.inputField(
                        "e.g. 3000",
                        "",
                        InputType.TYPE_CLASS_NUMBER |
                                InputType.TYPE_NUMBER_FLAG_DECIMAL
                );

        TextView unitLabel =
                activity.fieldLabel(
                        "Production unit"
                );

        EditText unitField =
                activity.inputField(
                        "e.g. kg, bags, crates",
                        "",
                        InputType.TYPE_CLASS_TEXT
                );

        form.addView(nameLabel);
        form.addView(nameField);

        form.addView(areaLabel);
        form.addView(areaField);

        form.addView(plantingLabel);
        form.addView(plantingField);

        form.addView(harvestLabel);
        form.addView(harvestField);

        form.addView(quantityLabel);
        form.addView(quantityField);

        form.addView(unitLabel);
        form.addView(unitField);

        plantingField.setOnClickListener(
                v -> showDatePicker(plantingField)
        );

        harvestField.setOnClickListener(
                v -> showDatePicker(harvestField)
        );

        ScrollView scrollForm =
                new ScrollView(activity);

        scrollForm.addView(
                form,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        android.app.AlertDialog dialog =
                new android.app.AlertDialog.Builder(activity)
                        .setTitle("Add crop")
                        .setView(scrollForm)
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Save crop",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                d -> {

                    Button saveButton =
                            dialog.getButton(
                                    android.app.AlertDialog.BUTTON_POSITIVE
                            );

                    saveButton.setOnClickListener(
                            v -> {

                                String name =
                                        nameField
                                                .getText()
                                                .toString()
                                                .trim();

                                String areaText =
                                        areaField
                                                .getText()
                                                .toString()
                                                .trim();

                                if (name.isEmpty()) {

                                    nameField.setError(
                                            "Enter the crop name"
                                    );

                                    return;
                                }

                                if (areaText.isEmpty()) {

                                    areaField.setError(
                                            "Enter the acreage"
                                    );

                                    return;
                                }

                                double acreage;

                                try {

                                    acreage =
                                            Double.parseDouble(
                                                    areaText
                                            );

                                } catch (
                                        NumberFormatException e
                                ) {

                                    areaField.setError(
                                            "Enter a valid number"
                                    );

                                    return;
                                }

                                if (acreage <= 0) {

                                    areaField.setError(
                                            "Acreage must be greater than zero"
                                    );

                                    return;
                                }

                                double expectedQuantity = 0;

                                String quantityText =
                                        quantityField
                                                .getText()
                                                .toString()
                                                .trim();

                                if (!quantityText.isEmpty()) {

                                    try {

                                        expectedQuantity =
                                                Double.parseDouble(
                                                        quantityText
                                                );

                                    } catch (
                                            NumberFormatException e
                                    ) {

                                        quantityField.setError(
                                                "Enter a valid number"
                                        );

                                        return;
                                    }
                                }

                                String unit =
                                        unitField
                                                .getText()
                                                .toString()
                                                .trim();

                                Crop crop =
                                        new Crop(
                                                name,
                                                acreage,
                                                plantingField
                                                        .getText()
                                                        .toString()
                                                        .trim(),
                                                harvestField
                                                        .getText()
                                                        .toString()
                                                        .trim(),
                                                expectedQuantity,
                                                unit
                                        );

                                long result =
                                        database.insertCrop(
                                                crop
                                        );

                                if (result == -1) {

                                    Toast.makeText(
                                            activity,
                                            "Could not save crop",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    return;
                                }

                                dialog.dismiss();

                                Toast.makeText(
                                        activity,
                                        "Crop saved successfully",
                                        Toast.LENGTH_SHORT
                                ).show();

                                activity.showScreen(
                                        MainActivity.CROPS
                                );
                            }
                    );
                }
        );

        dialog.show();
    }

    private void showDatePicker(
            EditText target
    ) {

        Calendar calendar =
                Calendar.getInstance();

        DatePickerDialog picker =
                new DatePickerDialog(
                        activity,
                        (view, year, month, dayOfMonth) -> {

                            Calendar selected =
                                    Calendar.getInstance();

                            selected.set(
                                    year,
                                    month,
                                    dayOfMonth
                            );

                            target.setText(
                                    storageFormat.format(
                                            selected.getTime()
                                    )
                            );
                        },
                        calendar.get(
                                Calendar.YEAR
                        ),
                        calendar.get(
                                Calendar.MONTH
                        ),
                        calendar.get(
                                Calendar.DAY_OF_MONTH
                        )
                );

        picker.show();
    }

    private String formatStoredDate(
            String value
    ) {

        try {

            java.util.Date date =
                    storageFormat.parse(value);

            if (date == null) {
                return value;
            }

            return displayFormat.format(date);

        } catch (Exception e) {

            return value;
        }
    }

    private String formatNumber(
            double value
    ) {

        if (value == (long) value) {

            return String.valueOf(
                    (long) value
            );
        }

        return String.format(
                Locale.US,
                "%.2f",
                value
        );
    }

    private void confirmDelete(
            Crop crop
    ) {

        new android.app.AlertDialog.Builder(activity)
                .setTitle("Remove crop?")
                .setMessage(
                        "Remove " +
                                crop.getName() +
                                " from your farm records?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Remove",
                        (dialog, which) -> {

                            database.deleteCrop(
                                    crop.getId()
                            );

                            activity.showScreen(
                                    MainActivity.CROPS
                            );
                        }
                )
                .show();
    }
}