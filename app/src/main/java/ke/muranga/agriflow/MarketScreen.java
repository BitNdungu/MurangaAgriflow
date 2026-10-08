package ke.muranga.agriflow;

import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class MarketScreen extends ScrollView {

    private final MainActivity activity;
    private final LinearLayout content;

    public MarketScreen(MainActivity activity) {

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
                        "MURANG'A MARKET",
                        11,
                        R.color.brand_green,
                        Typeface.BOLD
                )
        );

        header.addView(
                activity.text(
                        "Market intelligence",
                        27,
                        R.color.text_primary,
                        Typeface.BOLD
                )
        );

        content.addView(header);

        LinearLayout prices =
                activity.informationCard(
                        "Market prices",
                        "Crop prices from selected markets "
                                + "will appear here.",
                        "Prices coming next"
                );

        content.addView(prices);

        LinearLayout linkage =
                activity.informationCard(
                        "Market linkage",
                        "Connect available produce with buyers "
                                + "and markets.",
                        "Coming next"
                );

        content.addView(linkage);

        LinearLayout produce =
                activity.informationCard(
                        "My produce",
                        "Record what you have available for "
                                + "sale and expected quantities.",
                        "Coming next"
                );

        content.addView(produce);
    }
}