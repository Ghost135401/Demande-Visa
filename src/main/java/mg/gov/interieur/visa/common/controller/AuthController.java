package mg.gov.interieur.visa.common.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mg.gov.interieur.visa.dto.request.LoginRequest;
import mg.gov.interieur.visa.dto.response.ApiResponse;
import mg.gov.interieur.visa.dto.response.UserProfileResponse;
import mg.gov.interieur.visa.common.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserProfileResponse>> login(
            @Valid @RequestBody LoginRequest req,
            HttpServletRequest request) {
        UserProfileResponse profile = userService.authenticate(req.getUsername(), req.getPassword());
        HttpSession session = request.getSession(true);
        session.setAttribute("currentUser", profile);
        return ResponseEntity.ok(ApiResponse.ok("Authentification reussie", profile));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        HttpSession s = request.getSession(false);
        if (s != null) s.invalidate();
        return ResponseEntity.ok(ApiResponse.ok("Deconnexion effectuee", null));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> profile(HttpServletRequest request) {
        HttpSession s = request.getSession(false);
        if (s == null) return ResponseEntity.status(401).body(ApiResponse.error("Non authentifie"));
        UserProfileResponse p = (UserProfileResponse) s.getAttribute("currentUser");
        if (p == null) return ResponseEntity.status(401).body(ApiResponse.error("Non authentifie"));
        return ResponseEntity.ok(ApiResponse.ok(p));
    }
}
