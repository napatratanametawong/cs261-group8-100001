// form-autofill.js
(() => {
  document.addEventListener('DOMContentLoaded', () => {
    // ====== 1) Select elements ภายใต้การ์ดนี้เท่านั้น ======
    const card = document.querySelector('.booking-card');
    if (!card) return;

    const form = card.querySelector('form');
    const groupInput   = card.querySelector('input[type="text"]'); // บรรทัด 76
    const phoneInput   = card.querySelector('input[type="tel"]');  // บรรทัด 81
    const purposeInput = card.querySelector('textarea');           // บรรทัด 86
    const dropzone     = card.querySelector('.dropzone');          // บรรทัด 91

    // สร้าง input file ที่ซ่อนสำหรับ dropzone (multiple)
    const fileInput = document.createElement('input');
    fileInput.type = 'file';
    fileInput.multiple = true;
    fileInput.hidden = true;
    // ถ้าต้องการจำกัดชนิดไฟล์ ใส่ accept ได้ เช่น:
    // fileInput.accept = ".pdf,.doc,.docx,.png,.jpg,.jpeg";
    dropzone.after(fileInput);

    // Digits-only for phone with inline note (no alert)
    if (phoneInput) {
      phoneInput.setAttribute('inputmode', 'numeric');
      phoneInput.setAttribute('pattern', '[0-9]*');

      // Create (or get) small validation note element next to input
      const getNote = () => {
        const key = '__note';
        if (phoneInput[key] && phoneInput[key].isConnected) return phoneInput[key];
        const note = document.createElement('span');
        note.textContent = 'เฉพาะเบอร์โทรศัพท์';
        note.style.cssText = 'display:none;color:#C82B2B;font-size:12px;margin-top:4px;';
        // Insert right after the input
        phoneInput.insertAdjacentElement('afterend', note);
        phoneInput[key] = note;
        return note;
      };
      const showNote = () => {
        const n = getNote();
        n.style.display = 'block';
        phoneInput.setAttribute('aria-invalid', 'true');
      };
      const hideNote = () => {
        const n = getNote();
        n.style.display = 'none';
        phoneInput.removeAttribute('aria-invalid');
      };

      // Prevent non-digit typed characters
      phoneInput.addEventListener('beforeinput', (e) => {
        if (e.inputType === 'insertText' && typeof e.data === 'string' && /\D/.test(e.data)) {
          e.preventDefault();
          showNote();
        }
      });

      // Sanitize paste to digits-only
      phoneInput.addEventListener('paste', (e) => {
        e.preventDefault();
        const text = (e.clipboardData || window.clipboardData)?.getData('text') || '';
        const digits = text.replace(/\D+/g, '');
        if (!digits) { showNote(); return; }
        const el = phoneInput;
        const start = el.selectionStart ?? el.value.length;
        const end = el.selectionEnd ?? el.value.length;
        el.value = el.value.slice(0, start) + digits + el.value.slice(end);
        const caret = start + digits.length;
        if (typeof el.setSelectionRange === 'function') el.setSelectionRange(caret, caret);
        el.dispatchEvent(new Event('input', { bubbles: true }));
      });

      // Final guard for any remaining non-digits; hide note when valid/empty
      phoneInput.addEventListener('input', () => {
        const v = phoneInput.value;
        const digitsOnly = v.replace(/\D+/g, '');
        if (v !== digitsOnly) {
          phoneInput.value = digitsOnly;
          showNote();
        } else {
          if (digitsOnly === '') hideNote();
        }
      });

      // Also hide note on blur if the value is valid (or empty)
      phoneInput.addEventListener('blur', () => {
        const v = phoneInput.value || '';
        if (/^\d*$/.test(v)) hideNote();
      });
    }

    // ====== 2) Autofill with localStorage ======
    const STORAGE_KEYS = {
      group:   'booking.group',
      phone:   'booking.phone',
      purpose: 'booking.purpose',
    };

    // เติมค่าจาก localStorage ถ้ามี
    try {
      if (groupInput && localStorage.getItem(STORAGE_KEYS.group) !== null) {
        groupInput.value = localStorage.getItem(STORAGE_KEYS.group);
      }
      if (phoneInput && localStorage.getItem(STORAGE_KEYS.phone) !== null) {
        phoneInput.value = localStorage.getItem(STORAGE_KEYS.phone);
      }
      if (purposeInput && localStorage.getItem(STORAGE_KEYS.purpose) !== null) {
        purposeInput.value = localStorage.getItem(STORAGE_KEYS.purpose);
      }
    } catch (e) {
      // บางสภาพแวดล้อมอาจปิดใช้ localStorage ได้
      console.warn('localStorage is not available:', e);
    }

    // บันทึกค่าทันทีที่ผู้ใช้พิมพ์
    const persist = (el, key) => {
      if (!el) return;
      el.addEventListener('input', () => {
        try { localStorage.setItem(key, el.value); } catch {}
      });
    };
    persist(groupInput,   STORAGE_KEYS.group);
    persist(phoneInput,   STORAGE_KEYS.phone);
    persist(purposeInput, STORAGE_KEYS.purpose);

    // ====== 3) Dropzone: click-to-upload + drag & drop ======
    const renderFileList = (files) => {
      if (!files || files.length === 0) {
        dropzone.textContent = 'คลิกหรือวางไฟล์ที่นี่เพื่อเพิ่ม/อัปโหลด';
        return;
      }
      const names = Array.from(files).map(f => `• ${f.name}`).join('\n');
      dropzone.textContent = `${files.length} ไฟล์ที่เลือก:\n${names}`;
    };

    // เปิด file picker เมื่อคลิก
    dropzone.addEventListener('click', () => fileInput.click());

    // อัปเดตข้อความเมื่อเลือกไฟล์จาก file picker
    fileInput.addEventListener('change', () => {
      renderFileList(fileInput.files);
    });

    // ป้องกัน behavior ของ browser ตอน drag ไฟล์ผ่านหน้า
    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(evName => {
      dropzone.addEventListener(evName, (e) => {
        e.preventDefault();
        e.stopPropagation();
      });
    });

    // ใส่/เอา class ตอนลากเข้า-ออก เพื่อให้คุณไป stylize ต่อใน CSS ได้
    ['dragenter', 'dragover'].forEach(evName => {
      dropzone.addEventListener(evName, () => dropzone.classList.add('is-dragover'));
    });
    ['dragleave', 'drop'].forEach(evName => {
      dropzone.addEventListener(evName, () => dropzone.classList.remove('is-dragover'));
    });

    // รับไฟล์ตอนวาง และ sync ให้ fileInput
    dropzone.addEventListener('drop', (e) => {
      const dt = e.dataTransfer;
      if (!dt || !dt.files || dt.files.length === 0) return;

      // สร้าง DataTransfer ใหม่เพื่อเซ็ตให้ fileInput (บางเบราว์เซอร์รองรับโดยตรง)
      const transfer = new DataTransfer();
      Array.from(dt.files).forEach(f => transfer.items.add(f));
      fileInput.files = transfer.files;

      renderFileList(fileInput.files);
    });

    // ====== 4) เตรียมพร้อมส่งตอนกด submit ======
    form?.addEventListener('submit', (e) => {
      e.preventDefault(); // ลองล็อกดูก่อน ปรับเป็นส่งจริงเมื่อพร้อม

      const fd = new FormData(form);
      // แน่ใจว่าแนบไฟล์ทั้งหมด (เผื่อ browser ไม่ auto รวมให้)
      if (fileInput.files && fileInput.files.length > 0) {
        Array.from(fileInput.files).forEach((file, idx) => {
          // ชื่อ field "attachments" ปรับได้ตาม backend
          fd.append('attachments', file, file.name);
        });
      }

      // DEBUG: ดูสิ่งที่จะส่ง
      console.group('FormData ready to send');
      for (const [k, v] of fd.entries()) {
        console.log(k, v);
      }
      console.groupEnd();

      // TODO: ส่งจริง (ปลดคอมเมนต์เมื่อมี endpoint)
      // fetch('/api/bookings', { method: 'POST', body: fd })
      //   .then(res => {
      //     if (!res.ok) throw new Error('Network response was not ok');
      //     return res.json();
      //   })
      //   .then(data => {
      //     console.log('Uploaded:', data);
      //     alert('ส่งคำขอเรียบร้อย!');
      //     form.reset();
      //     fileInput.value = '';
      //     renderFileList(null);
      //   })
      //   .catch(err => {
      //     console.error(err);
      //     alert('ไม่สามารถส่งคำขอได้ กรุณาลองใหม่');
      //   });
    });
  });
})();
