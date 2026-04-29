/**
 * Gestionnaire d'upload de fichiers
 * Gère l'upload des fichiers avec une meilleure gestion des erreurs et des messages de progression.
 *
 * IMPORTANT : les noms des inputs fichiers dans le JSP doivent suivre la convention
 *   piece.<NOM_ENUM_TYPEPIECE>
 * Exemple : piece.PHOTOS_IDENTITE, piece.COPIE_PASSEPORT, piece.COPIE_VISA_TRANSFORMABLE ...
 *
 * Le handler extrait la partie après "piece." et la compare DIRECTEMENT (sans toUpperCase)
 * à p.typePiece retourné par l'API, qui renvoie déjà la valeur de l'enum en majuscules.
 */

class FileUploadHandler {
    constructor(contextPath) {
        this.contextPath = contextPath || '';
        this.debug = true; // Active les logs de debug
    }

    log(message, data) {
        if (this.debug) {
            console.log(`[FileUploadHandler] ${message}`, data || '');
        }
    }

    logError(message, error) {
        console.error(`[FileUploadHandler] ${message}`, error || '');
    }

    /**
     * Uploade tous les fichiers pour une demande donnée.
     * @param {number} demandeId - L'ID de la demande
     * @param {Object} files     - Objet { "piece.PHOTOS_IDENTITE": File, ... }
     * @returns {Promise<{uploadedCount:number, errorCount:number, totalFiles:number}>}
     */
    async uploadFiles(demandeId, files) {
        this.log('Début upload', { demandeId, nbFiles: Object.keys(files).length });

        if (!files || Object.keys(files).length === 0) {
            this.log('Aucun fichier à uploader');
            return { uploadedCount: 0, errorCount: 0, totalFiles: 0 };
        }

        // 1. Récupérer la liste des pièces attendues pour cette demande
        const piecesUrl = `${this.contextPath}/demandes/${demandeId}/pieces`;
        this.log('Récupération des pièces', { url: piecesUrl });

        let piecesResponse;
        try {
            piecesResponse = await fetch(piecesUrl, {
                method: 'GET',
                credentials: 'same-origin',
                headers: { 'Accept': 'application/json' }
            });
        } catch (error) {
            this.logError('Erreur réseau lors de la récupération des pièces', error);
            throw new Error(`Impossible de récupérer les pièces : ${error.message}`);
        }

        if (!piecesResponse.ok) {
            throw new Error(
                `Erreur lors de la récupération des pièces (HTTP ${piecesResponse.status})`
            );
        }

        let piecesData;
        try {
            piecesData = await piecesResponse.json();
        } catch (error) {
            throw new Error('Réponse non-JSON lors de la récupération des pièces');
        }

        this.log('Pièces récupérées', piecesData);

        if (piecesData.success === false || !piecesData.data) {
            throw new Error(
                'Format de réponse invalide pour les pièces : ' +
                (piecesData.message || 'réponse inattendue')
            );
        }

        const pieces = piecesData.data; // tableau de PieceJustificativeResponse
        this.log('Liste des pièces disponibles', pieces.map(p => p.typePiece));

        let uploadedCount = 0;
        let errorCount    = 0;
        const totalFiles  = Object.keys(files).length;

        for (const [inputName, file] of Object.entries(files)) {
            this.log(`Traitement du fichier`, { inputName, fileName: file.name, fileSize: file.size });

            /*
             * On supprime le préfixe "piece." pour obtenir directement
             * la valeur de l'enum TypePiece (déjà en MAJUSCULES dans le name de l'input).
             * Exemple : "piece.COPIE_PASSEPORT" → "COPIE_PASSEPORT"
             */
// Si le nom est déjà une valeur d'enum (ex: PHOTOS_IDENTITE), on le garde tel quel
// Sinon on enlève le préfixe "piece."
            var pieceType = inputName;
            if (pieceType.indexOf('piece.') === 0) {
                pieceType = pieceType.replace(/^piece\./, '');
            }               
    this.log(`Recherche de la pièce pour le type`, { pieceType });

            const piece = pieces.find(p => p.typePiece === pieceType);

            if (!piece) {
                this.log(`Aucune pièce trouvée pour le type "${pieceType}". Types disponibles : ${pieces.map(p => p.typePiece).join(', ')}`);
                // On ne compte pas comme erreur : la pièce n'est simplement pas requise
                continue;
            }

            this.log(`Pièce trouvée`, { pieceId: piece.id, typePiece: piece.typePiece });

            try {
                const formData = new FormData();
                // IMPORTANT : le paramètre doit s'appeler "fichier" exactement
                formData.append('fichier', file, file.name);

                const uploadUrl = `${this.contextPath}/demandes/${demandeId}/pieces/${piece.id}/upload`;
                this.log(`Upload vers`, { url: uploadUrl });

                const uploadResponse = await fetch(uploadUrl, {
                    method: 'POST',
                    body: formData,
                    credentials: 'same-origin'
                    // Ne PAS mettre Content-Type pour FormData
                });

                if (uploadResponse.ok) {
                    uploadedCount++;
                    this.log(`✓ Upload réussi`, { inputName, pieceId: piece.id, pieceType });

                    // Mise à jour DOM : afficher le nom du fichier dans la boîte correspondante
                    try {
                        const selectors = [
                            `input[type="file"][name="piece.${inputName}"]`,
                            `input[type="file"][name="${inputName}"]`
                        ];
                        let inputEl = null;
                        for (const sel of selectors) {
                            inputEl = document.querySelector(sel);
                            if (inputEl) break;
                        }
                        if (inputEl) {
                            const box = inputEl.closest('.upload-box');
                            const fileNameEl = box ? box.querySelector('.file-name') : null;
                            if (fileNameEl) {
                                fileNameEl.textContent = '✓ ' + file.name;
                                if (box) box.classList.add('has-file');
                            }
                        }
                    } catch (domErr) {
                        this.logError('Erreur mise à jour DOM après upload', domErr);
                    }
                } else {
                    errorCount++;
                    let errorMessage = `HTTP ${uploadResponse.status}`;
                    try {
                        const errorData = await uploadResponse.json();
                        errorMessage += ` - ${errorData.message || 'Erreur inconnue'}`;
                    } catch (e) {
                        // Réponse non-JSON
                    }
                    this.logError(`✗ Upload échoué`, { inputName, status: uploadResponse.status, message: errorMessage });

                    // Marquer la boîte comme en erreur (si trouvée)
                    try {
                        const selectors = [
                            `input[type="file"][name="piece.${inputName}"]`,
                            `input[type="file"][name="${inputName}"]`
                        ];
                        let inputEl = null;
                        for (const sel of selectors) {
                            inputEl = document.querySelector(sel);
                            if (inputEl) break;
                        }
                        if (inputEl) {
                            const box = inputEl.closest('.upload-box');
                            const fileNameEl = box ? box.querySelector('.file-name') : null;
                            if (fileNameEl) {
                                fileNameEl.textContent = '✗ ' + file.name + ' (erreur)';
                                if (box) box.classList.add('upload-error');
                            }
                        }
                    } catch (domErr) {
                        this.logError('Erreur mise à jour DOM après échec upload', domErr);
                    }
                }
            } catch (fileError) {
                errorCount++;
                this.logError(`Exception lors de l'upload`, { inputName, error: fileError.message });
            }
        }

        const result = { uploadedCount, errorCount, totalFiles };
        this.log('Résultat de l\'upload', result);
        return result;
    }

