// js/sideMenu.js
(function () {
  'use strict';

  const sideMenu = document.getElementById('sideMenu');
  const backdrop = document.getElementById('sideBackdrop');
  const menuToggle = document.getElementById('menu-toggle'); // The button in the top bar
  const siteLogo = document.getElementById('siteLogo'); // The logo inside the menu

  if (!sideMenu || !backdrop || !menuToggle) return;

  // open menu: add .open
  function openMenu() {
    sideMenu.classList.add('open');
    sideMenu.setAttribute('aria-hidden', 'false');
    // focus first focusable element inside (for accessibility)
    const first = sideMenu.querySelector('button.side-item');
    if (first) first.focus();
  }

  // close menu
  function closeMenu() {
    sideMenu.classList.remove('open');
    sideMenu.setAttribute('aria-hidden', 'true');
  }

  // toggle (you can call from top-bar button)
  function toggleMenu() {
    if (sideMenu.classList.contains('open')) closeMenu();
    else openMenu();
  }

  // --- Event Listeners ---

  // Open/Close menu when clicking the top-bar logo
  menuToggle.addEventListener('click', toggleMenu);

  // Close menu when clicking the backdrop
  backdrop.addEventListener('click', closeMenu);

  // close when pressing Escape
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && sideMenu.classList.contains('open')) closeMenu();
  });

  // example: handle navigation clicks (data-route)
  sideMenu.addEventListener('click', (ev) => {
    const btn = ev.target.closest('.side-item');
    if (!btn) return;
    const route = btn.dataset.route || btn.classList.contains('side-item--settings') && 'settings';
    if (!route) return;

    // mark active
    sideMenu.querySelectorAll('.side-item').forEach(i => i.classList.remove('active'));
    btn.classList.add('active');

    // close drawer after choosing
    closeMenu();

    // dispatch custom event for app to handle navigation
    const navEv = new CustomEvent('side:navigate', { detail: { route } });
    document.dispatchEvent(navEv);
  });

  // Also allow clicking the logo inside the menu to toggle it
  if (siteLogo) {
    siteLogo.setAttribute('role', 'button');
    siteLogo.setAttribute('tabindex', '0');
    siteLogo.addEventListener('click', toggleMenu);
    siteLogo.addEventListener('keydown', (ev) => {
      if (ev.key === 'Enter' || ev.key === ' ') {
        ev.preventDefault();
        toggleMenu();
      }
    });
  }
})();
