const API_BASE = window.APP_CONTEXT_PATH || "";

const state = {
    allDemandes: [],
    displayedDemandes: [],
    selectedDemande: null,
    selectedSprintFilter: "ALL"
};

const elements = {
    tableBody: document.getElementById("demande-table-body"),
    listSelector: document.getElementById("list-selector"),
    refreshButton: document.getElementById("refresh-dashboard"),
    sprint1Form: document.getElementById("sprint1-form"),
    sprint2Form: document.getElementById("sprint2-form"),
    filterStatut: document.getElementById("filter-statut"),
    filterCategorie: document.getElementById("filter-categorie"),
    filterType: document.getElementById("filter-type"),
    applyFilters: document.getElementById("apply-filters"),
    detailEmpty: document.getElementById("detail-empty"),
    detailContent: document.getElementById("detail-content"),
    piecesList: document.getElementById("pieces-list"),
    scanTermineButton: document.getElementById("scan-termine-button"),
    openAcceptModal: document.getElementById("open-accept-modal"),
    acceptModal: document.getElementById("accept-modal"),
    acceptForm: document.getElementById("accept-form"),
    linkedRequestForm: document.getElementById("linked-request-form"),
    linkedRequestNote: document.getElementById("linked-request-note"),
    toast: document.getElementById("toast"),
    sprint1List: document.getElementById("sprint1-list"),
    sprint2List: document.getElementById("sprint2-list"),
    sprint1Result: document.getElementById("sprint1-result"),
    sprint2Result: document.getElementById("sprint2-result")
};

// Safe toast helper used by global error handlers (falls back to DOM if showToast isn't defined yet)
function safeShowToast(message, isError = false) {
    try {
        if (typeof showToast === 'function') {
            showToast(message, isError);
            return;
        }
        const t = document.getElementById('toast');
        if (!t) return;
        t.textContent = message;
        t.classList.remove('hidden');
        if (isError) t.classList.add('error');
        window.clearTimeout(safeShowToast.timeout);
        safeShowToast.timeout = window.setTimeout(() => t.classList.add('hidden'), 4000);
    } catch (e) {
        console.error('safeShowToast failed', e);
    }
}

// Global error handlers to surface JS errors visibly and to console for debugging
window.addEventListener('error', function (ev) {
    console.error('Global JS error:', ev.message, ev.filename + ':' + ev.lineno);
    safeShowToast('Erreur JavaScript: ' + ev.message, true);
});
window.addEventListener('unhandledrejection', function (ev) {
    console.error('Unhandled promise rejection:', ev.reason);
    const msg = ev.reason && ev.reason.message ? ev.reason.message : String(ev.reason);
    safeShowToast('Erreur promise non gérée: ' + msg, true);
});

// Utilitaire partagé pour parser une réponse d'API JSON et remonter une erreur lisible.
// Détecte aussi les réponses HTML (login page) et redirige vers la page de connexion.
async function parseApiResponse(response) {
    const contentType = response.headers.get('content-type') || '';

    // Cas: non authentifié -> AuthInterceptor peut rediriger vers la page de login
    if (response.status === 401 || contentType.includes('text/html') || (response.url && response.url.includes('/backoffice/login'))) {
        // Redirection côté client vers le formulaire de connexion
        console.warn('API réponse indique non authentifié ou retourne HTML; redirection vers login');
        window.location.href = `${API_BASE}/backoffice/login`;
        throw new Error('Redirection vers la page de connexion');
    }

    // Si la réponse n'est pas OK et est JSON, extraire le message d'erreur
    if (!response.ok) {
        if (contentType.includes('application/json')) {
            try {
                const err = await response.json();
                throw new Error(err.message || JSON.stringify(err) || response.statusText);
            } catch (e) {
                throw new Error(response.statusText || 'Une erreur est survenue');
            }
        }
        throw new Error(response.statusText || 'Une erreur est survenue');
    }

    // OK: attendre JSON
    if (contentType.includes('application/json')) {
        return await response.json();
    }

    // Si on arrive ici, la réponse est OK mais pas JSON (probablement HTML) -> rediriger
    console.warn('Réponse API inattendue (non-JSON) :', contentType, response.url);
    window.location.href = `${API_BASE}/backoffice/login`;
    throw new Error('Redirection vers la page de connexion');
}

