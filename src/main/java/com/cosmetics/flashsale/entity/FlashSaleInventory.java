package com.cosmetics.flashsale.entity;

/**
 * =======================================================
 * MÔ HÌNH THỰC THỂ (ENTITY): FlashSaleInventory
 * Thuộc chức năng: US2 - Xử lý tồn kho và thanh toán
 * 
 * QUY TẮC NGHIỆP VỤ (BUSINESS RULES):
 * 1. Đảm bảo chốt đúng số lượng tồn kho còn lại thực tế.
 * 2. Ngăn chặn việc mua quá suất (Overselling) trong thời lượng cực ngắn.
 * 
 * LOGIC XỬ LÝ (BUSINESS LOGIC):
 * - Sử dụng cơ chế 'Xếp hàng' (Synchronized) để xử lý tuần tự các yêu cầu trừ kho.
 * - Kiểm tra số lượng khả dụng trước khi thực hiện giao dịch (Check-then-Act).
 * =======================================================
 */
public class FlashSaleInventory {
    private String productId;
    private int availableQuantity;

    public FlashSaleInventory(String productId, int availableQuantity) {
        this.productId = productId;
        this.availableQuantity = availableQuantity;
    }

    public synchronized boolean holdInventory(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng mua phải lớn hơn 0");
        }
        if (quantity > availableQuantity) {
            throw new IllegalStateException("Sản phẩm đã hết suất Flash Sale");
        }
        availableQuantity -= quantity;
        return true;
    }
    
    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public String getProductId() {
        return productId;
    }
}
