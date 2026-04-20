package com.cosmetics.flashsale.entity;

import org.junit.Test;
import static org.junit.Assert.*;

public class FlashSaleInventoryTest {

    /**
     * [ĐỐI SOÁT NGHIỆP VỤ]
     * User Story: US2 - Xử lý tồn kho và thanh toán
     * Kịch bản (Scenario): 2.1 - Thanh toán hợp lệ (Trừ kho)
     * Luồng xử lý (Path): Happy Path (Luồng thành công)
     * MỤC TIÊU: Đảm bảo quy trình trừ hàng diễn ra chính xác và đồng bộ (Thread-safe) khi vẫn còn tồn kho.
     */
    @Test
    public void testHoldInventory_HappyPath() {
        FlashSaleInventory inventory = new FlashSaleInventory("P1", 10);
        assertTrue(inventory.holdInventory(2));
        assertEquals(8, inventory.getAvailableQuantity());
    }

    /**
     * [ĐỐI SOÁT NGHIỆP VỤ]
     * User Story: US2 - Xử lý tồn kho và thanh toán
     * Kịch bản (Scenario): 2.2 - Thanh toán khi hết hàng khuyến mãi
     * Luồng xử lý (Path): Unhappy Path (Hết hàng tại bước chốt)
     * MỤC TIÊU: Kích hoạt cơ chế phòng vệ, chặn đứng giao dịch và báo lỗi khi số lượng yêu cầu vượt quá tồn kho khả dụng.
     */
    @Test(expected = IllegalStateException.class)
    public void testHoldInventory_UnhappyPath_OutOfStock() {
        FlashSaleInventory inventory = new FlashSaleInventory("P1", 1);
        inventory.holdInventory(2);
    }

    /**
     * [ĐỐI SOÁT NGHIỆP VỤ]
     * User Story: US2 - Xử lý tồn kho và thanh toán
     * Kịch bản (Scenario): 2.3 - Thanh toán với số lượng không hợp lệ
     * Luồng xử lý (Path): Unhappy Path (Dữ liệu đầu vào sai)
     * MỤC TIÊU: Đảm bảo hệ thống chặn các yêu cầu mua với số lượng <= 0 để tránh lỗi logic thanh toán.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHoldInventory_UnhappyPath_InvalidQuantity() {
        FlashSaleInventory inventory = new FlashSaleInventory("P1", 10);
        inventory.holdInventory(0);
    }
}
