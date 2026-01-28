package com.crs.iamservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "password_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @Column(name = "password_hash", nullable = false)
    String passwordHash;

    @Column(name = "change_at")
    LocalDateTime changeAt;

    @PrePersist
    protected void onCreate() {
        changeAt = LocalDateTime.now();
    }
}