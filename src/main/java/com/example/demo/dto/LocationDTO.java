package com.example.demo.dto;

public class LocationDTO {
    private String city;

    private String address;

    private String country;

    private String postCode;

    private Double latitude;
    private Double longitude;

    public LocationDTO(){}

    public LocationDTO(String city, String address, String country, String postCode, Double latitude, Double longitude) {
        this.city = city;
        this.address = address;
        this.country = country;
        this.postCode = postCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getAddress() {return address;}

    public void setAddress(String address) {this.address = address;}

    public String getCity() {return city;}

    public void setCity(String city) {this.city = city;}

    public String getCountry() {return country;}

    public void setCountry(String country) {this.country = country;}

    public String getPostCode() {return postCode;}

    public void setPostCode(String postCode) {this.postCode = postCode;}

    public Double getLatitude() {return latitude;}

    public void setLatitude(Double latitude) {this.latitude = latitude;}

    public Double getLongitude() {return longitude;}

    public void setLongitude(Double longitude) {this.longitude = longitude;}
}
