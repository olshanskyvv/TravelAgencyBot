package bcd.solution.dvgKiprBot.core.models;

import lombok.*;

import jakarta.persistence.*;

import java.util.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table
public class StateMachine {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @OneToOne(fetch = FetchType.EAGER)
    public User user;
    @ManyToMany(fetch = FetchType.EAGER)
    public List<Activity> activities;
    @Column(columnDefinition = "boolean default false")
    public boolean activitiesGot;
    @ManyToOne(fetch = FetchType.EAGER)
    public Resort resort;
    @Column(columnDefinition = "boolean default false")
    public boolean resortGot;
    @Enumerated(EnumType.STRING)
    public Stars stars;
    @ManyToOne(fetch = FetchType.EAGER)
    public Hotel hotel;
    @ManyToOne(fetch = FetchType.EAGER)
    public CustomTour customTour;
    public boolean authorized;
    public boolean wait_password;
    public Integer auth_message_id;
    public boolean waitPhone;
    public Integer phoneMessageId;
}
