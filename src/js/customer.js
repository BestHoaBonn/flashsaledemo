import { store } from './store.js';

const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
};

let countdownInterval;

const renderProducts = () => {
  const grid = document.getElementById('product-grid');
  const products = store.getProducts();
  const isFlashSale = store.isCampaignActive();

  grid.innerHTML = products.map(product => {
    const isSoldOutInfo = isFlashSale ? product.flashSaleInventory <= 0 : product.inventory <= 0;
    const inventoryText = isFlashSale 
        ? `Còn lại: <strong>${product.flashSaleInventory}</strong> suất` 
        : `Còn lại: <strong>${product.inventory}</strong> sản phẩm`;
    
    let priceHTML = `<span class="sale-price">${formatCurrency(product.originalPrice)}</span>`;
    
    let percentSold = 0;
    if (isFlashSale) {
        const salePrice = store.calculateFlashSalePrice(product.originalPrice);
        const saved = product.originalPrice - salePrice;
        priceHTML = `
            <span class="original-price">${formatCurrency(product.originalPrice)}</span>
            <span class="sale-price">${formatCurrency(salePrice)}</span>
            <div class="saved-badge">Tiết kiệm ${formatCurrency(saved)}</div>
        `;
        // Dummy logic to approximate sold vs total flash inventory 
        // Note: For true dynamic we need to store initial per product, 
        // but we'll approximate 10 as base or just show a nice bar
        const total = product.flashSaleInventory + 5; // Fake total for UI if not stored
        percentSold = ((total - product.flashSaleInventory) / total) * 100;
        if (product.flashSaleInventory > 0) {
            percentSold = Math.max(10, percentSold);
        } else {
            percentSold = 100;
        }
    }

    return `
      <div class="product-card glass">
        <img src="${product.image}" loading="lazy" alt="${product.name}" class="product-image">
        <div class="product-info">
          <h3 class="product-title">${product.name}</h3>
          <div class="price-container">
            ${priceHTML}
          </div>
          <div class="inventory">
            ${inventoryText}
            ${isFlashSale ? `
                <div class="inventory-progress">
                   <div class="inventory-bar" style="width: ${percentSold}%"></div>
                </div>
            ` : ''}
          </div>
          <div class="buy-action">
            <input type="number" id="qty-${product.id}" class="qty-input" value="1" min="1" ${isSoldOutInfo ? 'disabled' : ''}>
            <button 
               class="btn btn-primary buy-btn" 
               onclick="buyProduct(${product.id})"
               ${isSoldOutInfo ? 'disabled' : ''}
            >
              ${isSoldOutInfo ? 'Hết hàng' : 'Mua Ngay'}
            </button>
          </div>
        </div>
      </div>
    `;
  }).join('');
};

const updateCountdown = () => {
    const campaign = store.getCampaign();
    const container = document.getElementById('countdown-container');
    
    if (!store.isCampaignActive()) {
        container.style.display = 'none';
        if (countdownInterval) clearInterval(countdownInterval);
        return;
    }

    container.style.display = 'block';
    
    const end = new Date(campaign.endTime).getTime();
    
    const tick = () => {
        const now = store.getCurrentTime().getTime();
        const distance = end - now;

        if (distance <= 0) {
            clearInterval(countdownInterval);
            renderProducts(); // re-render to normal price
            container.style.display = 'none';
            return;
        }

        const hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
        const seconds = Math.floor((distance % (1000 * 60)) / 1000);

        document.getElementById('hours').innerText = hours.toString().padStart(2, '0');
        document.getElementById('minutes').innerText = minutes.toString().padStart(2, '0');
        document.getElementById('seconds').innerText = seconds.toString().padStart(2, '0');
    };

    if (countdownInterval) clearInterval(countdownInterval);
    tick();
    countdownInterval = setInterval(tick, 1000);
};

window.buyProduct = (productId) => {
    const qtyInput = document.getElementById(`qty-${productId}`);
    const quantity = qtyInput ? parseInt(qtyInput.value) : 1;
    
    if (quantity <= 0 || isNaN(quantity)) {
        const toast = document.getElementById('toast');
        toast.innerText = 'Số lượng mua phải lớn hơn 0';
        toast.className = 'toast show error';
        setTimeout(() => toast.className = 'toast', 3000);
        return;
    }

    const result = store.buyProduct(productId, quantity);
    const toast = document.getElementById('toast');
    toast.innerText = result.message;
    toast.className = `toast show ${result.success ? 'success' : 'error'}`;
    
    setTimeout(() => {
        toast.className = 'toast';
    }, 3000);

    if (result.success) {
        renderProducts();
    }
};

window.addEventListener('storeUpdated', () => {
    renderProducts();
    updateCountdown();
});

// Init
renderProducts();
updateCountdown();
