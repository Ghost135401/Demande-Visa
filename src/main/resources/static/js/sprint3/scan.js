const API_BASE = window.APP_CONTEXT_PATH || "";

const elements = {
    startScanButton: document.getElementById("start-scan"),
    scanContainer: document.getElementById("scan-container"),
    scanResults: document.getElementById("scan-results"),
    totalScanned: document.getElementById("total-scanned"),
    totalErrors: document.getElementById("total-errors"),
    businessLogicOk: document.getElementById("business-logic-ok"),
    scanErrors: document.getElementById("scan-errors"),
    scanDetails: document.getElementById("scan-details"),
    scanStatus: document.querySelector(".scan-status"),
    progressBar: document.querySelector(".progress-bar"),
    demandeInfo: document.getElementById("demande-info"),
    demandeId: document.getElementById("demande-id")
};

// Element bouton 'Scan terminé'
elements.scanCompleteButton = document.getElementById("scan-complete");

elements.startScanButton.addEventListener("click", startScan);

// Si un ID de demande est fourni dans l'URL, lancer le scan automatiquement
if (window.DEMANDE_ID) {
    elements.demandeInfo.classList.remove("hidden");
    elements.demandeId.textContent = window.DEMANDE_ID;
    // Attendre un court délai avant de lancer le scan automatiquement
    setTimeout(() => {
        startScan();
    }, 500);
}

// Handler pour le bouton 'Scan terminé' : toujours visible après le scan.
if (elements.scanCompleteButton) {
    elements.scanCompleteButton.addEventListener('click', function () {
        // Si on scannait une demande précise, ouvrir le backoffice sur cette demande
        if (window.DEMANDE_ID) {
            window.location.href = `${API_BASE}/backoffice?openDemande=${encodeURIComponent(window.DEMANDE_ID)}&openAccept=true`;
        } else {
            // Rediriger vers le dashboard backoffice
            window.location.href = `${API_BASE}/backoffice`;
        }
    });
}

async function startScan() {
    elements.startScanButton.disabled = true;
    elements.scanContainer.classList.remove("hidden");
    elements.scanResults.classList.add("hidden");
    elements.scanErrors.innerHTML = "";
    elements.scanDetails.innerHTML = "";

    try {
        const params = new URLSearchParams();
        if (window.DEMANDE_ID) {
            params.set("id", window.DEMANDE_ID);
        }
        const response = await fetch(
            `${API_BASE}/scan/complete${params.toString() ? `?${params.toString()}` : ""}`
        );
        const result = await parseApiResponse(response);

        await animateScan(result);
        displayResults(result);
    } catch (error) {
        showToast(error.message, true);
        elements.startScanButton.disabled = false;
    }
}

async function animateScan(result) {
    const totalItems = result.totalItems || 100;
    const scanDuration = 3000; // 3 secondes pour l'animation
    const updateInterval = 50; // Mise à jour toutes les 50ms
    const steps = scanDuration / updateInterval;
    const increment = totalItems / steps;

    let current = 0;
    let errors = 0;

    return new Promise((resolve) => {
        const interval = setInterval(() => {
            current += increment;
            if (current >= totalItems) {
                current = totalItems;
                clearInterval(interval);
                resolve();
            }

            elements.progressBar.style.width = `${(current / totalItems) * 100}%`;

            const progress = Math.floor((current / totalItems) * 100);

            if (progress < 20) {
                elements.scanStatus.textContent = "Initialisation du scan...";
            } else if (progress < 40) {
                elements.scanStatus.textContent = "Scan des demandes en cours...";
            } else if (progress < 60) {
                elements.scanStatus.textContent = "Vérification de la logique métier...";
            } else if (progress < 80) {
                elements.scanStatus.textContent = "Analyse des erreurs...";
            } else {
                elements.scanStatus.textContent = "Finalisation...";
            }

            // Simuler la détection d'erreurs
            if (result.errors && result.errors.length > 0) {
                const errorIndex = Math.floor((current / totalItems) * result.errors.length);
                errors = result.errors.slice(0, errorIndex + 1).length;
            }

            elements.totalScanned.textContent = Math.floor(current);
            elements.totalErrors.textContent = errors;
        }, updateInterval);
    });
}

function displayResults(result) {
    elements.scanResults.classList.remove("hidden");

    // Afficher le récapitulatif
    elements.totalScanned.textContent = result.totalItems || 0;
    elements.totalErrors.textContent = result.errors ? result.errors.length : 0;

    const businessLogicPercentage = result.businessLogicValid !== undefined && result.totalItems > 0
        ? Math.round((result.businessLogicValid / result.totalItems) * 100)
        : 100;
    elements.businessLogicOk.textContent = `${businessLogicPercentage}%`;

    // Afficher les erreurs
    if (result.errors && result.errors.length > 0) {
        elements.scanErrors.innerHTML = result.errors.map(error => {
            const metaParts = [];
            if (error.demandeId) metaParts.push('Demande #' + error.demandeId);
            if (error.pieceId) metaParts.push('Pièce #' + error.pieceId);
            const metaHtml = metaParts.length ? `<div class="error-meta">${escapeHtml(metaParts.join(' — '))}</div>` : '';
            return `
            <div class="error-item">
                <div class="error-message">
                    <div class="error-type">${escapeHtml(error.type)}</div>
                    <div class="error-detail">${escapeHtml(error.message)}</div>
                    ${metaHtml}
                </div>
            </div>
        `;
        }).join("");
    }

    // Toujours afficher le bouton 'Scan terminé' après affichage des résultats
    if (elements.scanCompleteButton) {
        elements.scanCompleteButton.classList.remove('hidden');
        elements.scanCompleteButton.disabled = false;
    }

    // Afficher les détails
    if (result.details && result.details.length > 0) {
        elements.scanDetails.innerHTML = result.details.map(detail => `
            <div class="detail-item">
                <div class="detail-type">${escapeHtml(detail.type)}</div>
                <div class="detail-content">${escapeHtml(detail.content)}</div>
            </div>
        `).join("");
    }

    elements.startScanButton.disabled = false;
}

function escapeHtml(text) {
    if (!text) return "";
    const div = document.createElement("div");
    div.textContent = text;
    return div.innerHTML;
}

async function parseApiResponse(response) {
    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || "Une erreur est survenue");
    }
    return await response.json();
}

function showToast(message, isError = false) {
    const toast = document.getElementById("toast");
    if (!toast) return;

    toast.textContent = message;
    toast.classList.remove("hidden");
    toast.classList.toggle("error", isError);

    setTimeout(() => {
        toast.classList.add("hidden");
    }, 3000);
}
