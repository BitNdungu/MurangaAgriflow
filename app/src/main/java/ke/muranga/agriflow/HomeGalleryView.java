package ke.muranga.agriflow;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Image-only carousel backed by the county dashboard's public gallery API.
 * It has no visible text or navigation buttons. Horizontal swipes work on every
 * connection type, and timed advancement runs every five seconds for all users.
 * Loaded images and the last gallery manifest are cached so previously viewed
 * photos can still appear offline.
 */
public final class HomeGalleryView extends LinearLayout {

    private static final String CACHE_PREFS = "agriflow_home_gallery";
    private static final String CACHE_MANIFEST_KEY = "active_manifest_v1";
    private static final String CACHE_DIRECTORY = "agriflow_home_gallery";
    private static final String GALLERY_ENDPOINT = "/api/gallery";
    private static final long AUTO_ADVANCE_MS = 5000L;
    // A subtle Ken Burns motion: a 3% scale range avoids aggressive cropping.
    private static final long ZOOM_CYCLE_MS = 5000L;
    private static final float MIN_ZOOM_SCALE = 1.0f;
    private static final float MAX_ZOOM_SCALE = 1.04f;
    private static final long REFRESH_INTERVAL_MS = 5 * 60 * 1000L;
    private static final int CONNECT_TIMEOUT_MS = 7000;
    private static final int READ_TIMEOUT_MS = 12000;
    private static final int MAX_MANIFEST_BYTES = 512 * 1024;
    private static final int MAX_IMAGE_BYTES = 8 * 1024 * 1024;

    private static final ExecutorService IO_EXECUTOR =
            Executors.newFixedThreadPool(2);

    private final MainActivity activity;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final FrameLayout imageFrame;
    private final ImageView imageView;
    private final LinearLayout indicatorRow;
    private final View[] indicators = new View[3];
    private final File cacheDirectory;
    private final ArrayList<GalleryImage> images = new ArrayList<>();
    private AnimatorSet zoomAnimator;

    private int currentIndex = 0;
    private int imageRequestToken = 0;
    private boolean refreshInProgress = false;
    private boolean imageLoading = false;
    private float touchStartX;
    private float touchStartY;
    private boolean horizontalSwipeStarted;

