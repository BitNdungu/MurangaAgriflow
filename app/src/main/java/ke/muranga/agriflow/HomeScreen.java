package ke.muranga.agriflow;

import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class HomeScreen extends ScrollView {

    private final MainActivity activity;
    private final LinearLayout content;

    public HomeScreen(MainActivity activity) {

        super(activity);

        this.activity = activity;

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

        String name =
                activity.getPreferencesStore()
                        .getString("full_name", "")
                        .trim();

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

        TextView county =
                activity.text(
                        "MURANG'A COUNTY",
                        11,
                        R.color.brand_green,
                        Typeface.BOLD
                );

        TextView title =
                activity.text(
                        "AGRIFLOW",
                        27,
                        R.color.text_primary,
                        Typeface.BOLD
                );

        header.addView(county);
        header.addView(title);

        content.addView(header);

        TextView welcome =
                activity.text(
                        name.isEmpty()
                                ? "Welcome to MURANG'A AGRIFLOW"
                                : "Welcome, " + name,
                        18,
                        R.color.text_primary,
                        Typeface.BOLD
                );

        welcome.setPadding(
                activity.dp(18),
                activity.dp(20),
                activity.dp(18),
                activity.dp(10)
        );

        content.addView(welcome);

        // --------------------------------------------------------
        // HERO
        // --------------------------------------------------------

        LinearLayout hero =
                activity.coloredCard(
                        R.color.brand_green_dark
                );

        hero.setPadding(
                activity.dp(18),
                activity.dp(20),
                activity.dp(18),
                activity.dp(20)
        );

        TextView heroTitle =
                activity.text(
                        "Grow with better information.",
                        25,
                        R.color.white,
                        Typeface.BOLD
                );

        heroTitle.setPadding(
                0,
                0,
                0,
                activity.dp(8)
        );

        TextView heroBody =
                activity.text(
                        "Track your farm, understand the market, "
                                + "receive useful alerts and get support "
                                + "when problems arise.",
                        15,
                        R.color.white,
                        Typeface.NORMAL
                );

        hero.addView(heroTitle);
        hero.addView(heroBody);

        content.addView(
                hero,
                activity.marginParams(
                        18,
                        8,
                        18,
                        8
                )
        );

        // --------------------------------------------------------
        // QUICK ACTIONS
        // --------------------------------------------------------

        TextView quickTitle =
                activity.sectionTitle(
                        "QUICK ACTIONS"
                );

        content.addView(quickTitle);

        LinearLayout quickActions =
                new LinearLayout(activity);

        quickActions.setOrientation(
                LinearLayout.HORIZONTAL
        );

        Button addCrop =
                activity.actionButton("Add crop");

        Button reportIssue =
                activity.actionButton("Report issue");

        Button checkMarket =
                activity.actionButton("Check market");

        addCrop.setOnClickListener(
                v -> activity.showScreen(
                        MainActivity.FARM
                )
        );

        reportIssue.setOnClickListener(
                v -> activity.showScreen(
                        MainActivity.ALERTS
                )
        );

        checkMarket.setOnClickListener(
                v -> activity.showScreen(
                        MainActivity.MARKET
                )
        );

        LinearLayout.LayoutParams actionParams =
                new LinearLayout.LayoutParams(
                        0,
                        activity.dp(50),
                        1
                );

        actionParams.setMargins(
                activity.dp(18),
                0,
                activity.dp(4),
                0
        );

        quickActions.addView(
                addCrop,
                actionParams
        );

        LinearLayout.LayoutParams middleParams =
                new LinearLayout.LayoutParams(
                        0,
                        activity.dp(50),
                        1
                );

        middleParams.setMargins(
                activity.dp(4),
                0,
                activity.dp(4),
                0
        );

        quickActions.addView(
                reportIssue,
                middleParams
        );

        LinearLayout.LayoutParams lastParams =
                new LinearLayout.LayoutParams(
                        0,
                        activity.dp(50),
                        1
                );

        lastParams.setMargins(
                activity.dp(4),
                0,
                activity.dp(18),
                0
        );

        quickActions.addView(
                checkMarket,
                lastParams
        );

        content.addView(quickActions);

        // --------------------------------------------------------
        // FARM SNAPSHOT
        // --------------------------------------------------------

        content.addView(
                activity.sectionTitle(
                        "FARM SNAPSHOT"
                )
        );

        String farmSize =
                activity.getPreferencesStore()
                        .getString("farm_size", "");

        String enterprise =
                activity.getPreferencesStore()
                        .getString("main_enterprise", "");

        LinearLayout snapshot =
                activity.card();

        snapshot.addView(
                activity.statRow(
                        "Main enterprise",
                        enterprise.isEmpty()
                                ? "Not added yet"
                                : enterprise
                )
        );

        snapshot.addView(
                activity.statRow(
                        "Farm size",
                        farmSize.isEmpty()
                                ? "Not added yet"
                                : farmSize + " acres"
                )
        );

        snapshot.addView(
                activity.statRow(
                        "Next harvest",
                        "Not recorded yet"
                )
        );

        content.addView(snapshot);

        // --------------------------------------------------------
        // PROFILE
        // --------------------------------------------------------

        LinearLayout profileCard =
                activity.informationCard(
                        "Complete your farmer profile",
                        "Add your name, location, farm size and "
                                + "main enterprise. This information "
                                + "will help AGRIFLOW understand your farm.",
                        "Open profile"
                );

        activity.getInformationButton(
                profileCard
        ).setOnClickListener(
                v -> activity.showScreen(
                        MainActivity.PROFILE
                )
        );

        content.addView(profileCard);

        // --------------------------------------------------------
        // MARKET
        // --------------------------------------------------------

        LinearLayout marketCard =
                activity.informationCard(
                        "Market intelligence",
                        "Market prices and buyer linkage will "
                                + "appear here as the platform grows.",
                        "View market"
                );

        activity.getInformationButton(
                marketCard
        ).setOnClickListener(
                v -> activity.showScreen(
                        MainActivity.MARKET
                )
        );

        content.addView(marketCard);

        // --------------------------------------------------------
        // ALERTS
        // --------------------------------------------------------

        LinearLayout alertsCard =
                activity.informationCard(
                        "Agricultural alerts",
                        "Useful notices about weather, crops, "
                                + "markets and county support will "
                                + "appear here.",
                        "View alerts"
                );

        activity.getInformationButton(
                alertsCard
        ).setOnClickListener(
                v -> activity.showScreen(
                        MainActivity.ALERTS
                )
        );

        content.addView(alertsCard);

        TextView bottomSpace =
                activity.text(
                        "",
                        4,
                        R.color.background,
                        Typeface.NORMAL
                );

        content.addView(bottomSpace);
    }
}