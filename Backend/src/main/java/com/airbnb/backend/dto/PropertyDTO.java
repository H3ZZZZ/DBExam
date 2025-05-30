package com.airbnb.backend.dto;

import java.math.BigDecimal;

public class PropertyDTO {
    private Integer id;
    private Integer hostId;
    private BigDecimal price;
    private String roomType;
    private Integer personCapacity;
    private Integer bedrooms;
    private BigDecimal centerDistance;
    private BigDecimal metroDistance;
    private String city;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getHostId() {
        return hostId;
    }

    public void setHostId(Integer hostId) {
        this.hostId = hostId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public Integer getPersonCapacity() {
        return personCapacity;
    }

    public void setPersonCapacity(Integer personCapacity) {
        this.personCapacity = personCapacity;
    }

    public Integer getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(Integer bedrooms) {
        this.bedrooms = bedrooms;
    }

    public BigDecimal getCenterDistance() {
        return centerDistance;
    }

    public void setCenterDistance(BigDecimal centerDistance) {
        this.centerDistance = centerDistance;
    }

    public BigDecimal getMetroDistance() {
        return metroDistance;
    }

    public void setMetroDistance(BigDecimal metroDistance) {
        this.metroDistance = metroDistance;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}