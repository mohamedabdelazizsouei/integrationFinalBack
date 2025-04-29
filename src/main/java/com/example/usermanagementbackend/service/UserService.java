package com.example.usermanagementbackend.service;

import com.example.usermanagementbackend.dto.UserDTO;
import com.example.usermanagementbackend.entity.Evenement;
import com.example.usermanagementbackend.entity.Fidelite;
import com.example.usermanagementbackend.entity.Livreur;
import com.example.usermanagementbackend.entity.User;
import com.example.usermanagementbackend.mapper.UserMapper;
import com.example.usermanagementbackend.repository.EvenementRepository;
import com.example.usermanagementbackend.repository.LivreurRepository;
import com.example.usermanagementbackend.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LivreurRepository livreurRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private FideliteService fideliteService; // ✅ Inject FideliteService
    @Autowired
    private EvenementRepository evenementRepository;

    public UserDTO saveUser(UserDTO userDTO) {
        User user = UserMapper.toEntity(userDTO);

        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Un compte avec cet email existe déjà.");
        }

        if (user.getMotDePasse() != null && !user.getMotDePasse().isEmpty()) {
            String hashedPassword = passwordEncoder.encode(user.getMotDePasse());
            user.setMotDePasse(hashedPassword);
        }

        String code = String.valueOf((int)(Math.random() * 900000) + 100000);
        user.setVerificationCode(code);
        user.setVerified(false);

        sendVerificationEmail(user.getEmail(), code);

        User savedUser = userRepository.save(user);

        if ("LIVREUR".equalsIgnoreCase(savedUser.getRole())) {
            Livreur livreur = new Livreur();
            livreur.setNom(savedUser.getNom() + " " + savedUser.getPrenom());
            livreur.setEmail(savedUser.getEmail());
            livreur.setTelephone(savedUser.getNumeroDeTelephone());
            livreur.setUser(savedUser);
            livreurRepository.save(livreur);
        }

        // ✅ Create and save Fidelite after saving User
        Fidelite fidelite = new Fidelite(null, 0, "Bronze", savedUser);
        fideliteService.saveFidelite(fidelite);

        return UserMapper.toDTO(savedUser);
    }

    public void sendVerificationEmail(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            helper.setTo(toEmail);
            helper.setSubject("Vérification de votre compte - AgriConnect");

            String htmlContent = "<div style='font-family: Arial, sans-serif; font-size: 16px;'>"
                    + "<p>Bonjour,</p>"
                    + "<p>Merci pour votre inscription sur <strong>AgriConnect</strong>.</p>"
                    + "<p>Voici votre code de vérification :</p>"
                    + "<h2 style='color: #2e7d32; font-size: 28px;'>" + code + "</h2>"
                    + "<p>Ce code est valable pour une durée limitée.</p>"
                    + "<br><p>Cordialement,<br>L'équipe AgriConnect</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            helper.setFrom("noreply@agriconnect.com");

            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'envoi de l'email de vérification.");
        }
    }

    @Transactional
    public User saveUserDirect(User user) {
        User savedUser = userRepository.saveAndFlush(user);
        entityManager.clear();
        return savedUser;
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        entityManager.clear();
        return users;
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        entityManager.flush();
        entityManager.clear();
    }

    public User updateUser(Long id, User user) {
        Optional<User> existingUserOpt = userRepository.findById(id);
        if (!existingUserOpt.isPresent()) {
            throw new RuntimeException("User not found with id: " + id);
        }

        User existingUser = existingUserOpt.get();
        existingUser.setNom(user.getNom());
        existingUser.setPrenom(user.getPrenom());
        existingUser.setEmail(user.getEmail());

        if (user.getMotDePasse() != null && !user.getMotDePasse().isEmpty()) {
            String hashedPassword = passwordEncoder.encode(user.getMotDePasse());
            existingUser.setMotDePasse(hashedPassword);
        }

        existingUser.setNumeroDeTelephone(user.getNumeroDeTelephone());
        existingUser.setRole(user.getRole());
        existingUser.setAdresseLivraison(user.getAdresseLivraison());
        return userRepository.save(existingUser);
    }

    public List<User> searchUsers(String query) {
        return userRepository.findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query, query);
    }

    public void sendPasswordResetCode(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("Aucun utilisateur trouvé avec cet email.");
        }

        User user = userOpt.get();

        String code = String.valueOf((int)(Math.random() * 900000) + 100000);
        user.setResetCode(code);
        userRepository.save(user);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            helper.setTo(email);
            helper.setSubject("Réinitialisation de mot de passe - AgriConnect");

            String htmlContent = "<div style='font-family: Arial, sans-serif; font-size: 16px;'>"
                    + "<p>Bonjour,</p>"
                    + "<p>Voici votre code de réinitialisation de mot de passe :</p>"
                    + "<h2 style='color: #007bff;'>" + code + "</h2>"
                    + "<p>Utilisez ce code pour réinitialiser votre mot de passe.</p>"
                    + "<br><p>Cordialement,<br>L'équipe AgriConnect</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            helper.setFrom("noreply@agriconnect.com");

            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'envoi du mail de réinitialisation.");
        }
    }

    public Map<String, Long> getUserStats() {
        List<User> allUsers = userRepository.findAll();

        long blocked = allUsers.stream().filter(User::isBlocked).count();
        long admins = allUsers.stream().filter(u -> "admin".equalsIgnoreCase(u.getRole())).count();
        long normal = allUsers.stream().filter(u -> !"admin".equalsIgnoreCase(u.getRole())).count();

        Map<String, Long> stats = new HashMap<>();
        stats.put("bloqués", blocked);
        stats.put("admins", admins);
        stats.put("normaux", normal);
        return stats;
    }

    public void mettreAJourConnexion(User user) {
        user.setDerniereConnexion(LocalDateTime.now());
        user.setNombreConnexions(user.getNombreConnexions() + 1);
        userRepository.save(user);
    }

    @Transactional
    public void incrementerActions(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.incrementerActions();
            userRepository.saveAndFlush(user);
            entityManager.clear();
        }
    }

    public double predictChurnRisk(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Map<String, Object> data = new HashMap<>();
        data.put("last_login", user.getDerniereConnexion() != null
                ? user.getDerniereConnexion().toString()
                : LocalDateTime.now().toString());
        data.put("login_count", user.getNombreConnexions());
        data.put("actions_count", user.getActionsEffectuees());
        data.put("is_blocked", user.isBlocked());
        data.put("is_verified", user.isVerified());
        data.put("block_count", user.getNombreBlocages());

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "http://localhost:8000/predict", data, Map.class);

            return ((Number) response.getBody().get("risk_score")).doubleValue();
        } catch (Exception e) {
            throw new RuntimeException("Erreur dans la prédiction : " + e.getMessage());
        }
    }

    public Map<String, Long> getUserStatistics() {
        long totalUsers = userRepository.count();
        long blockedUsers = userRepository.countByIsBlocked(true);
        long admins = userRepository.countByRole("admin");
        long clients = userRepository.countByRole("user");

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", totalUsers);
        stats.put("blocked", blockedUsers);
        stats.put("admins", admins);
        stats.put("clients", clients);

        return stats;
    }

    @Transactional
    public void participerEvenement(Long userId, Long evenementId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        Evenement evenement = evenementRepository.findById(evenementId)
                .orElseThrow(() -> new RuntimeException("Événement introuvable"));

        if (evenement.getCapaciteMax() <= 0) {
            throw new RuntimeException("L'événement est complet !");
        }

        if (user.getEvenementsParticipes().add(evenement)) {
            evenement.setCapaciteMax(evenement.getCapaciteMax() - 1);
            userRepository.save(user);
            evenementRepository.save(evenement);
        }
    }

    @Transactional
    public void annulerParticipation(Long userId, Long evenementId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        Evenement evenement = evenementRepository.findById(evenementId)
                .orElseThrow(() -> new RuntimeException("Événement introuvable"));

        if (!user.getEvenementsParticipes().contains(evenement)) {
            throw new RuntimeException("L'utilisateur n'est pas inscrit à cet événement");
        }

        user.getEvenementsParticipes().remove(evenement);
        evenement.getParticipants().remove(user); // Important aussi côté événement !!

        evenement.setCapaciteMax(evenement.getCapaciteMax() + 1); // Libérer une place

        userRepository.save(user);
        evenementRepository.save(evenement);
    }



    // dans UserService

}
