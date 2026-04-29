package mg.gov.interieur.visa.sprint3.controller;

import mg.gov.interieur.visa.sprint3.service.ScanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/scan")
public class ScanController {

    private final ScanService scanService;

    public ScanController(ScanService scanService) {
        this.scanService = scanService;
    }

    @GetMapping("/complete")
    public ResponseEntity<Map<String, Object>> completeScan(
            @RequestParam(name = "id", required = false) Long demandeId
    ) {
        return ResponseEntity.ok(scanService.performCompleteScan(demandeId));
    }
}
