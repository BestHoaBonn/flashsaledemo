package com.cosmetics.flashsale.control;

import com.cosmetics.flashsale.entity.FlashSaleCampaign;
import org.junit.Test;
import java.time.LocalDateTime;
import static org.junit.Assert.*;

public class CampaignManagerTest {

    @Test
    public void testCreateCampaign_HappyPath() {
        // [Acceptance Test]: US3 - Scenario 3.1 (Happy Path)
        CampaignManager manager = new CampaignManager();
        LocalDateTime t1 = LocalDateTime.now();
        LocalDateTime t2 = t1.plusHours(1);
        
        // Gửi lệnh tạo chiến dịch bắt đầu ngay thời điểm hiện tại và kéo dài 1 giờ, giảm giá 25%
        manager.createCampaign(t1, t2, 25.0);
        
        FlashSaleCampaign campaign = manager.getCurrentCampaign();
        // Trình kiểm duyệt lưu ý kiểm tra hệ thống đã ghi nhận tồn tại thông tin chiến dịch chưa
        assertNotNull(campaign);
        // Xác minh mức biên độ % xuất ra chính xác ở mốc 25%
        assertEquals(25.0, campaign.getDiscountPercent(), 0.01);
    }
}
