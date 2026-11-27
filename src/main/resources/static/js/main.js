document.addEventListener('DOMContentLoaded', () => {
    const htmlEl = document.documentElement;

    /* 1. LÓGICA DEL SPLASH SCREEN */
    const splash = document.getElementById('lauki-splash');
    if (splash) {
        if (!sessionStorage.getItem('lauki_splash_visto')) {
            const logo = document.getElementById('splash-logo');
            const dot = document.getElementById('splash-dot');
            const progress = document.getElementById('splash-progress');

            requestAnimationFrame(() => {
                if (logo) logo.style.transform = 'scale(1)';
                if (progress) progress.style.width = '100%';
                if (dot) setTimeout(() => { dot.style.opacity = '1'; }, 700);
            });

            setTimeout(() => {
                splash.style.opacity = '0';
                setTimeout(() => {
                    splash.style.display = 'none';
                    sessionStorage.setItem('lauki_splash_visto', 'true');
                }, 300);
            }, 3000);
        } else {
            splash.style.display = 'none';
        }
    }

    /* 2. GESTIÓN DE TEMAS Y ACENTOS */
    const currentTheme = htmlEl.getAttribute('data-theme') || 'light';
    const currentAccent = htmlEl.getAttribute('data-accent') || 'magenta';

    const themeBtn = document.getElementById('theme-toggle');
    const accentBtns = document.querySelectorAll('[data-accent-option]');

    function updateControlsUI(theme, accent) {
        if (themeBtn) {
            const icon = themeBtn.querySelector('i');
            if (icon) {
                icon.className = theme === 'light' ? 'bi bi-moon' : 'bi bi-sun';
            }
        }
        accentBtns.forEach(btn => {
            if (btn.getAttribute('data-accent-option') === accent) {
                btn.classList.add('active');
            } else {
                btn.classList.remove('active');
            }
        });
    }

    updateControlsUI(currentTheme, currentAccent);

    if (themeBtn) {
        themeBtn.addEventListener('click', () => {
            const nextTheme = htmlEl.getAttribute('data-theme') === 'light' ? 'dark' : 'light';
            htmlEl.setAttribute('data-theme', nextTheme);
            localStorage.setItem('lauki-theme', nextTheme);
            updateControlsUI(nextTheme, htmlEl.getAttribute('data-accent') || 'magenta');
        });
    }

    accentBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const nextAccent = btn.getAttribute('data-accent-option');
            htmlEl.setAttribute('data-accent', nextAccent);
            localStorage.setItem('lauki-accent', nextAccent);
            updateControlsUI(htmlEl.getAttribute('data-theme') || 'light', nextAccent);
        });
    });

    /* 3. SIDEBAR COLAPSABLE (ESCRITORIO) Y RESPONSIVE (MÓVIL) */
    const sidebarToggleBtn = document.querySelector('[data-sidebar-toggle]');
    if (sidebarToggleBtn) {
        sidebarToggleBtn.addEventListener('click', () => {
            const currentState = htmlEl.getAttribute('data-sidebar') || 'expanded';
            const nextState = currentState === 'collapsed' ? 'expanded' : 'collapsed';
            htmlEl.setAttribute('data-sidebar', nextState);
            localStorage.setItem('lauki-sidebar', nextState);
        });
    }

    const mobileToggle = document.getElementById('mobile-menu-toggle');
    const sidebar = document.querySelector('.sidebar');
    const backdrop = document.getElementById('sidebar-backdrop');

    function toggleMobileMenu() {
        if (sidebar && backdrop) {
            sidebar.classList.toggle('sidebar--open');
            backdrop.classList.toggle('d-none');
        }
    }

    if (mobileToggle && backdrop) {
        mobileToggle.addEventListener('click', toggleMobileMenu);
        backdrop.addEventListener('click', toggleMobileMenu);
    }

    /* 4. MOSTRAR / OCULTAR CONTRASEÑA (INTERACCIÓN VISUAL UI) */
    const togglePasswordBtn = document.getElementById('toggle-password');
    const passwordInput = document.getElementById('password');

    if (togglePasswordBtn && passwordInput) {
        togglePasswordBtn.addEventListener('click', () => {
            const isPassword = passwordInput.getAttribute('type') === 'password';
            passwordInput.setAttribute('type', isPassword ? 'text' : 'password');
            const icon = togglePasswordBtn.querySelector('i');
            if (icon) {
                icon.className = isPassword ? 'bi bi-eye' : 'bi bi-eye-slash';
            }
        });
    }

    /* =========================================================================
       5. VALIDACIÓN EN CLIENTE DEL LOGIN (COMENTADA)
       Motivo: La validación de campos, formato y credenciales corresponde al 
       backend (AuthController.java en Spring Boot). Se deja comentada para no 
       interceptar el envío del formulario ni duplicar reglas del servidor.
       =========================================================================
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', (e) => {
            const emailInput = document.getElementById('email');
            const emailError = document.getElementById('email-error');
            const passwordError = document.getElementById('password-error');
            let isValid = true;

            if (emailInput) emailInput.classList.remove('is-invalid');
            if (passwordInput) passwordInput.classList.remove('is-invalid');
            if (emailError) emailError.classList.add('d-none');
            if (passwordError) passwordError.classList.add('d-none');

            if (emailInput && emailError) {
                const emailVal = emailInput.value.trim();
                if (!emailVal) {
                    emailInput.classList.add('is-invalid');
                    emailError.textContent = 'Campos requeridos';
                    emailError.classList.remove('d-none');
                    isValid = false;
                } else if (!emailVal.includes('@')) {
                    emailInput.classList.add('is-invalid');
                    emailError.textContent = 'Formato de correo inválido';
                    emailError.classList.remove('d-none');
                    isValid = false;
                }
            }

            if (passwordInput && passwordError) {
                if (!passwordInput.value.trim()) {
                    passwordInput.classList.add('is-invalid');
                    passwordError.textContent = 'Campos requeridos';
                    passwordError.classList.remove('d-none');
                    isValid = false;
                }
            }

            if (!isValid) {
                e.preventDefault();
            }
        });
    }
    */

    /* 6. GESTIÓN GENÉRICA DE MODALES (Nuevo Curso, Usuario, Grupo, Reto...) */
    document.querySelectorAll('[data-modal-open]').forEach(trigger => {
        trigger.addEventListener('click', () => {
            const targetId = trigger.getAttribute('data-modal-open');
            const modal = document.getElementById(targetId);
            if (modal) {
                modal.classList.remove('d-none');
                const firstInput = modal.querySelector('input:not([type="hidden"]), select, textarea');
                if (firstInput) setTimeout(() => firstInput.focus(), 50);
            }
        });
    });

    function closeAllModals() {
        document.querySelectorAll('.modal-overlay').forEach(modal => {
            modal.classList.add('d-none');
            const form = modal.querySelector('form');
            if (form) form.reset();
        });
    }

    document.querySelectorAll('[data-modal-close]').forEach(btn => {
        btn.addEventListener('click', closeAllModals);
    });

    document.querySelectorAll('.modal-overlay').forEach(overlay => {
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) closeAllModals();
        });
    });

    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') closeAllModals();
    });

    /* 7. SELECTORES DESPLEGABLES PERSONALIZADOS (Sincronizados con <select> nativo) */
    function resetCustomSelects() {
        document.querySelectorAll('[data-custom-select]').forEach(wrapper => {
            const nativeSelect = wrapper.querySelector('select');
            const triggerText = wrapper.querySelector('.lauki-select__value');
            const menu = wrapper.querySelector('.lauki-select__menu');
            wrapper.classList.remove('is-open');
            if (menu) menu.classList.add('d-none');
            if (nativeSelect && triggerText) {
                const defaultOpt = nativeSelect.options[0];
                triggerText.textContent = defaultOpt ? defaultOpt.textContent : 'Selecciona una opción';
                triggerText.classList.add('is-placeholder');
                if (menu) {
                    menu.querySelectorAll('.lauki-select__option').forEach((li, idx) => {
                        li.classList.toggle('is-selected', idx === 0);
                        const checkIcon = li.querySelector('i');
                        if (checkIcon) checkIcon.classList.toggle('d-none', idx !== 0);
                    });
                }
            }
        });
    }

    document.querySelectorAll('[data-custom-select]').forEach(wrapper => {
        const nativeSelect = wrapper.querySelector('select');
        const trigger = wrapper.querySelector('.lauki-select__trigger');
        const triggerText = wrapper.querySelector('.lauki-select__value');
        const menu = wrapper.querySelector('.lauki-select__menu');

        if (!nativeSelect || !trigger || !triggerText || !menu) return;

        // Construye las opciones visuales a partir de los <option> (incluidos los generados por Thymeleaf)
        Array.from(nativeSelect.options).forEach((opt, index) => {
            const li = document.createElement('li');
            li.className = 'lauki-select__option' + (opt.selected ? ' is-selected' : '');
            li.setAttribute('role', 'option');
            li.innerHTML = `<span>${opt.textContent}</span><i class="bi bi-check-circle ${opt.selected ? '' : 'd-none'}"></i>`;

            li.addEventListener('click', () => {
                nativeSelect.value = opt.value;
                triggerText.textContent = opt.textContent;
                triggerText.classList.toggle('is-placeholder', opt.disabled || opt.value === '');

                menu.querySelectorAll('.lauki-select__option').forEach((item, idx) => {
                    item.classList.toggle('is-selected', idx === index);
                    const icon = item.querySelector('i');
                    if (icon) icon.classList.toggle('d-none', idx !== index);
                });

                wrapper.classList.remove('is-open');
                menu.classList.add('d-none');
            });

            menu.appendChild(li);
        });

        trigger.addEventListener('click', (e) => {
            e.stopPropagation();
            const isOpen = wrapper.classList.contains('is-open');
            document.querySelectorAll('[data-custom-select]').forEach(other => {
                other.classList.remove('is-open');
                other.querySelector('.lauki-select__menu')?.classList.add('d-none');
            });
            if (!isOpen) {
                wrapper.classList.add('is-open');
                menu.classList.remove('d-none');
            }
        });
    });

    document.addEventListener('click', () => {
        document.querySelectorAll('[data-custom-select]').forEach(wrapper => {
            wrapper.classList.remove('is-open');
            wrapper.querySelector('.lauki-select__menu')?.classList.add('d-none');
        });
    });

    /* 8. PRECARGA DE DATOS EN MODALES DE EDICIÓN */
    document.querySelectorAll('[data-modal-open="modal-editar-curso"]').forEach(btn => {
        btn.addEventListener('click', () => {
            const idInput = document.getElementById('edit-curso-id');
            const nombreInput = document.getElementById('edit-curso-nombre');
            const periodoInput = document.getElementById('edit-curso-periodo');

            if (idInput) idInput.value = btn.getAttribute('data-curso-id') || '';
            if (nombreInput) nombreInput.value = btn.getAttribute('data-curso-nombre') || '';
            if (periodoInput) periodoInput.value = btn.getAttribute('data-curso-periodo') || '';
        });
    });

    document.querySelectorAll('[data-modal-open="modal-editar-reto"]').forEach(btn => {
        btn.addEventListener('click', () => {
            const idInput = document.getElementById('edit-reto-id');
            const nombreInput = document.getElementById('edit-reto-nombre');
            const descInput = document.getElementById('edit-reto-descripcion');
            const grupoSelect = document.getElementById('edit-reto-grupo');

            if (idInput) idInput.value = btn.getAttribute('data-reto-id') || '';
            if (nombreInput) nombreInput.value = btn.getAttribute('data-reto-nombre') || '';
            if (descInput) descInput.value = btn.getAttribute('data-reto-descripcion') || '';

            const grupoId = btn.getAttribute('data-reto-grupo-id');
            if (grupoSelect && grupoId) {
                grupoSelect.value = grupoId;
                const wrapper = grupoSelect.closest('[data-custom-select]');
                const triggerText = wrapper?.querySelector('.lauki-select__value');
                const menu = wrapper?.querySelector('.lauki-select__menu');
                const selectedIndex = grupoSelect.selectedIndex;

                if (triggerText && selectedIndex >= 0) {
                    triggerText.textContent = grupoSelect.options[selectedIndex].textContent;
                    triggerText.classList.remove('is-placeholder');
                }
                if (menu) {
                    menu.querySelectorAll('.lauki-select__option').forEach((li, idx) => {
                        li.classList.toggle('is-selected', idx === selectedIndex);
                        li.querySelector('i')?.classList.toggle('d-none', idx !== selectedIndex);
                    });
                }
            }
        });
    });

    /* 9. TABLERO KANBAN (ROL ESTUDIANTE: Filtro de fase, Modales de tarea y Drag & Drop) */
    function showLaukiToast(message, type = 'success') {
        let container = document.getElementById('lauki-toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'lauki-toast-container';
            document.body.appendChild(container);
        }
        const toast = document.createElement('div');
        toast.className = `toast-lauki ${type === 'error' ? 'toast-lauki--error' : 'toast-lauki--success'}`;
        toast.setAttribute('role', type === 'error' ? 'alert' : 'status');
        const iconClass = type === 'error' ? 'bi-exclamation-circle-fill' : 'bi-check-circle-fill';
        const iconColor = type === 'error' ? 'var(--color-error)' : 'var(--color-success)';
        toast.innerHTML = `
            <i class="bi ${iconClass}" style="color: ${iconColor}; font-size: 16px;"></i>
            <span>${message}</span>
            <button type="button" class="icon-btn ms-auto" aria-label="Cerrar aviso">
                <i class="bi bi-x-lg" style="font-size: 12px;"></i>
            </button>
        `;
        toast.querySelector('button').addEventListener('click', () => toast.remove());
        container.appendChild(toast);
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transition = 'opacity 0.25s ease';
            setTimeout(() => toast.remove(), 250);
        }, 4000);
    }

    // Sincronización del icono/avatar de responsable en Nueva Tarea y Editar Tarea
    function updateResponsablePreview(selectEl) {
        if (!selectEl) return;
        const preview = document.querySelector(`[data-responsable-preview="${selectEl.id}"]`);
        if (!preview) return;
        const selectedOpt = selectEl.options[selectEl.selectedIndex];
        if (!selectEl.value || !selectedOpt) {
            preview.className = 'task-card__avatar task-card__avatar--unassigned';
            preview.innerHTML = '<i class="bi bi-exclamation-triangle"></i>';
        } else {
            const parts = selectedOpt.textContent.trim().split(/\s+/);
            const initials = (parts[0]?.[0] || '') + (parts[1]?.[0] || '');
            preview.className = 'task-card__avatar';
            preview.textContent = initials.toUpperCase();
        }
    }

    ['nueva-tarea-responsable', 'edit-tarea-responsable'].forEach(id => {
        const sel = document.getElementById(id);
        if (!sel) return;
        const wrapper = sel.closest('[data-custom-select]');
        wrapper?.querySelectorAll('.lauki-select__option').forEach(li => {
            li.addEventListener('click', () => updateResponsablePreview(sel));
        });
    });

    // Sincronizar helper para establecer valor en custom selects dinámicamente
    function setCustomSelectValue(selectEl, value) {
        if (!selectEl) return;
        selectEl.value = value ?? '';
        const wrapper = selectEl.closest('[data-custom-select]');
        const triggerText = wrapper?.querySelector('.lauki-select__value');
        const menu = wrapper?.querySelector('.lauki-select__menu');
        const selectedIndex = selectEl.selectedIndex;

        if (triggerText && selectedIndex >= 0) {
            const opt = selectEl.options[selectedIndex];
            triggerText.textContent = opt.textContent;
            triggerText.classList.toggle('is-placeholder', opt.disabled);
        }
        if (menu) {
            menu.querySelectorAll('.lauki-select__option').forEach((li, idx) => {
                li.classList.toggle('is-selected', idx === selectedIndex);
                li.querySelector('i')?.classList.toggle('d-none', idx !== selectedIndex);
            });
        }
        updateResponsablePreview(selectEl);
    }

    // Actualizar contadores de columnas del Kanban
    function updateKanbanCounts() {
        document.querySelectorAll('.kanban__column').forEach(col => {
            const visibleCards = Array.from(col.querySelectorAll('.task-card'))
                .filter(card => !card.classList.contains('d-none'));
            const counter = col.querySelector('.kanban__column-count');
            if (counter) counter.textContent = visibleCards.length;
        });
    }

    // Filtro por fase en cabecera de Mi Tablero
    const phaseFilterWrapper = document.querySelector('[data-board-phase-filter]');
    if (phaseFilterWrapper) {
        const phaseSelect = phaseFilterWrapper.querySelector('select');
        phaseFilterWrapper.querySelectorAll('.lauki-select__option').forEach(li => {
            li.addEventListener('click', () => {
                const selectedPhase = phaseSelect.value;
                document.querySelectorAll('.task-card').forEach(card => {
                    const cardPhase = card.getAttribute('data-task-fase-id');
                    const show = selectedPhase === 'todas' || cardPhase === selectedPhase;
                    card.classList.toggle('d-none', !show);
                });
                updateKanbanCounts();
            });
        });
    }

    // Apertura de modales desde tarjetas (Editar, Descripción, Comentarios)
    document.querySelectorAll('.task-card').forEach(card => {
        const openEdit = () => {
            const modal = document.getElementById('modal-editar-tarea');
            if (!modal) return;
            document.getElementById('edit-tarea-id').value = card.getAttribute('data-task-id') || '';
            document.getElementById('edit-tarea-titulo').value = card.getAttribute('data-task-titulo') || '';
            setCustomSelectValue(document.getElementById('edit-tarea-fase'), card.getAttribute('data-task-fase-id'));
            setCustomSelectValue(document.getElementById('edit-tarea-responsable'), card.getAttribute('data-task-responsable-id'));
            modal.classList.remove('d-none');
        };

        card.addEventListener('click', (e) => {
            if (e.target.closest('[data-open-desc-task]') || e.target.closest('[data-open-comments-task]')) return;
            openEdit();
        });

        card.querySelector('[data-open-desc-task]')?.addEventListener('click', (e) => {
            e.stopPropagation();
            const modal = document.getElementById('modal-descripcion-tarea');
            if (!modal) return;
            document.getElementById('desc-tarea-id').value = card.getAttribute('data-task-id') || '';
            document.getElementById('desc-tarea-subtitle').textContent = card.getAttribute('data-task-fase-label') || '';
            document.getElementById('desc-tarea-texto').value = card.getAttribute('data-task-descripcion') || '';
            modal.classList.remove('d-none');
        });

        card.querySelector('[data-open-comments-task]')?.addEventListener('click', (e) => {
            e.stopPropagation();
            const modal = document.getElementById('modal-comentarios-tarea');
            if (!modal) return;
            document.getElementById('comments-tarea-id').value = card.getAttribute('data-task-id') || '';
            document.getElementById('modal-comments-tarea-title').textContent = card.getAttribute('data-task-titulo') || 'Comentarios';
            const commentsList = modal.querySelector('#comments-list-container');
            const taskComments = card.querySelector('template[data-task-comments]')?.content;
            const emptyComments = modal.querySelector('#comments-empty-template').content;
            const comments = taskComments?.firstElementChild ? taskComments : emptyComments;
            commentsList.replaceChildren(comments.cloneNode(true));
            modal.classList.remove('d-none');
        });
    });

    // Arrastre de tarjetas del Kanban
    const boardSection = document.querySelector('.kanban--board');
    if (boardSection) {
        let draggedCard = null;
        let pendingBlockCard = null;
        const cardsBeingMoved = new WeakSet();

        function rejectCardMove(card, errorMsg) {
            card.classList.remove('task-card--rejected');
            void card.offsetWidth;
            card.classList.add('task-card--rejected');
            showLaukiToast(errorMsg, 'error');
        }

        function applyCardStatusStyle(card, newStatus) {
            card.classList.remove('task-card--progreso', 'task-card--supervision', 'task-card--completada', 'task-card--bloqueada');
            if (newStatus === 'EN_PROGRESO') card.classList.add('task-card--progreso');
            if (newStatus === 'EN_SUPERVISION') card.classList.add('task-card--supervision');
            if (newStatus === 'COMPLETADA') card.classList.add('task-card--completada');
            if (newStatus === 'BLOQUEADA') card.classList.add('task-card--bloqueada');
            card.setAttribute('data-task-status', newStatus);
        }

        document.querySelectorAll('.task-card').forEach(card => {
            card.addEventListener('dragstart', () => {
                draggedCard = card;
                card.classList.add('task-card--dragging');
            });
            card.addEventListener('dragend', () => {
                draggedCard = null;
                card.classList.remove('task-card--dragging');
                document.querySelectorAll('.kanban__column').forEach(col => col.classList.remove('kanban__column--over'));
            });
        });

        document.querySelectorAll('.kanban__column').forEach(col => {
            col.addEventListener('dragover', (e) => {
                e.preventDefault();
                col.classList.add('kanban__column--over');
            });

            col.addEventListener('dragleave', () => {
                col.classList.remove('kanban__column--over');
            });

            col.addEventListener('drop', async (e) => {
                e.preventDefault();
                col.classList.remove('kanban__column--over');
                const card = draggedCard;
                if (!card || cardsBeingMoved.has(card)) return;

                const currentStatus = card.getAttribute('data-task-status');
                const targetStatus = col.getAttribute('data-column-status');
                if (currentStatus === targetStatus) return;

                const hasResponsable = card.getAttribute('data-has-responsable') === 'true';

                if (!hasResponsable) {
                    rejectCardMove(card, 'Debes asignar un responsable antes de mover la tarea');
                    return;
                }

                if (targetStatus === 'BLOQUEADA') {
                    pendingBlockCard = card;
                    const blockModal = document.getElementById('modal-motivo-bloqueo');
                    const blockIdInput = document.getElementById('bloqueo-tarea-id');
                    if (blockIdInput) blockIdInput.value = card.getAttribute('data-task-id') || '';
                    if (blockModal) blockModal.classList.remove('d-none');
                    return;
                }

                const listContainer = col.querySelector('.kanban__cards');
                if (!listContainer) return;

                const baseMoveUrl = boardSection.getAttribute('data-move-url');
                const taskId = card.getAttribute('data-task-id');
                if (baseMoveUrl && taskId && window.location.protocol !== 'file:') {
                    const headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
                    const csrfToken = document.querySelector('input[name="_csrf"]')?.value;
                    if (csrfToken) headers['X-CSRF-TOKEN'] = csrfToken;

                    cardsBeingMoved.add(card);
                    try {
                        const response = await fetch(`${baseMoveUrl}/${taskId}/mover`, {
                            method: 'POST',
                            headers,
                            body: new URLSearchParams({ estado: targetStatus })
                        });
                        if (!response.ok || response.redirected) {
                            const error = await response.json().catch(() => null);
                            rejectCardMove(card, error?.mensaje || 'No se pudo guardar el cambio de estado');
                            return;
                        }
                    } catch {
                        rejectCardMove(card, 'No se pudo conectar con el servidor');
                        return;
                    } finally {
                        cardsBeingMoved.delete(card);
                    }
                }

                listContainer.appendChild(card);
                applyCardStatusStyle(card, targetStatus);
                updateKanbanCounts();
            });
        });

        // Envío en vista previa estática del modal de motivo de bloqueo
        const blockForm = document.getElementById('form-motivo-bloqueo');
        blockForm?.addEventListener('submit', (e) => {
            if (window.location.protocol === 'file:' && pendingBlockCard) {
                e.preventDefault();
                const motivo = document.getElementById('bloqueo-motivo-texto').value.trim();
                if (!motivo) return;
                const blockedCol = document.querySelector('.kanban__column[data-column-status="BLOQUEADA"] .kanban__cards');
                blockedCol?.appendChild(pendingBlockCard);
                applyCardStatusStyle(pendingBlockCard, 'BLOQUEADA');
                updateKanbanCounts();
                pendingBlockCard = null;
                closeAllModals();
            }
        });
    }

    /* 10. EDICIÓN EN LÍNEA DE ROLES EN MI EQUIPO (estudiante/equipo.html) */
    const roleClassMap = {
        GESTOR: 'role-badge--gestor',
        ANALISTA: 'role-badge--analista',
        DESARROLLADOR: 'role-badge--desarrollador',
        COLABORADOR: 'role-badge--colaborador'
    };

    document.addEventListener('click', (e) => {
        const editBtn = e.target.closest('[data-team-edit-btn]');
        if (editBtn) {
            document.querySelectorAll('.team-member-card').forEach(otherCard => {
                otherCard.querySelector('[data-team-view]')?.classList.remove('d-none');
                otherCard.querySelector('[data-team-edit-form]')?.classList.add('d-none');
            });

            const card = editBtn.closest('.team-member-card');
            if (!card) return;
            card.querySelector('[data-team-view]')?.classList.add('d-none');
            card.querySelector('[data-team-edit-form]')?.classList.remove('d-none');
            return;
        }

        const cancelBtn = e.target.closest('[data-team-cancel-btn]');
        if (cancelBtn) {
            const card = cancelBtn.closest('.team-member-card');
            if (!card) return;
            card.querySelector('[data-team-edit-form]')?.classList.add('d-none');
            card.querySelector('[data-team-view]')?.classList.remove('d-none');
        }
    });

    document.addEventListener('submit', (e) => {
        const form = e.target.closest('[data-team-edit-form]');
        if (!form) return;

        // En vista previa VS Code (sin backend) actualiza la píldora y su color al instante
        const actionAttr = form.getAttribute('action');
        if (!actionAttr || actionAttr === '#') {
            e.preventDefault();
            const card = form.closest('.team-member-card');
            const select = form.querySelector('select[name="rol"]');
            const badge = card?.querySelector('.role-badge');
            if (badge && select) {
                const newRole = select.value;
                badge.textContent = newRole;
                badge.className = 'role-badge ' + (roleClassMap[newRole] || '');
            }
            form.classList.add('d-none');
            card?.querySelector('[data-team-view]')?.classList.remove('d-none');
        }
    });

    /* 11. GESTIÓN DE FASES DEL RETO (estudiante/fases.html) */
    document.addEventListener('click', (e) => {
        const deleteBtn = e.target.closest('[data-phase-delete-btn]');
        if (!deleteBtn) return;

        const card = deleteBtn.closest('.phase-card');
        if (!card) return;

        const activeTasks = parseInt(card.getAttribute('data-active-tasks') || '0', 10);
        if (activeTasks > 0) {
            e.preventDefault();
            const blockedModal = document.getElementById('modal-fase-bloqueada');
            if (blockedModal) blockedModal.classList.remove('d-none');
            return;
        }

        // En vista previa VS Code (sin backend) elimina la tarjeta con 0 tareas al instante
        const form = deleteBtn.closest('form');
        const actionAttr = form?.getAttribute('action');
        if (!actionAttr || actionAttr === '#') {
            e.preventDefault();
            card.remove();
        }
    });

    const newPhaseForm = document.getElementById('form-nueva-fase');
    newPhaseForm?.addEventListener('submit', (e) => {
        const actionAttr = newPhaseForm.getAttribute('action');
        if (!actionAttr || actionAttr === '#') {
            e.preventDefault();
            const nombre = document.getElementById('nueva-fase-nombre')?.value.trim();
            const orden = parseInt(document.getElementById('nueva-fase-orden')?.value || '0', 10);
            const list = document.querySelector('.phases-list');
            if (nombre && orden > 0 && list) {
                const padded = orden < 10 ? `0${orden}` : `${orden}`;
                const article = document.createElement('article');
                article.className = 'phase-card';
                article.setAttribute('data-active-tasks', '0');
                article.innerHTML = `
                    <div class="phase-card__info">
                        <span class="phase-card__number">${padded}</span>
                        <div>
                            <h2 class="phase-card__title">${orden}. ${nombre}</h2>
                            <p class="phase-card__count">0 TAREAS ACTIVAS</p>
                        </div>
                    </div>
                    <form action="#" method="post" class="m-0">
                        <button type="submit" class="phase-delete-btn" data-phase-delete-btn title="Eliminar fase" aria-label="Eliminar fase">
                            <i class="bi bi-trash"></i>
                        </button>
                    </form>
                `;
                list.appendChild(article);
            }
            closeAllModals();
        }
    });
});
