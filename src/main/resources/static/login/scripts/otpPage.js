const BASE_URL = "http://localhost:8080";

document.addEventListener("DOMContentLoaded", () => {
  const inputs = document.querySelectorAll(".otp");
  const verifyBtn = document.getElementById("verifyBtn");
  const loadingMessage = document.getElementById("loading-message");
  const email = sessionStorage.getItem("email");
  const backToLogin = document.getElementById("back-to-login"); // ปุ่มย้อนกลับ
  const resendMsg = document.getElementById("resend-message");  // ข้อความขอรหัสใหม่ (จะเพิ่มใน HTML)

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

  // ✅ กดปุ่มยืนยัน OTP → fetch ได้เหมือนเดิม
  verifyBtn.addEventListener("click", async () => {
    const otpCode = [...inputs].map((inp) => inp.value).join("");
    if (otpCode.length < 6) {
      showMessage("กรุณากรอกรหัส OTP ให้ครบ", "error");
      return;
    }

    try {
      showMessage("กำลังตรวจสอบรหัส OTP...", "loading");
      verifyBtn.disabled = true;

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
        window.location.href = "../../admin/homepage_admin.html";
      } else {
        window.location.href = "termsPage.html";
      }
    } catch (err) {
      showMessage(err.message || "OTP ไม่ถูกต้องหรือหมดอายุ", "error");
    } finally {
      verifyBtn.disabled = false;
    }
  });

  // ✅ เพิ่มปุ่มขอรหัสใหม่
  let cooldown = 60;
  const timer = setInterval(() => {
    cooldown--;
    resendMsg.textContent = `สามารถขอรหัสใหม่ได้ใน ${cooldown} วินาที`;
    if (cooldown <= 0) {
      clearInterval(timer);
      resendMsg.innerHTML = `<span id="resendBtn" class="resend-link">ส่งรหัส OTP อีกครั้ง</span>`;
      document.getElementById("resendBtn").addEventListener("click", resendOTP);
    }
  }, 1000);

  async function resendOTP() {
    try {
      resendMsg.textContent = "กำลังส่งรหัสใหม่...";
      const res = await fetch(`${BASE_URL}/auth/request-otp`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email }),
      });
      if (!res.ok) throw new Error(await res.text());
      resendMsg.textContent = "ส่งรหัสใหม่สำเร็จ! โปรดตรวจอีเมลของคุณ";
      cooldown = 60;
    } catch (err) {
      resendMsg.textContent = "ไม่สามารถส่งรหัสใหม่ได้ กรุณาลองอีกครั้ง";
    }
  }

  // ✅ ปุ่มย้อนกลับ
  backToLogin.addEventListener("click", () => {
    window.location.href = "loginPage.html";
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

// แยก role 
fetch('/api/auth/verify-otp', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, otp })
})
.then(res => res.json())
.then(data => {
  localStorage.setItem('jwt_token', data.token);
  localStorage.setItem('user_email', data.email);
  localStorage.setItem('is_admin', data.isAdmin); // เก็บสถานะ admin

  if (data.isAdmin) {
    window.location.href = '/admin/homepage_admin.html';
  } else {
    window.location.href = '/bookingRoom/homepage_user.html';
  }
});
