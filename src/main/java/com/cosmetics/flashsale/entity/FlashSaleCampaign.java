package com.cosmetics.flashsale.entity;

import java.time.LocalDateTime;

/**
 * =======================================================
 * MÔ HÌNH THỰC THỂ (ENTITY): FlashSaleCampaign
 * Thuộc chức năng: US3 - Thiết lập chiến dịch Flash Sale
 * 
 * QUY TẮC NGHIỆP VỤ (BUSINESS RULES):
 * 1. Mức giảm giá tối đa không được vượt quá 50% (Quy định kinh doanh).
 * 2. Thời gian bắt đầu phải luôn đứng trước thời gian kết thúc (Logic hợp lệ).
 * 
 * LOGIC XỬ LÝ (BUSINESS LOGIC):
 * - Ràng buộc được thực thi ngay trong Constructor để ngăn dữ liệu sai lệch.
 * - Sử dụng LocalDateTime để đối soát thời gian thực chuẩn xác.
 * =======================================================
 */
public class FlashSaleCampaign {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double discountPercent;
    
    public FlashSaleCampaign(LocalDateTime startTime, LocalDateTime endTime, double discountPercent) {
        if (discountPercent > 50.0) {
            throw new IllegalArgumentException("Mức giảm không được vượt quá 50%");
        }
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu");
        }
        this.startTime = startTime;
        this.endTime = endTime;
        this.discountPercent = discountPercent;
    }
    
    public boolean isActive(LocalDateTime currentTime) {
        return currentTime.compareTo(startTime) >= 0 && currentTime.compareTo(endTime) <= 0;
    }
    
    public double getDiscountPercent() {
        return discountPercent;
    }
}
