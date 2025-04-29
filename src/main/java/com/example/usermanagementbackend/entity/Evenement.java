package com.example.usermanagementbackend.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "evenements")
@JsonIgnoreProperties({"participants"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Evenement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(mappedBy = "evenementsParticipes", fetch = FetchType.LAZY)
    private Set<User> participants = new HashSet<>();
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String lieu;
    private Integer capaciteMax;
    private String organisateur;
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private StatutEvenement statut;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "evenement_categorie",
            joinColumns = @JoinColumn(name = "evenement_id"),
            inverseJoinColumns = @JoinColumn(name = "categorie_id"))
    private Set<Categorie> categories;

    public enum StatutEvenement {
        PLANIFIE, EN_COURS, TERMINE, ANNULE
    }
}