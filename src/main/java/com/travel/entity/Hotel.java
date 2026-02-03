package com.travel.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "hotel")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String map;
    private String city;
    private String country;

    private String days;
    private String stars;

    @Column(columnDefinition = "TEXT")
    private String about;

    // ✅ store list fields as JSON strings
    @Column(columnDefinition = "JSON")
    private String images;

    @Column(columnDefinition = "JSON")
    private String highlights;

    @Column(columnDefinition = "TEXT")
    private String cancellationPolicy;

    @Column(columnDefinition = "TEXT")
    private String availableDates;

    @Column(columnDefinition = "TEXT")
    private String bestTimeToVisit;

    @Column(columnDefinition = "JSON")
    private String travelTips;

    @Column(columnDefinition = "JSON")
    private String suitedFor;

    @Column(columnDefinition = "JSON")
    private String includes;

    @Column(columnDefinition = "JSON")
    private String excludes;

    @Column(name = "available_from")
    private LocalDate availableFrom;

    @Column(name = "available_to")
    private LocalDate availableTo;

    // ✅ Relationships
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Room> rooms;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Nearby> nearby;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Faq> faq;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Programme> programme;

    // =======================
    // getters/setters
    // =======================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getmap() { return map; }
    public void setmap(String map) { this.map = map; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getDays() { return days; }
    public void setDays(String days) { this.days = days; }

    public String getStars() { return stars; }
    public void setStars(String stars) { this.stars = stars; }

    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = about; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public String getHighlights() { return highlights; }
    public void setHighlights(String highlights) { this.highlights = highlights; }

    public String getCancellationPolicy() { return cancellationPolicy; }
    public void setCancellationPolicy(String cancellationPolicy) { this.cancellationPolicy = cancellationPolicy; }

    public String getAvailableDates() { return availableDates; }
    public void setAvailableDates(String availableDates) { this.availableDates = availableDates; }

    public String getBestTimeToVisit() { return bestTimeToVisit; }
    public void setBestTimeToVisit(String bestTimeToVisit) { this.bestTimeToVisit = bestTimeToVisit; }

    public String getTravelTips() { return travelTips; }
    public void setTravelTips(String travelTips) { this.travelTips = travelTips; }

    public String getSuitedFor() { return suitedFor; }
    public void setSuitedFor(String suitedFor) { this.suitedFor = suitedFor; }

    public String getIncludes() { return includes; }
    public void setIncludes(String includes) { this.includes = includes; }

    public String getExcludes() { return excludes; }
    public void setExcludes(String excludes) { this.excludes = excludes; }

    public LocalDate getAvailableFrom() { return availableFrom; }
    public void setAvailableFrom(LocalDate availableFrom) { this.availableFrom = availableFrom; }

    public LocalDate getAvailableTo() { return availableTo; }
    public void setAvailableTo(LocalDate availableTo) { this.availableTo = availableTo; }

    public List<Room> getRooms() { return rooms; }
    public void setRooms(List<Room> rooms) { this.rooms = rooms; }

    public List<Nearby> getNearby() { return nearby; }
    public void setNearby(List<Nearby> nearby) { this.nearby = nearby; }

    public List<Faq> getFaq() { return faq; }
    public void setFaq(List<Faq> faq) { this.faq = faq; }

    public List<Programme> getProgramme() { return programme; }
    public void setProgramme(List<Programme> programme) { this.programme = programme; }
}
