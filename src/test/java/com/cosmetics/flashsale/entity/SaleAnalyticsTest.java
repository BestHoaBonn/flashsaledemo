package com.cosmetics.flashsale.entity;

import org.junit.Test;
import static org.junit.Assert.*;

public class SaleAnalyticsTest {

    /**
     * [ĐỐI SOÁT NGHIỆP VỤ]
     * User Story: US4 - Báo cáo hiệu quả thời gian thực
     * Kịch bản (Scenario): 4.1 - Xem báo cáo khi có đơn hàng
     * Luồng xử lý (Path): Happy Path (Luồng thành công)
     * MỤC TIÊU: Kiểm tra thuật toán tính toán tỷ lệ bán ra (Sold %) và Doanh thu tổng từ dữ liệu thực tế.
     */
    @Test
    public void testGetSoldPercentage_HappyPath() {
        SaleAnalytics analytics = new SaleAnalytics(100);
        analytics.recordSale(20, 50000);
        assertEquals(20.0, analytics.getSoldPercentage(), 0.01);
        assertEquals(1000000.0, analytics.getTotalRevenue(), 0.01);
    }

    /**
     * [ĐỐI SOÁT NGHIỆP VỤ]
     * User Story: US4 - Báo cáo hiệu quả thời gian thực
     * Kịch bản (Scenario): 4.2 - Xử lý lỗi chia cho 0
     * Luồng xử lý (Path): Unhappy Path (Lỗi cấu hình tổng kho)
     * MỤC TIÊU: Đảm bảo tính vững chắc của mã nguồn, tung ngoại lệ kiểm soát khi gặp lỗi chia cho số 0.
     */
    @Test(expected = IllegalStateException.class)
    public void testGetSoldPercentage_UnhappyPath_ZeroInitial() {
        SaleAnalytics analytics = new SaleAnalytics(0);
        analytics.getSoldPercentage();
    }

    /**
     * [ĐỐI SOÁT NGHIỆP VỤ]
     * User Story: US4 - Báo cáo hiệu quả thời gian thực
     * Kịch bản (Scenario): 4.3 - Xem báo cáo khi chưa có giao dịch nào
     * Luồng xử lý (Path): Unhappy Path (Trạng thái rỗng)
     * MỤC TIÊU: Xác nhận hệ thống hiển thị Doanh thu 0 và Tỷ lệ 0% một cách chính xác khi mới bắt đầu.
     */
    @Test
    public void testGetSoldPercentage_EmptyState() {
        SaleAnalytics analytics = new SaleAnalytics(100);
        assertEquals(0.0, analytics.getSoldPercentage(), 0.01);
        assertEquals(0.0, analytics.getTotalRevenue(), 0.01);
    }
}