    private final Runnable autoAdvanceRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isAttachedToWindow()) {
                return;
            }

            if (images.size() > 1 && !imageLoading) {
                advanceToIndex((currentIndex + 1) % images.size());
                return;
            }

            // If an image is still loading, retry after the same five-second interval.
            if (images.size() > 1) {
                mainHandler.postDelayed(this, AUTO_ADVANCE_MS);
            }
        }
    };

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isAttachedToWindow()) {
                return;
            }

            refreshGalleryFromServer();
            mainHandler.postDelayed(this, REFRESH_INTERVAL_MS);
        }
    };

    public HomeGalleryView(MainActivity activity) {
        super(activity);
        this.activity = activity;
        this.cacheDirectory = new File(
                activity.getCacheDir(),
                CACHE_DIRECTORY
        );

        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setVisibility(View.GONE);
        setClipChildren(false);
        setClipToPadding(false);
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        setContentDescription("Murang'a County agricultural photo gallery");

        imageFrame = new FrameLayout(activity);
        GradientDrawable frameBackground = new GradientDrawable();
        frameBackground.setColor(activity.color(R.color.surface));
        frameBackground.setCornerRadius(activity.dp(22));
        frameBackground.setStroke(
                Math.max(1, activity.dp(1)),
                activity.color(R.color.border)
        );
        imageFrame.setBackground(frameBackground);
        imageFrame.setClipToOutline(true);
        imageFrame.setElevation(activity.dp(3));
        // A one-dp inset keeps the rounded theme border visible around the photo.
        imageFrame.setPadding(
                activity.dp(1),
                activity.dp(1),
                activity.dp(1),
                activity.dp(1)
        );
        imageFrame.setContentDescription("Agricultural photo");

        imageView = new ImageView(activity);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setAdjustViewBounds(false);
        imageView.setBackgroundColor(activity.color(R.color.surface));
        imageView.setContentDescription("Agricultural gallery image");

        FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        imageFrame.addView(imageView, imageParams);

        LayoutParams frameParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                calculatePortraitHeightPx()
        );
        addView(imageFrame, frameParams);

        indicatorRow = new LinearLayout(activity);
        indicatorRow.setOrientation(LinearLayout.HORIZONTAL);
        indicatorRow.setGravity(Gravity.CENTER);
        indicatorRow.setPadding(
                0,
                activity.dp(11),
                0,
                activity.dp(5)
        );
        indicatorRow.setContentDescription("Gallery position indicator");

        for (int i = 0; i < indicators.length; i++) {
            View dot = new View(activity);
            indicators[i] = dot;

            LayoutParams dotParams = new LayoutParams(
                    activity.dp(7),
                    activity.dp(7)
            );
            dotParams.setMargins(
                    activity.dp(5),
                    0,
                    activity.dp(5),
                    0
            );
            indicatorRow.addView(dot, dotParams);
        }

        addView(
                indicatorRow,
                new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        imageView.setOnTouchListener(this::handleSwipeTouch);

        loadCachedManifest();
        refreshGalleryFromServer();
    }

    private int calculatePortraitHeightPx() {
        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        float density = metrics.density <= 0 ? 1f : metrics.density;
        int screenWidthDp = Math.round(metrics.widthPixels / density);
        int availableWidthDp = Math.max(260, screenWidthDp - 36);
        int heightDp = Math.round(availableWidthDp * 1.12f);
        heightDp = Math.max(300, Math.min(390, heightDp));
        return activity.dp(heightDp);
    }

    private boolean handleSwipeTouch(View view, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                touchStartX = event.getX();
                touchStartY = event.getY();
                horizontalSwipeStarted = false;
                return true;

            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX() - touchStartX;
                float moveY = event.getY() - touchStartY;
                if (Math.abs(moveX) > activity.dp(18)
                        && Math.abs(moveX) > Math.abs(moveY)) {
                    horizontalSwipeStarted = true;
                    ViewParentHelper.disallowParentIntercept(view, true);
                }
                return true;

            case MotionEvent.ACTION_UP:
                float deltaX = event.getX() - touchStartX;
                float deltaY = event.getY() - touchStartY;
                boolean isSwipe = horizontalSwipeStarted
                        && Math.abs(deltaX) >= activity.dp(42)
                        && Math.abs(deltaX) > Math.abs(deltaY);

                ViewParentHelper.disallowParentIntercept(view, false);

                if (isSwipe && images.size() > 1) {
                    if (deltaX < 0) {
                        advanceToIndex((currentIndex + 1) % images.size());
                    } else {
                        advanceToIndex((currentIndex - 1 + images.size()) % images.size());
                    }
                }
                horizontalSwipeStarted = false;
                return true;

            case MotionEvent.ACTION_CANCEL:
                ViewParentHelper.disallowParentIntercept(view, false);
                horizontalSwipeStarted = false;
                return true;

            default:
                return true;
        }
    }

    private void loadCachedManifest() {
        String cachedJson = activity.getSharedPreferences(
                CACHE_PREFS,
                Context.MODE_PRIVATE
        ).getString(CACHE_MANIFEST_KEY, "");

        if (cachedJson == null || cachedJson.trim().isEmpty()) {
            return;
        }

        try {
            JSONArray array = new JSONArray(cachedJson);
            List<GalleryImage> cachedItems = parseManifest(array, true);
            replaceItems(cachedItems, false);
        } catch (JSONException ignored) {
            // A corrupt cache must not prevent the live gallery from loading.
        }
    }

    private void refreshGalleryFromServer() {
        if (refreshInProgress) {
            return;
        }
        refreshInProgress = true;

        IO_EXECUTOR.execute(() -> {
            List<GalleryImage> remoteItems = null;
            String serializedManifest = null;

            try {
                JSONObject response = getJson(
                        AgriflowApiClient.DEFAULT_BASE_URL + GALLERY_ENDPOINT
                );
                JSONArray remoteArray = response.optJSONArray("images");
                if (remoteArray == null) {
                    throw new IOException("Gallery response did not contain an images list.");
                }
                remoteItems = parseManifest(remoteArray, false);
                serializedManifest = serialiseManifest(remoteItems).toString();
            } catch (Exception ignored) {
                // Keep the previously cached images when the backend is unavailable.
            }

            final List<GalleryImage> loadedItems = remoteItems;
            final String cachedManifest = serializedManifest;

            mainHandler.post(() -> {
                refreshInProgress = false;
                if (loadedItems == null) {
                    return;
                }

                activity.getSharedPreferences(
                        CACHE_PREFS,
                        Context.MODE_PRIVATE
                ).edit()
                        .putString(CACHE_MANIFEST_KEY, cachedManifest)
                        .apply();

                replaceItems(loadedItems, true);
            });
        });
    }

    private List<GalleryImage> parseManifest(
            JSONArray array,
            boolean requireCachedImage
    ) {
        ArrayList<GalleryImage> parsed = new ArrayList<>();
        Set<Long> seenIds = new HashSet<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.optJSONObject(i);
            if (object == null) {
                continue;
            }

            long id = object.optLong("id", 0L);
            String path = object.optString("url", "");

            // Only accept paths from the expected image endpoint.
            if (id <= 0
                    || !path.startsWith("/api/gallery/")
                    || !path.endsWith("/image")
                    || !seenIds.add(id)) {
                continue;
            }

            GalleryImage item = new GalleryImage(id, path);
            if (requireCachedImage) {
                File cached = getCachedImageFile(id);
                if (!cached.isFile() || cached.length() <= 0) {
                    continue;
                }
            }
            parsed.add(item);
        }

        return parsed;
    }

    private JSONArray serialiseManifest(List<GalleryImage> galleryImages) {
        JSONArray array = new JSONArray();
        for (GalleryImage image : galleryImages) {
            JSONObject object = new JSONObject();
            try {
                object.put("id", image.id);
                object.put("url", image.path);
                array.put(object);
            } catch (JSONException ignored) {
                // These two primitive fields cannot normally fail to serialize.
            }
        }
        return array;
    }

    private void replaceItems(List<GalleryImage> newItems, boolean keepCurrentImage) {
        long previousId = 0L;
        long previousFirstId = images.isEmpty() ? 0L : images.get(0).id;
        int previousIndex = currentIndex;
        if (currentIndex >= 0 && currentIndex < images.size()) {
            previousId = images.get(currentIndex).id;
        }

        images.clear();
        images.addAll(newItems);

        if (images.isEmpty()) {
            currentIndex = 0;
            imageLoading = false;
            imageRequestToken++;
            stopZoomAnimation();
            imageView.animate().cancel();
            setVisibility(View.GONE);
            mainHandler.removeCallbacks(autoAdvanceRunnable);
            return;
        }

        setVisibility(View.VISIBLE);
        currentIndex = 0;

        if (keepCurrentImage && previousId > 0) {
            boolean newLatestImage = previousIndex == 0
                    && previousFirstId > 0
                    && images.get(0).id != previousFirstId;

            if (!newLatestImage) {
                for (int i = 0; i < images.size(); i++) {
                    if (images.get(i).id == previousId) {
                        currentIndex = i;
                        break;
                    }
                }
            }
        }

        updateIndicators();
        renderCurrentImage();
    }

    private void renderCurrentImage() {
        if (images.isEmpty()
                || currentIndex < 0
                || currentIndex >= images.size()) {
            return;
        }

        updateIndicators();

        GalleryImage selected = images.get(currentIndex);
        int requestToken = ++imageRequestToken;
        imageLoading = true;

        IO_EXECUTOR.execute(() -> {
            Bitmap bitmap = null;
            File imageFile = getCachedImageFile(selected.id);

            try {
                if (!imageFile.isFile() || imageFile.length() <= 0) {
                    byte[] imageBytes = getBytes(
                            AgriflowApiClient.DEFAULT_BASE_URL + selected.path,
                            MAX_IMAGE_BYTES
                    );
                    saveImageCache(imageFile, imageBytes);
                }
                bitmap = decodeSampledBitmap(imageFile);
                if (bitmap == null) {
                    // Replace a corrupt partial cache entry with a clean download.
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                    byte[] imageBytes = getBytes(
                            AgriflowApiClient.DEFAULT_BASE_URL + selected.path,
                            MAX_IMAGE_BYTES
                    );
                    saveImageCache(imageFile, imageBytes);
                    bitmap = decodeSampledBitmap(imageFile);
                }
            } catch (Exception ignored) {
                // If the network is unavailable, a cached image is still shown when present.
            }

            final Bitmap loadedBitmap = bitmap;
            mainHandler.post(() -> {
                if (requestToken != imageRequestToken || !isAttachedToWindow()) {
                    return;
                }

                imageLoading = false;
                if (loadedBitmap != null) {
                    imageView.animate().cancel();
                    stopZoomAnimation();
                    imageView.setAlpha(0f);
                    imageView.setImageBitmap(loadedBitmap);
                    startContinuousZoomAnimation();
                    imageView.animate()
                            .alpha(1f)
                            .setDuration(480L)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();
                    imageView.setContentDescription("Agricultural gallery photograph");
                } else {
                    imageView.animate().cancel();
                    imageView.setImageDrawable(null);
                    imageView.setAlpha(1f);
                    imageView.setScaleX(1f);
                    imageView.setScaleY(1f);
                }

                // Start a full five-second dwell after the current load attempt
                // finishes, so download time does not shorten the viewing interval.
                updateAutoAdvanceSchedule();
            });
        });
    }

    /**
     * Keeps the visible photograph moving gently for its whole on-screen life.
     * Each image starts at a random end of a small scale range, then reverses
     * forever until the next image is displayed. The scale never drops below
     * 1.0, so the ImageView never exposes empty edges.
     */
    private void startContinuousZoomAnimation() {
        stopZoomAnimation();

        boolean zoomOutFirst = Math.random() < 0.5d;
        float startScale = zoomOutFirst ? MAX_ZOOM_SCALE : MIN_ZOOM_SCALE;
        float endScale = zoomOutFirst ? MIN_ZOOM_SCALE : MAX_ZOOM_SCALE;

        imageView.setScaleX(startScale);
        imageView.setScaleY(startScale);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(
                imageView,
                View.SCALE_X,
                startScale,
                endScale
        );
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(
                imageView,
                View.SCALE_Y,
                startScale,
                endScale
        );

        scaleX.setDuration(ZOOM_CYCLE_MS);
        scaleY.setDuration(ZOOM_CYCLE_MS);
        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.setRepeatMode(ValueAnimator.REVERSE);
        scaleY.setRepeatMode(ValueAnimator.REVERSE);
        scaleX.setInterpolator(new LinearInterpolator());
        scaleY.setInterpolator(new LinearInterpolator());

        zoomAnimator = new AnimatorSet();
        zoomAnimator.playTogether(scaleX, scaleY);
        zoomAnimator.start();
    }

    private void stopZoomAnimation() {
        if (zoomAnimator != null) {
            zoomAnimator.cancel();
            zoomAnimator = null;
        }
    }

    private void updateIndicators() {
        // The centre dot remains selected at all times, deliberately hiding the
        // total number of gallery images. The side dots animate to suggest motion.
        for (int i = 0; i < indicators.length; i++) {
            GradientDrawable dot = new GradientDrawable();
            dot.setShape(GradientDrawable.OVAL);
            dot.setColor(activity.color(
                    i == 1 ? R.color.brand_green : R.color.border
            ));
            indicators[i].setBackground(dot);
            int size = activity.dp(i == 1 ? 8 : 7);
            ViewGroup.LayoutParams params = indicators[i].getLayoutParams();
            params.width = size;
            params.height = size;
            indicators[i].setLayoutParams(params);
        }
    }

    /**
     * A compact, looping three-dot motion: the left dot slips away, the right
     * dot pops in, and the selected green centre dot gently pulses without
     * leaving the centre position.
     */
    private void animateIndicatorCycle() {
        if (indicators.length != 3) {
            return;
        }

        final View left = indicators[0];
        final View centre = indicators[1];
        final View right = indicators[2];

        left.animate().cancel();
        centre.animate().cancel();
        right.animate().cancel();

        left.setAlpha(1f);
        left.setTranslationX(0f);
        left.setScaleX(1f);
        left.setScaleY(1f);

        right.setAlpha(0f);
        right.setTranslationX(activity.dp(7));
        right.setScaleX(0.35f);
        right.setScaleY(0.35f);

        left.animate()
                .alpha(0f)
                .translationX(-activity.dp(7))
                .scaleX(0.45f)
                .scaleY(0.45f)
                .setDuration(230L)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    left.setTranslationX(0f);
                    left.setScaleX(1f);
                    left.setScaleY(1f);
                    left.postDelayed(() -> left.animate()
                            .alpha(1f)
                            .setDuration(200L)
                            .start(), 320L);
                })
                .start();

        right.animate()
                .alpha(1f)
                .translationX(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(380L)
                .setInterpolator(new OvershootInterpolator(1.15f))
                .start();

        centre.setScaleX(1f);
        centre.setScaleY(1f);
        centre.animate()
                .scaleX(1.22f)
                .scaleY(1.22f)
                .setDuration(150L)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> centre.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(220L)
                        .start())
                .start();
    }

    private void advanceToIndex(int nextIndex) {
        if (images.size() < 2) {
            return;
        }

        currentIndex = nextIndex;
        animateIndicatorCycle();
        renderCurrentImage();
    }

    private void updateAutoAdvanceSchedule() {
        mainHandler.removeCallbacks(autoAdvanceRunnable);
        if (images.size() > 1 && isAttachedToWindow()) {
            mainHandler.postDelayed(autoAdvanceRunnable, AUTO_ADVANCE_MS);
        }
    }

    private File getCachedImageFile(long imageId) {
        return new File(cacheDirectory, "gallery_" + imageId + ".img");
    }

    private void saveImageCache(File destination, byte[] bytes) throws IOException {
        if (!cacheDirectory.exists() && !cacheDirectory.mkdirs()) {
            throw new IOException("Could not prepare gallery image cache.");
        }

        File temporary = new File(destination.getParentFile(), destination.getName() + ".tmp");
        try (FileOutputStream output = new FileOutputStream(temporary)) {
            output.write(bytes);
            output.flush();
        }

        if (destination.exists() && !destination.delete()) {
            temporary.delete();
            throw new IOException("Could not update the gallery image cache.");
        }

        if (!temporary.renameTo(destination)) {
            try (FileInputStream input = new FileInputStream(temporary);
                 FileOutputStream output = new FileOutputStream(destination)) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    output.write(buffer, 0, count);
                }
            } finally {
                temporary.delete();
            }
        }
    }

    private Bitmap decodeSampledBitmap(File file) {
        if (file == null || !file.isFile() || file.length() <= 0) {
            return null;
        }

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), bounds);

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null;
        }

        int targetWidth = Math.max(480, activity.getResources().getDisplayMetrics().widthPixels);
        int targetHeight = activity.dp(430);
        int sampleSize = 1;

        while (bounds.outWidth / sampleSize > targetWidth * 1.5f
                || bounds.outHeight / sampleSize > targetHeight * 1.5f) {
            sampleSize *= 2;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inDither = true;
        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }

    private JSONObject getJson(String address) throws IOException, JSONException {
        byte[] bytes = getBytes(address, MAX_MANIFEST_BYTES);
        return new JSONObject(new String(bytes, java.nio.charset.Charset.forName("UTF-8")));
    }

    private byte[] getBytes(String address, int maximumBytes) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(address).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setUseCaches(true);
            connection.setRequestProperty("Accept", "application/json, image/*");

            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                InputStream error = connection.getErrorStream();
                if (error != null) {
                    error.close();
                }
                throw new IOException("Gallery server returned HTTP " + status + ".");
            }

            try (InputStream input = connection.getInputStream();
                 ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int total = 0;
                int count;
                while ((count = input.read(buffer)) != -1) {
                    total += count;
                    if (total > maximumBytes) {
                        throw new IOException("Gallery response exceeded the allowed size.");
                    }
                    output.write(buffer, 0, count);
                }
                if (total == 0) {
                    throw new IOException("Gallery server returned an empty response.");
                }
                return output.toByteArray();
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!images.isEmpty()) {
            renderCurrentImage();
        } else {
            updateAutoAdvanceSchedule();
        }
        mainHandler.removeCallbacks(refreshRunnable);
        mainHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS);
    }

    @Override
    protected void onDetachedFromWindow() {
        mainHandler.removeCallbacks(autoAdvanceRunnable);
        mainHandler.removeCallbacks(refreshRunnable);
        stopZoomAnimation();
        imageView.animate().cancel();
        imageRequestToken++;
        super.onDetachedFromWindow();
    }

    private static final class GalleryImage {
        final long id;
        final String path;

        GalleryImage(long id, String path) {
            this.id = id;
            this.path = path;
        }
    }

    /** Keeps horizontal carousel gestures from stealing vertical Home scrolling. */
    private static final class ViewParentHelper {
        static void disallowParentIntercept(View view, boolean disallow) {
            android.view.ViewParent parent = view.getParent();
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(disallow);
            }
        }
    }
}
