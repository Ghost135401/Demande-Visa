package mg.gov.interieur.visa.common.service;

import lombok.RequiredArgsConstructor;
import mg.gov.interieur.visa.dto.response.UserProfileResponse;
import mg.gov.interieur.visa.entity.User;
import mg.gov.interieur.visa.enums.Role;
import mg.gov.interieur.visa.exception.ResourceNotFoundException;
import mg.gov.interieur.visa.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserProfileResponse create(String username, String rawPassword, String nom, String prenom, Role role) {
        Optional<User> existing = userRepository.findByUsername(username);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Nom d'utilisateur deja utilise");
        }
        User u = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .nom(nom)
                .prenom(prenom)
                .role(role)
                .actif(true)
                .build();
        u = userRepository.save(u);
        return toProfile(u);
    }

    public UserProfileResponse authenticate(String username, String rawPassword) {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Identifiants invalides"));
        if (!u.isActif()) throw new IllegalArgumentException("Utilisateur desactive");
        if (!passwordEncoder.matches(rawPassword, u.getPassword()))
            throw new IllegalArgumentException("Identifiants invalides");
        return toProfile(u);
    }

    public UserProfileResponse getById(Long id) {
        return userRepository.findById(id)
                .map(this::toProfile)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
    }

    public UserProfileResponse update(Long id, String nom, String prenom, Role role, Boolean actif) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
        if (nom != null) u.setNom(nom);
        if (prenom != null) u.setPrenom(prenom);
        if (role != null) u.setRole(role);
        if (actif != null) u.setActif(actif);
        u = userRepository.save(u);
        return toProfile(u);
    }

    public void deactivate(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
        u.setActif(false);
        userRepository.save(u);
    }

    private UserProfileResponse toProfile(User u) {
        return UserProfileResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .nom(u.getNom())
                .prenom(u.getPrenom())
                .role(u.getRole())
                .actif(u.isActif())
                .build();
    }
}
