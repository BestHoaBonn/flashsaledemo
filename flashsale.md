# BÁO CÁO TỔNG HỢP: TÍNH NĂNG FLASH SALE TOÀN DIỆN (BA + BDD)

---

## 1. Mục tiêu Nghiệp vụ & Bối cảnh 2026
Tính năng **Flash Sale** tạo ra sự khan hiếm chiến lược để thúc đẩy chốt đơn nhanh.
---

## 2. Chi tiết User Stories (Vertical Slices)

### 🟢 US1: Hiển thị trạng thái Flash Sale trên sản phẩm

> **Business Rules:** Là khách hàng, tôi muốn thấy giá ưu đãi, số tiền tiết kiệm được và đồng hồ đếm ngược trên trang sản phẩm để ra quyết định mua hàng nhanh hơn.

#### ✅ Acceptance Criteria:
*   **Scenario 1.1 (Happy Path): Xem sản phẩm trong giờ Flash Sale**
    *   **Given** chiến dịch "Siêu Sale" đang diễn ra, kết thúc lúc 23:59.
    *   **When** khách hàng xem sản phẩm "Son MAC Ruby Woo".
    *   **Then** hệ thống hiển thị giá Flash Sale "1.000.000 VNĐ", nhãn "Tiết kiệm 500.000 VNĐ" và đồng hồ đếm ngược đến 23:59.
*   **Scenario 1.2 (Unhappy Path): Xem sản phẩm khi chiến dịch đã kết thúc**
    *   **Given** chiến dịch "Siêu Sale" đã kết thúc.
    *   **When** khách hàng xem sản phẩm "Son MAC Ruby Woo".
    *   **Then** hệ thống hiển thị giá gốc "1.500.000 VNĐ" và không có đồng hồ đếm ngược.

#### 🛠 Tasks (Dependency Order):
- [x] Viết code Acceptance Test cho hiển thị giá và đếm ngược.
- [x] Viết code & Unit Test cho Entity `FlashSaleCampaign` (xử lý logic thời gian hợp lệ/hết hạn).
- [x] Viết code & Unit Test cho Control `ProductCatalog` (ghép nối dữ liệu).
- [x] Viết code cho Boundary UI (Không Unit Test).

---

### 🟢 US2: Xử lý tồn kho và thanh toán

> **Business Rules:** Là khách hàng, tôi muốn hệ thống chốt đúng giá ưu đãi nếu tôi mua trong giới hạn số lượng cho phép, để đảm bảo tính công bằng.

#### ✅ Acceptance Criteria:
*   **Scenario 2.1 (Happy Path): Thanh toán hợp lệ**
    *   **Given** kho Flash Sale còn "10" sản phẩm.
    *   **When** khách hàng thêm "1" sản phẩm và thanh toán.
    *   **Then** đơn hàng được tạo với giá Flash Sale và kho giảm còn "9".
*   **Scenario 2.2 (Unhappy Path): Thanh toán khi hết kho khuyến mãi**
    *   **Given** khách hàng đang ở trang thanh toán nhưng kho Flash Sale vừa về "0".
    *   **When** khách nhấn "Xác nhận đặt hàng".
    *   **Then** hệ thống báo lỗi "Sản phẩm đã hết suất Flash Sale" và cập nhật giỏ hàng về giá gốc.

#### 🛠 Tasks (Dependency Order):
- [x] Viết code Acceptance Test cho luồng thanh toán và trừ tồn kho.
- [x] Viết code & Unit Test cho Entity `FlashSaleInventory` (xử lý giữ chỗ, trừ kho, báo lỗi hết hàng).
- [x] Viết code & Unit Test cho Control `OrderCheckout`.
- [x] Tích hợp Boundary UI và Mock Bank API thanh toán (Không Unit Test).

---

### 🟢 US3: Thiết lập chiến dịch (Dành cho Quản lý)

> **Business Rules:** Là Quản lý cửa hàng, tôi muốn lên lịch Ngày & Giờ và cấu hình mức giảm giá cho chiến dịch để hệ thống tự động chạy.

