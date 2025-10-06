// จำลอง flow การเปลี่ยนหน้า ยังไม่ได้เรียก API

document.addEventListener("DOMContentLoaded", () => {
  const form = document.querySelector("form");
  const loadingMessage = document.getElementById("loading-message");

  form.addEventListener("submit", (e) => {
    e.preventDefault();

    const email = form.querySelector('input[name="email"]').value;
    const studentID = form.querySelector('input[name="studentID"]').value;

    // ตรวจสอบว่ากรอกครบหรือยัง
    if (!email || !studentID) {
      // แสดงข้อความเตือน
      loadingMessage.style.display = "block";
      loadingMessage.style.color = "red";
      loadingMessage.textContent = "กรุณากรอกอีเมลและรหัสนักศึกษาให้ครบถ้วน";
      return; // หยุดไม่ให้ไปต่อ
    }

    // ถ้ากรอกครบแล้ว ให้แสดงสถานะ “กำลังส่ง OTP...”
    loadingMessage.style.display = "block";
    loadingMessage.style.color = "#555";
    loadingMessage.textContent = "กำลังส่งรหัส OTP...";

    // ปิดปุ่มชั่วคราว (กันกดซ้ำ)
    const submitBtn = form.querySelector("button");
    submitBtn.disabled = true;

    // จำลองการส่ง OTP
    setTimeout(() => {
      sessionStorage.setItem("email", email);
      sessionStorage.setItem("studentID", studentID);

      // ไปหน้า OTP หลังจาก 2 วินาที
      window.location.href = "otpPage.html";
    }, 2000);
  });
});
