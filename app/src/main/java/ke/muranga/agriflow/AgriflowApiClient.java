
package ke.muranga.agriflow;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AgriflowApiClient {

    public static final String DEFAULT_BASE_URL =
            "http://127.0.0.1:5050";

    private static final int CONNECT_TIMEOUT_MS = 10000;
    private static final int READ_TIMEOUT_MS = 20000;
    private static final int MAX_PHOTO_BYTES = 7 * 1024 * 1024;

    private final Context appContext;
    private final String baseUrl;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public interface Callback {
        void onSuccess(JSONObject response);

        void onError(String message);
    }

    private interface ApiOperation {
        JSONObject execute() throws Exception;
    }

    public AgriflowApiClient(Context context) {
        this(context, DEFAULT_BASE_URL);
    }

    public AgriflowApiClient(Context context, String baseUrl) {
        this.appContext = context.getApplicationContext();

        String normalized = baseUrl.trim();

        while (normalized.endsWith("/")) {
            normalized = normalized.substring(
                    0,
                    normalized.length() - 1
            );
        }

        this.baseUrl = normalized;
    }

    public void checkHealth(Callback callback) {
        executeAsync(
                () -> requestJson(
                        "/api/health",
                        "GET",
                        null
                ),
                callback
        );
    }

    public void syncFarmer(
            long existingServerFarmerId,
            JSONObject payload,
            Callback callback
    ) {
        final String path;
        final String method;

        if (existingServerFarmerId > 0) {
            path = "/api/farmers/" + existingServerFarmerId;
            method = "PUT";
        } else {
            path = "/api/farmers";
            method = "POST";
        }

        executeAsync(
                () -> requestJson(path, method, payload),
                callback
        );
    }

    public void createProblemReport(
            long serverFarmerId,
            JSONObject payload,
            Callback callback
    ) {
        String path =
                "/api/farmers/"
                        + serverFarmerId
                        + "/problem-reports";

        executeAsync(
                () -> requestJson(path, "POST", payload),
                callback
        );
    }

    public void uploadProblemReportPhoto(
            long serverFarmerId,
            long serverReportId,
            Uri photoUri,
            Callback callback
    ) {
        executeAsync(
                () -> {
                    if (photoUri == null) {
                        throw new IOException(
                                "No photo was selected."
                        );
                    }

                    InputStream source =
                            appContext.getContentResolver()
                                    .openInputStream(photoUri);

                    if (source == null) {
                        throw new IOException(
                                "Could not open the selected photo."
                        );
                    }

                    byte[] photoBytes;

                    try (InputStream input = source) {
                        photoBytes = readLimited(
                                input,
                                MAX_PHOTO_BYTES
                        );
                    }

                    return uploadPhotoRequest(
                            serverFarmerId,
                            serverReportId,
                            photoBytes
                    );
                },
                callback
        );
    }

    private void executeAsync(
            ApiOperation operation,
            Callback callback
    ) {
        executor.execute(() -> {
            try {
                JSONObject result = operation.execute();

                if (callback != null) {
                    mainHandler.post(
                            () -> callback.onSuccess(result)
                    );
                }

            } catch (Exception exception) {
                String message = exception.getMessage();

                if (message == null || message.trim().isEmpty()) {
                    message = "The server request failed.";
                }

                final String errorMessage = message;

                if (callback != null) {
                    mainHandler.post(
                            () -> callback.onError(errorMessage)
                    );
                }
            }
        });
    }

    private JSONObject requestJson(
            String path,
            String method,
            JSONObject payload
    ) throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(baseUrl + path);

            connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod(method);
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setUseCaches(false);

            connection.setRequestProperty(
                    "Accept",
                    "application/json"
            );

            if (payload != null) {
                connection.setDoOutput(true);

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                byte[] body =
                        payload.toString().getBytes("UTF-8");

                try (OutputStream output =
                             connection.getOutputStream()) {
                    output.write(body);
                }
            }

            return readJsonResponse(connection);

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private JSONObject uploadPhotoRequest(
            long farmerId,
            long reportId,
            byte[] photoBytes
    ) throws Exception {
        try {
            return uploadPhotoRequestWithMethod(
                    farmerId,
                    reportId,
                    photoBytes,
                    "POST"
            );
        } catch (Exception exception) {
            String message = exception.getMessage();

            // The server may have saved the photo even if the app did not
            // receive or persist the successful response. Reconcile that case
            // through the backend's existing PUT replacement endpoint.
            if (message != null
                    && message.contains("HTTP 409:")
                    && message.toLowerCase(java.util.Locale.ROOT)
                            .contains("already has a photo")) {
                return uploadPhotoRequestWithMethod(
                        farmerId,
                        reportId,
                        photoBytes,
                        "PUT"
                );
            }

            throw exception;
        }
    }

    private JSONObject uploadPhotoRequestWithMethod(
            long farmerId,
            long reportId,
            byte[] photoBytes,
            String requestMethod
    ) throws Exception {
        HttpURLConnection connection = null;

        String path =
                "/api/farmers/"
                        + farmerId
                        + "/problem-reports/"
                        + reportId
                        + "/photo";

        String boundary =
                "AgriflowBoundary"
                        + UUID.randomUUID().toString()
                        .replace("-", "");

        try {
            URL url = new URL(baseUrl + path);

            connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod(requestMethod);
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setUseCaches(false);
            connection.setDoOutput(true);

            connection.setRequestProperty(
                    "Accept",
                    "application/json"
            );

            connection.setRequestProperty(
                    "Content-Type",
                    "multipart/form-data; boundary=" + boundary
            );

            try (OutputStream output =
                         connection.getOutputStream()) {

                writeUtf8(
                        output,
                        "--" + boundary + "\r\n"
                );

                writeUtf8(
                        output,
                        "Content-Disposition: form-data; "
                                + "name=\"photo\"; "
                                + "filename=\"evidence.jpg\"\r\n"
                );

                writeUtf8(
                        output,
                        "Content-Type: application/octet-stream\r\n\r\n"
                );

                output.write(photoBytes);

                writeUtf8(
                        output,
                        "\r\n--" + boundary + "--\r\n"
                );
            }

            return readJsonResponse(connection);

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private JSONObject readJsonResponse(
            HttpURLConnection connection
    ) throws Exception {
        int status = connection.getResponseCode();

        InputStream responseStream =
                status >= 200 && status < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream();

        String responseText = readText(responseStream);

        if (status < 200 || status >= 300) {
            throw new IOException(
                    describeHttpError(status, responseText)
            );
        }

        if (responseText.trim().isEmpty()) {
            return new JSONObject();
        }

        return new JSONObject(responseText);
    }

    private String describeHttpError(
            int status,
            String responseText
    ) {
        try {
            JSONObject body = new JSONObject(responseText);

            String message = body.optString(
                    "error",
                    "The server rejected the request."
            );

            JSONObject fields = body.optJSONObject("fields");

            if (fields != null) {
                message += " " + fields.toString();
            }

            return "HTTP " + status + ": " + message;

        } catch (JSONException ignored) {
            return "HTTP " + status
                    + (responseText.isEmpty()
                    ? ": The server request failed."
                    : ": " + responseText);
        }
    }

    private String readText(InputStream input)
            throws IOException {
        if (input == null) {
            return "";
        }

        StringBuilder text = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, "UTF-8")
        )) {
            String line;

            while ((line = reader.readLine()) != null) {
                text.append(line);
            }
        }

        return text.toString();
    }

    private byte[] readLimited(
            InputStream input,
            int maximumBytes
    ) throws IOException {
        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        byte[] buffer = new byte[8192];
        int total = 0;
        int count;

        while ((count = input.read(buffer)) != -1) {
            total += count;

            if (total > maximumBytes) {
                throw new IOException(
                        "Photo must not exceed 7 MiB."
                );
            }

            output.write(buffer, 0, count);
        }

        if (total == 0) {
            throw new IOException(
                    "The selected photo is empty."
            );
        }

        return output.toByteArray();
    }

    private void writeUtf8(
            OutputStream output,
            String text
    ) throws IOException {
        output.write(text.getBytes("UTF-8"));
    }

    public void close() {
        executor.shutdown();
    }
}
