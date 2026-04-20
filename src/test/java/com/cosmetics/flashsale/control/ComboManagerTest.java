package com.cosmetics.flashsale.control;

import com.cosmetics.flashsale.entity.FlashSaleCombo;
import com.cosmetics.flashsale.entity.FlashSaleInventory;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

public class ComboManagerTest {

    /**
     * [ĐỐI SOÁT NGHIỆP VỤ]
     * User Story: US5 - Quản lý Sản phẩm và Combo Sale
     * Kịch bản (Scenario): 5.1 - Tạo Combo hợp lệ
     * Luồng xử lý (Path): Happy Path (Luồng thành công)
     * MỤC TIÊU: Xác nhận khả năng đóng gói nhiều sản phẩm thành một thực thể Combo và giữ nguyên vẹn thông tin cấu hình.
     */
    @Test
    public void testCreateCombo_Success() {
        ComboManager manager = new ComboManager();
        FlashSaleInventory p1 = new FlashSaleInventory("Son MAC Ruby Woo", 10);
        FlashSaleInventory p2 = new FlashSaleInventory("Nước hoa Chanel", 5);
        List<FlashSaleInventory> products = Arrays.asList(p1, p2);
        
        FlashSaleCombo combo = manager.createCombo("Combo Xịn", products, 1000000.0, 20.0);
        
        assertNotNull(combo);
        assertEquals(1, manager.getCurrentCombos().size());
        assertEquals("Combo Xịn", manager.getCurrentCombos().get(0).getComboName());
    }

    /**
     * [ĐỐI SOÁT NGHIỆP VỤ]
     * User Story: US5 - Quản lý Sản phẩm và Combo Sale
     * Kịch bản (Scenario): 5.2 - Sản phẩm rỗng trong Combo
     * Luồng xử lý (Path): Unhappy Path (Thành phần Combo hết hàng)
     * MỤC TIÊU: Đảm bảo nguyên tắc "Mắt xích yếu nhất" - Chặn đứng việc tạo Combo nếu bất kỳ sản phẩm nào bên trong đã rỗng kho.
     */
    @Test(expected = IllegalStateException.class)
    public void testCreateCombo_Failure_OutOfStock() {
        ComboManager manager = new ComboManager();
        FlashSaleInventory p1 = new FlashSaleInventory("Sản phẩm hết hàng", 0);
        List<FlashSaleInventory> products = Arrays.asList(p1);
        manager.createCombo("Combo Lỗi", products, 100000.0, 10.0);
    }

    /**
     * [ĐỐI SOÁT NGHIỆP VỤ]
     * User Story: US5 - Quản lý Sản phẩm và Combo Sale
     * Kịch bản (Scenario): 5.3 - Tạo Combo không có sản phẩm nào
     * Luồng xử lý (Path): Unhappy Path (Thiếu dữ liệu thành phần)
     * MỤC TIÊU: Chặn việc tạo các thực thể Combo rỗng, đảm bảo tính toàn vẹn của dữ liệu sản phẩm.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateCombo_Failure_EmptyProducts() {
        ComboManager manager = new ComboManager();
        List<FlashSaleInventory> products = new ArrayList<>();
        manager.createCombo("Combo Rỗng", products, 100000.0, 10.0);
    }
}
