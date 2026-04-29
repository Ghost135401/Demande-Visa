package mg.gov.interieur.visa.common.service;

import mg.gov.interieur.visa.dto.request.AccepterVisaRequest;
import mg.gov.interieur.visa.dto.response.DemandeResponse;
import mg.gov.interieur.visa.entity.CarteResident;
import mg.gov.interieur.visa.entity.Demande;
import mg.gov.interieur.visa.entity.VisaTransformable;
import mg.gov.interieur.visa.enums.StatutDemande;
import mg.gov.interieur.visa.enums.TypeDemande;
import mg.gov.interieur.visa.repository.CarteResidentRepository;
import mg.gov.interieur.visa.repository.DemandeRepository;
import mg.gov.interieur.visa.repository.PieceJustificativeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemandeServiceTest {

    @Mock
    private DemandeRepository demandeRepository;

    @Mock
    private PieceJustificativeRepository pieceRepo;

    @Mock
    private CarteResidentRepository carteRepo;

    @InjectMocks
    private DemandeService demandeService;

    @Test
    void accepterVisaTransfertReprendCarteParenteSansNouvelleCarte() {
        CarteResident carteParente = CarteResident.builder()
                .id(10L)
                .numeroCarte("CR-EXIST-001")
                .dateDelivrance(LocalDate.of(2026, 1, 10))
                .dateExpiration(LocalDate.of(2027, 1, 10))
                .build();

        Demande parente = Demande.builder()
                .id(1L)
                .numeroDemande("DEM-2026-00001")
                .statut(StatutDemande.VISA_ACCEPTE)
                .typeDemande(TypeDemande.NOUVEAU_TITRE)
                .carteResident(carteParente)
                .build();

        Demande transfert = Demande.builder()
                .id(2L)
                .numeroDemande("DEM-2026-00002")
                .statut(StatutDemande.SCAN_TERMINE)
                .typeDemande(TypeDemande.TRANSFERT_VISA)
                .demandeParente(parente)
                .build();

        when(demandeRepository.findByIdWithPieces(2L)).thenReturn(Optional.of(transfert));
        when(demandeRepository.save(any(Demande.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DemandeResponse response = demandeService.accepterVisa(2L, new AccepterVisaRequest());

        assertEquals(StatutDemande.VISA_ACCEPTE, response.getStatut());
        assertEquals("CR-EXIST-001", response.getCarteResident().getNumeroCarte());
        assertSame(carteParente, transfert.getCarteResident());
        verify(carteRepo, never()).save(any(CarteResident.class));
    }

    @Test
    void accepterVisaNouvelleDemandeUtiliseDateSortieDuVisaCommeExpiration() {
        Demande demande = Demande.builder()
                .id(3L)
                .numeroDemande("DEM-2026-00003")
                .statut(StatutDemande.SCAN_TERMINE)
                .typeDemande(TypeDemande.NOUVEAU_TITRE)
                .visaTransformable(VisaTransformable.builder()
                        .refVisa("VT-01")
                        .dateSortie(LocalDate.of(2026, 12, 31))
                        .build())
                .build();

        AccepterVisaRequest request = new AccepterVisaRequest();
        request.setNumeroCarte("CR-NEW-001");
        request.setDateDelivrance(LocalDate.of(2026, 5, 1));

        when(demandeRepository.findByIdWithPieces(3L)).thenReturn(Optional.of(demande));
        when(carteRepo.save(any(CarteResident.class))).thenAnswer(invocation -> {
            CarteResident carte = invocation.getArgument(0);
            carte.setId(22L);
            return carte;
        });
        when(demandeRepository.save(any(Demande.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DemandeResponse response = demandeService.accepterVisa(3L, request);

        assertEquals(LocalDate.of(2026, 12, 31), response.getCarteResident().getDateExpiration());
        assertEquals("CR-NEW-001", response.getCarteResident().getNumeroCarte());
        assertTrue(!response.isModifiable());
    }
}
