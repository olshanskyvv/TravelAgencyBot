package bcd.solution.dvgKiprBot.core.models;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    public String name;
    @Nullable
    @Column(columnDefinition = "TEXT")
    public String description;
    @Enumerated(EnumType.STRING)
    public ActivityType activityType;
    @Nullable
    public Boolean free;
    public String media;
    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    public boolean isDeleted = false;

    @Override
    public String toString() {
        return this.name + "\n\n"
                + this.description + "\n\n"
                + (Boolean.TRUE.equals(this.free) ? "Бесплатно" : "Платно");
    }

}