document.querySelectorAll(".segment").forEach((button) => {
    button.addEventListener("click", () => {
        document.querySelectorAll(".segment").forEach((segment) => segment.classList.remove("active"));
        button.classList.add("active");
        setSelectedScope(button.dataset.filter, true);
        renderTable();
    });
});
if (elements.listSelector) {
    elements.listSelector.addEventListener("change", () => {
        setSelectedScope(elements.listSelector.value, true);
        renderTable();
    });
}
if (elements.filterType) {
    elements.filterType.addEventListener('change', () => {
        renderTable();
    });
}
if (elements.refreshButton) elements.refreshButton.addEventListener("click", loadDashboard);
if (elements.applyFilters) elements.applyFilters.addEventListener("click", loadDashboard);
if (elements.sprint1Form) elements.sprint1Form.addEventListener("submit", (event) => submitDemande(event, false));
if (elements.sprint2Form) elements.sprint2Form.addEventListener("submit", (event) => submitDemande(event, true));
if (elements.scanTermineButton) elements.scanTermineButton.addEventListener("click", () => markScanTermine(false));
if (elements.openAcceptModal) elements.openAcceptModal.addEventListener("click", async () => {
    if (!state.selectedDemande) {
        showToast("Selectionnez une demande avant de lancer cette action.", true);
        return;
    }
    // Si le statut n'est pas SCAN_TERMINE, tenter de marquer le scan comme termine
    if (state.selectedDemande.statut !== "SCAN_TERMINE") {
        if (!confirm('La demande n\'est pas au statut "Scan termine". Voulez-vous marquer le scan comme termine (proposition de forcer si des pièces manquent) ?')) {
            return;
        }
        await markScanTermine(false);
        // recharger la demande après tentative
        await selectDemande(state.selectedDemande.id);
    }
    updateAcceptModalForDemande(state.selectedDemande);
    try { if (elements.acceptModal) elements.acceptModal.showModal(); } catch (e) { console.warn('open accept modal failed', e); }
});

// Bouton "Generer QR / Generer visa" : suivre même workflow que acceptation
const genBtn = document.getElementById('generate-qr-button');
if (genBtn) {
    genBtn.addEventListener('click', async () => {
        if (!state.selectedDemande) {
            showToast('Selectionnez une demande.', true);
            return;
        }
        return fetchAndShowQr(`${API_BASE}/qrcode/demandes/${state.selectedDemande.id}`);
        if (state.selectedDemande.statut !== 'SCAN_TERMINE') {
            if (!confirm('La demande n\'est pas au statut "Scan termine". Voulez-vous marquer le scan comme termine (proposition de forcer si des pièces manquent) ?')) {
                return;
            }
            await markScanTermine(false);
            await selectDemande(state.selectedDemande.id);
        }

        // Ouvrir la modal d'acceptation pour permettre la generation finale (carte/visa)
        try { elements.acceptModal.showModal(); } catch (e) { console.warn('open accept modal failed', e); }
    });
}
if (elements.acceptForm) elements.acceptForm.addEventListener("submit", acceptVisa);
if (elements.linkedRequestForm) elements.linkedRequestForm.addEventListener("submit", createLinkedRequest);

// QR modal handlers
function showQRModal(dataUri, details = null) {
    const modal = document.getElementById('qr-modal');
    const img = document.getElementById('qr-image');
    const dl = document.getElementById('qr-download');
    const detailsContainer = document.getElementById('qr-details');
    if (!modal || !img) return;
    img.src = dataUri;
    dl.href = dataUri;
    if (detailsContainer) {
        detailsContainer.innerHTML = renderQrDetails(details);
    }
    try { modal.showModal(); } catch (e) { console.warn('show qr modal failed', e); }
}
document.addEventListener('click', (ev) => {
    if (ev.target && ev.target.id === 'qr-close') {
        const m = document.getElementById('qr-modal');
        if (m) try { m.close(); } catch (e) { console.warn(e); }
    }
});

async function fetchAndShowQr(url) {
    try {
        const resp = await fetch(url, { method: 'GET', credentials: 'same-origin', headers: { 'Accept': 'application/json' } });
        const payload = await parseApiResponse(resp);
        if (!payload) throw new Error('Reponse QR vide');
        if (payload.qrCode) {
            showQRModal(payload.qrCode, payload.data);
            return;
        }
        if (payload.data && payload.data.qrCode) {
            showQRModal(payload.data.qrCode, payload.data.data || payload.data);
            return;
        }
        throw new Error('Reponse QR inattendue');
    } catch (err) {
        showToast(err.message || 'Impossible de generer le QR', true);
    }
}

async function loadDashboard() {
    try {
        const params = new URLSearchParams();
        if (elements.filterStatut && elements.filterStatut.value) {
            params.set("statut", elements.filterStatut.value);
        }
        if (elements.filterCategorie && elements.filterCategorie.value) {
            params.set("categorie", elements.filterCategorie.value);
        }
        const query = params.toString();
        const response = await fetch(`${API_BASE}/demandes${query ? `?${query}` : ""}`, {
            method: 'GET',
            credentials: 'same-origin',
            headers: { 'Accept': 'application/json' }
        });

        // Si l'utilisateur n'est pas authentifié, rediriger vers la page de login
        if (response.status === 401) {
            // Utiliser le contexte d'application si présent
            window.location.href = `${API_BASE}/backoffice/login`;
            return;
        }

        const payload = await parseApiResponse(response);
        state.allDemandes = payload.data ?? [];
        updateStats();
        renderTable();
        if (state.selectedDemande) {
            const stillExisting = state.allDemandes.find((demande) => demande.id === state.selectedDemande.id);
            if (stillExisting) {
                await selectDemande(stillExisting.id);
            } else {
                resetDetail();
            }
        }
    } catch (error) {
        showToast(error.message, true);
    }
}

function updateStats() {
    const total = state.allDemandes.length;
    const sprint1 = state.allDemandes.filter((demande) => demande.typeDemande === "NOUVEAU_TITRE").length;
    const sprint2 = state.allDemandes.filter((demande) =>
        demande.typeDemande === "DUPLICATA" || demande.typeDemande === "TRANSFERT_VISA"
    ).length;
    const accepted = state.allDemandes.filter((demande) => demande.statut === "VISA_ACCEPTE").length;

    document.getElementById("stat-total").textContent = total;
    document.getElementById("stat-sprint1").textContent = sprint1;
    document.getElementById("stat-sprint2").textContent = sprint2;
    document.getElementById("stat-accepted").textContent = accepted;

    renderSprintLists();
}

