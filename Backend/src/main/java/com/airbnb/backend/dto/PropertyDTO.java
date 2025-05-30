package com.airbnb.backend.dto;

public class PropertyDTO {
    private int hostId;
    private double price;
    private String roomType;
    private int personCapacity;
    private int bedrooms;
    private double centerDistance;
    private double metroDistance;
    private String city;

    public PropertyDTO() {}

    public int getHostId() { return hostId; }
    public void setHostId(int hostId) { this.hostId = hostId; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public int getPersonCapacity() { return personCapacity; }
    public void setPersonCapacity(int personCapacity) { this.personCapacity = personCapacity; }

    public int getBedrooms() { return bedrooms; }
    public void setBedrooms(int bedrooms) { this.bedrooms = bedrooms; }

    public double getCenterDistance() { return centerDistance; }
    public void setCenterDistance(double centerDistance) { this.centerDistance = centerDistance; }

    public double getMetroDistance() { return metroDistance; }
    public void setMetroDistance(double metroDistance) { this.metroDistance = metroDistance; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}
