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
        this.id = id;
        this.category = category;
        this.cropAffected = cropAffected;
        this.subcounty = subcounty;
        this.ward = ward;
        this.description = description;
        this.photoUri = photoUri;
        this.createdAt = createdAt;
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
}