    /**
     * Affiche le résultat de l'upload dans un élément DOM.
     * @param {HTMLElement} resultElement
     * @param {{uploadedCount:number, errorCount:number, totalFiles:number}} uploadResult
     */
    displayUploadResult(resultElement, uploadResult) {
        if (!resultElement) {
            this.log('Element résultat introuvable');
            return;
        }

        this.log('Affichage du résultat', uploadResult);
        resultElement.classList.remove('hidden');

        if (uploadResult.totalFiles === 0) {
            resultElement.style.borderColor = 'var(--success)';
            resultElement.innerHTML =
                '<strong>Demande validée</strong>' +
                '<span>Aucun fichier à uploader. Redirection vers le scan en cours…</span>';
        } else if (uploadResult.errorCount === 0) {
            resultElement.style.borderColor = 'var(--success)';
            resultElement.innerHTML =
                '<strong>Demande validée</strong>' +
                `<span>${uploadResult.uploadedCount} fichier(s) uploadé(s) avec succès. ` +
                'Redirection vers le scan en cours…</span>';
        } else {
            resultElement.style.borderColor = 'var(--warning)';
            resultElement.innerHTML =
                '<strong>Demande créée (upload partiel)</strong>' +
                `<span>${uploadResult.uploadedCount} fichier(s) uploadé(s) sur ` +
                `${uploadResult.totalFiles}. ` +
                `${uploadResult.errorCount} erreur(s). ` +
                'Redirection vers le scan en cours…</span>';
        }

        // Scroll vers le résultat
        resultElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }

    /**
     * Affiche une erreur d'upload dans un élément DOM.
     * @param {HTMLElement} resultElement
     * @param {Error} error
     */
    displayUploadError(resultElement, error) {
        if (!resultElement) {
            this.logError('Element résultat introuvable pour afficher l\'erreur');
            return;
        }

        this.logError('Affichage erreur', error);
        resultElement.classList.remove('hidden');
        resultElement.style.borderColor = 'var(--danger)';
        resultElement.innerHTML =
            '<strong>Erreur upload</strong>' +
            `<span>Erreur lors de l'upload des fichiers : ${error.message}</span>`;

        // Scroll vers l'erreur
        resultElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
}

// Rendre disponible globalement
if (typeof window !== 'undefined') {
    window.FileUploadHandler = FileUploadHandler;
}