import { store } from './store.js';

const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
};

// Navigation
document.querySelectorAll('.nav-menu a').forEach(link => {
    if(link.getAttribute('href').startsWith('#')) {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            document.querySelector('.nav-menu li.active')?.classList.remove('active');
            e.target.closest('li').classList.add('active');
            
            document.querySelectorAll('.admin-section').forEach(s => s.classList.remove('active'));
            document.querySelector(link.getAttribute('href')).classList.add('active');
        });
    }
});

const showToast = (message, success) => {
    const toast = document.getElementById('toast');
    toast.innerText = message;
    toast.className = `toast show ${success ? 'success' : 'error'}`;
    setTimeout(() => { toast.className = 'toast'; }, 3000);
}

// Render Dashboard Data
const renderDashboard = () => {
    const analytics = store.getAnalytics();
    document.getElementById('revenue-val').innerText = formatCurrency(analytics.revenue);
    
    document.getElementById('sold-val').innerText = analytics.soldPercentage;
    document.getElementById('sold-bar').style.width = `${analytics.soldPercentage}%`;
    document.getElementById('inventory-stats').innerText = `Đã bán ${analytics.totalSoldFlashSale}/${analytics.totalInitialInventory} suất`;
};

// Render Product Select for Combos
const renderProductOptions = () => {
    const select = document.getElementById('combo-products');
    const products = store.getProducts();
    select.innerHTML = products.map(p => 
        `<option value="${p.id}">${p.name} (Tồn: ${p.inventory})</option>`
    ).join('');
};

const renderComboList = () => {
    const combos = store.get('combos') || [];
    const list = document.getElementById('combo-list');
    const products = store.getProducts();
    
    if(combos.length === 0) {
        list.innerHTML = '<p style="color:var(--text-secondary)">Chưa có combo nào.</p>';
        return;
    }
    
    list.innerHTML = combos.map(c => {
        const itemNames = c.productIds.map(id => {
            const p = products.find(prod => prod.id === parseInt(id));
            return p ? p.name : id;
        }).join(', ');
        
        return `<div class="combo-item">
            <strong>${c.name}</strong> - Giảm ${c.discountPercent}%
            <div style="font-size:12px; color:var(--text-secondary)">Sản phẩm: ${itemNames}</div>
        </div>`;
    }).join('');
}

// Event Listeners
document.getElementById('campaign-form').addEventListener('submit', (e) => {
    e.preventDefault();
    const start = document.getElementById('start-time').value;
    const end = document.getElementById('end-time').value;
    const pct = parseInt(document.getElementById('discount-pct').value);
    
    const result = store.saveCampaign(start, end, pct);
    showToast(result.message, result.success);
    if(result.success) renderDashboard();
});

document.getElementById('combo-form').addEventListener('submit', (e) => {
    e.preventDefault();
    const name = document.getElementById('combo-name').value;
    const pct = parseInt(document.getElementById('combo-discount').value);
    
    const select = document.getElementById('combo-products');
    const selectedIds = Array.from(select.selectedOptions).map(opt => parseInt(opt.value));
    
    if(selectedIds.length < 2) {
        showToast('Vui lòng chọn ít nhất 2 sản phẩm', false);
        return;
    }
    
    const result = store.createCombo(name, selectedIds, pct);
    showToast(result.message, result.success);
    if(result.success) {
        renderComboList();
        e.target.reset();
    }
});

window.addEventListener('storeUpdated', () => {
    renderDashboard();
});

// Init
renderDashboard();
renderProductOptions();
renderComboList();

// Prefill campaign form with active campaign if any for UX
const currentCampaign = store.getCampaign();
if (currentCampaign) {
    const toDatetimeLocal = (date) => {
        const offset = date.getTimezoneOffset() * 60000;
        return (new Date(date - offset)).toISOString().slice(0, 16);
    };
    const sDate = new Date(currentCampaign.startTime);
    const eDate = new Date(currentCampaign.endTime);
    document.getElementById('start-time').value = toDatetimeLocal(sDate);
    document.getElementById('end-time').value = toDatetimeLocal(eDate);
    document.getElementById('discount-pct').value = currentCampaign.discountPercent;
}
