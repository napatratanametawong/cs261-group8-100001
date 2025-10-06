const checkbox = document.getElementById('agree');
const button = document.getElementById('acceptBtn');

checkbox.addEventListener('change', () => {
  if (checkbox.checked) {
    button.disabled = false;
    button.classList.add('enabled');
  } else {
    button.disabled = true;
    button.classList.remove('enabled');
  }
});

button.addEventListener('click', () => {
  if (checkbox.checked) {
    //ไปต่อหน้าอื่น เช่น หน้าหลัก
    window.location.href = "home.html"; // เปลี่ยนชื่อไฟล์ตามหน้าโฮม
  }
});
