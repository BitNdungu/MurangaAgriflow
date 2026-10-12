package ke.muranga.agriflow;

public class FarmProblemReport {

    private final long id;
    private final String category;
    private final String cropAffected;
    private final String subcounty;
    private final String ward;
    private final String description;
    private final String photoUri;
    private final String createdAt;

    private final Long serverReportId;
    private final String syncStatus;
    private final String syncError;
    private final String photoSyncStatus;
    private final String photoSyncError;

    // Existing constructor, kept for compatibility.
    public FarmProblemReport(
            long id,
            String category,
            String cropAffected,
            String subcounty,
            String ward,
            String description,
            String photoUri,
            String createdAt
    ) {
        this(
                id,
                category,
                cropAffected,
                subcounty,
                ward,
                description,
                photoUri,
                createdAt,
                null,
                "pending",
                "",
                hasPhoto(photoUri) ? "pending" : "not_needed",
                ""
        );
    }

    // Full constructor, including synchronization information.
    public FarmProblemReport(
            long id,
            String category,
            String cropAffected,
            String subcounty,
            String ward,
            String description,
            String photoUri,
            String createdAt,
            Long serverReportId,
            String syncStatus,
            String syncError,
            String photoSyncStatus,
            String photoSyncError
    ) {
        this.id = id;
        this.category = category;
        this.cropAffected = cropAffected;
        this.subcounty = subcounty;
        this.ward = ward;
        this.description = description;
        this.photoUri = photoUri;
        this.createdAt = createdAt;
        this.serverReportId = serverReportId;
        this.syncStatus = syncStatus;
        this.syncError = syncError;
        this.photoSyncStatus = photoSyncStatus;
        this.photoSyncError = photoSyncError;
    }

    private static boolean hasPhoto(String photoUri) {
        return photoUri != null && !photoUri.trim().isEmpty();
    }

    public long getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getCropAffected() {
        return cropAffected;
    }

    public String getSubcounty() {
        return subcounty;
    }

    public String getWard() {
        return ward;
    }

    public String getDescription() {
        return description;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public Long getServerReportId() {
        return serverReportId;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public String getSyncError() {
        return syncError;
    }

    public String getPhotoSyncStatus() {
        return photoSyncStatus;
    }

    public String getPhotoSyncError() {
        return photoSyncError;
    }
}
