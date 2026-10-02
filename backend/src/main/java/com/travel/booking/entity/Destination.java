package com.travel.booking.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/** Maps to the DESTINATIONS table. */
@Entity
@Table(name = "destinations")
public class Destination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "destination_id")
    private Long destinationId;

    @NotBlank(message = "Destination name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Country is required")
    @Column(nullable = false)
    private String country;

    private String description;

    public Long getDestinationId() { return destinationId; }
    public void setDestinationId(Long destinationId) { this.destinationId = destinationId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
