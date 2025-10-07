// จำลอง flow การเปลี่ยนหน้า ยังไม่ได้เรียก API

document.addEventListener("DOMContentLoaded", () => {
  const inputs = document.querySelectorAll(".otp");
  const verifyBtn = document.getElementById("verifyBtn");

  inputs.forEach((input, index) => {
    input.addEventListener("input", () => {
      //อนุญาตเฉพาะตัวเลข
      input.value = input.value.replace(/[^0-9]/g, "");

      if (input.value.length === 1 && index < inputs.length - 1) {
        inputs[index + 1].focus();
      }
      checkAllFilled();
    });

    input.addEventListener("keydown", (e) => {
      if (e.key === "Backspace" && input.value === "" && index > 0) {
        inputs[index - 1].focus();
      }
    });
  });

  function checkAllFilled() {
    const allFilled = [...inputs].every((inp) => inp.value !== "");
    verifyBtn.disabled = !allFilled;
  }

  verifyBtn.addEventListener("click", () => {
    const otpCode = [...inputs].map((inp) => inp.value).join("");
    console.log("OTP:", otpCode);
    window.location.href = "termsPage.html"; //สมมติว่า OTP ถูก -> ไปหน้าต่อไป
  });
});
