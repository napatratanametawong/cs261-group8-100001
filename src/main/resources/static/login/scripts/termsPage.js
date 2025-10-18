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
    if (checkbox.checked) {
      btn.disabled = false;
      btn.classList.add("enabled");
    } else {
      btn.disabled = true;
      btn.classList.remove("enabled");
    }
  });

  btn.addEventListener("click", () => {
    window.location.href = "../../bookingRoom/homepage_user.html";
  });
});
