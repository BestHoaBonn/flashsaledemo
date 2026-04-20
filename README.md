# TÀI LIỆU YÊU CẦU: TÍNH NĂNG FLASH SALE TOÀN DIỆN (BA + BDD)

## 1. Giới thiệu (Mục tiêu Nghiệp vụ)
Tính năng Flash Sale tạo ra sự khan hiếm chiến lược để thúc đẩy chốt đơn nhanh. Trong bối cảnh thương mại điện tử 2026, tính năng này không chỉ phục vụ khách hàng trực tiếp mà còn phải tối ưu hóa dữ liệu (cấu trúc rõ ràng) để các Trợ lý AI (AI Shopping Agents) có thể đọc, so sánh giá và tự động chốt đơn thay cho người dùng.
Hệ thống được xây dựng theo phương pháp Cắt dọc (Vertical Slicing), đảm bảo mỗi User Story (US) là một tính năng hoàn chỉnh, độc lập (chuẩn INVEST).

---

## 2. Chi tiết Use Cases & User Stories

### US1: Hiển thị trạng thái Flash Sale trên sản phẩm
**Use Case / Business Rules**: Là khách hàng, tôi muốn thấy giá ưu đãi, số tiền tiết kiệm được và đồng hồ đếm ngược trên trang sản phẩm để ra quyết định mua hàng nhanh hơn.

- **Acceptance Criteria (Tiêu chí chấp nhận) & Các Paths**:
  - **Scenario 1.1 (Happy Path) - Xem sản phẩm trong giờ Flash Sale**
    - **Given**: Chiến dịch "Siêu Sale" đang diễn ra, kết thúc lúc 23:59.
    - **When**: Khách hàng xem sản phẩm "Son MAC Ruby Woo".
    - **Then**: Hệ thống hiển thị giá Flash Sale "1.000.000 VNĐ", nhãn "Tiết kiệm 500.000 VNĐ" và đồng hồ đếm ngược đến 23:59.
  - **Scenario 1.2 (Unhappy Path) - Xem sản phẩm khi chiến dịch đã kết thúc**
    - **Given**: Chiến dịch "Siêu Sale" đã kết thúc.
    - **When**: Khách hàng xem sản phẩm "Son MAC Ruby Woo".
    - **Then**: Hệ thống hiển thị giá gốc "1.500.000 VNĐ" và không có đồng hồ đếm ngược.



---

### US2: Xử lý tồn kho và thanh toán
**Use Case / Business Rules**: Là khách hàng, tôi muốn hệ thống chốt đúng giá ưu đãi nếu tôi mua trong giới hạn số lượng cho phép, để đảm bảo tính công bằng.

- **Acceptance Criteria (Tiêu chí chấp nhận) & Các Paths**:
  - **Scenario 2.1 (Happy Path) - Thanh toán hợp lệ**
    - **Given**: Kho Flash Sale còn "10" sản phẩm.
    - **When**: Khách hàng thêm "1" sản phẩm và thanh toán.
    - **Then**: Đơn hàng được tạo với giá Flash Sale và kho giảm còn "9".
  - **Scenario 2.2 (Unhappy Path) - Thanh toán khi hết kho khuyến mãi**
    - **Given**: Khách hàng đang ở trang thanh toán nhưng kho Flash Sale vừa về "0".
    - **When**: Khách nhấn "Xác nhận đặt hàng".
    - **Then**: Hệ thống báo lỗi "Sản phẩm đã hết suất Flash Sale" và cập nhật giỏ hàng về giá gốc.



---

### US3: Thiết lập chiến dịch (Dành cho Quản lý)
**Use Case / Business Rules**: Là Quản lý cửa hàng, tôi muốn lên lịch Ngày & Giờ và cấu hình mức giảm giá cho chiến dịch để hệ thống tự động chạy.

