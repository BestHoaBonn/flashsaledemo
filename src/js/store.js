// src/js/store.js
class Store {
  constructor() {
    this.initData();
  }

  initData() {
    const defaultProducts = [
      { id: 1, name: 'Son MAC Ruby Woo', originalPrice: 650000, inventory: 100, flashSaleInventory: 15, image: 'https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=500&q=80' },
      { id: 2, name: 'Nước hoa Chanel N°5', originalPrice: 3500000, inventory: 50, flashSaleInventory: 5, image: 'https://images.unsplash.com/photo-1594035910387-fea47794261f?w=500&q=80' },
      { id: 3, name: 'Kem chống nắng La Roche-Posay', originalPrice: 480000, inventory: 200, flashSaleInventory: 30, image: 'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=500&q=80' }
    ];

    const currentProds = JSON.parse(localStorage.getItem('products'));
    if (!currentProds || currentProds[0].name.includes('Tai nghe')) {
        localStorage.setItem('products', JSON.stringify(defaultProducts));
        localStorage.removeItem('campaign');
        localStorage.removeItem('combos');
        localStorage.removeItem('orders');
    }

    if (!localStorage.getItem('campaign')) {
      // Create a default active campaign ending in 1 hour for demo purposes
      const now = new Date();
      const end = new Date(now.getTime() + 60 * 60 * 1000);
      localStorage.setItem('campaign', JSON.stringify({
        isActive: true,
        startTime: now.toISOString(),
        endTime: end.toISOString(),
        discountPercent: 20 // 20% for cosmetics is common
      }));
    }
    if (!localStorage.getItem('combos')) localStorage.setItem('combos', JSON.stringify([]));
    if (!localStorage.getItem('orders')) localStorage.setItem('orders', JSON.stringify([]));
  }

  get(key) {
    return JSON.parse(localStorage.getItem(key));
  }

  set(key, data) {
    localStorage.setItem(key, JSON.stringify(data));
    window.dispatchEvent(new Event('storeUpdated'));
  }

  getProducts() {
    return this.get('products');
  }

  getCampaign() {
    return this.get('campaign');
  }

  getCurrentTime() {
    return new Date();
  }

  isCampaignActive() {
    const campaign = this.getCampaign();
    if (!campaign || !campaign.isActive) return false;
    const now = this.getCurrentTime();
    const start = new Date(campaign.startTime);
    const end = new Date(campaign.endTime);
    return now >= start && now <= end;
  }

  calculateFlashSalePrice(originalPrice) {
    const campaign = this.getCampaign();
    if (!this.isCampaignActive()) return originalPrice;
    return originalPrice * (1 - campaign.discountPercent / 100);
  }

  buyProduct(productId, quantity) {
    const products = this.getProducts();
    const productIndex = products.findIndex(p => p.id === productId);
    if (productIndex === -1) return { success: false, message: 'Không tìm thấy sản phẩm' };

    const product = products[productIndex];
    const isFlashSale = this.isCampaignActive();

    if (isFlashSale) {
        if (product.flashSaleInventory < quantity) {
            return { success: false, message: 'Sản phẩm đã hết suất Flash Sale' };
        }
        products[productIndex].flashSaleInventory -= quantity;
        products[productIndex].inventory -= quantity; // also reduce total inventory
    } else {
        if (product.inventory < quantity) {
             return { success: false, message: 'Hết hàng' };
        }
        products[productIndex].inventory -= quantity;
    }
    
    // Save order
    const price = isFlashSale ? this.calculateFlashSalePrice(product.originalPrice) : product.originalPrice;
    const orders = this.get('orders');
    orders.push({
      productId,
      productName: product.name,
      quantity,
      price,
      total: price * quantity,
      isFlashSale,
      timestamp: new Date().toISOString()
    });
    
    this.set('products', products);
    this.set('orders', orders);
    return { success: true, message: 'Đặt hàng thành công!' };
  }

  getAnalytics() {
    const orders = this.get('orders');
    const products = this.getProducts();
    
    // Total revenue from flash sale
    const flashSaleOrders = orders.filter(o => o.isFlashSale);
    const revenue = flashSaleOrders.reduce((sum, order) => sum + order.total, 0);
    
    // Calculate initial flash sale inventory by adding current + sold
    const totalCurrentFlashSaleInventory = products.reduce((sum, p) => sum + p.flashSaleInventory, 0);
    const totalSoldFlashSale = flashSaleOrders.reduce((sum, o) => sum + o.quantity, 0);
    const totalInitialInventory = totalCurrentFlashSaleInventory + totalSoldFlashSale;
    
    let soldPercentage = 0;
    if (totalInitialInventory > 0) {
       soldPercentage = (totalSoldFlashSale / totalInitialInventory) * 100;
    }

    return {
       revenue,
       soldPercentage: soldPercentage.toFixed(2),
       totalInitialInventory,
       totalSoldFlashSale
    };
  }

  saveCampaign(startTimeStr, endTimeStr, discountPct) {
    if (discountPct > 50) {
      return { success: false, message: 'Mức giảm không được vượt quá 50%' };
    }
    const startTime = new Date(startTimeStr);
    const endTime = new Date(endTimeStr);
    
    if (startTime >= endTime) {
      return { success: false, message: 'Thời gian bắt đầu phải trước thời gian kết thúc' };
    }
    
    this.set('campaign', {
      isActive: true,
      startTime: startTime.toISOString(),
      endTime: endTime.toISOString(),
      discountPercent: discountPct
    });
    return { success: true, message: 'Chiến dịch đã được lên lịch' };
  }

  createCombo(name, productIds, discountPercent) {
      const products = this.getProducts();
      // Check inventory
      for (const id of productIds) {
          const p = products.find(prod => prod.id === id);
          if (!p || p.inventory <= 0) {
             return { success: false, message: `Sản phẩm ${p ? p.name : id} không đủ tồn kho để tạo Combo` };
          }
      }
      
      const combos = this.get('combos');
      combos.push({
          id: Date.now(),
          name,
          productIds,
          discountPercent
      });
      this.set('combos', combos);
      return { success: true, message: 'Tạo Combo thành công!' };
  }
}

export const store = new Store();
