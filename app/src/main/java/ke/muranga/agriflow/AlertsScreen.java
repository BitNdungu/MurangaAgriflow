package ke.muranga.agriflow;

import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class AlertsScreen extends ScrollView {

    private final MainActivity activity;
    private final LinearLayout content;

    public AlertsScreen(MainActivity activity) {

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
                        "AGRIFLOW ALERTS",
                        11,
                        R.color.brand_green,
                        Typeface.BOLD
                )
        );

        header.addView(
                activity.text(
                        "Useful information",
                        27,
                        R.color.text_primary,
                        Typeface.BOLD
                )
        );

        content.addView(header);

        // --------------------------------------------------------
        // CURRENT ALERT STATE
        // --------------------------------------------------------

        LinearLayout noAlerts =
                activity.card();

        TextView title =
                activity.text(
                        "No new alerts",
                        19,
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
                        "There are no alerts recorded for your "
                                + "farm yet. This area will become "
                                + "a key source of agricultural intelligence.",
                        14,
                        R.color.text_secondary,
                        Typeface.NORMAL
                );

        noAlerts.addView(title);
        noAlerts.addView(message);

        content.addView(noAlerts);

        LinearLayout report =
                activity.informationCard(
                        "Report a crop problem",
                        "Tell AGRIFLOW about a disease, pest, "
                                + "crop loss or other farm problem.",
                        "Report problem"
                );

        activity.getInformationButton(
                report
        ).setOnClickListener(
                v -> activity.showScreen(
                        MainActivity.PROBLEM_REPORT
                )
        );

        content.addView(report);

        LinearLayout extension =
                activity.informationCard(
                        "Extension assistance",
                        "Future versions can connect your report "
                                + "to agricultural extension support.",
                        "Coming next"
                );

        content.addView(extension);
    }
}