package com.cosmetics.flashsale.entity;

import org.junit.Test;
import java.time.LocalDateTime;
import static org.junit.Assert.*;

public class FlashSaleCampaignTest {

    /**
     * [ĐỐI SOÁT NGHIỆP VỤ]
     * User Story: US1 - Hiển thị trạng thái Flash Sale
     * Kịch bản (Scenario): 1.1 - Xem sản phẩm trong giờ Sale
     * Luồng xử lý (Path): Happy Path (Luồng thành công)
     * MỤC TIÊU: Đảm bảo khi thời gian hiện tại nằm trong khung Start-End, hệ thống phải kích hoạt chế độ Sale.
     */
    @Test
    public void testIsActive_HappyPath() {
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        FlashSaleCampaign campaign = new FlashSaleCampaign(start, end, 20.0);
        assertTrue(campaign.isActive(LocalDateTime.now()));
    }

    /**
     * [ĐỐI SOÁT NGHIỆP VỤ]
     * User Story: US1 - Hiển thị trạng thái Flash Sale
     * Kịch bản (Scenario): 1.2 - Xem sản phẩm khi chiến dịch kết thúc
     * Luồng xử lý (Path): Unhappy Path (Ngoại lệ thời gian)
     * MỤC TIÊU: Xác nhận hệ thống tự động gỡ bỏ trạng thái Sale ngay khi hết giờ (Logic "Người canh gác thời gian").
     */
    @Test
    public void testIsInactive_PastCampaign() {
        LocalDateTime start = LocalDateTime.now().minusHours(2);
        LocalDateTime end = LocalDateTime.now().minusHours(1);
        FlashSaleCampaign campaign = new FlashSaleCampaign(start, end, 20.0);
        assertFalse(campaign.isActive(LocalDateTime.now()));
    }

    /**
     * [ĐỐI SOÁT NGHIỆP VỤ]
     * User Story: US3 - Thiết lập chiến dịch (Dành cho Quản lý)
     * Kịch bản (Scenario): 3.2 - Mức giảm giá vượt quá biên lợi nhuận
     * Luồng xử lý (Path): Unhappy Path (Vi phạm ràng buộc)
     * MỤC TIÊU: Kiểm chứng cơ chế "Bộ lọc thông minh", chặn đứng việc tạo sale > 50%.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDiscountExceedsThreshold_UnhappyPath() {
        new FlashSaleCampaign(LocalDateTime.now(), LocalDateTime.now().plusHours(1), 60.0);
    }
    
    /**
     * [ĐỐI SOÁT NGHIỆP VỤ]
     * User Story: US1 - Hiển thị trạng thái Flash Sale
     * Kịch bản (Scenario): 1.3 - Xem SP khi chiến dịch chưa bắt đầu (Coming Soon)
     * Luồng xử lý (Path): Unhappy Path (Sắp diễn ra)
     * MỤC TIÊU: Đảm bảo khách hàng không thể mua giá Sale trước thời điểm bắt đầu.
     */
    @Test
    public void testIsInactive_FutureCampaign() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        FlashSaleCampaign campaign = new FlashSaleCampaign(start, end, 20.0);
        assertFalse(campaign.isActive(LocalDateTime.now()));
    }

    /**
     * [ĐỐI SOÁT NGHIỆP VỤ]
     * User Story: US3 - Thiết lập chiến dịch (Dành cho Quản lý)
     * Kịch bản (Scenario): 3.3 - Thiết lập thời gian sai (Ngược)
     * Luồng xử lý (Path): Unhappy Path (Dữ liệu thời gian vô lý)
     * MỤC TIÊU: Chặn người dùng lưu chiến dịch có thời điểm kết thúc trước thời điểm bắt đầu.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateCampaign_UnhappyPath_InvalidTimeRange() {
        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        new FlashSaleCampaign(start, end, 10.0);
    }
}
