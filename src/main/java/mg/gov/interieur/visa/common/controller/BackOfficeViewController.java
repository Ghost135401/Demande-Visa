package mg.gov.interieur.visa.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BackOfficeViewController {

    @GetMapping({"/", "/backoffice"})
    public String backOffice() {
        return "backoffice/index";
    }

    @GetMapping("/backoffice/dossiers")
    public String dossiers() {
        return "backoffice/dossiers";
    }

    @GetMapping("/backoffice/sprint1")
    public String sprint1() {
        return "backoffice/sprint1/creation-visa";
    }

    @GetMapping("/backoffice/sprint2/duplicata")
    public String sprint2Duplicata() {
        return "backoffice/sprint2/sprint2-initial/duplicata";
    }

    @GetMapping("/backoffice/sprint2/transfert")
    public String sprint2Transfert() {
        return "backoffice/sprint2/sprint2-bis/transfert";
    }

    @GetMapping("/backoffice/scan")
    public String scan() {
        return "backoffice/sprint3/scan";
    }

    @GetMapping("/backoffice/sprint4/qrcode")
    public String sprint4Qrcode() {
        return "backoffice/sprint4/qrcode";
    }
}