function renderTable() {
    const filtered = state.allDemandes.filter((demande) => {
        if (state.selectedSprintFilter === "SPRINT1") {
            return demande.typeDemande === "NOUVEAU_TITRE";
        }
        if (state.selectedSprintFilter === "SPRINT2") {
            return demande.typeDemande === "DUPLICATA";
        }
        if (state.selectedSprintFilter === "SPRINT2BIS") {
            return demande.typeDemande === "TRANSFERT_VISA";
        }
        if (state.selectedSprintFilter === "LEGACY") {
            return demande.sansDonneesAnterieures === true;
        }
        if (state.selectedSprintFilter === "DOSSIER_CREE"
            || state.selectedSprintFilter === "SCAN_TERMINE"
            || state.selectedSprintFilter === "VISA_ACCEPTE") {
            return demande.statut === state.selectedSprintFilter;
        }
        // Filtre type dossier (client-side)
        const typeFilter = elements.filterType ? elements.filterType.value : '';
        if (typeFilter) {
            if (typeFilter === 'PASSEPORT' && !demande.passeport) return false;
            if (typeFilter === 'TRANSFERT_VISA' && demande.typeDemande !== 'TRANSFERT_VISA') return false;
            if (typeFilter === 'VISA' && demande.typeDemande !== 'NOUVEAU_TITRE') return false;
            if (typeFilter === 'CARTE_RESIDENT' && !demande.carteResident && demande.statut !== 'VISA_ACCEPTE') return false;
        }
        return true;
    });
    state.displayedDemandes = filtered;

    if (!filtered.length) {
        elements.tableBody.innerHTML = `<tr><td colspan="7" class="empty-state">Aucune demande pour ce filtre.</td></tr>`;
        return;
    }

    elements.tableBody.innerHTML = filtered.map((demande) => `
        <tr>
            <td>${escapeHtml(demande.numeroDemande)}</td>
            <td>${humanizeSprint(demande)}</td>
            <td>${humanizeType(demande.typeDemande)}</td>
            <td>${humanizeCategorie(demande.categorie)}</td>
            <td><span class="status-badge" data-status="${demande.statut}">${escapeHtml(demande.statutLibelle)}</span></td>
            <td>${escapeHtml(`${demande.nom ?? ""} ${demande.prenoms ?? ""}`.trim())}</td>
            <td>
                <div class="table-actions">
                    <button type="button" class="row-action" data-id="${demande.id}">Ouvrir</button>
                    <button type="button" class="row-qr-action" data-qr-id="${demande.id}">QR</button>
                    <select class="row-menu" data-id="${demande.id}">
                        <option value="">Actions</option>
                        <option value="open">Afficher</option>
                        <option value="qr">QR code</option>
                        <option value="scan">Marquer scan termine</option>
                    </select>
                </div>
            </td>
        </tr>
    `).join("");

    document.querySelectorAll(".row-action").forEach((button) => {
        button.addEventListener("click", () => selectDemande(button.dataset.id));
    });
    document.querySelectorAll(".row-qr-action").forEach((button) => {
        button.addEventListener("click", () => fetchAndShowQr(`${API_BASE}/qrcode/demandes/${button.dataset.qrId}`));
    });
    // Dropdown per ligne: actions rapides
    document.querySelectorAll('.row-menu').forEach((sel) => {
        sel.addEventListener('change', async function () {
            const val = this.value;
            const id = this.dataset.id;
            if (!val) return;
            try {
                if (val === 'open') await selectDemande(id);
                if (val === 'qr') await fetchAndShowQr(`${API_BASE}/qrcode/demandes/${id}`);
                if (val === 'scan') {
                    await selectDemande(id);
                    await markScanTermine(false);
                }
            } catch (e) {
                showToast(e.message || 'Action impossible', true);
            }
            this.value = '';
        });
    });
}

async function submitDemande(event, sprint2) {
    event.preventDefault();
    const form = event.currentTarget;
    const payload = buildPayloadFromForm(form, sprint2);
    const endpoint = sprint2 ? `${API_BASE}/demandes/sans-donnees-anterieures` : `${API_BASE}/demandes`;

    try {
        const response = await fetch(endpoint, {
            method: "POST",
            credentials: 'same-origin',
            headers: { "Content-Type": "application/json", "Accept": "application/json" },
            body: JSON.stringify(payload)
        });
        const result = await parseApiResponse(response);
        showToast(result.message || "Demande enregistree avec succes.");
        renderLastSubmission(result.data, sprint2);
        form.reset();
        await loadDashboard();
        await selectDemande(result.data.id);
    } catch (error) {
        showToast(error.message, true);
    }
}

function renderSprintLists() {
    renderMiniList(
        elements.sprint1List,
        state.allDemandes.filter((demande) => demande.typeDemande === "NOUVEAU_TITRE").slice(0, 5),
        "Aucune nouvelle demande pour le moment."
    );
    renderMiniList(
        elements.sprint2List,
        state.allDemandes.filter((demande) =>
            demande.typeDemande === "DUPLICATA" || demande.typeDemande === "TRANSFERT_VISA"
        ).slice(0, 5),
        "Aucune demande liee pour le moment."
    );
}

