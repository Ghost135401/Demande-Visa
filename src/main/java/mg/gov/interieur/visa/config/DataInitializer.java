package mg.gov.interieur.visa.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mg.gov.interieur.visa.enums.Role;
import mg.gov.interieur.visa.repository.UserRepository;
import mg.gov.interieur.visa.common.service.UserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserService userService;
    private final UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String defaultAdmin = "admin";
        if (userRepository.findByUsername(defaultAdmin).isEmpty()) {
            userService.create(defaultAdmin, "admin123", "Admin", "System", Role.ADMIN);
            log.info("Admin par defaut cree: {} / admin123", defaultAdmin);
        } else {
            log.info("Admin par defaut deja present");
        }
    }
}
