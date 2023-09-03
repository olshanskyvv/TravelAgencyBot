package bcd.solution.dvgKiprBot.core.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    public String name;
    @Column(columnDefinition = "TEXT")
    public String description;
    @ManyToMany(fetch = FetchType.EAGER)
    public List<HotelFeature> features;
    @ManyToOne(fetch = FetchType.EAGER)
    public Resort resort;
    @ManyToMany(fetch = FetchType.EAGER)
    public List<Activity> activities;
    @Enumerated(EnumType.STRING)
    public Stars stars;
    @ElementCollection(targetClass = Food.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    public List<Food> food;
    public String media;
    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    public boolean isDeleted = false;
}
