// js/sidebar.js
// Responsive collapsible sidebar with mobile drawer behavior.
// Emits 'sidebar:navigate' when a menu item selected.

(function(){
  'use strict';

  const sidebar = document.getElementById('appSidebar');
  const toggle = document.getElementById('sidebarToggle');
  const backdrop = document.getElementById('sidebarBackdrop');
  const items = Array.from(document.querySelectorAll('.sidebar__item'));

  if (!sidebar || !toggle) return;

  // initial state: collapse on wide screens? you can choose default
  let collapsed = false;

  // helpers
  function setCollapsed(val) {
    collapsed = !!val;
    if (collapsed) sidebar.classList.add('collapsed');
    else sidebar.classList.remove('collapsed');
    toggle.setAttribute('aria-expanded', String(!collapsed));
    backdrop.classList.toggle('show', !collapsed && window.innerWidth > 920);
  }

  function openMobile() {
    sidebar.classList.add('open');
    backdrop.classList.add('show');
    sidebar.classList.remove('hidden');
    // focus first item
    const first = items[0];
    if (first) first.focus();
  }

  function closeMobile() {
    sidebar.classList.remove('open');
    backdrop.classList.remove('show');
    sidebar.classList.add('hidden');
    toggle.focus();
  }

  function toggleSidebar() {
    // on mobile (window width <= 920) behave as drawer
    if (window.innerWidth <= 920) {
      if (sidebar.classList.contains('open')) closeMobile();
      else openMobile();
      return;
    }
    // desktop: toggle collapsed state
    setCollapsed(!collapsed);

  }

  // attach events
  toggle.addEventListener('click', (e)=> {
    e.preventDefault();
    toggleSidebar();
  });
  toggle.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      toggleSidebar();
    }
  });

  // click items -> dispatch event + active state
  items.forEach(btn => {
    btn.addEventListener('click', (e) => {
      items.forEach(i => i.classList.remove('sidebar__item--active'));
      btn.classList.add('sidebar__item--active');
      const route = btn.dataset.route;
      document.dispatchEvent(new CustomEvent('sidebar:navigate', { detail: { route, source: btn } }));
      // if mobile drawer open, close after selection
      if (window.innerWidth <= 920) closeMobile();
    });

    btn.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' || e.key === ' ') {
        e.preventDefault();
        btn.click();
      }
    });
  });

  // backdrop
  if (backdrop) {
    backdrop.addEventListener('click', (e) => {
      if (window.innerWidth > 920) {
        setCollapsed(true); // หุบเมนูในโหมดเดสก์ท็อป
      } else {
        closeMobile(); // ปิดเมนูในโหมดมือถือ
      }
    });
  }

  // close on Escape
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      if (sidebar.classList.contains('open')) closeMobile();
    }
  });

  // responsive: close drawer automatically on resize (if switching from mobile -> desktop)
  let resizeTimeout = null;
  window.addEventListener('resize', () => {
    clearTimeout(resizeTimeout);
    resizeTimeout = setTimeout(() => {
      if (window.innerWidth > 920) {
        // On resize to desktop, ensure mobile classes are removed and restore desktop state.
        sidebar.classList.remove('open', 'hidden');
        backdrop.classList.remove('show');
        setCollapsed(collapsed); // Restore the last desktop collapsed state.
      } else {
        // On resize to mobile, remove desktop 'collapsed' state.
        sidebar.classList.remove('collapsed');
      }
    }, 120);
  });

  // export control
  window.SidebarUX = {
    collapse: () => setCollapsed(true),
    expand:  () => setCollapsed(false),
    open:    () => openMobile(),
    close:   () => closeMobile()
  };

  // init: if screen small start hidden
  if (window.innerWidth <= 920) {
    sidebar.classList.add('hidden');
  } else {
    // optionally start collapsed
    setCollapsed(true);
  }
})();
