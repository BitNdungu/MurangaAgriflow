package ke.muranga.agriflow;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

    // ============================================================
    // MURANG'A AGRIFLOW - FIRST REAL UI
    // ============================================================

    private static final int GREEN = Color.rgb(25, 105, 57);
    private static final int GREEN_DARK = Color.rgb(18, 78, 43);
    private static final int GREEN_LIGHT = Color.rgb(231, 244, 235);

    private static final int GOLD = Color.rgb(224, 165, 48);
    private static final int RED = Color.rgb(190, 67, 67);

    private static final int INK = Color.rgb(27, 43, 35);
    private static final int TEXT = Color.rgb(60, 72, 65);
    private static final int MUTED = Color.rgb(111, 123, 116);

    private static final int BACKGROUND = Color.rgb(247, 249, 247);
    private static final int WHITE = Color.WHITE;
    private static final int BORDER = Color.rgb(225, 231, 226);

    private LinearLayout contentArea;
    private final TextView[] navItems = new TextView[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setStatusBarColor(GREEN_DARK);
        window.setNavigationBarColor(WHITE);

        buildApplicationShell();
        showHome();
        selectTab(0);
    }

    // ============================================================
    // APP SHELL
    // ============================================================

    private void buildApplicationShell() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BACKGROUND);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(BACKGROUND);

        contentArea = new LinearLayout(this);
        contentArea.setOrientation(LinearLayout.VERTICAL);
        contentArea.setPadding(
                dp(20),
                dp(18),
                dp(20),
                dp(24)
        );

        scrollView.addView(contentArea);

        root.addView(
                scrollView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        // Bottom navigation
        LinearLayout bottomNav = new LinearLayout(this);
        bottomNav.setOrientation(LinearLayout.HORIZONTAL);
        bottomNav.setGravity(Gravity.CENTER);
        bottomNav.setPadding(
                dp(8),
                dp(7),
                dp(8),
                dp(7)
        );
        bottomNav.setBackgroundColor(WHITE);

        bottomNav.addView(
                createNavItem(
                        "⌂",
                        "Home",
                        0,
                        v -> {
                            showHome();
                            selectTab(0);
                        }
                )
        );

        bottomNav.addView(
                createNavItem(
                        "✿",
                        "Farm",
                        1,
                        v -> {
                            showFarm();
                            selectTab(1);
                        }
                )
        );

        bottomNav.addView(
                createNavItem(
                        "▣",
                        "Market",
                        2,
                        v -> {
                            showMarket();
                            selectTab(2);
                        }
                )
        );

        bottomNav.addView(
                createNavItem(
                        "!",
                        "Alerts",
                        3,
                        v -> {
                            showAlerts();
                            selectTab(3);
                        }
                )
        );

        bottomNav.addView(
                createNavItem(
                        "●",
                        "Profile",
                        4,
                        v -> {
                            showProfile();
                            selectTab(4);
                        }
                )
        );

        root.addView(
                bottomNav,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(72)
                )
        );

        setContentView(root);
    }

    private TextView createNavItem(
            String icon,
            String label,
            int index,
            View.OnClickListener listener
    ) {

        TextView item = new TextView(this);

        item.setText(icon + "\n" + label);
        item.setGravity(Gravity.CENTER);
        item.setTextSize(11);
        item.setTypeface(null, Typeface.BOLD);
        item.setPadding(
                dp(4),
                dp(5),
                dp(4),
                dp(5)
        );
        item.setLineSpacing(0, 0.9f);
        item.setOnClickListener(listener);

        item.setLayoutParams(
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                )
        );

        navItems[index] = item;

        return item;
    }

    private void selectTab(int selectedIndex) {

        for (int i = 0; i < navItems.length; i++) {

            TextView item = navItems[i];

            if (i == selectedIndex) {

                item.setTextColor(GREEN);

                item.setBackground(
                        roundedBackground(
                                GREEN_LIGHT,
                                18,
                                GREEN_LIGHT
                        )
                );

            } else {

                item.setTextColor(MUTED);

                item.setBackground(
                        roundedBackground(
                                WHITE,
                                18,
                                WHITE
                        )
                );
            }
        }
    }

    // ============================================================
    // HOME
    // ============================================================

    private void showHome() {

        clearContent();

        TextView county = smallLabel("MURANG'A COUNTY");
        contentArea.addView(county);

        TextView appName = heading("AGRIFLOW");
        appName.setTextSize(25);
        contentArea.addView(appName);

        TextView welcome = bodyText(
                "Agricultural intelligence for better farming decisions."
        );

        contentArea.addView(
                welcome,
                marginParams(0, 0, 0, dp(16))
        );

        // Hero
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(
                dp(22),
                dp(22),
                dp(22),
                dp(22)
        );

        hero.setBackground(
                roundedBackground(
                        GREEN,
                        22,
                        GREEN
                )
        );

        TextView heroTitle = textView(
                "Grow with better information.",
                23,
                WHITE
        );

        heroTitle.setTypeface(null, Typeface.BOLD);

        TextView heroBody = textView(
                "Track your farm, understand markets, "
                        + "spot problems early and stay informed.",
                15,
                Color.rgb(226, 241, 230)
        );

        heroBody.setPadding(
                0,
                dp(10),
                0,
                dp(18)
        );

        TextView profileButton = actionButton(
                "Set up your farm profile  →",
                WHITE,
                GREEN_DARK
        );

        profileButton.setOnClickListener(
                v -> showProfile()
        );

        hero.addView(heroTitle);
        hero.addView(heroBody);
        hero.addView(profileButton);

        contentArea.addView(
                hero,
                marginParams(0, 0, 0, dp(22))
        );

        // Quick actions
        addSectionTitle("Quick actions");

        LinearLayout actions = horizontalContainer();

        actions.addView(
                smallAction(
                        "＋",
                        "Add crop",
                        GREEN,
                        v -> showFarm()
                )
        );

        actions.addView(
                smallAction(
                        "!",
                        "Report issue",
                        RED,
                        v -> showAlerts()
                )
        );

        actions.addView(
                smallAction(
                        "▣",
                        "Check market",
                        GOLD,
                        v -> showMarket()
                )
        );

        contentArea.addView(
                actions,
                marginParams(0, 0, 0, dp(14))
        );

        // Farm snapshot
        addSectionTitle("Your farm at a glance");

        LinearLayout snapshot = cardContainer();

        snapshot.addView(
                metricBlock("0", "Crops")
        );

        snapshot.addView(metricDivider());

        snapshot.addView(
                metricBlock("—", "Farm size")
        );

        snapshot.addView(metricDivider());

        snapshot.addView(
                metricBlock("—", "Next harvest")
        );

        contentArea.addView(
                snapshot,
                marginParams(0, 0, 0, dp(14))
        );

        // Attention
        addSectionTitle("Needs your attention");

        contentArea.addView(
                informationCard(
                        "Complete your farmer profile",
                        "Add your location, farm size and enterprises "
                                + "so Agriflow can provide more useful information.",
                        "Get started  →",
                        GREEN,
                        v -> showProfile()
                )
        );

        // Market
        addSectionTitle("Market intelligence");

        contentArea.addView(
                informationCard(
                        "Your market view",
                        "Crop prices and market opportunities will appear "
                                + "here once market data is connected.",
                        "Open market  →",
                        GOLD,
                        v -> showMarket()
                )
        );

        // Alerts
        addSectionTitle("Alerts");

        contentArea.addView(
                informationCard(
                        "Stay informed",
                        "Important agricultural, weather and crop alerts "
                                + "will be surfaced here.",
                        "View alerts  →",
                        RED,
                        v -> showAlerts()
                )
        );
    }

    // ============================================================
    // FARM
    // ============================================================

    private void showFarm() {

        clearContent();

        addPageHeader(
                "My Farm",
                "Your farm records and production information."
        );

        contentArea.addView(
                informationCard(
                        "Farm profile",
                        "Tell Agriflow where your farm is and how it operates.",
                        "Set up profile  →",
                        GREEN,
                        v -> showProfile()
                )
        );

        addSectionTitle("Production");

        contentArea.addView(
                informationCard(
                        "My crops",
                        "Record crops, planting dates and expected harvests.",
                        "Manage crops  →",
                        GREEN,
                        v -> showFarm()
                )
        );

        contentArea.addView(
                informationCard(
                        "Expected harvest",
                        "Your production forecast will eventually help "
                                + "you plan sales and market access.",
                        "View forecast  →",
                        GOLD,
                        v -> showFarm()
                )
        );

        addSectionTitle("Farm support");

        contentArea.addView(
                informationCard(
                        "Report a farm problem",
                        "Capture a crop, pest or disease problem and request assistance.",
                        "Report problem  →",
                        RED,
                        v -> showAlerts()
                )
        );
    }

    // ============================================================
    // MARKET
    // ============================================================

    private void showMarket() {

        clearContent();

        addPageHeader(
                "Market",
                "Make better selling decisions with market intelligence."
        );

        contentArea.addView(
                informationCard(
                        "Market prices",
                        "Crop prices will be shown here as our market data layer is connected.",
                        "View prices  →",
                        GOLD,
                        v -> showMarket()
                )
        );

        addSectionTitle("Market linkage");

        contentArea.addView(
                informationCard(
                        "Find market opportunities",
                        "Available produce can later be matched "
                                + "with buyers and relevant markets.",
                        "Explore markets  →",
                        GREEN,
                        v -> showMarket()
                )
        );

        contentArea.addView(
                informationCard(
                        "My produce",
                        "Record what you expect to harvest so it can "
                                + "support future aggregation and market linkage.",
                        "Add produce  →",
                        GREEN,
                        v -> showFarm()
                )
        );
    }

    // ============================================================
    // ALERTS
    // ============================================================

    private void showAlerts() {

        clearContent();

        addPageHeader(
                "Alerts",
                "Important information that may affect your farm."
        );

        contentArea.addView(
                emptyStateCard(
                        "✓",
                        "No new alerts",
                        "When important agricultural information is available, "
                                + "it will appear here."
                )
        );

        addSectionTitle("Farm support");

        contentArea.addView(
                informationCard(
                        "Report a crop problem",
                        "Pests, disease, unusual crop symptoms or other "
                                + "problems can be reported here.",
                        "Report problem  →",
                        RED,
                        v -> showAlerts()
                )
        );

        contentArea.addView(
                informationCard(
                        "Extension assistance",
                        "Future versions will connect reported problems "
                                + "with agricultural extension support.",
                        "Learn more  →",
                        GREEN,
                        v -> showAlerts()
                )
        );
    }

    // ============================================================
    // PROFILE
    // ============================================================

    private void showProfile() {

        clearContent();

        addPageHeader(
                "My Profile",
                "Your farmer identity and farm information."
        );

        LinearLayout profileCard = cardContainer();

        TextView avatar = textView(
                "F",
                28,
                WHITE
        );

        avatar.setGravity(Gravity.CENTER);
        avatar.setTypeface(null, Typeface.BOLD);

        avatar.setBackground(
                roundedBackground(
                        GREEN,
                        50,
                        GREEN
                )
        );

        profileCard.addView(
                avatar,
                new LinearLayout.LayoutParams(
                        dp(58),
                        dp(58)
                )
        );

        LinearLayout profileInfo = new LinearLayout(this);
        profileInfo.setOrientation(LinearLayout.VERTICAL);
        profileInfo.setPadding(
                dp(16),
                0,
                0,
                0
        );

        TextView name = textView(
                "Farmer profile not yet set up",
                17,
                INK
        );

        name.setTypeface(null, Typeface.BOLD);

        TextView phone = textView(
                "Add your details to personalise Agriflow.",
                14,
                MUTED
        );

        phone.setPadding(
                0,
                dp(5),
                0,
                0
        );

        profileInfo.addView(name);
        profileInfo.addView(phone);

        profileCard.addView(
                profileInfo,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        contentArea.addView(
                profileCard,
                marginParams(0, 0, 0, dp(16))
        );

        addSectionTitle("Profile information");

        contentArea.addView(
                informationCard(
                        "Farmer details",
                        "Name, phone number and basic contact information.",
                        "Add details  →",
                        GREEN,
                        v -> showProfile()
                )
        );

        contentArea.addView(
                informationCard(
                        "Location & farm",
                        "Ward, area, farm size and agricultural enterprises.",
                        "Add farm information  →",
                        GREEN,
                        v -> showFarm()
                )
        );
    }

    // ============================================================
    // REUSABLE COMPONENTS
    // ============================================================

    private void clearContent() {
        contentArea.removeAllViews();
    }

    private void addPageHeader(
            String title,
            String subtitle
    ) {

        contentArea.addView(
                heading(title)
        );

        TextView subtitleView = bodyText(subtitle);

        contentArea.addView(
                subtitleView,
                marginParams(0, 0, 0, dp(18))
        );
    }

    private void addSectionTitle(String text) {

        TextView title = textView(
                text,
                17,
                INK
        );

        title.setTypeface(null, Typeface.BOLD);

        contentArea.addView(
                title,
                marginParams(
                        0,
                        dp(8),
                        0,
                        dp(10)
                )
        );
    }

    private TextView heading(String text) {

        TextView view = textView(
                text,
                28,
                INK
        );

        view.setTypeface(null, Typeface.BOLD);

        return view;
    }

    private TextView smallLabel(String text) {

        TextView view = textView(
                text,
                11,
                GREEN
        );

        view.setTypeface(null, Typeface.BOLD);
        view.setLetterSpacing(0.08f);

        return view;
    }

    private TextView bodyText(String text) {

        return textView(
                text,
                15,
                MUTED
        );
    }

    private TextView textView(
            String text,
            float size,
            int color
    ) {

        TextView view = new TextView(this);

        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);

        return view;
    }

    private TextView actionButton(
            String text,
            int textColor,
            int backgroundColor
    ) {

        TextView button = textView(
                text,
                14,
                textColor
        );

        button.setGravity(Gravity.CENTER);
        button.setTypeface(null, Typeface.BOLD);

        button.setPadding(
                dp(16),
                dp(12),
                dp(16),
                dp(12)
        );

        button.setBackground(
                roundedBackground(
                        backgroundColor,
                        16,
                        backgroundColor
                )
        );

        return button;
    }

    private LinearLayout informationCard(
            String title,
            String description,
            String action,
            int accent,
            View.OnClickListener listener
    ) {

        LinearLayout card = new LinearLayout(this);

        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(18)
        );

        card.setBackground(
                roundedBackground(
                        WHITE,
                        20,
                        BORDER
                )
        );

        TextView titleView = textView(
                title,
                17,
                INK
        );

        titleView.setTypeface(null, Typeface.BOLD);

        TextView descriptionView = textView(
                description,
                14,
                MUTED
        );

        descriptionView.setPadding(
                0,
                dp(7),
                0,
                dp(13)
        );

        TextView actionView = actionButton(
                action,
                WHITE,
                accent
        );

        actionView.setOnClickListener(listener);

        card.addView(titleView);
        card.addView(descriptionView);
        card.addView(actionView);

        card.setLayoutParams(
                marginParams(
                        0,
                        0,
                        0,
                        dp(12)
                )
        );

        return card;
    }

    private LinearLayout smallAction(
            String icon,
            String label,
            int accent,
            View.OnClickListener listener
    ) {

        LinearLayout card = new LinearLayout(this);

        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(
                dp(8),
                dp(13),
                dp(8),
                dp(13)
        );

        card.setBackground(
                roundedBackground(
                        WHITE,
                        18,
                        BORDER
                )
        );

        card.setOnClickListener(listener);

        TextView iconView = textView(
                icon,
                22,
                accent
        );

        iconView.setGravity(Gravity.CENTER);

        TextView labelView = textView(
                label,
                12,
                TEXT
        );

        labelView.setGravity(Gravity.CENTER);
        labelView.setTypeface(null, Typeface.BOLD);

        labelView.setPadding(
                0,
                dp(5),
                0,
                0
        );

        card.addView(iconView);
        card.addView(labelView);

        card.setLayoutParams(
                new LinearLayout.LayoutParams(
                        0,
                        dp(88),
                        1
                )
        );

        return card;
    }

    private LinearLayout horizontalContainer() {

        LinearLayout layout = new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.HORIZONTAL
        );

        layout.setGravity(Gravity.CENTER);

        layout.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        return layout;
    }

    private LinearLayout cardContainer() {

        LinearLayout card = new LinearLayout(this);

        card.setOrientation(
                LinearLayout.HORIZONTAL
        );

        card.setGravity(
                Gravity.CENTER_VERTICAL
        );

        card.setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(18)
        );

        card.setBackground(
                roundedBackground(
                        WHITE,
                        20,
                        BORDER
                )
        );

        return card;
    }

    private LinearLayout metricBlock(
            String value,
            String label
    ) {

        LinearLayout metric = new LinearLayout(this);

        metric.setOrientation(
                LinearLayout.VERTICAL
        );

        metric.setGravity(
                Gravity.CENTER
        );

        metric.setPadding(
                dp(4),
                dp(4),
                dp(4),
                dp(4)
        );

        TextView valueView = textView(
                value,
                24,
                GREEN
        );

        valueView.setGravity(Gravity.CENTER);
        valueView.setTypeface(null, Typeface.BOLD);

        TextView labelView = textView(
                label,
                12,
                MUTED
        );

        labelView.setGravity(Gravity.CENTER);

        labelView.setPadding(
                0,
                dp(4),
                0,
                0
        );

        metric.addView(valueView);
        metric.addView(labelView);

        metric.setLayoutParams(
                new LinearLayout.LayoutParams(
                        0,
                        dp(75),
                        1
                )
        );

        return metric;
    }

    private View metricDivider() {

        View divider = new View(this);

        divider.setBackgroundColor(BORDER);

        divider.setLayoutParams(
                new LinearLayout.LayoutParams(
                        dp(1),
                        dp(44)
                )
        );

        return divider;
    }

    private TextView emptyStateCard(
            String icon,
            String title,
            String description
    ) {

        TextView card = textView(
                icon + "\n\n"
                        + title
                        + "\n"
                        + description,
                15,
                TEXT
        );

        card.setGravity(
                Gravity.CENTER
        );

        card.setPadding(
                dp(20),
                dp(28),
                dp(20),
                dp(28)
        );

        card.setBackground(
                roundedBackground(
                        GREEN_LIGHT,
                        20,
                        GREEN_LIGHT
                )
        );

        card.setLayoutParams(
                marginParams(
                        0,
                        0,
                        0,
                        dp(16)
                )
        );

        return card;
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private GradientDrawable roundedBackground(
            int fill,
            int radiusDp,
            int strokeColor
    ) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(fill);
        drawable.setCornerRadius(
                dp(radiusDp)
        );

        if (strokeColor != fill) {
            drawable.setStroke(
                    dp(1),
                    strokeColor
            );
        }

        return drawable;
    }

    private LinearLayout.LayoutParams marginParams(
            int left,
            int top,
            int right,
            int bottom
    ) {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                left,
                top,
                right,
                bottom
        );

        return params;
    }

    private int dp(int value) {

        return (int) (
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
                        + 0.5f
        );
    }
}