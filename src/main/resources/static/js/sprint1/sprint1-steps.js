// Gestion des étapes du formulaire Sprint 1
document.addEventListener('DOMContentLoaded', function () {

    // ── Création dynamique du toast (évite l'erreur si absent du HTML) ────────
    let toast = document.getElementById('toast');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'toast';
        toast.className = 'toast hidden';
        document.body.appendChild(toast);
    }

    // ── Navigation entre les étapes ─────────────────────────────────────────────
    const slidesEl = document.querySelector('.s1-slides');
    if (!slidesEl) return;
    
    const slides = Array.from(slidesEl.children);
    let idx = 0;
    const dotsContainer = document.getElementById('s1-dots');
    const prevBtn = document.getElementById('s1-prev');
    const nextBtn = document.getElementById('s1-next');
    
    // Logs de débogage
    console.log('Éléments du carrousel:', {
        slidesEl,
        slides,
        idx,
        dotsContainer,
        prevBtn,
        nextBtn
    });

    function updateCarousel() {
        slidesEl.style.transform = 'translateX(' + (-idx * 100) + '%)';
        slides.forEach((slide, i) => {
            slide.classList.toggle('active', i === idx);
        });
        Array.from(dotsContainer.children).forEach((d, i) => {
            d.classList.toggle('active', i === idx);
        });
        prevBtn.classList.toggle('disabled', idx === 0);
        nextBtn.classList.toggle('disabled', idx === slides.length - 1);
        // Dispatch custom event so other scripts can react to slide changes
        try {
            document.dispatchEvent(new CustomEvent('s1:slide-changed', { detail: { index: idx } }));
        } catch (e) {
            console.warn('[sprint1-steps] Impossible de dispatch l\'evenement slide-changed', e);
        }
    }

    slides.forEach((s, i) => {
        const dot = document.createElement('span');
        dot.className = 'carousel-dot' + (i === 0 ? ' active' : '');
        dot.addEventListener('click', function() {
            idx = i;
            updateCarousel();
        });
        dotsContainer.appendChild(dot);
    });

    prevBtn.addEventListener('click', function(e) {
        console.log('Clic sur la flèche précédente, idx:', idx);
        e.preventDefault();
        e.stopPropagation();
        if (idx > 0) {
            idx--;
            console.log('Nouvel idx:', idx);
            updateCarousel();
        }
    });

    nextBtn.addEventListener('click', function(e) {
        console.log('Clic sur la flèche suivante, idx:', idx);
        e.preventDefault();
        e.stopPropagation();
        if (idx < slides.length - 1) {
            idx++;
            console.log('Nouvel idx:', idx);
            updateCarousel();
        }
    });

    document.addEventListener('keydown', function(e) {
        if (e.key === 'ArrowLeft' && idx > 0) {
            e.preventDefault();
            idx--;
            updateCarousel();
        }
        if (e.key === 'ArrowRight' && idx < slides.length - 1) {
            e.preventDefault();
            idx++;
            updateCarousel();
        }
    });

    updateCarousel();

    // ── État de chaque étape ───────────────────────────────────────────────────
    const stepStates = {
        1: { saved: false, editable: true },
        2: { saved: false, editable: true },
        3: { saved: false, editable: true },
        4: { saved: false, editable: true }
    };

    // ── Boutons Enregistrer ───────────────────────────────────────────────────
    document.querySelectorAll('.save-step').forEach(function (button) {
        button.addEventListener('click', function () {
            saveStep(parseInt(this.dataset.step));
        });
    });

    // ── Boutons Modifier ──────────────────────────────────────────────────────
    document.querySelectorAll('.edit-step').forEach(function (button) {
        button.addEventListener('click', function () {
            editStep(parseInt(this.dataset.step));
        });
    });

    // ─────────────────────────────────────────────────────────────────────────
    // saveStep : valide les champs obligatoires, verrouille la slide
    // ─────────────────────────────────────────────────────────────────────────
    function saveStep(step) {
        const slide = document.querySelector('.s1-slide[data-step="' + step + '"]');
        if (!slide) return;

        // Validation des champs required
        var inputs  = slide.querySelectorAll('input[required], select[required], textarea[required]');
        var isValid = true;

        inputs.forEach(function (input) {
            if (!input.value.trim()) {
                isValid = false;
                input.style.borderColor = '#c94c4c';
            } else {
                input.style.borderColor = '';
            }
        });

        if (!isValid) {
            showToast('Veuillez remplir tous les champs obligatoires', true);
            return;
        }

        // CORRECTION : template literal remplacé par concaténation
        // (évite les conflits potentiels avec les moteurs de templates côté serveur)
        console.log('Enregistrement de l\'etape ' + step);

        stepStates[step].saved    = true;
        stepStates[step].editable = false;

        updateStepUI(step);
        showToast('Etape ' + step + ' enregistree avec succes');

        // Si toutes les étapes sont sauvegardées, verrouiller et afficher la section d'upload
        lockAllSteps();

        // Avancer automatiquement vers la prochaine slide si disponible
        var nextSlide = document.querySelector('.s1-slide[data-step="' + (step + 1) + '"]');
        if (nextSlide) {
            var nextBtn = document.getElementById('s1-next');
            if (nextBtn && !nextBtn.classList.contains('disabled')) {
                nextBtn.click();
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // editStep : déverrouille la slide pour correction
    // ─────────────────────────────────────────────────────────────────────────
    function editStep(step) {
        var slide = document.querySelector('.s1-slide[data-step="' + step + '"]');
        if (!slide) return;

        stepStates[step].saved    = false;
        stepStates[step].editable = true;

        updateStepUI(step);
        showToast('Etape ' + step + ' modifiable');
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateStepUI : active/désactive les champs et bascule les boutons
    // ─────────────────────────────────────────────────────────────────────────
    function updateStepUI(step) {
        var slide = document.querySelector('.s1-slide[data-step="' + step + '"]');
        if (!slide) return;

        var saveButton = slide.querySelector('.save-step');
        var editButton = slide.querySelector('.edit-step');
        var inputs     = slide.querySelectorAll('input, select, textarea');

        if (stepStates[step].saved && !stepStates[step].editable) {
            // Étape verrouillée
            if (saveButton) saveButton.classList.add('hidden');
            if (editButton) editButton.classList.remove('hidden');
            // Marquer visuellement la slide comme enregistrée
            slide.classList.add('saved');
            inputs.forEach(function (input) { input.disabled = true; });
        } else {
            // Étape modifiable
            if (saveButton) saveButton.classList.remove('hidden');
            if (editButton) editButton.classList.add('hidden');
            slide.classList.remove('saved');
            inputs.forEach(function (input) { input.disabled = false; });
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // allStepsSaved : retourne true si toutes les étapes sont validées
    // ─────────────────────────────────────────────────────────────────────────
    function allStepsSaved() {
        return Object.values(stepStates).every(function (state) { return state.saved; });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // lockAllSteps : verrouille tout une fois toutes les étapes confirmées
    // ─────────────────────────────────────────────────────────────────────────
    function lockAllSteps() {
        if (allStepsSaved()) {
            Object.keys(stepStates).forEach(function (step) {
                stepStates[step].editable = false;
                updateStepUI(parseInt(step));
            });
            // Afficher la section d'upload global si présente
            try {
                var uploadSection = document.getElementById('upload-section-global');
                if (uploadSection) uploadSection.style.display = 'block';
                // Activer le required seulement pour les boîtes marquées obligatoires
                uploadSection.querySelectorAll('.upload-box').forEach(function(box){
                        var input = box.querySelector('input[type="file"]');
                        var hasStar = box.querySelector('.required-star') !== null;
                        // Ne rendre required que si la boîte est visible afin d'éviter
                        // l'erreur "An invalid form control ... is not focusable" qui
                        // bloque la soumission native lorsque des inputs requis sont
                        // dans une section masquée.
                        var style = window.getComputedStyle(box);
                        var visible = style.display !== 'none' && style.visibility !== 'hidden' && box.offsetParent !== null;
                        if (input) {
                            input.required = !!hasStar && visible;
                        }
                    });
            } catch (e) {
                console.warn('[sprint1-steps] Impossible d afficher la section upload', e);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // showToast : affiche un message temporaire en bas de page
    // CORRECTION : le toast est créé dynamiquement si absent du DOM
    // ─────────────────────────────────────────────────────────────────────────
    function showToast(message, isError) {
        if (!toast) return;
        isError = isError || false;

        toast.textContent = message;
        toast.classList.remove('hidden');
        toast.classList.remove('error');

        if (isError) {
            toast.classList.add('error');
        }

        // Annuler le timer précédent si encore en cours
        if (showToast._timer) {
            clearTimeout(showToast._timer);
        }
        showToast._timer = setTimeout(function () {
            toast.classList.add('hidden');
        }, 3000);
    }

    // ── Initialisation ────────────────────────────────────────────────────────
    Object.keys(stepStates).forEach(function (step) {
        updateStepUI(parseInt(step));
    });

    // S'assurer que les inputs file ne sont pas requis tant que la section d'upload
    // n'est pas affichée (évite les validations natives bloquantes).
    try {
        document.querySelectorAll('#s1-form input[type="file"]').forEach(function (f) {
            f.required = false;
        });
    } catch (e) {
        console.warn('[sprint1-steps] Impossible de desactiver required sur inputs file', e);
    }
});