package com.airbnb.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public class PropertyDetailsDTO {
    // SQL source
    private int id;
    private int hostId;
    private BigDecimal price;
    private String roomType;
    private int personCapacity;
    private Integer bedrooms;
    private BigDecimal centerDistance;
    private BigDecimal metroDistance;
    private String city;

    // MongoDB source
    private Double avgCleanlinessRating;
    private Double avgSatisfactionRating;
    private Integer totalReviews;
    private List<Object> reviews; // optionally define a proper ReviewDTO

    public PropertyDetailsDTO() {}

    public List<Object> getReviews() {
        return reviews;
    }

    public void setReviews(List<Object> reviews) {
        this.reviews = reviews;
    }

    public Integer getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Integer totalReviews) {
        this.totalReviews = totalReviews;
    }

    public Double getAvgSatisfactionRating() {
        return avgSatisfactionRating;
    }

    public void setAvgSatisfactionRating(Double avgSatisfactionRating) {
        this.avgSatisfactionRating = avgSatisfactionRating;
    }

    public Double getAvgCleanlinessRating() {
        return avgCleanlinessRating;
    }

    public void setAvgCleanlinessRating(Double avgCleanlinessRating) {
        this.avgCleanlinessRating = avgCleanlinessRating;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public BigDecimal getMetroDistance() {
        return metroDistance;
    }

    public void setMetroDistance(BigDecimal metroDistance) {
        this.metroDistance = metroDistance;
    }

    public BigDecimal getCenterDistance() {
        return centerDistance;
    }

    public void setCenterDistance(BigDecimal centerDistance) {
        this.centerDistance = centerDistance;
    }

    public Integer getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(Integer bedrooms) {
        this.bedrooms = bedrooms;
    }

    public int getPersonCapacity() {
        return personCapacity;
    }

    public void setPersonCapacity(int personCapacity) {
        this.personCapacity = personCapacity;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getHostId() {
        return hostId;
    }

    public void setHostId(int hostId) {
        this.hostId = hostId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
