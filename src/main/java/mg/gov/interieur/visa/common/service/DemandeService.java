package mg.gov.interieur.visa.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mg.gov.interieur.visa.dto.request.AccepterVisaRequest;
import mg.gov.interieur.visa.dto.request.CreateDemandeAvecVisaRequest;
import mg.gov.interieur.visa.dto.request.CreateDemandeRequest;
import mg.gov.interieur.visa.dto.request.EtatCivilRequest;
import mg.gov.interieur.visa.dto.request.PasseportRequest;
import mg.gov.interieur.visa.dto.request.UpdateDemandeRequest;
import mg.gov.interieur.visa.dto.request.VisaTransformableRequest;
import mg.gov.interieur.visa.dto.response.CarteResidentResponse;
import mg.gov.interieur.visa.dto.response.DemandeResponse;
import mg.gov.interieur.visa.dto.response.PasseportResponse;
import mg.gov.interieur.visa.dto.response.PieceJustificativeResponse;
import mg.gov.interieur.visa.dto.response.VisaTransformableResponse;
import mg.gov.interieur.visa.entity.CarteResident;
import mg.gov.interieur.visa.entity.Demande;
import mg.gov.interieur.visa.entity.EtatCivil;
import mg.gov.interieur.visa.entity.Passeport;
import mg.gov.interieur.visa.entity.PieceJustificative;
import mg.gov.interieur.visa.entity.VisaTransformable;
import mg.gov.interieur.visa.enums.CategorieDemande;
import mg.gov.interieur.visa.enums.StatutDemande;
import mg.gov.interieur.visa.enums.TypeDemande;
import mg.gov.interieur.visa.enums.TypePiece;
import mg.gov.interieur.visa.exception.DemandeNonModifiableException;
import mg.gov.interieur.visa.exception.ResourceNotFoundException;
import mg.gov.interieur.visa.repository.CarteResidentRepository;
import mg.gov.interieur.visa.repository.DemandeRepository;
import mg.gov.interieur.visa.repository.PieceJustificativeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DemandeService {

    private final DemandeRepository demandeRepository;
    private final PieceJustificativeRepository pieceRepo;
    private final CarteResidentRepository carteRepo;

    public DemandeResponse creerDemande(CreateDemandeRequest req) {
        validerCreation(req);

        TypeDemande type = req.getTypeDemande();
        Passeport passeport = mapPasseport(req.getPasseport());
        VisaTransformable visaTransformable = null;

        if (TypeDemande.NOUVEAU_TITRE.equals(type)) {
            visaTransformable = mapVisa(req.getVisaTransformable());
        } else {
            Demande parente = chargerEtValiderDemandeParente(req.getDemandeParenteId(), type);
            if (TypeDemande.DUPLICATA.equals(type)) {
                passeport = parente.getPasseport();
            } else if (parente.getPasseport() != null
                    && req.getPasseport().getNumero().equals(parente.getPasseport().getNumero())) {
                throw new IllegalArgumentException(
                        "Le numero du nouveau passeport doit etre different de l'ancien pour un transfert de visa");
            }
        }

        Demande demande = Demande.builder()
                .numeroDemande(genererNumero())
                .categorie(req.getCategorie())
                .typeDemande(type)
                .statut(StatutDemande.DOSSIER_CREE)
                .sansDonneesAnterieures(false)
                .resident(Boolean.TRUE.equals(req.getResident()))
                .etatCivil(mapEtatCivil(req.getEtatCivil()))
                .passeport(passeport)
                .visaTransformable(visaTransformable)
                .build();

        rattacherDemandeParente(demande, req.getDemandeParenteId());

        demande = sauvegarderDemande(demande);
        List<PieceJustificative> pieces = genererPieces(demande);
        pieceRepo.saveAll(pieces);
        demande.setPiecesJustificatives(pieces);

        log.info("Demande creee : {}", demande.getNumeroDemande());
        return toResponse(demande);
    }

    public DemandeResponse creerDemandeAvecVisa(CreateDemandeAvecVisaRequest req) {
        validerCreationAvecVisa(req);

        TypeDemande type = req.getTypeDemande();
        Passeport passeport = mapPasseport(req.getPasseport());
        VisaTransformable visaTransformable = null;
        CarteResident carte = null;

        if (TypeDemande.NOUVEAU_TITRE.equals(type)) {
            visaTransformable = mapVisa(req.getVisaTransformable());
            carte = CarteResident.builder()
                    .numeroCarte(req.getNumeroCarte())
                    .dateDelivrance(req.getDateDelivranceCarte())
                    .dateExpiration(visaTransformable.getDateSortie())
                    .build();
            validerDatesCarte(carte.getDateDelivrance(), carte.getDateExpiration());
            carte = sauvegarderCarte(carte);
        } else {
            Demande parente = chargerEtValiderDemandeParente(req.getDemandeParenteId(), type);
            if (TypeDemande.DUPLICATA.equals(type)) {
                passeport = parente.getPasseport();
                LocalDate dateExp = req.getDateExpirationCarte() != null
                        ? req.getDateExpirationCarte()
                        : parente.getCarteResident().getDateExpiration();
                carte = CarteResident.builder()
                        .numeroCarte(req.getNumeroCarte())
                        .dateDelivrance(req.getDateDelivranceCarte())
                        .dateExpiration(dateExp)
                        .build();
                validerDatesCarte(carte.getDateDelivrance(), carte.getDateExpiration());
                carte = sauvegarderCarte(carte);
            } else {
                if (parente.getPasseport() != null
                        && req.getPasseport().getNumero().equals(parente.getPasseport().getNumero())) {
                    throw new IllegalArgumentException(
                            "Le numero du nouveau passeport doit etre different de l'ancien pour un transfert de visa");
                }
                carte = parente.getCarteResident();
            }
        }

        Demande demande = Demande.builder()
                .numeroDemande(genererNumero())
                .categorie(req.getCategorie())
                .typeDemande(type)
                .statut(StatutDemande.VISA_ACCEPTE)
                .sansDonneesAnterieures(true)
                .resident(Boolean.TRUE.equals(req.getResident()))
                .etatCivil(mapEtatCivil(req.getEtatCivil()))
                .passeport(passeport)
                .visaTransformable(visaTransformable)
                .carteResident(carte)
                .build();

        rattacherDemandeParente(demande, req.getDemandeParenteId());
        demande = sauvegarderDemande(demande);
        log.info("Demande legacy creee : {}", demande.getNumeroDemande());
        return toResponse(demande);
    }

    @Transactional(readOnly = true)
    public DemandeResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<DemandeResponse> getAll() {
        return demandeRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DemandeResponse> getByStatut(StatutDemande statut) {
        return demandeRepository.findByStatut(statut).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DemandeResponse> getByCategorie(CategorieDemande categorie) {
        return demandeRepository.findByCategorie(categorie).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DemandeResponse> getBySansDonneesAnterieures(boolean sansDonneesAnterieures) {
        return demandeRepository.findBySansDonneesAnterieures(sansDonneesAnterieures).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public DemandeResponse modifier(Long id, UpdateDemandeRequest req) {
        Demande demande = findOrThrow(id);
        verifierModifiable(demande);

        if (req.getEtatCivil() != null) {
            demande.setEtatCivil(mapEtatCivil(req.getEtatCivil()));
        }
        if (req.getPasseport() != null) {
            verifierModificationPasseport(demande, req.getPasseport());
            demande.setPasseport(mapPasseport(req.getPasseport()));
        }
        if (req.getVisaTransformable() != null) {
            verifierModificationVisa(demande);
            demande.setVisaTransformable(mapVisa(req.getVisaTransformable()));
        }
        if (req.getCategorie() != null && !req.getCategorie().equals(demande.getCategorie())) {
            pieceRepo.deleteAll(demande.getPiecesJustificatives());
            demande.setCategorie(req.getCategorie());
            demande = demandeRepository.save(demande);
            List<PieceJustificative> pieces = genererPieces(demande);
            pieceRepo.saveAll(pieces);
            demande.setPiecesJustificatives(pieces);
        }
        if (req.getResident() != null) {
            demande.setResident(req.getResident());
        }

        return toResponse(demandeRepository.save(demande));
    }

    public DemandeResponse marquerScanTermine(Long id, boolean force) {
        Demande demande = findOrThrow(id);

        if (!StatutDemande.DOSSIER_CREE.equals(demande.getStatut())) {
            throw new IllegalArgumentException(
                    "Le scan ne peut etre termine que depuis le statut DOSSIER_CREE");
        }

        if (!force && !demande.toutesLesPiecesObligatoiresUploadees()) {
            throw new IllegalArgumentException(
                    "Toutes les pieces obligatoires doivent etre uploadees avant de terminer le scan");
        }

        demande.setStatut(StatutDemande.SCAN_TERMINE);
        log.info("Scan termine pour la demande : {} (force={})", demande.getNumeroDemande(), force);
        return toResponse(demandeRepository.save(demande));
    }

    public DemandeResponse accepterVisa(Long id, AccepterVisaRequest req) {
        Demande demande = findOrThrow(id);
        AccepterVisaRequest request = req != null ? req : new AccepterVisaRequest();

        if (!StatutDemande.SCAN_TERMINE.equals(demande.getStatut())) {
            throw new IllegalArgumentException(
                    "L'acceptation du visa n'est possible que depuis le statut SCAN_TERMINE");
        }

        if (TypeDemande.TRANSFERT_VISA.equals(demande.getTypeDemande())) {
            if (demande.getDemandeParente() == null || demande.getDemandeParente().getCarteResident() == null) {
                throw new IllegalArgumentException(
                        "Le transfert de visa doit rester rattache a une carte de resident existante");
            }
            demande.setCarteResident(demande.getDemandeParente().getCarteResident());
            demande.setStatut(StatutDemande.VISA_ACCEPTE);
            return toResponse(sauvegarderDemande(demande));
        }

        if (request.getNumeroCarte() == null || request.getNumeroCarte().isBlank()) {
            throw new IllegalArgumentException("Le numero de carte est obligatoire pour finaliser ce dossier");
        }
        if (request.getDateDelivrance() == null) {
            throw new IllegalArgumentException("La date de delivrance de la carte est obligatoire");
        }

        LocalDate dateExpirationCarte;
        if (TypeDemande.NOUVEAU_TITRE.equals(demande.getTypeDemande())
                && demande.getVisaTransformable() != null) {
            dateExpirationCarte = demande.getVisaTransformable().getDateSortie();
        } else if (request.getDateExpiration() != null) {
            dateExpirationCarte = request.getDateExpiration();
        } else if (demande.getDemandeParente() != null
                && demande.getDemandeParente().getCarteResident() != null) {
            dateExpirationCarte = demande.getDemandeParente().getCarteResident().getDateExpiration();
        } else {
            throw new IllegalArgumentException("La date d'expiration de la carte est obligatoire");
        }

        CarteResident carte = CarteResident.builder()
                .numeroCarte(request.getNumeroCarte().trim())
                .dateDelivrance(request.getDateDelivrance())
                .dateExpiration(dateExpirationCarte)
                .build();
        validerDatesCarte(carte.getDateDelivrance(), carte.getDateExpiration());
        carte = sauvegarderCarte(carte);

        demande.setCarteResident(carte);
        demande.setStatut(StatutDemande.VISA_ACCEPTE);
        log.info("Visa accepte pour la demande : {}", demande.getNumeroDemande());
        return toResponse(sauvegarderDemande(demande));
    }

    private Demande findOrThrow(Long id) {
        return demandeRepository.findByIdWithPieces(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : " + id));
    }

    private void verifierModifiable(Demande demande) {
        if (!demande.isModifiable()) {
            throw new DemandeNonModifiableException(
                    "La demande " + demande.getNumeroDemande()
                            + " n'est plus modifiable (statut : " + demande.getStatut() + ")");
        }
    }

    private void verifierModificationPasseport(Demande demande, PasseportRequest req) {
        if (TypeDemande.DUPLICATA.equals(demande.getTypeDemande())) {
            throw new IllegalArgumentException(
                    "Le passeport d'un duplicata reprend automatiquement celui de la demande parente");
        }
        if (TypeDemande.TRANSFERT_VISA.equals(demande.getTypeDemande())
                && demande.getDemandeParente() != null
                && demande.getDemandeParente().getPasseport() != null
                && Objects.equals(req.getNumero(), demande.getDemandeParente().getPasseport().getNumero())) {
            throw new IllegalArgumentException(
                    "Le numero du nouveau passeport doit etre different de l'ancien pour un transfert de visa");
        }
    }

    private void verifierModificationVisa(Demande demande) {
        if (!TypeDemande.NOUVEAU_TITRE.equals(demande.getTypeDemande())) {
            throw new IllegalArgumentException(
                    "Le visa transformable ne peut etre modifie que pour une nouvelle demande");
        }
    }

    private String genererNumero() {
        long next = demandeRepository.findMaxId().orElse(0L) + 1;
        return String.format("DEM-%d-%05d", Year.now().getValue(), next);
    }

    private void validerCreation(CreateDemandeRequest req) {
        if (req.getCategorie() == null || req.getTypeDemande() == null || req.getEtatCivil() == null) {
            throw new IllegalArgumentException("La categorie, le type de demande et l'etat civil sont obligatoires");
        }
        if (TypeDemande.NOUVEAU_TITRE.equals(req.getTypeDemande()) && req.getPasseport() == null) {
            throw new IllegalArgumentException(
                    "Les informations du passeport sont obligatoires pour un nouveau titre");
        }
        if (TypeDemande.TRANSFERT_VISA.equals(req.getTypeDemande()) && req.getPasseport() == null) {
            throw new IllegalArgumentException(
                    "Les informations du nouveau passeport sont obligatoires pour un transfert de visa");
        }
        if (TypeDemande.NOUVEAU_TITRE.equals(req.getTypeDemande()) && req.getVisaTransformable() == null) {
            throw new IllegalArgumentException(
                    "Les informations du visa transformable sont obligatoires pour un nouveau titre");
        }
        if ((TypeDemande.DUPLICATA.equals(req.getTypeDemande())
                || TypeDemande.TRANSFERT_VISA.equals(req.getTypeDemande()))
                && req.getDemandeParenteId() == null) {
            throw new IllegalArgumentException(
                    "L'ID de la demande parente est obligatoire pour un duplicata ou transfert de visa");
        }
    }

    private void validerCreationAvecVisa(CreateDemandeAvecVisaRequest req) {
        if (req.getCategorie() == null || req.getTypeDemande() == null || req.getEtatCivil() == null) {
            throw new IllegalArgumentException("La categorie, le type de demande et l'etat civil sont obligatoires");
        }
        if (TypeDemande.NOUVEAU_TITRE.equals(req.getTypeDemande()) && req.getPasseport() == null) {
            throw new IllegalArgumentException(
                    "Les informations du passeport sont obligatoires pour un nouveau titre");
        }
        if (TypeDemande.TRANSFERT_VISA.equals(req.getTypeDemande()) && req.getPasseport() == null) {
            throw new IllegalArgumentException(
                    "Les informations du nouveau passeport sont obligatoires pour un transfert de visa");
        }
        if (TypeDemande.NOUVEAU_TITRE.equals(req.getTypeDemande()) && req.getVisaTransformable() == null) {
            throw new IllegalArgumentException(
                    "Les informations du visa transformable sont obligatoires pour un nouveau titre");
        }
        if ((TypeDemande.DUPLICATA.equals(req.getTypeDemande())
                || TypeDemande.TRANSFERT_VISA.equals(req.getTypeDemande()))
                && req.getDemandeParenteId() == null) {
            throw new IllegalArgumentException(
                    "L'ID de la demande parente est obligatoire pour un duplicata ou transfert de visa");
        }
    }

    private Demande chargerEtValiderDemandeParente(Long demandeParenteId, TypeDemande typeDemande) {
        Demande parente = demandeRepository.findById(demandeParenteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Demande parente introuvable : " + demandeParenteId));

        if (!StatutDemande.VISA_ACCEPTE.equals(parente.getStatut())) {
            throw new IllegalArgumentException(
                    "La demande parente doit etre au statut VISA_ACCEPTE pour creer un "
                            + typeDemande.getLibelle());
        }
        if ((TypeDemande.DUPLICATA.equals(typeDemande) || TypeDemande.TRANSFERT_VISA.equals(typeDemande))
                && parente.getCarteResident() == null) {
            throw new IllegalArgumentException(
                    "La demande parente doit deja disposer d'une carte de resident");
        }

        return parente;
    }

    private void validerDatesCarte(LocalDate dateDelivrance, LocalDate dateExpiration) {
        if (dateDelivrance == null || dateExpiration == null) {
            throw new IllegalArgumentException("Les dates de la carte de resident sont obligatoires");
        }
        if (dateExpiration.isBefore(dateDelivrance)) {
            throw new IllegalArgumentException(
                    "La date d'expiration de la carte doit etre posterieure ou egale a la date de delivrance");
        }
    }

    private void rattacherDemandeParente(Demande demande, Long demandeParenteId) {
        if (demandeParenteId == null) {
            return;
        }
        try {
            demandeRepository.findById(demandeParenteId).ifPresent(demande::setDemandeParente);
        } catch (Exception e) {
            log.warn("Impossible de charger la demande parente: {}", demandeParenteId);
        }
    }

    private Demande sauvegarderDemande(Demande demande) {
        try {
            return demandeRepository.save(demande);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException(
                    "Impossible d'enregistrer la demande. Verifiez l'unicite du numero de passeport, "
                            + "du numero de carte ou des references associees.");
        }
    }

    private CarteResident sauvegarderCarte(CarteResident carte) {
        try {
            return carteRepo.save(carte);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException(
                    "Le numero de carte de resident existe deja : " + carte.getNumeroCarte());
        }
    }

    private List<PieceJustificative> genererPieces(Demande demande) {
        return Arrays.stream(TypePiece.values())
                .filter(typePiece ->
                        (CategorieDemande.TRAVAILLEUR.equals(demande.getCategorie()) && typePiece.isTravailleur())
                                || (CategorieDemande.INVESTISSEUR.equals(demande.getCategorie())
                                && typePiece.isInvestisseur()))
                .map(typePiece -> PieceJustificative.builder()
                        .demande(demande)
                        .typePiece(typePiece)
                        .libelle(typePiece.getLibelle())
                        .estCochee(false)
                        .estUploadee(false)
                        .obligatoire(typePiece.isObligatoire())
                        .build())
                .collect(Collectors.toList());
    }

    private EtatCivil mapEtatCivil(EtatCivilRequest request) {
        return EtatCivil.builder()
                .nom(request.getNom())
                .prenoms(request.getPrenoms())
                .nomJeuneFille(request.getNomJeuneFille())
                .situationFamiliale(request.getSituationFamiliale())
                .nationalite(request.getNationalite())
                .profession(request.getProfession())
                .dateNaissance(request.getDateNaissance())
                .lieuNaissance(request.getLieuNaissance())
                .adresseMadagascar(request.getAdresseMadagascar())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .build();
    }

    private Passeport mapPasseport(PasseportRequest request) {
        if (request == null) {
            return null;
        }
        return Passeport.builder()
                .numero(request.getNumero())
                .dateDelivrance(request.getDateDelivrance())
                .dateExpiration(request.getDateExpiration())
                .build();
    }

    private VisaTransformable mapVisa(VisaTransformableRequest request) {
        if (request == null) {
            return null;
        }
        return VisaTransformable.builder()
                .refVisa(request.getRefVisa())
                .dateEntree(request.getDateEntree())
                .lieuEntree(request.getLieuEntree())
                .dateSortie(request.getDateSortie())
                .build();
    }

    public DemandeResponse toResponse(Demande demande) {
        return DemandeResponse.builder()
                .id(demande.getId())
                .numeroDemande(demande.getNumeroDemande())
                .categorie(demande.getCategorie())
                .statut(demande.getStatut())
                .statutLibelle(demande.getStatut().getLibelle())
                .typeDemande(demande.getTypeDemande())
                .sansDonneesAnterieures(demande.isSansDonneesAnterieures())
                .modifiable(demande.isModifiable())
                .nom(demande.getEtatCivil() != null ? demande.getEtatCivil().getNom() : null)
                .prenoms(demande.getEtatCivil() != null ? demande.getEtatCivil().getPrenoms() : null)
                .nomJeuneFille(demande.getEtatCivil() != null ? demande.getEtatCivil().getNomJeuneFille() : null)
                .situationFamiliale(demande.getEtatCivil() != null ? demande.getEtatCivil().getSituationFamiliale() : null)
                .nationalite(demande.getEtatCivil() != null ? demande.getEtatCivil().getNationalite() : null)
                .profession(demande.getEtatCivil() != null ? demande.getEtatCivil().getProfession() : null)
                .dateNaissance(demande.getEtatCivil() != null ? demande.getEtatCivil().getDateNaissance() : null)
                .lieuNaissance(demande.getEtatCivil() != null ? demande.getEtatCivil().getLieuNaissance() : null)
                .adresseMadagascar(demande.getEtatCivil() != null ? demande.getEtatCivil().getAdresseMadagascar() : null)
                .email(demande.getEtatCivil() != null ? demande.getEtatCivil().getEmail() : null)
                .telephone(demande.getEtatCivil() != null ? demande.getEtatCivil().getTelephone() : null)
                .passeport(demande.getPasseport() != null
                        ? PasseportResponse.builder()
                        .id(demande.getPasseport().getId())
                        .numero(demande.getPasseport().getNumero())
                        .dateDelivrance(demande.getPasseport().getDateDelivrance())
                        .dateExpiration(demande.getPasseport().getDateExpiration())
                        .build()
                        : null)
                .visaTransformable(demande.getVisaTransformable() != null
                        ? VisaTransformableResponse.builder()
                        .id(demande.getVisaTransformable().getId())
                        .refVisa(demande.getVisaTransformable().getRefVisa())
                        .dateEntree(demande.getVisaTransformable().getDateEntree())
                        .lieuEntree(demande.getVisaTransformable().getLieuEntree())
                        .dateSortie(demande.getVisaTransformable().getDateSortie())
                        .build()
                        : null)
                .carteResident(demande.getCarteResident() != null
                        ? CarteResidentResponse.builder()
                        .id(demande.getCarteResident().getId())
                        .numeroCarte(demande.getCarteResident().getNumeroCarte())
                        .dateDelivrance(demande.getCarteResident().getDateDelivrance())
                        .dateExpiration(demande.getCarteResident().getDateExpiration())
                        .createdAt(demande.getCarteResident().getCreatedAt())
                        .build()
                        : null)
                .piecesJustificatives(demande.getPiecesJustificatives() != null
                        ? demande.getPiecesJustificatives().stream()
                        .map(piece -> PieceJustificativeResponse.builder()
                                .id(piece.getId())
                                .typePiece(piece.getTypePiece())
                                .libelle(piece.getLibelle())
                                .estCochee(piece.isEstCochee())
                                .estUploadee(piece.isEstUploadee())
                                .nomFichier(piece.getNomFichier())
                                .dateUpload(piece.getDateUpload())
                                .obligatoire(piece.isObligatoire())
                                .build())
                        .collect(Collectors.toList())
                        : Collections.emptyList())
                .demandeParenteId(demande.getDemandeParente() != null ? demande.getDemandeParente().getId() : null)
                .demandeParenteNumero(demande.getDemandeParente() != null ? demande.getDemandeParente().getNumeroDemande() : null)
                .createdAt(demande.getCreatedAt())
                .updatedAt(demande.getUpdatedAt())
                .resident(demande.isResident())
                .build();
    }
}
