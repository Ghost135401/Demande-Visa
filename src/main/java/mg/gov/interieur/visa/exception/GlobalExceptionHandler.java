package mg.gov.interieur.visa.exception;

import mg.gov.interieur.visa.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(DemandeNonModifiableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNonModifiable(
            DemandeNonModifiableException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(
            MaxUploadSizeExceededException exception
    ) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiResponse.error("Fichier trop volumineux. Taille maximale : 20 Mo"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach(error -> {
            String field = error instanceof FieldError fieldError
                    ? fieldError.getField()
                    : error.getObjectName();
            errors.put(field, error.getDefaultMessage());
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .message("Erreurs de validation")
                        .data(errors)
                        .build());
    }

    @ExceptionHandler(BadSqlGrammarException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadSqlGrammar(BadSqlGrammarException exception) {
        String message = exception.getSQLException().getMessage();
        String errorMessage = "Erreur de base de données";
        String solution = "Veuillez contacter l'administrateur système";
        
        // Gérer spécifiquement l'erreur "la relation n'existe pas"
        if (message.contains("n'existe pas") || message.contains("does not exist")) {
            errorMessage = "La table demandée n'existe pas dans la base de données";
            solution = "Veuillez exécuter les migrations de base de données (Flyway)";
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(errorMessage + "\nSolution : " + solution));
    }
    
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccess(DataAccessException exception) {
        String errorMessage = "Erreur d'accès aux données";
        String solution = "Veuillez vérifier la connexion à la base de données";
        
        // Analyser le message d'erreur pour fournir une solution spécifique
        String message = exception.getMessage();
        if (message.contains("connexion") || message.contains("connection")) {
            errorMessage = "Impossible de se connecter à la base de données";
            solution = "Vérifiez que PostgreSQL est démarré et que les identifiants sont corrects";
        } else if (message.contains("duplicate key")) {
            errorMessage = "Cette donnée existe déjà dans la base";
            solution = "Vérifiez que vous n'essayez pas de créer un doublon";
        } else if (message.contains("violates foreign key")) {
            errorMessage = "Violation de contrainte de clé étrangère";
            solution = "Vérifiez que les données référencées existent";
        } else if (message.contains("violates check constraint")) {
            errorMessage = "Violation de contrainte de validation";
            solution = "Vérifiez que toutes les données respectent les règles de validation";
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(errorMessage + "\nSolution : " + solution));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erreur interne : " + exception.getMessage()));
    }
}
