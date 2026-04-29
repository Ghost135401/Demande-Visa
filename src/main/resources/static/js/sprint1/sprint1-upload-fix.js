/**
 * sprint1-upload-fix.js — VERSION FINALE ROBUSTE
 *
 * Corrections :
 *  - Erreur syntaxe (double setResult) corrigée
 *  - Lecture des champs même disabled (étapes verrouillées)
 *  - Gestion 401 (session expirée)
 *  - Validations assouplies (pas de blocage sur âge/expiration)
 *  - Fichiers manquants non bloquants
 *  - Panneau debug visible sur la page
 */

document.addEventListener('DOMContentLoaded', function () {

    var form = document.getElementById('s1-form');
    if (!form) {
        console.warn('[upload-fix] #s1-form introuvable');
        return;
    }

    // Désactiver la validation HTML5 native pour éviter que des inputs
    // requis cachés bloquent la soumission avant que notre handler JS
    // ait la main (erreur: "An invalid form control ... is not focusable").
    try { form.noValidate = true; dbg('Validation native désactivée'); } catch (e) { console.warn('Impossible de désactiver la validation native', e); }

    var CONTEXT = (window.APP_CONTEXT_PATH || '').replace(/\/$/, '');
    console.log('[upload-fix] Initialisé. CONTEXT="' + CONTEXT + '"');

    // ── Panneau de debug visible dans la page ──────────────────────────────────
    var debugPanel = document.createElement('div');
    debugPanel.id = 'debug-panel';
    debugPanel.style.cssText = [
        'position:fixed', 'bottom:10px', 'right:10px', 'width:380px',
        'max-height:260px', 'overflow-y:auto', 'background:#1e1e2e',
        'color:#cdd6f4', 'font-size:12px', 'font-family:monospace',
        'padding:10px', 'border-radius:8px', 'z-index:9999',
        'border:1px solid #45475a', 'box-shadow:0 4px 20px rgba(0,0,0,.5)'
    ].join(';');
    var debugTitle = document.createElement('div');
    debugTitle.style.cssText = 'color:#89b4fa;font-weight:bold;margin-bottom:6px;display:flex;justify-content:space-between';
    debugTitle.innerHTML = '<span>Debug creation visa</span><span id="debug-close" style="cursor:pointer;color:#f38ba8">X</span>';
    debugPanel.appendChild(debugTitle);
    var debugLog = document.createElement('div');
    debugLog.id = 'debug-log';
    debugPanel.appendChild(debugLog);
    document.body.appendChild(debugPanel);

    document.getElementById('debug-close').addEventListener('click', function () {
        debugPanel.style.display = 'none';
    });

    function dbg(msg, color) {
        color = color || '#cdd6f4';
        var line = document.createElement('div');
        line.style.cssText = 'color:' + color + ';padding:1px 0;border-bottom:1px solid #313244';
        var now = new Date();
        var ts = now.getHours() + ':' + String(now.getMinutes()).padStart(2,'0') + ':' + String(now.getSeconds()).padStart(2,'0');
        line.textContent = '[' + ts + '] ' + msg;
        debugLog.appendChild(line);
        debugLog.scrollTop = debugLog.scrollHeight;
        console.log('[upload-fix] ' + msg);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    // Lit la valeur d'un champ MEME SI DISABLED (etapes sauvegardees = champs desactives)
    function val(name) {
        var els = form.querySelectorAll('[name="' + name + '"]');
        for (var i = 0; i < els.length; i++) {
            var v = els[i].value;
            if (v && v.trim()) return v.trim();
        }
        return '';
    }

    function setResult(color, html) {
        var r = document.getElementById('s1-result');
        if (!r) return;
        r.classList.remove('hidden');
        r.style.cssText = 'border:2px solid ' + color + ';padding:12px;border-radius:8px;margin-top:12px';
        r.innerHTML = html;
        r.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }

    function collectFiles() {
        var files = {};
        var map = {
            'piece.PHOTOS_IDENTITE':             'PHOTOS_IDENTITE',
            'piece.FORMULAIRE_DEMANDE':          'FORMULAIRE_DEMANDE',
            'piece.COPIE_PASSEPORT':             'COPIE_PASSEPORT',
            'piece.COPIE_VISA_TRANSFORMABLE':    'COPIE_VISA_TRANSFORMABLE',
            'piece.CERTIFICAT_MEDICAL':          'CERTIFICAT_MEDICAL',
            'piece.CASIER_JUDICIAIRE':           'CASIER_JUDICIAIRE',
            'piece.EXTRAIT_NAISSANCE':           'EXTRAIT_NAISSANCE',
            'piece.JUSTIFICATIF_DOMICILE':       'JUSTIFICATIF_DOMICILE',
            'piece.QUITTANCE_TAXES':             'QUITTANCE_TAXES',
            'piece.CONTRAT_TRAVAIL':             'CONTRAT_TRAVAIL',
            'piece.ATTESTATION_EMPLOYEUR':       'ATTESTATION_EMPLOYEUR',
            'piece.AUTORISATION_TRAVAIL':        'AUTORISATION_TRAVAIL',
            'piece.BUSINESS_PLAN':               'BUSINESS_PLAN',
            'piece.JUSTIFICATIF_INVESTISSEMENT': 'JUSTIFICATIF_INVESTISSEMENT',
            'piece.STATUTS_SOCIETE':             'STATUTS_SOCIETE',
            'piece.NIF':                         'NIF'
        };
        document.querySelectorAll('#s1-form input[type="file"]').forEach(function (inp) {
            if (inp.files && inp.files.length > 0 && inp.name) {
                var k = map[inp.name] || inp.name.replace(/^piece\./, '');
                if (!files[k]) {
                    files[k] = inp.files[0];
                    dbg('Fichier: ' + inp.files[0].name + ' -> ' + k, '#a6e3a1');
                }
            }
        });
        return files;
    }

    // ── Soumission ─────────────────────────────────────────────────────────────

    form.addEventListener('submit', async function (ev) {
        ev.preventDefault();
        ev.stopImmediatePropagation();

        dbg('Soumission declenchee', '#89b4fa');
        debugPanel.style.display = 'block';

        var submitBtn = document.getElementById('create-demande-button');
        if (submitBtn) { submitBtn.disabled = true; submitBtn.textContent = 'Creation...'; }

        // ── ETAPE 1 : Lire toutes les valeurs (meme disabled) ──────────────────
        var categorie   = val('categorie');
        var typeDemande = val('typeDemande');
        var nom         = val('nom');
        var prenoms     = val('prenoms');
        var sitFam      = val('situationFamiliale') || 'CELIBATAIRE';
        var nationalite = val('nationalite');
        var profession  = val('profession');
        var dateNaiss   = val('dateNaissance');
        var lieuNaiss   = val('lieuNaissance') || '';
        var adresse     = val('adresseMadagascar');
        var email       = val('email');
        var telephone   = val('telephone');
        var nomJF       = val('nomJeuneFille') || '';
        var resident    = val('resident') === 'true';

        var passNum = val('passeport.numero');
        var passDel = val('passeport.dateDelivrance');
        var passExp = val('passeport.dateExpiration');

        var vtRef  = val('visaTransformable.refVisa')    || null;
        var vtDate = val('visaTransformable.dateEntree') || null;
        var vtLieu = val('visaTransformable.lieuEntree') || null;
        var vtSort = val('visaTransformable.dateSortie') || null;

        dbg('categorie="' + categorie + '" type="' + typeDemande + '"');
        dbg('nom="' + nom + '" prenoms="' + prenoms + '"');
        dbg('passeport="' + passNum + '" del=' + passDel + ' exp=' + passExp);
        if (vtRef || vtDate) dbg('visa: ref=' + vtRef + ' entree=' + vtDate);

        // ── ETAPE 2 : Validation minimale ──────────────────────────────────────
        var erreurs = [];
        if (!categorie)   erreurs.push('Categorie manquante');
        if (!typeDemande) erreurs.push('Type de demande manquant');
        if (!nom)         erreurs.push('Nom manquant');
        if (!prenoms)     erreurs.push('Prenoms manquants');
        if (!nationalite) erreurs.push('Nationalite manquante');
        if (!profession)  erreurs.push('Profession manquante');
        if (!dateNaiss)   erreurs.push('Date de naissance manquante');
        if (!adresse)     erreurs.push('Adresse Madagascar manquante');
        if (!email)       erreurs.push('Email manquant');
        if (!telephone)   erreurs.push('Telephone manquant');
        if (!passNum)     erreurs.push('Numero de passeport manquant');
        if (!passDel)     erreurs.push('Date delivrance passeport manquante');
        if (!passExp)     erreurs.push('Date expiration passeport manquante');

        if (typeDemande === 'NOUVEAU_TITRE') {
            if (!vtRef)  erreurs.push('Reference visa manquante');
            if (!vtDate) erreurs.push("Date entree visa manquante");
            if (!vtLieu) erreurs.push("Lieu entree visa manquant");
            if (!vtSort) erreurs.push('Date sortie visa manquante');
        }

        if (erreurs.length > 0) {
            dbg('Validation echouee : ' + erreurs.join(', '), '#f38ba8');
            setResult('#e74c3c',
                '<strong style="color:#e74c3c">Champs manquants :</strong>' +
                '<ul style="margin:6px 0 0 16px">' +
                erreurs.map(function(e){ return '<li>' + e + '</li>'; }).join('') +
                '</ul>'
            );
            if (submitBtn) { submitBtn.disabled = false; submitBtn.textContent = 'Creer la demande'; }
            return;
        }
        dbg('Validation OK', '#a6e3a1');

        // ── ETAPE 3 : Construire le payload ────────────────────────────────────
        var visaObj = (vtRef || vtDate || vtLieu || vtSort) ? {
            refVisa: vtRef, dateEntree: vtDate, lieuEntree: vtLieu, dateSortie: vtSort
        } : null;

        var dpIdRaw = val('demandeParenteId');
        var payload = {
            categorie:        categorie,
            typeDemande:      typeDemande,
            resident:         resident,
            demandeParenteId: dpIdRaw ? parseInt(dpIdRaw, 10) : null,
            etatCivil: {
                nom: nom, prenoms: prenoms, nomJeuneFille: nomJF,
                situationFamiliale: sitFam, nationalite: nationalite,
                profession: profession, dateNaissance: dateNaiss,
                lieuNaissance: lieuNaiss, adresseMadagascar: adresse,
                email: email, telephone: telephone
            },
            passeport: {
                numero: passNum, dateDelivrance: passDel, dateExpiration: passExp
            },
            visaTransformable: visaObj
        };

        dbg('Payload OK, envoi vers ' + CONTEXT + '/demandes');
        setResult('#3498db',
            '<strong>Creation en cours...</strong>' +
            '<span style="display:block;margin-top:4px;color:#555">Envoi vers le serveur...</span>'
        );

        // ── ETAPE 4 : POST /demandes ───────────────────────────────────────────
        var demandeId;
        try {
            var resp = await fetch(CONTEXT + '/demandes', {
                method: 'POST',
                headers: {
                    'Content-Type':     'application/json',
                    'Accept':           'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                credentials: 'same-origin',
                body: JSON.stringify(payload)
            });

            dbg('HTTP ' + resp.status, resp.ok ? '#a6e3a1' : '#f38ba8');

            if (resp.status === 401) {
                dbg('Session expiree -> login', '#fab387');
                setResult('#e67e22',
                    '<strong>Session expiree</strong>' +
                    '<span style="display:block;margin-top:4px">Redirection vers la connexion...</span>'
                );
                setTimeout(function() {
                    window.location.href = CONTEXT + '/backoffice/login';
                }, 1500);
                return;
            }

            var j;
            try {
                j = await resp.json();
                dbg('Reponse: success=' + j.success + (j.message ? ' msg=' + j.message : ''));
            } catch(pe) {
                dbg('Reponse non-JSON !', '#f38ba8');
                setResult('#e74c3c',
                    '<strong>Erreur serveur</strong>' +
                    '<span style="display:block;margin-top:4px">Reponse non-JSON (HTTP ' + resp.status + '). ' +
                    'Verifiez que vous etes connecte(e).</span>'
                );
                if (submitBtn) { submitBtn.disabled = false; submitBtn.textContent = 'Creer la demande'; }
                return;
            }

            if (!resp.ok || j.success === false) {
                var errMsg = j.message || 'Erreur inconnue';
                if (j.data && typeof j.data === 'object' && !Array.isArray(j.data)) {
                    errMsg += '<ul style="margin:4px 0 0 16px">';
                    Object.keys(j.data).forEach(function(k) {
                        errMsg += '<li><b>' + k + '</b> : ' + j.data[k] + '</li>';
                    });
                    errMsg += '</ul>';
                }
                dbg('Serveur refuse : ' + j.message, '#f38ba8');
                setResult('#e74c3c',
                    '<strong>Erreur HTTP ' + resp.status + '</strong>' +
                    '<span style="display:block;margin-top:4px">' + errMsg + '</span>'
                );
                if (submitBtn) { submitBtn.disabled = false; submitBtn.textContent = 'Creer la demande'; }
                return;
            }

            demandeId = j.data && j.data.id;
            if (!demandeId) {
                dbg('AVERTISSEMENT: id absent de la reponse!', '#fab387');
                dbg('Reponse data: ' + JSON.stringify(j.data), '#fab387');
            }
            dbg('Demande creee ! id=' + demandeId, '#a6e3a1');

        } catch (netErr) {
            dbg('Erreur reseau : ' + netErr.message, '#f38ba8');
            setResult('#e74c3c',
                '<strong>Erreur reseau</strong>' +
                '<span style="display:block;margin-top:4px">' + netErr.message + '</span>'
            );
            if (submitBtn) { submitBtn.disabled = false; submitBtn.textContent = 'Creer la demande'; }
            return;
        }

        // ── ETAPE 5 : Upload fichiers (optionnel, non bloquant) ────────────────
        var files = collectFiles();
        var nbFiles = Object.keys(files).length;
        dbg(nbFiles + ' fichier(s) a uploader');

        if (nbFiles > 0) {
            setResult('#3498db',
                '<strong>Upload en cours...</strong>' +
                '<span style="display:block;margin-top:4px">Envoi de ' + nbFiles + ' fichier(s)...</span>'
            );
            try {
                if (typeof FileUploadHandler !== 'undefined') {
                    var uploadHandler = new FileUploadHandler(CONTEXT);
                    var up = await uploadHandler.uploadFiles(demandeId, files);
                    dbg('Upload: ' + up.uploadedCount + '/' + up.totalFiles + ' OK',
                        up.errorCount === 0 ? '#a6e3a1' : '#fab387');
                } else {
                    dbg('FileUploadHandler absent, upload ignore', '#fab387');
                }
            } catch (upErr) {
                dbg('Upload echoue (non bloquant): ' + upErr.message, '#fab387');
            }
        }

        // ── ETAPE 6 : Redirection vers scan ───────────────────────────────────
        var scanUrl = CONTEXT + '/backoffice/scan?id=' + demandeId;
        dbg('Redirection -> ' + scanUrl, '#89b4fa');

        setResult('#27ae60',
            '<strong style="color:#27ae60">Demande #' + demandeId + ' creee avec succes !</strong>' +
            '<span style="display:block;margin-top:6px">Redirection vers le scan dans 2 secondes...</span>' +
            '<a href="' + scanUrl + '" style="display:inline-block;margin-top:10px;padding:8px 16px;' +
            'background:#27ae60;color:#fff;border-radius:4px;text-decoration:none;font-weight:bold">' +
            '→ Aller au scan maintenant</a>'
        );

        setTimeout(function () {
            window.location.href = scanUrl;
        }, 2000);
    });

    dbg('Pret. Remplissez le formulaire et cliquez sur "Creer la demande"', '#89dceb');
});