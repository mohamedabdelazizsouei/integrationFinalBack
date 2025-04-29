package com.example.usermanagementbackend.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import com.example.usermanagementbackend.entity.Evenement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties({"evenementsParticipes","hibernateLazyInitializer", "handler"})
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String faceDescriptor;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "participations",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "evenement_id"))
    private Set<Evenement> evenementsParticipes = new HashSet<>();

    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String numeroDeTelephone;
    private String role;
    private String adresseLivraison;
    private boolean isBlocked = false;
    private String verificationCode;
    private boolean verified = false;
    private String resetCode;
    private LocalDateTime derniereConnexion;
    private int nombreConnexions;
    private int actionsEffectuees = 0;
    private int nombreBlocages = 0;
    private LocalDate dateOfBirth;
    private Double creditLimit;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Fidelite fidelite;

    @Column(name = "photo")
    private String photo;

    public User() {}

    public User(String nom, String prenom, String email, String motDePasse, String numeroDeTelephone, String role, String adresseLivraison) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.numeroDeTelephone = numeroDeTelephone;
        this.role = role;
        this.adresseLivraison = adresseLivraison;
        this.creditLimit = creditLimit;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }
    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }
    public Double getCreditLimit() {
        return creditLimit;
    }
    public void setCreditLimit(Double creditLimit) {
        this.creditLimit = creditLimit;
    }
    public String getNumeroDeTelephone() {
        return numeroDeTelephone;
    }
    public void setNumeroDeTelephone(String numeroDeTelephone) {
        this.numeroDeTelephone = numeroDeTelephone;
    }

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    public String getAdresseLivraison() {
        return adresseLivraison;
    }
    public void setAdresseLivraison(String adresseLivraison) {
        this.adresseLivraison = adresseLivraison;
    }

    @JsonProperty("isBlocked")
    public boolean isBlocked() {
        return isBlocked;
    }
    public void setIsBlocked(boolean isBlocked) {
        this.isBlocked = isBlocked;
    }

    public String getVerificationCode() {
        return verificationCode;
    }
    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public boolean isVerified() {
        return verified;
    }
    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getResetCode() {
        return resetCode;
    }
    public void setResetCode(String resetCode) {
        this.resetCode = resetCode;
    }

    public LocalDateTime getDerniereConnexion() {
        return derniereConnexion;
    }
    public void setDerniereConnexion(LocalDateTime derniereConnexion) {
        this.derniereConnexion = derniereConnexion;
    }

    public int getNombreConnexions() {
        return nombreConnexions;
    }
    public void setNombreConnexions(int nombreConnexions) {
        this.nombreConnexions = nombreConnexions;
    }

    public int getActionsEffectuees() {
        return actionsEffectuees;
    }
    public void setActionsEffectuees(int actionsEffectuees) {
        this.actionsEffectuees = actionsEffectuees;
    }
    public void incrementerActions() {
        this.actionsEffectuees++;
    }

    public int getNombreBlocages() {
        return nombreBlocages;
    }
    public void setNombreBlocages(int nombreBlocages) {
        this.nombreBlocages = nombreBlocages;
    }
    public void incrementerBlocages() {
        this.nombreBlocages++;
    }

    public String getFaceDescriptor() {
        return faceDescriptor;
    }
    public void setFaceDescriptor(String faceDescriptor) {
        this.faceDescriptor = faceDescriptor;
    }

    public String getPhoto() {
        return photo;
    }
    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public Fidelite getFidelite() {
        return fidelite;
    }
    public void setFidelite(Fidelite fidelite) {
        this.fidelite = fidelite;
    }

    // == AJOUT MANQUANT ==

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    public Set<Evenement> getEvenementsParticipes() {
        return evenementsParticipes;
    }

    public void setEvenementsParticipes(Set<Evenement> evenementsParticipes) {
        this.evenementsParticipes = evenementsParticipes;
    }

}