- **Acceptance Criteria (Tiêu chí chấp nhận) & Các Paths**:
  - **Scenario 3.1 (Happy Path) - Tạo chiến dịch hợp lệ**
    - **Given**: Quản lý đang ở form tạo chiến dịch.
    - **When**: Nhập ngày giờ bắt đầu "2026-04-20T08:00", kết thúc "2026-04-20T12:00" và giá giảm "20%", rồi nhấn "Lưu".
    - **Then**: Hệ thống lưu thành công và báo "Chiến dịch đã được lên lịch".
  - **Scenario 3.2 (Unhappy Path) - Mức giảm giá vượt quá biên lợi nhuận**
    - **Given**: Chính sách giới hạn giảm tối đa "50%".
    - **When**: Quản lý nhập mức giảm "60%" và nhấn "Lưu".
    - **Then**: Hệ thống chặn lưu và báo lỗi "Mức giảm không được vượt quá 50%".



---

### US4: Báo cáo hiệu quả thời gian thực (Dành cho Quản lý)
**Use Case / Business Rules**: Là Quản lý cửa hàng, tôi muốn xem doanh thu và tỷ lệ bán ra theo thời gian thực để quyết định bổ sung hàng hoặc thay đổi chiến lược marketing.

- **Acceptance Criteria (Tiêu chí chấp nhận) & Các Paths**:
  - **Scenario 4.1 (Happy Path) - Xem báo cáo khi có đơn hàng**
    - **Given**: Chiến dịch đang diễn ra và có đơn hàng thành công.
    - **When**: Quản lý mở Dashboard Flash Sale.
    - **Then**: Hệ thống hiển thị doanh thu "50.000.000 VNĐ" và tỷ lệ bán ra "80%".
  - **Scenario 4.2 (Unhappy Path) - Xem báo cáo khi bị lỗi chia cho 0**
    - **Given**: Chiến dịch bị lỗi cấu hình tổng sản phẩm ban đầu là "0".
    - **When**: Quản lý mở Dashboard.
    - **Then**: Hệ thống hiển thị tỷ lệ bán ra "0%" và cảnh báo "Chưa cấu hình số lượng tổng".



---

### US5: Quản lý Sản phẩm và Combo Sale
**Use Case / Business Rules**: Là Quản lý cửa hàng, tôi muốn gộp nhiều sản phẩm thành một Combo Flash Sale để đẩy hàng tồn kho nhanh hơn.

- **Acceptance Criteria (Tiêu chí chấp nhận) & Các Paths**:
  - **Scenario 5.1 (Happy Path) - Tạo Combo hợp lệ**
    - **Given**: Quản lý chọn "Son MAC Ruby Woo" và "Nước hoa Chanel N°5".
    - **When**: Thiết lập giá Combo giảm "30%" và nhấn "Tạo".
    - **Then**: Hệ thống hiển thị "Combo Làm Đẹp" trong danh sách Flash Sale.
  - **Scenario 5.2 (Unhappy Path) - Thêm sản phẩm đã hết hàng vào Combo**
    - **Given**: Sản phẩm "Nước hoa Chanel N°5" có tồn kho tổng là "0".
    - **When**: Quản lý cố gắng ghép "Nước hoa Chanel N°5" vào Combo và nhấn "Tạo".
    - **Then**: Hệ thống báo lỗi "Sản phẩm Nước hoa Chanel N°5 không đủ tồn kho để tạo Combo".


---

## 3. Hướng dẫn chạy dự án

### Yêu cầu hệ thống:
- Cài đặt Java 8 hoặc mới hơn.
- Cài đặt phần mềm Maven (để quản lý thư viện và nạp môi trường test).

### Chạy hệ thống kiểm thử tự động
Toàn bộ dự án đã được tích hợp kiểm thử tự động (Unit Test cho lõi máy tính và BDD Cucumber cho luồng nghiệp vụ kinh doanh). Bạn sẽ không lo bị lỗi quy trình khi bàn giao.

Mở **Terminal (Command Prompt)** lên, đi đến thư mục dự án và gõ dòng lệnh sau:

```bash
mvn clean test
```

Hệ thống sẽ ngay lập tức tự động quét, giả lập khách hàng chạy qua 5 User Stories và trả về kết quả màu xanh (`BUILD SUCCESS`) nếu mọi tính năng vẫn đang hoạt động hoàn hảo!
