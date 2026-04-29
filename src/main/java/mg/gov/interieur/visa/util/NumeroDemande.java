package mg.gov.interieur.visa.util;

import java.time.Year;

public final class NumeroDemande {

    private NumeroDemande() {
    }

    public static String generer(long sequenceId) {
        int annee = Year.now().getValue();
        return String.format("DEM-%d-%05d", annee, sequenceId);
    }
}
