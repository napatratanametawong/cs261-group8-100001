document.addEventListener("DOMContentLoaded", () => {
  const token = localStorage.getItem("token");
  if (!token) {
    alert("กรุณาเข้าสู่ระบบก่อน");
    window.location.href = "loginPage.html";
    return;
  }

  const checkbox = document.getElementById("agree");
  const btn = document.getElementById("acceptBtn");

  checkbox.addEventListener("change", () => {
    btn.disabled = !checkbox.checked;
  });

  btn.addEventListener("click", () => {
    window.location.href = "/app";
  });
});
