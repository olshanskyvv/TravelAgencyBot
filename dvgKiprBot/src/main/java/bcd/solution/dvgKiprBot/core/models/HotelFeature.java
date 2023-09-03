package bcd.solution.dvgKiprBot.core.models;;

import lombok.*;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table
public class HotelFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private  Long id;

    public String name;

    public  String description;

    @Nullable
    public Boolean free;

    public HotelFeature(String name,
                        String description,
                        Boolean free) {
        this.name = name;
        this.description = description;
        this.free = free;
    }
}