function humanizeSprint(demande) {
    if (!demande) return "-";
    if (demande.sansDonneesAnterieures) return "Reprise sans donnees anterieures";
    if (demande.typeDemande === "NOUVEAU_TITRE") return "Nouvelle demande";
    if (demande.typeDemande === "DUPLICATA") return "Duplicata";
    if (demande.typeDemande === "TRANSFERT_VISA") return "Transfert";
    return "Parcours dossier";
}

function renderMiniList(target, demandes, emptyMessage) {
    if (!target) {
        return;
    }
    if (!demandes.length) {
        target.innerHTML = `<div class="placeholder-box">${emptyMessage}</div>`;
        return;
    }

    target.innerHTML = demandes.map((demande) => `
        <article class="mini-list-item">
            <div>
                <strong>${escapeHtml(demande.numeroDemande)}</strong>
                <span>${escapeHtml(`${humanizeType(demande.typeDemande)} - ${humanizeCategorie(demande.categorie)} - ${demande.statutLibelle}`)}</span>
            </div>
            <button type="button" class="mini-open-action" data-open-demande="${demande.id}">Ouvrir</button>
        </article>
    `).join("");

    target.querySelectorAll("[data-open-demande]").forEach((button) => {
        button.addEventListener("click", () => selectDemande(button.dataset.openDemande));
    });
}

function renderLastSubmission(demande, sprint2) {
    const target = sprint2 ? elements.sprint2Result : elements.sprint1Result;
    if (!target || !demande) {
        return;
    }
    target.classList.remove("hidden");
    target.innerHTML = `
        <strong>Derniere saisie ${sprint2 ? "Reprise sans donnees anterieures" : "Nouvelle demande"}</strong>
        <span>
            ${escapeHtml(demande.numeroDemande)} - ${humanizeCategorie(demande.categorie)} -
            ${humanizeType(demande.typeDemande)} - ${escapeHtml(demande.statutLibelle)}
        </span>
    `;
}

async function selectDemande(id) {
    try {
        const response = await fetch(`${API_BASE}/demandes/${id}`, {
            method: 'GET',
            credentials: 'same-origin',
            headers: { 'Accept': 'application/json' }
        });
        const payload = await parseApiResponse(response);
        state.selectedDemande = payload.data;
        renderDetail();
    } catch (error) {
        showToast(error.message, true);
    }
}

function renderDetail() {
    const demande = state.selectedDemande;
    if (!demande) {
        resetDetail();
        return;
    }

    elements.detailEmpty.classList.add("hidden");
    elements.detailContent.classList.remove("hidden");

    setText("detail-statut", demande.statutLibelle);
    setText("detail-sprint", humanizeSprint(demande));
    setText("detail-type", humanizeType(demande.typeDemande));
    setText("detail-numero", demande.numeroDemande);
    setText("detail-categorie", humanizeCategorie(demande.categorie));
    setText("detail-demandeur", `${demande.nom ?? ""} ${demande.prenoms ?? ""}`.trim());
    setText("detail-email", demande.email ?? "-");
    setText("detail-telephone", demande.telephone ?? "-");
    setText("detail-parente", demande.demandeParenteNumero ?? "Aucune");

    // Section Passeport
    if (demande.passeport) {
        setText("detail-passeport-numero", demande.passeport.numero);
        setText("detail-passeport-delivrance", demande.passeport.dateDelivrance);
        setText("detail-passeport-expiration", demande.passeport.dateExpiration);
    } else {
        setText("detail-passeport-numero", "Non renseigne");
        setText("detail-passeport-delivrance", "-");
        setText("detail-passeport-expiration", "-");
    }

    // Section Visa transformable
    if (demande.visaTransformable) {
        setText("detail-visa-reference", demande.visaTransformable.refVisa);
        setText("detail-visa-entree", demande.visaTransformable.dateEntree);
        setText("detail-visa-lieu", demande.visaTransformable.lieuEntree);
        setText("detail-visa-sortie", demande.visaTransformable.dateSortie);
    } else {
        setText("detail-visa-reference", "Non renseigne");
        setText("detail-visa-entree", "-");
        setText("detail-visa-lieu", "-");
        setText("detail-visa-sortie", "-");
    }

    // Section Carte de résident
    if (demande.carteResident) {
        setText("detail-carte-numero", demande.carteResident.numeroCarte);
        setText("detail-carte-delivrance", demande.carteResident.dateDelivrance);
        setText("detail-carte-expiration", demande.carteResident.dateExpiration);
    } else {
        setText("detail-carte-numero", "Non generee");
        setText("detail-carte-delivrance", "-");
        setText("detail-carte-expiration", "-");
    }

    const statusBadge = document.getElementById("detail-statut");
    statusBadge.dataset.status = demande.statut;

    // Assurer le binding du bouton Scan Terminé si nécessaire (robuste si script chargé tôt)
    if (!elements.scanTermineButton) {
        elements.scanTermineButton = document.getElementById("scan-termine-button");
        if (elements.scanTermineButton) {
            elements.scanTermineButton.addEventListener("click", () => markScanTermine(false));
        }
    }
    if (elements.scanTermineButton) elements.scanTermineButton.disabled = demande.statut !== "DOSSIER_CREE";
    updateActionButtons(demande);
    // Permettre l'ouverture de la modal d'acceptation depuis le dashboard même
    // si le statut n'est pas encore SCAN_TERMINE ; le handler proposera
    // de marquer le scan comme terminé (avec option de forcer si besoin).
    elements.openAcceptModal.disabled = false;
    toggleLinkedRequestForm(demande.statut === "VISA_ACCEPTE");

    elements.linkedRequestForm.elements.typeDemande.value = "DUPLICATA";
    renderPieces(demande.piecesJustificatives ?? [], demande.modifiable);
}

