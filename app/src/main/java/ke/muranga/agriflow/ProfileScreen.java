package ke.muranga.agriflow;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileScreen extends ScrollView {

    private final MainActivity activity;
    private final LinearLayout content;
    private final SharedPreferences preferences;

    public ProfileScreen(MainActivity activity) {

        super(activity);

        this.activity = activity;
        this.preferences =
                activity.getPreferencesStore();

        setFillViewport(true);
        setBackgroundColor(
                activity.color(R.color.background)
        );

        content = new LinearLayout(activity);
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

        TextView title =
                activity.text(
                        "Tell AGRIFLOW about your farm",
                        23,
                        R.color.text_primary,
                        Typeface.BOLD
                );

        title.setPadding(
                activity.dp(18),
                activity.dp(18),
                activity.dp(18),
                activity.dp(6)
        );

        content.addView(title);

        TextView explanation =
                activity.text(
                        "This information is currently saved on this "
                                + "phone. We will later connect it to a secure "
                                + "county/backend system.",
                        14,
                        R.color.text_secondary,
                        Typeface.NORMAL
                );

        explanation.setPadding(
                activity.dp(18),
                0,
                activity.dp(18),
                activity.dp(18)
        );

        content.addView(explanation);

        // --------------------------------------------------------
        // PERSONAL DETAILS
        // --------------------------------------------------------

        content.addView(
                activity.sectionTitle(
                        "PERSONAL DETAILS"
                )
        );

        content.addView(
                activity.fieldLabel(
                        "Full name"
                )
        );

        EditText nameField =
                activity.inputField(
                        "Enter your full name",
                        preferences.getString(
                                "full_name",
                                ""
                        ),
                        InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_FLAG_CAP_WORDS
                );

        content.addView(nameField);

        content.addView(
                activity.fieldLabel(
                        "Phone number"
                )
        );

        EditText phoneField =
                activity.inputField(
                        "e.g. 0712 345 678",
                        preferences.getString(
                                "phone",
                                ""
                        ),
                        InputType.TYPE_CLASS_PHONE
                );

        content.addView(phoneField);

        // --------------------------------------------------------
        // FARM LOCATION
        // --------------------------------------------------------

        content.addView(
                activity.sectionTitle(
                        "FARM LOCATION"
                )
        );

        content.addView(
                activity.fieldLabel(
                        "County"
                )
        );

        TextView county =
                activity.text(
                        "Murang'a County",
                        15,
                        R.color.text_primary,
                        Typeface.NORMAL
                );

        county.setBackground(
                activity.roundedBorderBackground(
                        activity.color(
                                R.color.brand_green_light
                        ),
                        activity.color(
                                R.color.border
                        ),
                        12
                )
        );

        county.setPadding(
                activity.dp(14),
                activity.dp(12),
                activity.dp(14),
                activity.dp(12)
        );

        content.addView(
                county,
                activity.marginParams(
                        18,
                        0,
                        18,
                        8
                )
        );

        content.addView(
                activity.fieldLabel(
                        "Sub-county"
                )
        );

        EditText subcountyField =
                activity.inputField(
                        "Enter your sub-county",
                        preferences.getString(
                                "subcounty",
                                ""
                        ),
                        InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_FLAG_CAP_WORDS
                );

        content.addView(subcountyField);

        content.addView(
                activity.fieldLabel(
                        "Ward"
                )
        );

        EditText wardField =
                activity.inputField(
                        "Enter your ward",
                        preferences.getString(
                                "ward",
                                ""
                        ),
                        InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_FLAG_CAP_WORDS
                );

        content.addView(wardField);

        // --------------------------------------------------------
        // FARM DETAILS
        // --------------------------------------------------------

        content.addView(
                activity.sectionTitle(
                        "FARM DETAILS"
                )
        );

        content.addView(
                activity.fieldLabel(
                        "Farm size (acres)"
                )
        );

        EditText farmSizeField =
                activity.inputField(
                        "e.g. 2.5",
                        preferences.getString(
                                "farm_size",
                                ""
                        ),
                        InputType.TYPE_CLASS_NUMBER
                                | InputType.TYPE_NUMBER_FLAG_DECIMAL
                );

        content.addView(farmSizeField);

        content.addView(
                activity.fieldLabel(
                        "Main crop / enterprise"
                )
        );

        EditText enterpriseField =
                activity.inputField(
                        "e.g. tea, coffee, avocado, dairy",
                        preferences.getString(
                                "main_enterprise",
                                ""
                        ),
                        InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                );

        content.addView(enterpriseField);

        // --------------------------------------------------------
        // SAVE PROFILE
        // --------------------------------------------------------

        Button saveButton =
                activity.primaryButton(
                        "Save farmer profile"
                );

        saveButton.setOnClickListener(
                v -> {

                    String name =
                            nameField
                                    .getText()
                                    .toString()
                                    .trim();

                    String phone =
                            phoneField
                                    .getText()
                                    .toString()
                                    .trim();

                    String subcounty =
                            subcountyField
                                    .getText()
                                    .toString()
                                    .trim();

                    String ward =
                            wardField
                                    .getText()
                                    .toString()
                                    .trim();

                    String farmSize =
                            farmSizeField
                                    .getText()
                                    .toString()
                                    .trim();

                    String enterprise =
                            enterpriseField
                                    .getText()
                                    .toString()
                                    .trim();

                    if (name.isEmpty()) {

                        nameField.setError(
                                "Please enter your name"
                        );

                        nameField.requestFocus();

                        return;
                    }

                    if (phone.isEmpty()) {

                        phoneField.setError(
                                "Please enter your phone number"
                        );

                        phoneField.requestFocus();

                        return;
                    }

                    preferences.edit()
                            .putString(
                                    "full_name",
                                    name
                            )
                            .putString(
                                    "phone",
                                    phone
                            )
                            .putString(
                                    "subcounty",
                                    subcounty
                            )
                            .putString(
                                    "ward",
                                    ward
                            )
                            .putString(
                                    "farm_size",
                                    farmSize
                            )
                            .putString(
                                    "main_enterprise",
                                    enterprise
                            )
                            .apply();

                    Toast.makeText(
                            activity,
                            "Farmer profile saved successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    activity.showScreen(
                            MainActivity.PROFILE
                    );
                }
        );

        content.addView(
                saveButton,
                activity.marginParams(
                        18,
                        12,
                        18,
                        18
                )
        );


        // --------------------------------------------------------
        // DEVELOPMENT NOTE
        // --------------------------------------------------------

        LinearLayout noteCard =
                activity.coloredCard(
                        R.color.brand_green_light
                );

        TextView noteTitle =
                activity.text(
                        "AGRIFLOW development note",
                        15,
                        R.color.brand_green_dark,
                        Typeface.BOLD
                );

        noteTitle.setPadding(
                0,
                0,
                0,
                activity.dp(5)
        );

        TextView noteBody =
                activity.text(
                        "Your profile is currently stored locally on "
                                + "this phone. Later we can securely "
                                + "synchronize farmer records, production "
                                + "and market information with a backend."
                                + "Icio ni ciugo cia NDUNGU",
                        13,
                        R.color.text_primary,
                        Typeface.NORMAL
                );

        noteCard.addView(noteTitle);
        noteCard.addView(noteBody);

        content.addView(
                noteCard,
                activity.marginParams(
                        18,
                        0,
                        18,
                        24
                )
        );
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
                        "MY PROFILE",
                        11,
                        R.color.brand_green,
                        Typeface.BOLD
                )
        );

        header.addView(
                activity.text(
                        "Farmer details",
                        27,
                        R.color.text_primary,
                        Typeface.BOLD
                )
        );

        content.addView(header);
    }
}