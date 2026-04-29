<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Duplicata de carte de resident</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/sidebar-fix.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/modern.css">
</head>
<body>
<div class="layout-shell">
    <%@ include file="/WEB-INF/jsp/backoffice/common/fragments/sidebar.jspf" %>
    <div class="main-area">
        <%@ include file="/WEB-INF/jsp/backoffice/common/fragments/topbar.jspf" %>

        <main>
            <section class="panel">
                <div class="panel-header">
                    <div>
                        <p class="eyebrow">Demande liee</p>
                        <h2>Creer un duplicata</h2>
                    </div>
                </div>

                <p class="overview-text">
                    Le duplicata cree un nouveau dossier a scanner. Le passeport et la carte actuelle sont repris depuis la demande parente acceptee.
                </p>

                <form id="s2-dup-form" class="admin-form">
                    <div class="form-block">
                        <h3>Dossier parent</h3>
                        <div class="form-grid">
                            <label>Categorie
                                <select name="categorie" required>
                                    <option value="">Selectionner...</option>
                                    <option value="TRAVAILLEUR">Travailleur</option>
                                    <option value="INVESTISSEUR">Investisseur</option>
                                </select>
                            </label>
                            <label>Demande parente
                                <select id="demandeParenteSelect" name="demandeParenteId" required>
                                    <option value="">Chargement des demandes...</option>
                                </select>
                            </label>
                        </div>
                    </div>

                    <div class="form-block">
                        <h3>Etat civil</h3>
                        <div class="form-grid">
                            <label>Nom<input type="text" name="nom" required></label>
                            <label>Prenoms<input type="text" name="prenoms" required></label>
                            <label>Nom jeune fille<input type="text" name="nomJeuneFille"></label>
                            <label>Situation familiale
                                <select name="situationFamiliale" required>
                                    <option value="CELIBATAIRE">Celibataire</option>
                                    <option value="MARIE">Marie</option>
                                    <option value="DIVORCE">Divorce</option>
                                    <option value="VEUF">Veuf</option>
                                    <option value="SEPARE">Separe</option>
                                </select>
                            </label>
                            <label>Nationalite<input type="text" name="nationalite" required></label>
                            <label>Statut
                                <select name="resident">
                                    <option value="false">Etranger</option>
                                    <option value="true">Resident</option>
                                </select>
                            </label>
                            <label>Profession<input type="text" name="profession"></label>
                            <label>Date naissance<input type="date" name="dateNaissance" required></label>
                            <label>Lieu naissance<input type="text" name="lieuNaissance"></label>
                            <label class="full-width">Adresse Madagascar
                                <textarea name="adresseMadagascar" rows="2"></textarea>
                            </label>
                            <label>Email<input type="email" name="email"></label>
                            <label>Telephone<input type="text" name="telephone"></label>
                        </div>
                    </div>

                    <div style="display:flex;gap:10px;margin-top:18px;">
                        <button class="button primary important-blue" type="submit">Creer le duplicata</button>
                        <a class="button secondary" href="${pageContext.request.contextPath}/backoffice">Annuler</a>
                    </div>
                </form>
                <div id="s2-dup-result" class="result-card hidden"></div>
            </section>
        </main>
    </div>
</div>

<script>
const contextPath = '${pageContext.request.contextPath}';

document.addEventListener('DOMContentLoaded', async function() {
    try {
        const response = await fetch(`${contextPath}/demandes?statut=VISA_ACCEPTE`);
        const result = await response.json();

        const select = document.getElementById('demandeParenteSelect');
        select.innerHTML = '<option value="">Selectionner une demande...</option>';

        if (result.success && result.data && result.data.length > 0) {
            result.data.forEach(demande => {
                const option = document.createElement('option');
                option.value = demande.id;
                option.textContent = `${demande.numeroDemande} - ${demande.nom} ${demande.prenoms} (${demande.categorie})`;
                select.appendChild(option);
            });
        } else {
            select.innerHTML = '<option value="">Aucune demande disponible</option>';
        }
    } catch (error) {
        console.error('Erreur lors du chargement des demandes:', error);
        document.getElementById('demandeParenteSelect').innerHTML = '<option value="">Erreur de chargement</option>';
    }
});

document.getElementById('s2-dup-form').addEventListener('submit', async function (ev) {
    ev.preventDefault();
    const f = ev.target;
    const get = n => f.elements[n] ? f.elements[n].value : null;

    const erreurs = [];
    [
        ['categorie', 'Categorie'],
        ['demandeParenteId', 'Demande parente'],
        ['nom', 'Nom'],
        ['prenoms', 'Prenoms'],
        ['situationFamiliale', 'Situation familiale'],
        ['nationalite', 'Nationalite'],
        ['dateNaissance', 'Date de naissance']
    ].forEach(([name, label]) => {
        if (!get(name)) erreurs.push(`Le champ ${label} est obligatoire`);
    });

    if (erreurs.length > 0) {
        const resultEl = document.getElementById('s2-dup-result');
        if (resultEl) {
            resultEl.classList.remove('hidden');
            resultEl.style.borderColor = 'var(--danger)';
            resultEl.innerHTML = '<strong>Erreurs de validation :</strong><ul>' + erreurs.map(e => '<li>' + e + '</li>').join('') + '</ul>';
            try { resultEl.scrollIntoView({behavior: 'smooth', block: 'center'}); } catch (e) {}
        }
        try { if (typeof showToast === 'function') showToast('Erreurs de validation', true); } catch (e) { /* ignore */ }
        return;
    }

    const parentId = Number(get('demandeParenteId'));
    const payload = {
        categorie: get('categorie'),
        typeDemande: 'DUPLICATA',
        etatCivil: {
            nom: get('nom'),
            prenoms: get('prenoms'),
            nomJeuneFille: get('nomJeuneFille') || '',
            situationFamiliale: get('situationFamiliale'),
            nationalite: get('nationalite'),
            profession: get('profession') || '',
            dateNaissance: get('dateNaissance'),
            lieuNaissance: get('lieuNaissance') || '',
            adresseMadagascar: get('adresseMadagascar') || '',
            email: get('email') || '',
            telephone: get('telephone') || ''
        },
        resident: get('resident') === 'true',
        demandeParenteId: parentId
    };

    const r = await fetch(`${contextPath}/demandes/${parentId}/duplicata`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });
    const j = await r.json();
    const result = document.getElementById('s2-dup-result');

    if (r.ok && j.success) {
        result.classList.remove('hidden');
        result.style.borderColor = 'var(--success)';
        result.innerHTML = '<strong>Duplicata cree</strong><span>Redirection vers le scan du dossier...</span>';
        setTimeout(() => {
            window.location.href = `${contextPath}/backoffice/scan?id=${j.data.id}`;
        }, 1200);
    } else {
        result.classList.remove('hidden');
        result.style.borderColor = 'var(--danger)';
        result.innerHTML = '<strong>Erreur</strong><span>' + (j.message || 'erreur inconnue') + '</span>';
    }
});
</script>
</body>
</html>