function renderPieces(pieces, isModifiable) {
    if (!pieces.length) {
        elements.piecesList.innerHTML = `<div class="placeholder-box">Aucune piece justificative pour cette demande.</div>`;
        return;
    }

    elements.piecesList.innerHTML = pieces.map((piece) => `
        <article class="piece-card ${isModifiable ? "" : "piece-card-locked"}">
            <header>
                <strong>${escapeHtml(piece.libelle)}</strong>
                <span class="tag">${piece.obligatoire ? "Obligatoire" : "Optionnelle"}</span>
            </header>
            <p>Etat: ${piece.estUploadee ? "Fichier present" : "Fichier absent"}${piece.nomFichier ? ` - ${escapeHtml(piece.nomFichier)}` : ""}</p>
            <p>${isModifiable
                ? "Actions disponibles: cocher, uploader, remplacer le fichier ou le supprimer."
                : "Dossier verrouille: les pieces restent visibles mais les actions d'edition sont desactivees."}</p>
            <div class="piece-actions">
                <div class="inline-actions">
                    <button class="button secondary" type="button" data-cocher="${piece.id}" ${isModifiable ? "" : "disabled"}>
                        ${piece.estCochee ? "Decocher" : "Cocher"}
                    </button>
                    <button class="button secondary" type="button" data-delete="${piece.id}" ${piece.estUploadee && isModifiable ? "" : "disabled"}>
                        Supprimer le fichier
                    </button>
                </div>
                <label class="upload-label ${isModifiable ? "" : "disabled"}">
                    Uploader / remplacer
                    <input type="file" data-upload="${piece.id}" accept=".pdf,.png,.jpg,.jpeg" ${isModifiable ? "" : "disabled"}>
                </label>
            </div>
        </article>
    `).join("");

    document.querySelectorAll("[data-cocher]").forEach((button) => {
        button.addEventListener("click", () => togglePiece(button.dataset.cocher));
    });
    document.querySelectorAll("[data-delete]").forEach((button) => {
        button.addEventListener("click", () => deletePieceFile(button.dataset.delete));
    });
    document.querySelectorAll("[data-upload]").forEach((input) => {
        input.addEventListener("change", () => uploadPieceFile(input.dataset.upload, input.files[0]));
    });
}

async function togglePiece(pieceId) {
    if (!state.selectedDemande) {
        return;
    }
    try {
        const response = await fetch(`${API_BASE}/demandes/${state.selectedDemande.id}/pieces/${pieceId}/cocher`, {
            method: "PATCH",
            credentials: 'same-origin',
            headers: { 'Accept': 'application/json' }
        });
        await parseApiResponse(response);
        showToast("Piece justificative mise a jour.");
        await selectDemande(state.selectedDemande.id);
        await loadDashboard();
    } catch (error) {
        showToast(error.message, true);
    }
}

async function uploadPieceFile(pieceId, file) {
    if (!state.selectedDemande || !file) {
        return;
    }
    
    // Vérifier que la demande est toujours modifiable
    if (!state.selectedDemande.modifiable) {
        showToast("Cette demande n\'est plus modifiable.", true);
        return;
    }
    
    const formData = new FormData();
    formData.append("fichier", file);

    try {
        // Désactiver le bouton pendant l'upload (protection si l'input est introuvable)
        const uploadInput = document.querySelector(`input[data-upload="${pieceId}"]`);
        const uploadButton = uploadInput ? uploadInput.closest('.upload-label') : null;
        if (uploadButton) {
            uploadButton.classList.add('uploading');
            uploadButton.disabled = true;
        }

        const response = await fetch(`${API_BASE}/demandes/${state.selectedDemande.id}/pieces/${pieceId}/upload`, {
            method: "POST",
            credentials: 'same-origin',
            body: formData
        });
        
        const result = await parseApiResponse(response);
        
        // Rafraîchir les données de la demande pour confirmer que le fichier est bien enregistré
        await selectDemande(state.selectedDemande.id);
        await loadDashboard();
        
        showToast("Fichier uploade avec succes.");
        
        // Réactiver le bouton après l'upload réussi
        if (uploadButton) {
            uploadButton.classList.remove('uploading');
            uploadButton.disabled = false;
        }
    } catch (error) {
        console.error("Erreur lors de l'upload:", error);
        showToast(error.message || "Erreur lors de l\'upload du fichier", true);
        
        // Réactiver le bouton en cas d'erreur
        const uploadInput = document.querySelector(`input[data-upload="${pieceId}"]`);
        const uploadButton2 = uploadInput ? uploadInput.closest('.upload-label') : null;
        if (uploadButton2) {
            uploadButton2.classList.remove('uploading');
            uploadButton2.disabled = false;
        }
    }
}

