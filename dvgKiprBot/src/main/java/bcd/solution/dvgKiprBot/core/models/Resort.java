package bcd.solution.dvgKiprBot.core.models;


import lombok.*;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table
@ToString(exclude = "activities")
public class Resort {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    public String name;
    @Column(columnDefinition = "TEXT")
    public String description;
    public String geo;
    public String media;
    @ManyToMany(fetch = FetchType.EAGER)
    public List<Activity> activities;
    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    public boolean isDeleted = false;
}
