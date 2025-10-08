const BASE_URL = "http://localhost:8080";

document.addEventListener("DOMContentLoaded", () => {
  const inputs = document.querySelectorAll(".otp");
  const verifyBtn = document.getElementById("verifyBtn");
  const loadingMessage = document.getElementById("loading-message");
  const email = sessionStorage.getItem("email");

  inputs.forEach((input, index) => {
    input.addEventListener("input", () => {
      input.value = input.value.replace(/[^0-9]/g, "");
      if (input.value && index < inputs.length - 1) {
        inputs[index + 1].focus();
      }
      checkAllFilled();
    });

    input.addEventListener("keydown", (e) => {
      if (e.key === "Backspace" && !input.value && index > 0) {
        inputs[index - 1].focus();
      }
    });
  });

  verifyBtn.addEventListener("click", async () => {
    const otpCode = [...inputs].map((inp) => inp.value).join("");
    if (otpCode.length < 6) {
      showMessage("กรุณากรอกรหัส OTP ให้ครบ", "error");
      return;
    }

    try {
      showMessage("กำลังตรวจสอบรหัส OTP...", "loading");
      verifyBtn.disabled = true;

       // เริ่มจับเวลา cooldown 60 วินาที
      let countdown = 60;
      const timer = setInterval(() => {
        countdown--;
        verifyBtn.textContent = `รอ ${countdown}s`;
        if (countdown <= 0) {
          clearInterval(timer);
          verifyBtn.textContent = "ยืนยันรหัส OTP";
          verifyBtn.disabled = false;
        }
      }, 1000);

      const res = await fetch(`${BASE_URL}/auth/verify-otp`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, otp: otpCode }),
      });

      if (!res.ok) {
        const errText = await res.text();
        throw new Error(errText);
      }

      const data = await res.json();
      localStorage.setItem("token", data.token);
      localStorage.setItem("role", data.role);
      localStorage.setItem("profile", JSON.stringify(data.profile));

      if (data.role === "BUILDING_ADMIN") {
        window.location.href = "/admin";
      } else {
        window.location.href = "termsPage.html";
      }
    } catch (err) {
      showMessage(err.message || "OTP ไม่ถูกต้องหรือหมดอายุ", "error");
    } finally {
      verifyBtn.disabled = false;
    }
  });

  function checkAllFilled() {
    const allFilled = [...inputs].every((inp) => inp.value !== "");
    verifyBtn.disabled = !allFilled;
  }

  function showMessage(msg, type) {
    loadingMessage.style.display = "block";
    loadingMessage.textContent = msg;
    loadingMessage.style.color = type === "error" ? "red" : "#333";
  }
});