#### ✅ Acceptance Criteria:
*   **Scenario 3.1 (Happy Path): Tạo chiến dịch hợp lệ**
    *   **Given** Quản lý đang ở form tạo chiến dịch.
    *   **When** nhập ngày giờ bắt đầu "2026-04-20T08:00", kết thúc "2026-04-20T12:00" và giá giảm "20%", rồi nhấn "Lưu".
    *   **Then** hệ thống lưu thành công và báo "Chiến dịch đã được lên lịch".
*   **Scenario 3.2 (Unhappy Path): Mức giảm giá vượt quá biên lợi nhuận**
    *   **Given** chính sách giới hạn giảm tối đa "50%".
    *   **When** Quản lý nhập mức giảm "60%" và nhấn "Lưu".
    *   **Then** hệ thống chặn lưu và báo lỗi "Mức giảm không được vượt quá 50%".

#### 🛠 Tasks (Dependency Order):
- [x] Viết code Acceptance Test cho cấu hình chiến dịch.
- [x] Viết code & Unit Test cho Entity `CampaignConfiguration` (validate thời gian, mức giảm).
- [x] Viết code & Unit Test cho Control `CampaignManager`.
- [x] Viết code Boundary UI form thiết lập (Không Unit Test).

---

### 🟢 US4: Báo cáo hiệu quả thời gian thực (Dành cho Quản lý)

> **Business Rules:** Là Quản lý cửa hàng, tôi muốn xem doanh thu và tỷ lệ bán ra theo thời gian thực để quyết định bổ sung hàng hoặc thay đổi chiến lược marketing.

#### ✅ Acceptance Criteria:
*   **Scenario 4.1 (Happy Path): Xem báo cáo khi có đơn hàng**
    *   **Given** chiến dịch đang diễn ra và có đơn hàng thành công.
    *   **When** Quản lý mở Dashboard Flash Sale.
    *   **Then** hệ thống hiển thị doanh thu "50.000.000 VNĐ" và tỷ lệ bán ra "80%".
*   **Scenario 4.2 (Unhappy Path): Xem báo cáo khi bị lỗi chia cho 0**
    *   **Given** chiến dịch bị lỗi cấu hình tổng sản phẩm ban đầu là "0".
    *   **When** Quản lý mở Dashboard.
    *   **Then** hệ thống hiển thị tỷ lệ bán ra "0%" và cảnh báo "Chưa cấu hình số lượng tổng".

#### 🛠 Tasks (Dependency Order):
- [x] Viết code Acceptance Test cho luồng tính toán số liệu.
- [x] Viết code & Unit Test cho Entity `SaleAnalytics` (đảm bảo xử lý Unhappy Path chia cho 0).
- [x] Viết code & Unit Test cho Control `DashboardController`.
- [x] Viết code Boundary UI biểu đồ (Không Unit Test).

---

### 🟢 US5: Quản lý Sản phẩm và Combo Sale

> **Business Rules:** Là Quản lý cửa hàng, tôi muốn gộp nhiều sản phẩm thành một Combo Flash Sale để đẩy hàng tồn kho nhanh hơn.

#### ✅ Acceptance Criteria:
*   **Scenario 5.1 (Happy Path): Tạo Combo hợp lệ**
    *   **Given** Quản lý chọn "Son MAC Ruby Woo" và "Nước hoa Chanel N°5".
    *   **When** thiết lập giá Combo giảm "30%" và nhấn "Tạo".
    *   **Then** hệ thống hiển thị "Combo Làm Đẹp" trong danh sách Flash Sale.
*   **Scenario 5.2 (Unhappy Path): Thêm sản phẩm đã hết hàng vào Combo**
    *   **Given** sản phẩm "Nước hoa Chanel N°5" có tồn kho tổng là "0".
    *   **When** Quản lý cố gắng ghép "Nước hoa Chanel N°5" vào Combo và nhấn "Tạo".
    *   **Then** hệ thống báo lỗi "Sản phẩm Nước hoa Chanel N°5 không đủ tồn kho để tạo Combo".

#### 🛠 Tasks (Dependency Order):
- [x] Viết code Acceptance Test cho luồng tạo/xóa Combo.
- [x] Viết code & Unit Test cho Entity `FlashSaleCombo` (xử lý logic gộp mã, tính giá, check tồn kho gốc).
- [x] Viết code & Unit Test cho Control `ComboManager`.
- [x] Viết code Boundary UI quản lý danh sách (Không Unit Test).
