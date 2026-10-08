package ke.muranga.agriflow;

public class Crop {

    private long id;
    private String name;
    private double acreage;
    private String plantingDate;
    private String expectedHarvestDate;
    private double expectedQuantity;
    private String quantityUnit;

    public Crop() {
    }

    public Crop(
            long id,
            String name,
            double acreage,
            String plantingDate,
            String expectedHarvestDate,
            double expectedQuantity,
            String quantityUnit
    ) {
        this.id = id;
        this.name = name;
        this.acreage = acreage;
        this.plantingDate = plantingDate;
        this.expectedHarvestDate = expectedHarvestDate;
        this.expectedQuantity = expectedQuantity;
        this.quantityUnit = quantityUnit;
    }

    public Crop(
            String name,
            double acreage,
            String plantingDate,
            String expectedHarvestDate,
            double expectedQuantity,
            String quantityUnit
    ) {
        this(
                0,
                name,
                acreage,
                plantingDate,
                expectedHarvestDate,
                expectedQuantity,
                quantityUnit
        );
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getAcreage() {
        return acreage;
    }

    public String getPlantingDate() {
        return plantingDate;
    }

    public String getExpectedHarvestDate() {
        return expectedHarvestDate;
    }

    public double getExpectedQuantity() {
        return expectedQuantity;
    }

    public String getQuantityUnit() {
        return quantityUnit;
    }
}