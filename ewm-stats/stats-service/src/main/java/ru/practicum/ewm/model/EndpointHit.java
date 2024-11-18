package ru.practicum.ewm.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "statistics")
public class EndpointHit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column
    private String app;

    @NotBlank
    @Column
    private String uri;

    @NotBlank
    @Column
    private String ip;

    @NotNull
    @Column
    private LocalDateTime timestamp;
}
