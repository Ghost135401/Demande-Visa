package mg.gov.interieur.visa.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mg.gov.interieur.visa.config.FileStorageConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final FileStorageConfig config;

    /**
     * Stocke le fichier dans uploads/{demandeId}/{pieceId}_{uuid}.ext
     */
    public String stocker(MultipartFile fichier, Long demandeId, Long pieceId) throws IOException {
        String extension = getExtension(fichier.getOriginalFilename());
        String nomFichier = pieceId + "_" + UUID.randomUUID() + extension;
        Path dossier = Paths.get(config.getUploadDir(), String.valueOf(demandeId));
        Files.createDirectories(dossier);
        Path destination = dossier.resolve(nomFichier);
        fichier.transferTo(destination);
        log.info("Fichier stocke : {}", destination);
        return destination.toString();
    }

    public void supprimer(String cheminFichier) throws IOException {
        if (cheminFichier == null) {
            return;
        }

        Path path = Paths.get(cheminFichier);
        if (Files.exists(path)) {
            Files.delete(path);
            log.info("Fichier supprime : {}", path);
        }
    }

    private String getExtension(String nomOriginal) {
        if (nomOriginal == null || !nomOriginal.contains(".")) {
            return "";
        }
        return nomOriginal.substring(nomOriginal.lastIndexOf("."));
    }
}
