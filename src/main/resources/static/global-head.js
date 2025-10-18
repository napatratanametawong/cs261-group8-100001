(function () {
  const SITE_NAME = "LC2 Booking";
  document.title = SITE_NAME;

  const head = document.head;
  head.querySelectorAll('link[rel="icon"],link[rel="apple-touch-icon"]').forEach(el => el.remove());

  [
    { rel: "icon", type: "image/png", href: "/resource/Logo_header.png" },
    { rel: "apple-touch-icon", href: "/resource/Logo_header.png" }
  ].forEach(attrs => {
    const link = document.createElement("link");
    Object.entries(attrs).forEach(([k, v]) => link.setAttribute(k, v));
    head.appendChild(link);
  });
})();
