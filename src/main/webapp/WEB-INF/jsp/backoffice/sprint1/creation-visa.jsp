<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Création de visa</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/sidebar-fix.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/sprint1/upload-section.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/sprint1/step-actions.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/modern.css">
    <style>
        .upload-box {
            border: 2px dashed #ccc;
            border-radius: 8px;
            padding: 10px 12px;
            background: #f9f9f9;
            position: relative;
            overflow: hidden;
        }
        .upload-box:hover { border-color: #007bff; background: #f0f8ff; }
        .upload-box input[type="file"] {
            position: absolute; top: 0; left: 0; width: 100%; height: 100%;
            opacity: 0; cursor: pointer;
        }
        .upload-box .box-content { display: flex; flex-direction: column; gap: 3px; pointer-events: none; }
        .upload-box .box-content .label { font-size: 0.95em; font-weight: 500; }
        .upload-box .box-content .file-name { font-size: 0.8em; color: #999; }
        .upload-box .box-content .required-star { color: #c94c4c; margin-left: 2px; }
        .upload-box.has-file { border-color: #28a745; border-style: solid; background: #f0fff0; }
        .upload-box.has-file .file-name { color: #28a745; font-weight: bold; }
        .upload-box.upload-error { border-color: #c94c4c; background: #fff5f5; }
        .upload-box.upload-error .file-name { color: #c94c4c; font-weight: bold; }
        .section-upload { border: 1px solid #ddd; padding: 16px; border-radius: 8px; margin-bottom: 16px; }
        .section-upload h4 { margin-top: 0; }
        .section-travailleur, .section-investisseur { display: none; }
        .section-travailleur.active, .section-investisseur.active { display: block; }
    </style>
</head>
<body>
<div class="layout-shell">
    <%@ include file="/WEB-INF/jsp/backoffice/common/fragments/sidebar.jspf" %>
    <div class="main-area">
        <%@ include file="/WEB-INF/jsp/backoffice/common/fragments/topbar.jspf" %>

        <main>
            <section class="panel">
                <div class="panel-header">
                    <div><p class="eyebrow">Nouvelle demande</p><h2>Demande de visa</h2></div>
                </div>
                <form id="s1-form" class="admin-form" novalidate>
                    <div class="s1-carousel">
                        <div class="s1-overflow">
                            <div class="s1-slides">

                                <!-- Slide 1 -->
                                <div class="s1-slide active" data-step="1">
                                    <h3>Type de demande</h3>
                                    <div class="form-grid">
                                        <label>Categorie
                                            <select name="categorie" id="select-categorie" required>
                                                <option value="">-- Choisir --</option>
                                                <option value="TRAVAILLEUR">Travailleur</option>
                                                <option value="INVESTISSEUR">Investisseur</option>
                                            </select>
                                        </label>
                                        <label>Type de demande
                                            <select name="typeDemande" required>
                                                <option value="NOUVEAU_TITRE">Nouveau titre</option>
                                            </select>
                                        </label>
                                    </div>
                                    <div class="step-actions">
                                        <button type="button" class="button primary save-step" data-step="1">Enregistrer</button>
                                        <button type="button" class="button secondary edit-step hidden" data-step="1">Faire des modifications</button>
                                    </div>
                                </div>

                                <!-- Slide 2 -->
                                <div class="s1-slide" data-step="2">
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
                                        <label>Profession<input type="text" name="profession" required></label>
                                        <label>Date naissance<input type="date" name="dateNaissance" required></label>
                                        <label>Lieu naissance<input type="text" name="lieuNaissance"></label>
                                        <label class="full-width">Adresse Madagascar
                                            <textarea name="adresseMadagascar" rows="2" required></textarea>
                                        </label>
                                        <label>Email<input type="email" name="email" required></label>
                                        <label>Telephone<input type="text" name="telephone" required></label>
                                    </div>
                                    <div class="step-actions">
                                        <button type="button" class="button primary save-step" data-step="2">Enregistrer</button>
                                        <button type="button" class="button secondary edit-step hidden" data-step="2">Faire des modifications</button>
                                    </div>
                                </div>

                                <!-- Slide 3 -->
                                <div class="s1-slide" data-step="3">
                                    <h3>Passeport</h3>
                                    <div class="form-grid">
                                        <label>Numero passeport<input type="text" name="passeport.numero" required></label>
                                        <label>Date delivrance<input type="date" name="passeport.dateDelivrance" required></label>
                                        <label>Date expiration<input type="date" name="passeport.dateExpiration" required></label>
                                    </div>
                                    <div class="step-actions">
                                        <button type="button" class="button primary save-step" data-step="3">Enregistrer</button>
                                        <button type="button" class="button secondary edit-step hidden" data-step="3">Faire des modifications</button>
                                    </div>
                                </div>

                                <!-- Slide 4 -->
                                <div class="s1-slide" data-step="4">
                                    <h3>Visa transformable</h3>
                                    <div class="form-grid">
                                        <label>Ref visa<input type="text" name="visaTransformable.refVisa"></label>
                                        <label>Date entree<input type="date" name="visaTransformable.dateEntree"></label>
                                        <label>Lieu entree<input type="text" name="visaTransformable.lieuEntree"></label>
                                        <label>Date sortie<input type="date" name="visaTransformable.dateSortie"></label>
                                    </div>
                                    <div class="step-actions">
                                        <button type="button" class="button primary save-step" data-step="4">Enregistrer</button>
                                        <button type="button" class="button secondary edit-step hidden" data-step="4">Faire des modifications</button>
                                    </div>
                                </div>

                            </div>
                        </div>
                        <div class="carousel-arrow left disabled" id="s1-prev" title="Etape precedente">◀</div>
                        <div class="carousel-arrow right" id="s1-next" title="Etape suivante">▶</div>
                    </div>

                    <!-- ===== SECTION UPLOAD EN DEHORS DU CAROUSEL ===== -->
                    <div id="upload-section-global" style="margin-top: 24px; display: block;">
                        <h3>Pièces justificatives à uploader</h3>
                        <p style="color: #666; font-size: 0.9em;">
                            Cliquez sur chaque boîte pour sélectionner un fichier. <span style="color:#c94c4c;">*</span> = obligatoire
                        </p>

                        <!-- ===== PIÈCES COMMUNES (Travailleur + Investisseur) ===== -->
                        <div class="section-upload">
                            <h4>Pièces communes</h4>
                            <div class="upload-grid">
                                <div class="upload-box">
                                    <input type="file" name="piece.PHOTOS_IDENTITE" accept=".pdf,.png,.jpg,.jpeg" required>
                                    <div class="box-content">
                                        <span class="label"><span class="required-star">*</span> Copie de la carte d'identité (4 photos)</span>
                                        <span class="file-name">Aucun fichier sélectionné</span>
                                    </div>
                                </div>
                                <div class="upload-box">
                                    <input type="file" name="piece.FORMULAIRE_DEMANDE" accept=".pdf,.png,.jpg,.jpeg" required>
                                    <div class="box-content">
                                        <span class="label"><span class="required-star">*</span> Formulaire de demande rempli et signé</span>
                                        <span class="file-name">Aucun fichier sélectionné</span>
                                    </div>
                                </div>
                                <div class="upload-box">
                                    <input type="file" name="piece.COPIE_PASSEPORT" accept=".pdf,.png,.jpg,.jpeg" required>
                                    <div class="box-content">
                                        <span class="label"><span class="required-star">*</span> Copie du passeport (pages identité + visa)</span>
                                        <span class="file-name">Aucun fichier sélectionné</span>
                                    </div>
                                </div>
                                <div class="upload-box">
                                    <input type="file" name="piece.COPIE_VISA_TRANSFORMABLE" accept=".pdf,.png,.jpg,.jpeg" required>
                                    <div class="box-content">
                                        <span class="label"><span class="required-star">*</span> Copie du visa transformable</span>
                                        <span class="file-name">Aucun fichier sélectionné</span>
                                    </div>
                                </div>
                                <div class="upload-box">
                                    <input type="file" name="piece.CERTIFICAT_MEDICAL" accept=".pdf,.png,.jpg,.jpeg" required>
                                    <div class="box-content">
                                        <span class="label"><span class="required-star">*</span> Certificat médical (médecin agréé)</span>
                                        <span class="file-name">Aucun fichier sélectionné</span>
                                    </div>
                                </div>
                                <div class="upload-box">
                                    <input type="file" name="piece.CASIER_JUDICIAIRE" accept=".pdf,.png,.jpg,.jpeg" required>
                                    <div class="box-content">
                                        <span class="label"><span class="required-star">*</span> Casier judiciaire (moins de 3 mois)</span>
                                        <span class="file-name">Aucun fichier sélectionné</span>
                                    </div>
                                </div>
                                <div class="upload-box">
                                    <input type="file" name="piece.EXTRAIT_NAISSANCE" accept=".pdf,.png,.jpg,.jpeg" required>
                                    <div class="box-content">
                                        <span class="label"><span class="required-star">*</span> Extrait d'acte de naissance légalisé</span>
                                        <span class="file-name">Aucun fichier sélectionné</span>
                                    </div>
                                </div>
                                <div class="upload-box">
                                    <input type="file" name="piece.JUSTIFICATIF_DOMICILE" accept=".pdf,.png,.jpg,.jpeg" required>
                                    <div class="box-content">
                                        <span class="label"><span class="required-star">*</span> Justificatif de domicile à Madagascar</span>
                                        <span class="file-name">Aucun fichier sélectionné</span>
                                    </div>
                                </div>
                                <div class="upload-box">
                                    <input type="file" name="piece.QUITTANCE_TAXES" accept=".pdf,.png,.jpg,.jpeg" required>
                                    <div class="box-content">
                                        <span class="label"><span class="required-star">*</span> Quittance de paiement des taxes et droits</span>
                                        <span class="file-name">Aucun fichier sélectionné</span>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- ===== PIÈCES SPÉCIFIQUES TRAVAILLEUR ===== -->
                        <div class="section-upload section-travailleur" id="section-travailleur">
                            <h4>Pièces spécifiques - Travailleur</h4>
                            <div class="upload-grid">
                                <div class="upload-box">
                                    <input type="file" name="piece.CONTRAT_TRAVAIL" accept=".pdf,.png,.jpg,.jpeg" required>
                                    <div class="box-content">
                                        <span class="label"><span class="required-star">*</span> Contrat de travail signé</span>
                                        <span class="file-name">Aucun fichier sélectionné</span>
                                    </div>
                                </div>
                                <div class="upload-box">
                                    <input type="file" name="piece.ATTESTATION_EMPLOYEUR" accept=".pdf,.png,.jpg,.jpeg" required>
                                    <div class="box-content">
                                        <span class="label"><span class="required-star">*</span> Attestation de l'employeur (poste + salaire)</span>
                                        <span class="file-name">Aucun fichier sélectionné</span>
                                    </div>
                                </div>
                                <div class="upload-box">
                                    <input type="file" name="piece.AUTORISATION_TRAVAIL" accept=".pdf,.png,.jpg,.jpeg" required>
                                    <div class="box-content">
                                        <span class="label"><span class="required-star">*</span> Autorisation de travail (Ministère du Travail)</span>
                                        <span class="file-name">Aucun fichier sélectionné</span>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- ===== PIÈCES SPÉCIFIQUES INVESTISSEUR ===== -->
                        <div class="section-upload section-investisseur" id="section-investisseur">
                            <h4>Pièces spécifiques - Investisseur</h4>
                            <div class="upload-grid">
                                <div class="upload-box">
                                    <input type="file" name="piece.BUSINESS_PLAN" accept=".pdf,.png,.jpg,.jpeg" required>
                                    <div class="box-content">
                                        <span class="label"><span class="required-star">*</span> Plan d'affaires / Business plan</span>
                                        <span class="file-name">Aucun fichier sélectionné</span>
                                    </div>
                                </div>
                                <div class="upload-box">
                                    <input type="file" name="piece.JUSTIFICATIF_INVESTISSEMENT" accept=".pdf,.png,.jpg,.jpeg" required>
                                    <div class="box-content">
                                        <span class="label"><span class="required-star">*</span> Justificatif d'investissement (fonds)</span>
                                        <span class="file-name">Aucun fichier sélectionné</span>
                                    </div>
                                </div>
                                <div class="upload-box">
                                    <input type="file" name="piece.STATUTS_SOCIETE" accept=".pdf,.png,.jpg,.jpeg" required>
                                    <div class="box-content">
                                        <span class="label"><span class="required-star">*</span> Statuts de la société enregistrés</span>
                                        <span class="file-name">Aucun fichier sélectionné</span>
                                    </div>
                                </div>
                                <div class="upload-box">
                                    <input type="file" name="piece.NIF" accept=".pdf,.png,.jpg,.jpeg">
                                    <div class="box-content">
                                        <span class="label">NIF (Numéro d'Identification Fiscale)</span>
                                        <span class="file-name">Aucun fichier sélectionné</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div style="margin-top:12px; display:flex; align-items:center; justify-content:space-between; gap:12px;">
                        <div class="carousel-dots" id="s1-dots"></div>
                        <div class="form-actions" style="display:flex;gap:10px;">
                            <button id="create-demande-button" class="button primary important-blue" type="submit">Creer la demande</button>
                            <a class="button secondary" href="${pageContext.request.contextPath}/backoffice">Annuler</a>
                        </div>
                    </div>
                </form>
                <div id="s1-result" class="result-card hidden"></div>
            </section>
        </main>
    </div>
</div>

<script>
// === CAROUSEL ===
(function(){
    var slidesEl = document.querySelector('.s1-slides');
    if(!slidesEl) return;
    var slides = Array.from(slidesEl.children);
    var idx = 0;
    var dotsContainer = document.getElementById('s1-dots');
    var prevBtn = document.getElementById('s1-prev');
    var nextBtn = document.getElementById('s1-next');
    function update(){
        slidesEl.style.transform = 'translateX('+(-idx*100)+'%)';
        slides.forEach(function(s,i){ s.classList.toggle('active', i===idx); });
        Array.from(dotsContainer.children).forEach(function(d,i){ d.classList.toggle('active', i===idx); });
        prevBtn.classList.toggle('disabled', idx===0);
        nextBtn.classList.toggle('disabled', idx===slides.length-1);
    }
    slides.forEach(function(s,i){
        var dot = document.createElement('span');
        dot.className = 'carousel-dot'+(i===0?' active':'');
        dot.addEventListener('click', function(){ idx=i; update(); });
        dotsContainer.appendChild(dot);
    });
    prevBtn.addEventListener('click', function(){ if(idx>0){ idx--; update(); } });
    nextBtn.addEventListener('click', function(){ if(idx<slides.length-1){ idx++; update(); } });
    document.addEventListener('keydown', function(e){
        if(e.key==='ArrowLeft' && idx>0){ idx--; update(); }
        if(e.key==='ArrowRight' && idx<slides.length-1){ idx++; update(); }
    });
    update();
})();

// === AFFICHAGE CATÉGORIE ===
document.getElementById('select-categorie').addEventListener('change', function(){
    var uploadSection = document.getElementById('upload-section-global');
    var sectionTravailleur = document.getElementById('section-travailleur');
    var sectionInvestisseur = document.getElementById('section-investisseur');

    if(this.value === 'TRAVAILLEUR'){
        uploadSection.style.display = 'block';
        sectionTravailleur.classList.add('active');
        sectionInvestisseur.classList.remove('active');
    } else if(this.value === 'INVESTISSEUR'){
        uploadSection.style.display = 'block';
        sectionTravailleur.classList.remove('active');
        sectionInvestisseur.classList.add('active');
    } else {
        uploadSection.style.display = 'none';
        sectionTravailleur.classList.remove('active');
        sectionInvestisseur.classList.remove('active');
    }
});

// === AFFICHAGE DU NOM DU FICHIER ===
document.querySelectorAll('.upload-box input[type="file"]').forEach(function(input){
    input.addEventListener('change', function(){
        var box = this.closest('.upload-box');
        var fileName = box.querySelector('.file-name');
        if(this.files && this.files[0]){
            fileName.textContent = '✓ ' + this.files[0].name;
            box.classList.add('has-file');
        } else {
            fileName.textContent = 'Aucun fichier sélectionné';
            box.classList.remove('has-file');
        }
    });
});
</script>

<script>
    window.APP_CONTEXT_PATH = '${pageContext.request.contextPath}';
</script>
<script src="${pageContext.request.contextPath}/js/sprint1/sprint1-steps.js"></script>
<script src="${pageContext.request.contextPath}/js/common/file-upload-handler.js"></script>
<script src="${pageContext.request.contextPath}/js/sprint1/sprint1-upload-fix.js"></script>
</body>
</html>
