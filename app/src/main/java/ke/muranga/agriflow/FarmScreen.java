package ke.muranga.agriflow;

import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class FarmScreen extends ScrollView {

    private final MainActivity activity;
    private final LinearLayout content;

    public FarmScreen(MainActivity activity) {

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

        addHeader();

        String farmSize =
                activity.getPreferencesStore()
                        .getString("farm_size", "");

        String enterprise =
                activity.getPreferencesStore()
                        .getString("main_enterprise", "");

        String ward =
                activity.getPreferencesStore()
                        .getString("ward", "");

        String subcounty =
                activity.getPreferencesStore()
                        .getString("subcounty", "");

        TextView intro =
                activity.text(
                        "Your farm information",
                        22,
                        R.color.text_primary,
                        Typeface.BOLD
                );

        intro.setPadding(
                activity.dp(18),
                activity.dp(18),
                activity.dp(18),
                activity.dp(6)
        );

        content.addView(intro);

        LinearLayout farmCard =
                activity.card();

        farmCard.addView(
                activity.detailRow(
                        "Location",
                        buildLocationText(
                                subcounty,
                                ward
                        )
                )
        );

        farmCard.addView(
                activity.detailRow(
                        "Farm size",
                        farmSize.isEmpty()
                                ? "Not recorded"
                                : farmSize + " acres"
                )
        );

        farmCard.addView(
                activity.detailRow(
                        "Main crop / enterprise",
                        enterprise.isEmpty()
                                ? "Not recorded"
                                : enterprise
                )
        );

        content.addView(farmCard);

        Button editProfile =
                activity.primaryButton(
                        "Edit farmer profile"
                );

        editProfile.setOnClickListener(
                v -> activity.showScreen(
                        MainActivity.PROFILE
                )
        );

        content.addView(
                editProfile,
                activity.marginParams(
                        18,
                        18,
                        18,
                        8
                )
        );

        LinearLayout cropsCard =
                activity.informationCard(
                        "Crops & production",
                        "Record your crops, planting dates, "
                                + "expected harvests and production quantities.",
                        "Manage crops"
                );

        activity.getInformationButton(
                cropsCard
        ).setOnClickListener(
                v -> activity.showScreen(
                        MainActivity.CROPS
                )
        );

        content.addView(cropsCard);

        LinearLayout problemCard =
                activity.informationCard(
                        "Farm problem",
                        "Record a disease, pest or other crop "
                                + "problem and request extension assistance.",
                        "Report problem"
                );

        activity.getInformationButton(
                problemCard
        ).setOnClickListener(
                v -> activity.showScreen(
                        MainActivity.ALERTS
                )
        );

        content.addView(problemCard);
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
                        "Farm & production",
                        27,
                        R.color.text_primary,
                        Typeface.BOLD
                )
        );

        content.addView(header);
    }

    private String buildLocationText(
            String subcounty,
            String ward
    ) {

        StringBuilder result =
                new StringBuilder(
                        "Murang'a County"
                );

        if (!subcounty.isEmpty()) {
            result.append(" • ")
                    .append(subcounty);
        }

        if (!ward.isEmpty()) {
            result.append(" • ")
                    .append(ward);
        }

        return result.toString();
    }
}