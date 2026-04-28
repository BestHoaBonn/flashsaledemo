package com.cosmetics.flashsale.entity;

import org.junit.Test;
import static org.junit.Assert.*;

public class US2_FlashSaleInventoryTest {

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

    /**
     * [ĐỐI SOÁT NGHIỆP VỤ]
     * User Story: US2 - Xử lý tồn kho và thanh toán
     * Kịch bản (Scenario): 2.4 - Edge Case: Mua đồng thời (Concurrency / Race Condition)
     * Luồng xử lý (Path): Concurrency Path
     * MỤC TIÊU: Mô phỏng nghẽn cổ chai khi Flash Sale diễn ra. 100 khách hàng (threads)
     * cùng tranh nhau mua 1 sản phẩm. Chỉ ĐÚNG 1 người được phép mua thành công,
     * các thread xếp hàng phía sau nhận lỗi. Đảm bảo tồn kho (inventory) KHÔNG BAO GIỜ bị âm (Overselling).
     *
     * TIÊU CHÍ ĐÁNH GIÁ THẮNG (WINNING CRITERIA):
     * Khách hàng nào xin được khóa màn hình giám sát JVM (Monitor Object Lock) thông qua
     * từ khóa `synchronized` đầu tiên sẽ chiếm được Context CPU và xử lý trừ kho.
     */
    @Test
    public void testHoldInventory_EdgePath_ConcurrentRaceCondition() throws InterruptedException {
        // Tồn kho CHỈ CÓ 1 sản phẩm
        final FlashSaleInventory inventory = new FlashSaleInventory("P_HOT_DEAL", 1);
        final int THREAD_COUNT = 100;

        // Mảng để đếm số giao dịch (thread) trừ kho THÀNH CÔNG và THẤT BẠI
        java.util.concurrent.atomic.AtomicInteger successOrders = new java.util.concurrent.atomic.AtomicInteger();
        java.util.concurrent.atomic.AtomicInteger failedOrders = new java.util.concurrent.atomic.AtomicInteger();

        // CountDownLatch giúp 100 thread xuất phát cùng MỘT LÚC để ép hệ thống chịu tải
        final java.util.concurrent.CountDownLatch startGate = new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.CountDownLatch endGate = new java.util.concurrent.CountDownLatch(THREAD_COUNT);

        Runnable buyTask = () -> {
            try {
                startGate.await(); // Tất cả các thread chờ ở đây

                try {
                    // Cố gắng trừ kho
                    inventory.holdInventory(1);
                    // Nếu thành công, tức là đây là người chiến thắng
                    successOrders.incrementAndGet();
                } catch (IllegalStateException e) {
                    // Những người vào sau khi kho đã về 0 sẽ bị Exception này văng ra
                    failedOrders.incrementAndGet();
                }
            } catch (InterruptedException ignored) {}
            finally {
                endGate.countDown();
            }
        };

        // Khởi tạo 100 thread (Mô phỏng 100 khách hàng cùng lúc bấm nút mua)
        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(buyTask).start();
        }

        // Action! Mở cổng cho 100 người cùng "chen lấn" mua hàng
        startGate.countDown();

        // Đợi tất cả 100 người báo cáo kết quả
        endGate.await();

        // Khẳng định (Assert) kết quả kinh doanh:
        // 1. Phải CHỈ CÓ DUY NHẤT 1 người mua thành công
        assertEquals("Chỉ đúng 1 đơn hàng thành công", 1, successOrders.get());

        // 2. 99 người còn lại phải nhận thất bại (hết hàng)
        assertEquals("99 người nhận báo hết hàng", 99, failedOrders.get());

        // 3. Quan trọng nhất: Tồn kho trên kho phải là 0, tuyệt đối KHÔNG ĐƯỢC LÀ SỐ ÂM.
        assertEquals("Tồn kho sau đợt càng quét phải bằng chính xác 0", 0, inventory.getAvailableQuantity());
    }
}
