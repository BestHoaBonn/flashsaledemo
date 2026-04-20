package com.cosmetics.flashsale.boundary;

import com.cosmetics.flashsale.control.CampaignManager;
import com.cosmetics.flashsale.control.ComboManager;
import com.cosmetics.flashsale.control.DashboardController;
import com.cosmetics.flashsale.entity.FlashSaleInventory;
import com.cosmetics.flashsale.entity.SaleAnalytics;

import java.time.LocalDateTime;
import java.util.List;

/**
 * =======================================================
 * LỚP BIÊN (BOUNDARY): AdminBoundary
 * Thuộc tầng: Giao diện Quản trị (Admin)
 * Nhiệm vụ: Tiếp nhận yêu cầu cấu hình và báo cáo từ UI/Test
 * và phối hợp dữ liệu với các Control chuyên trách.
 * =======================================================
 */
public class AdminBoundary {
    private CampaignManager campaignManager = new CampaignManager();
    private DashboardController dashboardController = new DashboardController();
    private ComboManager comboManager = new ComboManager();

    /**
     * Lên lịch cho chiến dịch mới.
     */
    public void scheduleCampaign(LocalDateTime start, LocalDateTime end, double discount) {
        campaignManager.createCampaign(start, end, discount);
    }

    /**
     * Xuất báo cáo phân tích số liệu.
     * 
     * @return mảng [tỷ lệ %, doanh thu]
     */
    public double[] getAnalyticsReport(SaleAnalytics analytics) {
        double perc = dashboardController.calculateSoldPercentage(analytics);
        double rev = dashboardController.getTotalRevenue(analytics);
        return new double[] { perc, rev };
    }

    /**
     * Tạo gói Combo sản phẩm.
     */
    public void createCombo(String name, List<FlashSaleInventory> products, double originalPrice, double discount) {
        comboManager.createCombo(name, products, originalPrice, discount);
    }
}