async function deletePieceFile(pieceId) {
    if (!state.selectedDemande) {
        return;
    }
    try {
        const response = await fetch(`${API_BASE}/demandes/${state.selectedDemande.id}/pieces/${pieceId}/fichier`, {
            method: "DELETE",
            credentials: 'same-origin',
            headers: { 'Accept': 'application/json' }
        });
        await parseApiResponse(response);
        showToast("Fichier supprime.");
        await selectDemande(state.selectedDemande.id);
        await loadDashboard();
    } catch (error) {
        showToast(error.message, true);
    }
}

async function markScanTermine(force = false) {
    if (!state.selectedDemande) {
        showToast("Selectionnez une demande.", true);
        return;
    }
    try {
        const url = new URL(`${API_BASE}/demandes/${state.selectedDemande.id}/scan-termine`, window.location.origin);
        if (force) url.searchParams.set('force', 'true');
        const response = await fetch(url.toString(), {
            method: "POST",
            credentials: 'same-origin',
            headers: { 'Accept': 'application/json' }
        });
        const result = await parseApiResponse(response);
        showToast("Le dossier est maintenant en scan termine.");
        await loadDashboard();
        await selectDemande(state.selectedDemande.id);
        if (result && result.data && result.data.statut === 'SCAN_TERMINE') {
            updateAcceptModalForDemande(state.selectedDemande);
            try { elements.acceptModal.showModal(); } catch(e) { /* ignore if not available */ }
        }
    } catch (error) {
        // Si l'erreur concerne des pièces manquantes, proposer de forcer
        if (!force && /pieces obligatoires|Fichier manquant|obligatoire/i.test(error.message)) {
            if (confirm("Des pièces obligatoires sont manquantes. Voulez-vous forcer la fin du scan malgré les erreurs ?")) {
                return markScanTermine(true);
            }
        }
        showToast(error.message, true);
    }
}

async function acceptVisa(event) {
    event.preventDefault();
    if (!state.selectedDemande) {
        showToast("Selectionnez une demande.", true);
        return;
    }
    const formData = new FormData(elements.acceptForm);
    const payload = {
        numeroCarte: formData.get("numeroCarte"),
        dateDelivrance: formData.get("dateDelivrance"),
        dateExpiration: formData.get("dateExpiration")
    };

    try {
        const response = await fetch(`${API_BASE}/demandes/${state.selectedDemande.id}/accepter-visa`, {
            method: "POST",
            credentials: 'same-origin',
            headers: { "Content-Type": "application/json", "Accept": "application/json" },
            body: JSON.stringify(payload)
        });
        const result = await parseApiResponse(response);
        showToast(state.selectedDemande.typeDemande === "TRANSFERT_VISA"
            ? "Transfert valide avec succes."
            : "Visa accepte et carte resident creee.");
        if (elements.acceptModal && typeof elements.acceptModal.close === 'function') {
            try { elements.acceptModal.close(); } catch (e) { console.warn('close accept modal failed', e); }
        }
        if (elements.acceptForm && typeof elements.acceptForm.reset === 'function') {
            elements.acceptForm.reset();
        }
        await loadDashboard();
        await selectDemande(state.selectedDemande.id);

        // Si la réponse contient une carte, generer et afficher son QR
        if (result && result.data && result.data.id) {
            fetchAndShowQr(`${API_BASE}/qrcode/demandes/${result.data.id}`);
        }
    } catch (error) {
        showToast(error.message, true);
    }
}

async function createLinkedRequest(event) {
    event.preventDefault();
    if (!state.selectedDemande) {
        showToast("Selectionnez une demande parente.", true);
        return;
    }

    const formData = new FormData(event.currentTarget);
    const typeDemande = formData.get("typeDemande");
    const endpoint = typeDemande === "DUPLICATA"
        ? `${API_BASE}/demandes/${state.selectedDemande.id}/duplicata`
        : `${API_BASE}/demandes/${state.selectedDemande.id}/transfert-visa`;

    const payload = {
        categorie: state.selectedDemande.categorie,
        typeDemande,
        etatCivil: {
            nom: state.selectedDemande.nom,
            prenoms: state.selectedDemande.prenoms,
            nomJeuneFille: state.selectedDemande.nomJeuneFille || "",
            situationFamiliale: state.selectedDemande.situationFamiliale,
            nationalite: state.selectedDemande.nationalite,
            profession: state.selectedDemande.profession,
            dateNaissance: state.selectedDemande.dateNaissance,
            lieuNaissance: state.selectedDemande.lieuNaissance || "",
            adresseMadagascar: state.selectedDemande.adresseMadagascar,
            email: state.selectedDemande.email,
            telephone: state.selectedDemande.telephone
        },
    };
    if (typeDemande === "TRANSFERT_VISA") {
        if (!formData.get("passeportNumero") || !formData.get("passeportDateDelivrance") || !formData.get("passeportDateExpiration")) {
            showToast("Le nouveau passeport est obligatoire pour un transfert de visa.", true);
            return;
        }
        payload.passeport = {
            numero: formData.get("passeportNumero"),
            dateDelivrance: formData.get("passeportDateDelivrance"),
            dateExpiration: formData.get("passeportDateExpiration")
        };
    }
    // propager le statut resident depuis la demande parente
    if (state.selectedDemande && typeof state.selectedDemande.resident !== 'undefined') {
        payload.resident = !!state.selectedDemande.resident;
    }

    try {
        const response = await fetch(endpoint, {
            method: "POST",
            credentials: 'same-origin',
            headers: { "Content-Type": "application/json", "Accept": "application/json" },
            body: JSON.stringify(payload)
        });
        const result = await parseApiResponse(response);
        showToast("Demande liee creee avec succes.");
        event.currentTarget.reset();
        await loadDashboard();
        await selectDemande(result.data.id);
    } catch (error) {
        showToast(error.message, true);
    }
}

