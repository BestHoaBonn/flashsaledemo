package com.cosmetics.flashsale.entity;

import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

public class US5_FlashSaleComboTest {

    /**
     * [ĐỐI SOÁT NGHIỆP VỤ]
     * User Story: US5 - Quản lý Sản phẩm và Combo Sale
     * Kịch bản (Scenario): 5.1 - Tạo Combo hợp lệ
     * Luồng xử lý (Path): Happy Path (Luồng thành công)
     * MỤC TIÊU: Xác nhận khả năng gộp nhóm thực thể và tính toán giá trị/số lượng khả dụng của Combo dựa trên thành phần.
     */
    @Test
    public void testCreateCombo_HappyPath() {
        // Cho chạy danh sách sản phẩm gồm "Son MAC" (tồn 10 đơn vị) và "Nước hoa" (tồn 5 đơn vị)
        FlashSaleInventory p1 = new FlashSaleInventory("Son MAC Ruby Woo", 10);
        FlashSaleInventory p2 = new FlashSaleInventory("Nước hoa Chanel N°5", 5);
        
        List<FlashSaleInventory> products = Arrays.asList(p1, p2);
        // Gộp hai mặt hàng vào chung một Combo giá trị 2 triệu đồng, chốt mức giảm giá 30%
        FlashSaleCombo combo = new FlashSaleCombo("Combo Làm Đẹp", products, 2000000.0, 30.0);
        
        // Máy tính phản hồi tên đã lưu khớp với "Combo Làm Đẹp"
        assertEquals("Combo Làm Đẹp", combo.getComboName());
        // Giá tiền Combo mới phải tự rớt xuống còn 1.400.000 VNĐ
        assertEquals(1400000.0, combo.getDiscountedPrice(), 0.01); // Giảm 30%
        // Kiểm đếm số lượng Combo được cấp phép bán ra tối đa dựa bằng mức tồn thấp nhất (5 chiếc nước hoa)
        assertEquals(5, combo.getAvailableComboQuantity()); // Thấp nhất là 5
    }

    /**
     * [ĐỐI SOÁT NGHIỆP VỤ]
     * User Story: US5 - Quản lý Sản phẩm và Combo Sale
     * Kịch bản (Scenario): 5.2 - Thêm sản phẩm đã hết hàng vào Combo
     * Luồng xử lý (Path): Unhappy Path (Thành phần Combo hết kho)
     * MỤC TIÊU: Đảm bảo nguyên tắc "Transactional Consistency" - Chặn đứng việc tạo Combo nếu có linh kiện rỗng hàng.
     */
    @Test(expected = IllegalStateException.class)
    public void testCreateCombo_UnhappyPath_OutOfStock() {
        // Rút sạch số lượng Nước hoa khỏi kho về ngưỡng 0
        FlashSaleInventory p1 = new FlashSaleInventory("Son MAC Ruby Woo", 10);
        FlashSaleInventory p2 = new FlashSaleInventory("Nước hoa Chanel N°5", 0);
        
        List<FlashSaleInventory> products = Arrays.asList(p1, p2);
        // Quản lý cố tình vón món Nước hoa vào Combo, máy chủ test đo lường xem hệ thống có tung còi báo lỗi không
        new FlashSaleCombo("Combo Lỗi", products, 2000000.0, 30.0);
    }
}
