const BASE_URL = "http://localhost:8080";

document.addEventListener("DOMContentLoaded", () => {
  const form = document.querySelector("form");
  const loadingMessage = document.getElementById("loading-message");
  const submitBtn = form.querySelector("button");
  
  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const email = form.querySelector('input[name="email"]').value.trim().toLowerCase();
    const studentID = form.querySelector('input[name="studentID"]').value.trim();

    // Validation เบื้องต้น
    if (!email || !studentID) {
      showMessage("กรุณากรอกอีเมลและรหัสนักศึกษาให้ครบถ้วน", "error");
      return;
    }

    try {
      showMessage("กำลังตรวจสอบข้อมูล...", "loading");
      submitBtn.disabled = true;

      // 1. TU Check
      const tuRes = await fetch(`${BASE_URL}/auth/tucheck`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userName: studentID, email }),
      });

      const tuText = await tuRes.text();
      if (!tuRes.ok) throw new Error(tuText);

      showMessage("ตรวจสอบสำเร็จ กำลังส่งรหัส OTP...", "loading");

      // 2. Request OTP
      const otpRes = await fetch(`${BASE_URL}/auth/request-otp`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email }),
      });

      const otpText = await otpRes.text();
      if (!otpRes.ok) throw new Error(otpText);

      // ถ้าทั้งสองขั้นผ่าน → ไปหน้า OTP
      sessionStorage.setItem("email", email);
      sessionStorage.setItem("studentID", studentID);
      window.location.href = "otpPage.html";
    } catch (err) {
      showMessage(err.message || "เกิดข้อผิดพลาด กรุณาลองใหม่", "error");
    } finally {
      submitBtn.disabled = false;
    }
  });

  function showMessage(msg, type) {
    loadingMessage.style.display = "block";
    loadingMessage.textContent = msg;
    loadingMessage.style.color = type === "error" ? "red" : "#333";
  }
});