function buildPayloadFromForm(form, sprint2) {
    const data = new FormData(form);
    const payload = {
        categorie: data.get("categorie"),
        typeDemande: data.get("typeDemande"),
        etatCivil: {
            nom: data.get("nom"),
            prenoms: data.get("prenoms"),
            nomJeuneFille: data.get("nomJeuneFille") || "",
            situationFamiliale: data.get("situationFamiliale"),
            nationalite: data.get("nationalite"),
            profession: data.get("profession"),
            dateNaissance: data.get("dateNaissance"),
            lieuNaissance: data.get("lieuNaissance") || "",
            adresseMadagascar: data.get("adresseMadagascar"),
            email: data.get("email"),
            telephone: data.get("telephone")
        },
        passeport: {
            numero: data.get("passeportNumero"),
            dateDelivrance: data.get("passeportDateDelivrance"),
            dateExpiration: data.get("passeportDateExpiration")
        }
    };

    // resident: choix Etranger / Resident dans les formulaires
    if (data.get("resident") !== null) {
        payload.resident = data.get("resident") === "true";
    }

    if (data.get("demandeParenteId")) {
        payload.demandeParenteId = Number(data.get("demandeParenteId"));
    }

    if (data.get("visaRefVisa") || data.get("visaDateEntree") || data.get("visaDateSortie") || data.get("visaLieuEntree")) {
        payload.visaTransformable = {
            refVisa: data.get("visaRefVisa"),
            dateEntree: data.get("visaDateEntree"),
            lieuEntree: data.get("visaLieuEntree"),
            dateSortie: data.get("visaDateSortie")
        };
    }

    if (sprint2) {
        payload.numeroCarte = data.get("numeroCarte");
        payload.dateDelivranceCarte = data.get("dateDelivranceCarte");
        payload.dateExpirationCarte = data.get("dateExpirationCarte");
    }
    return payload;
}

function formatErrorMessage(payload) {
    if (!payload) {
        return "Une erreur technique est survenue.";
    }
    if (payload.data && typeof payload.data === "object" && !Array.isArray(payload.data)) {
        const firstError = Object.values(payload.data)[0];
        if (firstError) {
            return `${payload.message || "Erreur"} : ${firstError}`;
        }
    }
    return payload.message || "Une erreur technique est survenue.";
}

function resetDetail() {
    state.selectedDemande = null;
    elements.detailEmpty.classList.remove("hidden");
    elements.detailContent.classList.add("hidden");
    elements.piecesList.innerHTML = "";
    elements.scanTermineButton.disabled = true;
    elements.openAcceptModal.disabled = true;
    toggleLinkedRequestForm(false);
}

function showToast(message, isError = false) {
    elements.toast.textContent = message;
    elements.toast.classList.remove("hidden", "error");
    if (isError) {
        elements.toast.classList.add("error");
    }
    window.clearTimeout(showToast.timeout);
    showToast.timeout = window.setTimeout(() => elements.toast.classList.add("hidden"), 3800);
}

function setText(id, text) {
    document.getElementById(id).textContent = text || "-";
}

function humanizeType(type) {
    return {
        NOUVEAU_TITRE: "Nouveau titre",
        DUPLICATA: "Duplicata",
        TRANSFERT_VISA: "Transfert de visa"
    }[type] || type;
}

function humanizeCategorie(categorie) {
    return {
        TRAVAILLEUR: "Travailleur",
        INVESTISSEUR: "Investisseur"
    }[categorie] || categorie;
}

