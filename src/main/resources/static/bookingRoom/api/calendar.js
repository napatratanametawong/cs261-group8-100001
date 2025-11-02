// calendar.js — emits `calendar:date-change` with { date: Date, iso: 'YYYY-MM-DD' }

const dpToggle = document.getElementById('dpToggle');
const dpPanel  = document.getElementById('dpPanel');
const calTitle = document.getElementById('calTitle');
const calGrid  = document.getElementById('calGrid');
const dpLabel  = document.getElementById('dpLabel');

let currentDate  = new Date();   // month being shown
let selectedDate = new Date();   // actual picked day

function isSameDay(a,b){
  return a && b &&
    a.getFullYear()===b.getFullYear() &&
    a.getMonth()===b.getMonth() &&
    a.getDate()===b.getDate();
}
function toISODate(d){
  const y=d.getFullYear(), m=('0'+(d.getMonth()+1)).slice(-2), day=('0'+d.getDate()).slice(-2);
  return `${y}-${m}-${day}`;
}
function toThai(d){ return d.toLocaleDateString('th-TH'); }

// render calendar grid (no CSS changes)
function renderCalendar(date){
  if(!calGrid || !calTitle) return;
  calGrid.innerHTML = '';
  const y=date.getFullYear(), m=date.getMonth();
  const first = new Date(y,m,1).getDay();
  const last  = new Date(y,m+1,0).getDate();
  calTitle.textContent = `${date.toLocaleString('th-TH',{month:'long'})} ${y}`;

  for(let i=0;i<first;i++){
    const blank=document.createElement('div');
    blank.className='blank';
    calGrid.appendChild(blank);
  }
  const today = new Date();
  for(let d=1; d<=last; d++){
    const cell=document.createElement('div');
    const val=new Date(y,m,d);
    cell.textContent = d;
    if(isSameDay(val,today))        cell.classList.add('is-today');
    if(isSameDay(val,selectedDate)) cell.classList.add('is-selected');
    cell.addEventListener('click', ()=>{
      selectedDate = val;
      dpLabel && (dpLabel.textContent = toThai(selectedDate));
      dpPanel?.classList.remove('show');
      dpToggle?.setAttribute('aria-expanded','false');
      renderCalendar(currentDate);
      // fire event out
      window.dispatchEvent(new CustomEvent('calendar:date-change', {
        detail: { date: selectedDate, iso: toISODate(selectedDate) }
      }));
    });
    calGrid.appendChild(cell);
  }
}

// open/close panel
document.addEventListener('click', (e)=>{
  if(e.target.closest('#dpToggle')){
    const open = !dpPanel?.classList.contains('show');
    dpPanel?.classList.toggle('show');
    dpToggle?.setAttribute('aria-expanded', open ? 'true' : 'false');
  } else if (!e.target.closest('#dpPanel')){
    dpPanel?.classList.remove('show');
    dpToggle?.setAttribute('aria-expanded','false');
  }
});

// month nav
document.querySelectorAll('.cal-nav').forEach(btn=>{
  btn.addEventListener('click', ()=>{
    const dir = parseInt(btn.getAttribute('data-dir'),10)||0;
    currentDate.setMonth(currentDate.getMonth()+dir);
    renderCalendar(currentDate);
  });
});

// init
dpLabel && (dpLabel.textContent = toThai(selectedDate));
renderCalendar(currentDate);

// expose (optional)
window.CalendarAPI = {
  getSelected(){ return new Date(selectedDate); },
  getSelectedISO(){ return toISODate(selectedDate); }
};
