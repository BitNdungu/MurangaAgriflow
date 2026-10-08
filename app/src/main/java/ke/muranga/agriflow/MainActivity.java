package ke.muranga.agriflow;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {

    public static final String HOME = "HOME";
    public static final String FARM = "FARM";
    public static final String MARKET = "MARKET";
    public static final String ALERTS = "ALERTS";
    public static final String PROFILE = "PROFILE";

    private static final String PREFS_NAME = "agriflow_profile";
    private static final String APPEARANCE_KEY = "appearance";

    private SharedPreferences preferences;

    private FrameLayout contentFrame;
    private LinearLayout bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        applyStoredAppearance();

        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        );

        buildMainLayout();
        showScreen(HOME);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatusBar();
    }

    // ============================================================
    // APPEARANCE
    // ============================================================

    private void applyStoredAppearance() {

        SharedPreferences prefs = getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        );

        String appearance = prefs.getString(
                APPEARANCE_KEY,
                "system"
        );

        switch (appearance) {

            case "light":
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO
                );
                break;

            case "dark":
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_YES
                );
                break;

            default:
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                );
                break;
        }
    }

    public String getAppearanceLabel() {

        String appearance = preferences.getString(
                APPEARANCE_KEY,
                "system"
        );

        switch (appearance) {

            case "light":
                return "Light";

            case "dark":
                return "Dark";

            default:
                return "System default";
        }
    }

    public void showAppearanceDialog() {

        String[] labels = {
                "System default",
                "Light",
                "Dark"
        };

        String[] values = {
                "system",
                "light",
                "dark"
        };

        String current = preferences.getString(
                APPEARANCE_KEY,
                "system"
        );

        int checked = 0;

        if ("light".equals(current)) {
            checked = 1;
        } else if ("dark".equals(current)) {
            checked = 2;
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Appearance")
                .setSingleChoiceItems(
                        labels,
                        checked,
                        (d, which) -> {

                            preferences.edit()
                                    .putString(
                                            APPEARANCE_KEY,
                                            values[which]
                                    )
                                    .apply();

                            d.dismiss();

                            applyAppearance(values[which]);
                        }
                )
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();
    }

    private void applyAppearance(String appearance) {

        switch (appearance) {

            case "light":
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO
                );
                break;

            case "dark":
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_YES
                );
                break;

            default:
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                );
                break;
        }
    }

    private void updateStatusBar() {

        getWindow().setStatusBarColor(
                color(R.color.status_bar)
        );

        // Keep white status-bar icons.
        int flags = getWindow()
                .getDecorView()
                .getSystemUiVisibility();

        flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;

        getWindow()
                .getDecorView()
                .setSystemUiVisibility(flags);
    }

    // ============================================================
    // MAIN LAYOUT
    // ============================================================

    private void buildMainLayout() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(
                color(R.color.background)
        );

        contentFrame = new FrameLayout(this);
        contentFrame.setBackgroundColor(
                color(R.color.background)
        );

        root.addView(
                contentFrame,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        bottomNavigation = createBottomNavigation();

        root.addView(
                bottomNavigation,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(68)
                )
        );

        setContentView(root);
    }

    // ============================================================
    // SCREEN NAVIGATION
    // ============================================================

    public void showScreen(String screen) {

        View screenView;

        switch (screen) {

            case FARM:
                screenView = new FarmScreen(this);
                break;

            case MARKET:
                screenView = new MarketScreen(this);
                break;

            case ALERTS:
                screenView = new AlertsScreen(this);
                break;

            case PROFILE:
                screenView = new ProfileScreen(this);
                break;

            default:
                screenView = new HomeScreen(this);
                break;
        }

        contentFrame.removeAllViews();

        contentFrame.addView(
                screenView,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        updateBottomNavigation(screen);
    }

    // ============================================================
    // BOTTOM NAVIGATION
    // ============================================================

    private LinearLayout createBottomNavigation() {

        LinearLayout nav = new LinearLayout(this);

        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setBackgroundColor(
                color(R.color.surface)
        );

        nav.addView(
                navButton("HOME", HOME),
                new LinearLayout.LayoutParams(0, dp(58), 1)
        );

        nav.addView(
                navButton("FARM", FARM),
                new LinearLayout.LayoutParams(0, dp(58), 1)
        );

        nav.addView(
                navButton("MARKET", MARKET),
                new LinearLayout.LayoutParams(0, dp(58), 1)
        );

        nav.addView(
                navButton("ALERTS", ALERTS),
                new LinearLayout.LayoutParams(0, dp(58), 1)
        );

        nav.addView(
                navButton("PROFILE", PROFILE),
                new LinearLayout.LayoutParams(0, dp(58), 1)
        );

        return nav;
    }

    private TextView navButton(
            String label,
            String destination
    ) {

        TextView button = new TextView(this);

        button.setText(label);
        button.setGravity(Gravity.CENTER);
        button.setTextSize(11);
        button.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        button.setTextColor(
                color(R.color.text_primary)
        );

        button.setPadding(
                dp(4),
                dp(8),
                dp(4),
                dp(8)
        );

        button.setOnClickListener(
                v -> showScreen(destination)
        );

        return button;
    }

    private void updateBottomNavigation(
            String activeScreen
    ) {

        for (int i = 0;
             i < bottomNavigation.getChildCount();
             i++) {

            View view =
                    bottomNavigation.getChildAt(i);

            if (!(view instanceof TextView)) {
                continue;
            }

            TextView item =
                    (TextView) view;

            String label =
                    item.getText().toString();

            if (label.equals(activeScreen)) {

                item.setTextColor(
                        color(R.color.brand_green)
                );

                item.setBackground(
                        roundedBackground(
                                color(R.color.brand_green_light),
                                14
                        )
                );

            } else {

                item.setTextColor(
                        color(R.color.text_primary)
                );

                item.setBackground(
                        roundedBackground(
                                color(R.color.surface),
                                14
                        )
                );
            }
        }
    }

    // ============================================================
    // SHARED STORAGE
    // ============================================================

    public SharedPreferences getPreferencesStore() {
        return preferences;
    }

    // ============================================================
    // SHARED UI HELPERS
    // ============================================================

    public int color(int resourceId) {
        return getResources().getColor(resourceId);
    }

    public int dp(int value) {

        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;

        return Math.round(value * density);
    }

    public TextView text(
            String value,
            float size,
            int colorResource,
            int typeface
    ) {

        TextView view =
                new TextView(this);

        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(
                color(colorResource)
        );
        view.setTypeface(
                Typeface.DEFAULT,
                typeface
        );

        return view;
    }

    public LinearLayout card() {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(18)
        );

        card.setBackground(
                roundedBackground(
                        color(R.color.surface),
                        18
                )
        );

        card.setLayoutParams(
                marginParams(
                        18,
                        8,
                        18,
                        8
                )
        );

        return card;
    }

    public LinearLayout coloredCard(
            int colorResource
    ) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(18)
        );

        card.setBackground(
                roundedBackground(
                        color(colorResource),
                        18
                )
        );

        return card;
    }

    public LinearLayout informationCard(
            String title,
            String body,
            String buttonText
    ) {

        LinearLayout card = card();

        TextView titleView =
                text(
                        title,
                        17,
                        R.color.text_primary,
                        Typeface.BOLD
                );

        titleView.setPadding(
                0,
                0,
                0,
                dp(5)
        );

        TextView bodyView =
                text(
                        body,
                        14,
                        R.color.text_secondary,
                        Typeface.NORMAL
                );

        bodyView.setPadding(
                0,
                0,
                0,
                dp(8)
        );

        Button button =
                secondaryButton(buttonText);

        card.addView(titleView);
        card.addView(bodyView);
        card.addView(button);

        return card;
    }

    public Button getInformationButton(
            LinearLayout card
    ) {

        return (Button) card.getChildAt(2);
    }

    public TextView sectionTitle(String title) {

        TextView view =
                text(
                        title,
                        12,
                        R.color.text_secondary,
                        Typeface.BOLD
                );

        view.setPadding(
                dp(18),
                dp(22),
                dp(18),
                dp(6)
        );

        return view;
    }

    public EditText inputField(
            String hint,
            String value,
            int inputType
    ) {

        EditText field =
                new EditText(this);

        field.setText(value);
        field.setHint(hint);
        field.setTextSize(15);

        field.setTextColor(
                color(R.color.text_primary)
        );

        field.setHintTextColor(
                color(R.color.text_secondary)
        );

        field.setInputType(inputType);
        field.setSingleLine(true);

        field.setPadding(
                dp(14),
                dp(10),
                dp(14),
                dp(10)
        );

        field.setBackground(
                roundedBorderBackground(
                        color(R.color.surface),
                        color(R.color.border),
                        12
                )
        );

        field.setLayoutParams(
                marginParams(
                        18,
                        0,
                        18,
                        8
                )
        );

        return field;
    }

    public TextView fieldLabel(String label) {

        TextView view =
                text(
                        label,
                        14,
                        R.color.text_primary,
                        Typeface.BOLD
                );

        view.setPadding(
                dp(18),
                dp(6),
                dp(18),
                dp(6)
        );

        return view;
    }

    public Button primaryButton(
            String label
    ) {

        Button button =
                new Button(this);

        button.setText(label);
        button.setTextSize(15);
        button.setTextColor(
                color(R.color.button_text)
        );

        button.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        button.setAllCaps(false);

        button.setBackground(
                roundedBackground(
                        color(R.color.brand_green),
                        14
                )
        );

        return button;
    }

    public Button secondaryButton(
            String label
    ) {

        Button button =
                new Button(this);

        button.setText(label);
        button.setTextSize(14);
        button.setTextColor(
                color(R.color.brand_green)
        );

        button.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        button.setAllCaps(false);

        button.setBackground(
                roundedBorderBackground(
                        color(R.color.brand_green_light),
                        color(R.color.border),
                        12
                )
        );

        return button;
    }

    public Button actionButton(
            String label
    ) {

        Button button =
                new Button(this);

        button.setText(label);
        button.setTextSize(12);
        button.setTextColor(
                color(R.color.brand_green_dark)
        );

        button.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        button.setAllCaps(false);

        button.setBackground(
                roundedBorderBackground(
                        color(R.color.surface),
                        color(R.color.border),
                        12
                )
        );

        return button;
    }

    public LinearLayout detailRow(
            String label,
            String value
    ) {

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.VERTICAL
        );

        TextView labelView =
                text(
                        label.toUpperCase(),
                        11,
                        R.color.text_secondary,
                        Typeface.BOLD
                );

        labelView.setPadding(
                0,
                0,
                0,
                dp(3)
        );

        TextView valueView =
                text(
                        value,
                        16,
                        R.color.text_primary,
                        Typeface.BOLD
                );

        valueView.setPadding(
                0,
                0,
                0,
                dp(10)
        );

        row.addView(labelView);
        row.addView(valueView);

        return row;
    }

    public LinearLayout statRow(
            String label,
            String value
    ) {

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setPadding(
                0,
                dp(5),
                0,
                dp(5)
        );

        TextView left =
                text(
                        label,
                        13,
                        R.color.text_secondary,
                        Typeface.NORMAL
                );

        TextView right =
                text(
                        value,
                        14,
                        R.color.text_primary,
                        Typeface.BOLD
                );

        row.addView(
                left,
                new LinearLayout.LayoutParams(
                        0,
                        dp(32),
                        1
                )
        );

        row.addView(
                right,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        dp(32)
                )
        );

        return row;
    }

    public LinearLayout.LayoutParams marginParams(
            int left,
            int top,
            int right,
            int bottom
    ) {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                dp(left),
                dp(top),
                dp(right),
                dp(bottom)
        );

        return params;
    }

    // ============================================================
    // DRAWABLE HELPERS
    // ============================================================

    public android.graphics.drawable.GradientDrawable
    roundedBackground(
            int backgroundColor,
            int radius
    ) {

        android.graphics.drawable.GradientDrawable drawable =
                new android.graphics.drawable.GradientDrawable();

        drawable.setColor(backgroundColor);
        drawable.setCornerRadius(dp(radius));

        return drawable;
    }

    public android.graphics.drawable.GradientDrawable
    roundedBorderBackground(
            int backgroundColor,
            int borderColor,
            int radius
    ) {

        android.graphics.drawable.GradientDrawable drawable =
                new android.graphics.drawable.GradientDrawable();

        drawable.setColor(backgroundColor);
        drawable.setCornerRadius(dp(radius));
        drawable.setStroke(
                dp(1),
                borderColor
        );

        return drawable;
    }
}