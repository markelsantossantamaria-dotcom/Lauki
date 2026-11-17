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

    /* 8. PRECARGA DE DATOS EN MODAL EDITAR RETO */
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
});