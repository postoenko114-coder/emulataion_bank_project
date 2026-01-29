package com.example.demo.models.branch;

import com.example.demo.dto.LocationDTO;
import jakarta.persistence.*;

@Embeddable
public class Location {

    private Double latitude;

    private Double longitude;

    private String city;

    private String address;

    private String country;

    private String postCode;

    public Location(String city, String address, String country, Double latitude, Double longitude,  String postCode) {
        this.city = city;
        this.address = address;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.postCode = postCode;
    }

    public Location() {}

    public LocationDTO toDTO(){
        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setCity(city);
        locationDTO.setAddress(address);
        locationDTO.setCountry(country);
        locationDTO.setPostCode(postCode);
        locationDTO.setLatitude(latitude);
        locationDTO.setLongitude(longitude);
        return locationDTO;
    }

    public String getAddress() {return address;}

    public void setAddress(String address) {this.address = address;}

    public String getCity() {return city;}

    public void setCity(String city) {this.city = city;}

    public String getCountry() {return country;}

    public void setCountry(String country) {this.country = country;}

    public Double getLatitude() {return latitude;}

    public void setLatitude(Double latitude) {this.latitude = latitude;}

    public Double getLongitude() {return longitude;}

    public void setLongitude(Double longitude) {this.longitude = longitude;}

    public String getPostCode() {return postCode;}

    public void setPostCode(String postCode) {this.postCode = postCode;}
}
