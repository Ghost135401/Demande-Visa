package mg.gov.interieur.visa.dto.response;

import lombok.Builder;
import lombok.Data;
import mg.gov.interieur.visa.enums.TypePiece;

import java.time.LocalDateTime;

@Data
@Builder
public class PieceJustificativeResponse {

    private Long id;
    private TypePiece typePiece;
    private String libelle;
    private boolean estCochee;
    private boolean estUploadee;
    private String nomFichier;
    private LocalDateTime dateUpload;
    private boolean obligatoire;
}
