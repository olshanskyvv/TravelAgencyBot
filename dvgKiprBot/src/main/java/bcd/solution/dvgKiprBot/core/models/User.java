package bcd.solution.dvgKiprBot.core.models;;;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {
    @Id
    private Long id;
    private String login; //TODO: phone number or username
    @Nullable
    private String phone; //TODO: phone number or username
    @Nullable
    private String password;
    @Enumerated(EnumType.STRING)
    private UserRole role;
}