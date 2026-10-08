package ke.muranga.agriflow;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private LinearLayout contentArea;

    private final int GREEN = Color.rgb(46, 125, 50);
    private final int DARK = Color.rgb(35, 35, 35);
    private final int GRAY = Color.rgb(110, 110, 110);
    private final int LIGHT_GRAY = Color.rgb(245, 245, 245);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buildApp();
        showHome();
    }

    private void buildApp() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        // Main content area
        contentArea = new LinearLayout(this);
        contentArea.setOrientation(LinearLayout.VERTICAL);
        contentArea.setPadding(32, 40, 32, 24);

        root.addView(
                contentArea,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        // Bottom navigation
        LinearLayout navigation = new LinearLayout(this);
        navigation.setOrientation(LinearLayout.HORIZONTAL);
        navigation.setGravity(Gravity.CENTER);
        navigation.setBackgroundColor(Color.WHITE);

        navigation.addView(createNavButton("⌂", "Home", v -> showHome()));
        navigation.addView(createNavButton("🌱", "Farm", v -> showFarm()));
        navigation.addView(createNavButton("▣", "Market", v -> showMarket()));
        navigation.addView(createNavButton("!", "Alerts", v -> showAlerts()));
        navigation.addView(createNavButton("●", "Profile", v -> showProfile()));

        root.addView(
                navigation,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        80
                )
        );

        setContentView(root);
    }

    private TextView createNavButton(String icon, String label, View.OnClickListener listener) {
        TextView button = new TextView(this);

        button.setText(icon + "\n" + label);
        button.setTextSize(12);
        button.setTextColor(GRAY);
        button.setGravity(Gravity.CENTER);
        button.setPadding(4, 6, 4, 6);
        button.setOnClickListener(listener);

        button.setLayoutParams(
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                )
        );

        return button;
    }

    private void clearContent() {
        contentArea.removeAllViews();
    }

    private TextView title(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(28);
        view.setTextColor(DARK);
        view.setTypeface(null, Typeface.BOLD);
        view.setPadding(0, 0, 0, 24);
        return view;
    }

    private TextView body(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(17);
        view.setTextColor(GRAY);
        view.setPadding(0, 0, 0, 20);
        return view;
    }

    private TextView card(String heading, String description) {
        TextView card = new TextView(this);

        card.setText(heading + "\n\n" + description);
        card.setTextSize(16);
        card.setTextColor(DARK);
        card.setPadding(24, 24, 24, 24);
        card.setBackgroundColor(LIGHT_GRAY);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(0, 0, 0, 18);
        card.setLayoutParams(params);

        return card;
    }

    private void showHome() {
        clearContent();

        TextView heading = title("MURANG'A AGRIFLOW");
        heading.setTextColor(GREEN);

        contentArea.addView(heading);

        contentArea.addView(
                body("Agricultural intelligence for farmers and the Murang'a agricultural community.")
        );

        contentArea.addView(
                card(
                        "🌾 Farm",
                        "Manage your crops, farm information and expected production."
                )
        );

        contentArea.addView(
                card(
                        "📈 Market",
                        "Check agricultural market information and crop prices."
                )
        );

        contentArea.addView(
                card(
                        "🔔 Alerts",
                        "Stay informed about important agricultural updates."
                )
        );
    }

    private void showFarm() {
        clearContent();

        contentArea.addView(title("My Farm"));
        contentArea.addView(
                body("Your farm information, crops and production will appear here.")
        );

        contentArea.addView(
                card(
                        "Farm Profile",
                        "Add your location, farm size and agricultural enterprises."
                )
        );

        contentArea.addView(
                card(
                        "Crops",
                        "Record crops grown, planting dates and expected harvests."
                )
        );
    }

    private void showMarket() {
        clearContent();

        contentArea.addView(title("Market"));
        contentArea.addView(
                body("Agricultural prices and market opportunities.")
        );

        contentArea.addView(
                card(
                        "Market Prices",
                        "Crop prices will be displayed here."
                )
        );

        contentArea.addView(
                card(
                        "Market Linkage",
                        "Connect available produce with potential buyers and markets."
                )
        );
    }

    private void showAlerts() {
        clearContent();

        contentArea.addView(title("Alerts"));
        contentArea.addView(
                body("Important agricultural information and notifications.")
        );

        contentArea.addView(
                card(
                        "Agricultural Alerts",
                        "Weather, crop, pest and other important alerts will appear here."
                )
        );

        contentArea.addView(
                card(
                        "Farm Problems",
                        "Report crop diseases, pests or other agricultural problems."
                )
        );
    }

    private void showProfile() {
        clearContent();

        contentArea.addView(title("My Profile"));
        contentArea.addView(
                body("Your farmer profile and personal agricultural information.")
        );

        contentArea.addView(
                card(
                        "Farmer Information",
                        "Name, phone number and location."
                )
        );

        contentArea.addView(
                card(
                        "Farm Information",
                        "Farm size, crops and agricultural enterprises."
                )
        );
    }
}