function escapeHtml(value) {
    return String(value ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/\"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function toggleLinkedRequestForm(isEnabled) {
    if (!elements.linkedRequestForm) return;
    Array.from(elements.linkedRequestForm.elements).forEach((field) => {
        if (field.tagName === "BUTTON" || field.name) {
            field.disabled = !isEnabled;
        }
    });
    if (elements.linkedRequestNote) {
        elements.linkedRequestNote.textContent = isEnabled
            ? "Le duplicata reprend le passeport parent. Le transfert de visa exige un nouveau passeport."
            : "Cette section s'active uniquement quand la demande selectionnee est au statut Visa accepte.";
    }
}

function setSelectedScope(scope, syncUrl = false) {
    state.selectedSprintFilter = scope || "ALL";
    document.querySelectorAll(".segment").forEach((segment) => {
        segment.classList.toggle("active", segment.dataset.filter === state.selectedSprintFilter);
    });
    if (elements.listSelector) {
        elements.listSelector.value = state.selectedSprintFilter;
    }
    if (syncUrl) {
        const url = new URL(window.location.href);
        if (state.selectedSprintFilter === "ALL") {
            url.searchParams.delete("scope");
        } else {
            url.searchParams.set("scope", state.selectedSprintFilter);
        }
        window.history.replaceState({}, "", url.toString());
    }
}

function initializeFiltersFromUrl() {
    const params = new URLSearchParams(window.location.search);
    setSelectedScope(params.get("scope") || "ALL", false);
    if (elements.filterStatut && params.get("statut")) {
        elements.filterStatut.value = params.get("statut");
    }
    if (elements.filterCategorie && params.get("categorie")) {
        elements.filterCategorie.value = params.get("categorie");
    }
}

function updateActionButtons(demande) {
    if (!demande || !elements.openAcceptModal) return;
    if (demande.typeDemande === "TRANSFERT_VISA") {
        elements.openAcceptModal.textContent = "Valider le transfert";
    } else if (demande.typeDemande === "DUPLICATA") {
        elements.openAcceptModal.textContent = "Delivrer le duplicata";
    } else {
        elements.openAcceptModal.textContent = "Delivrer le titre et la carte";
    }
}

function updateAcceptModalForDemande(demande) {
    const note = document.getElementById("accept-modal-note");
    const numberField = document.getElementById("accept-card-number-field");
    const delivranceField = document.getElementById("accept-card-delivrance-field");
    const expirationField = document.getElementById("accept-card-expiration-field");
    const numeroCarte = elements.acceptForm?.elements?.numeroCarte;
    const dateDelivrance = elements.acceptForm?.elements?.dateDelivrance;
    const dateExpiration = elements.acceptForm?.elements?.dateExpiration;
    if (!demande || !numeroCarte || !dateDelivrance || !dateExpiration) return;

    const transfert = demande.typeDemande === "TRANSFERT_VISA";
    [numberField, delivranceField, expirationField].forEach((field) => {
        if (field) field.classList.toggle("hidden", transfert);
    });

    numeroCarte.required = !transfert;
    dateDelivrance.required = !transfert;
    dateExpiration.required = false;
    numeroCarte.disabled = transfert;
    dateDelivrance.disabled = transfert;
    dateExpiration.disabled = transfert;

    if (transfert) {
        numeroCarte.value = "";
        dateDelivrance.value = "";
        dateExpiration.value = "";
        if (note) {
            note.textContent = "Le transfert conserve la carte de resident de la demande parente. Aucun nouveau numero de carte n'est requis.";
        }
        return;
    }

    numeroCarte.disabled = false;
    dateDelivrance.disabled = false;
    dateExpiration.disabled = false;
    if (note) {
        note.textContent = demande.typeDemande === "DUPLICATA"
            ? "Renseignez la nouvelle carte de resident du duplicata. La date d'expiration peut reprendre celle de la carte parente si vous la laissez vide."
            : "Renseignez la carte de resident pour finaliser la nouvelle demande. La date d'expiration suit la date de fin du visa transformable si vous la laissez vide.";
    }
}

function renderQrDetails(details) {
    if (!details || typeof details !== "object") {
        return "";
    }
    const lines = Object.entries(details)
        .filter(([, value]) => value !== null && value !== undefined && value !== "")
        .map(([key, value]) => `
            <div class="qr-detail-row">
                <strong>${escapeHtml(humanizeQrKey(key))}</strong>
                <span>${escapeHtml(String(value))}</span>
            </div>
        `)
        .join("");
    return lines ? `<div class="qr-detail-grid">${lines}</div>` : "";
}

function humanizeQrKey(key) {
    return {
        type: "Type",
        demandeId: "ID dossier",
        numeroDemande: "Numero dossier",
        parcours: "Parcours",
        categorie: "Categorie",
        statut: "Statut",
        statutLibelle: "Statut detaille",
        sansDonneesAnterieures: "Sans donnees anterieures",
        resident: "Resident",
        demandeur: "Demandeur",
        demandeParente: "Dossier parent",
        numeroPasseport: "Passeport",
        referenceVisa: "Reference visa",
        numeroCarteResident: "Carte de resident",
        expirationCarte: "Expiration carte",
        timestamp: "Horodatage"
    }[key] || key;
}

// Initialisation : charger le dashboard puis traiter d'éventuelles actions passées en URL
initializeFiltersFromUrl();
resetDetail();
loadDashboard().then(async () => {
    const params = new URLSearchParams(window.location.search);
    const openId = params.get('openDemande') || params.get('id');
    const force = params.get('force') === 'true';
    const openAccept = params.get('openAccept') === 'true';
    if (openId) {
        try {
            await selectDemande(openId);
            if (force) {
                await markScanTermine(true);
                if (openAccept) elements.acceptModal.showModal();
            } else if (openAccept) {
                // Essayer de marquer le scan normalement; markScanTermine gérera une proposition de forcer si nécessaire
                await markScanTermine(false);
            }
        } catch (err) {
            // Ne pas bloquer l'initialisation si la sélection échoue
            console.warn('Impossible d ouvrir la demande depuis l URL :', err);
        }
    }
}).catch((e) => console.warn('Erreur initialisation dashboard:', e));
