package mg.gov.interieur.visa.dto.response;

import lombok.Builder;
import lombok.Data;
import mg.gov.interieur.visa.enums.Role;

@Data
@Builder
public class UserProfileResponse {
    private Long id;
    private String username;
    private String nom;
    private String prenom;
    private Role role;
    private boolean actif;
}
