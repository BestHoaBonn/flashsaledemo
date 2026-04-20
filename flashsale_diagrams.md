## 🟢 US1: Hiển thị trạng thái Flash Sale trên sản phẩm

### 1. Kiến trúc BCE (Boundary - Control - Entity)
> **Giải thích:** Khách hàng (Boundary) không trực tiếp đòi dữ liệu từ hệ thống tính toán (Entity), mà bắt buộc phải thông qua Bộ lọc (Control) làm nhiệm vụ bảo vệ logic.
```text
  [ BOUNDARY / GIAO DIỆN ]                ( CONTROL / XỬ LÝ )                 { ENTITY / LÕI NGHIỆP VỤ }
  
  ╔════════════════════╗               ┌────────────────────┐               ⟪──────────────────────⟫
  ║ MÀN HÌNH MUA HÀNG  ║ = Lấy SP ===> │ TRÌNH QUẢN LÝ DANH │ == Truy vấn =>│ DỮ LIỆU CHIẾN DỊCH & │
  ║ (CustomerBoundary) ║               │ MỤC (ProductCatalog│               │ MỨC GIẢM (Campaign)  │
  ╚════════════════════╝ <== Trả về == └────────────────────┘ <== Kết quả ==⟪──────────────────────⟫
   Là nơi khách xem, nhấn                Là nhân viên chạy bàn                Là nhà bếp nấu công thức
```

### 2. Sơ đồ Kịch Bản (Sequence Diagram)
> **Giải thích:** Trình tự chi tiết hệ thống tính toán giá và trả về kết quả phụ thuộc vào chiến dịch đang Mở hay đã Tắt.
```mermaid
sequenceDiagram
    autonumber
    actor KhachHang as Khách Hàng
    participant UI as Giao diện Mua Hàng
    participant Control as Trình Quản lý Danh mục
    participant Entity as Dữ liệu Chiến dịch

    KhachHang->>UI: Mở xem trang chi tiết sản phẩm
    UI->>Control: Yêu cầu thông tin hiển thị (Giá bán)
    Control->>Entity: Kiểm tra giờ (Hàm isActive?)
    
    rect rgb(235, 255, 235)
        Note right of Control: Kịch bản 1: ĐANG TRONG GIỜ FLASH SALE
        Entity-->>Control: Trả về TRUE & Mức giảm phần trăm
        Control-->>UI: Cấp phép hiện Giá Flash Sale
        UI-->>KhachHang: Hiển thị ĐỒNG HỒ đếm ngược & GIÁ RẺ
    end
    
    rect rgb(255, 235, 235)
        Note right of Control: Kịch bản 2: QUÁ GIỜ (HẾT HẠN)
        Entity-->>Control: Trả về FALSE
        Control-->>UI: Từ chối chiết khấu
        UI-->>KhachHang: Hiển thị Giá Gốc, ẨN Đồng hồ
    end
```

### 3. Sơ đồ Trạng Thái (State Diagram)
> **Giải thích:** Vòng đời của một chiến dịch đi từ lúc ngủ đông đến lúc tự đào thải.
```mermaid
stateDiagram-v2
    [*] --> ChoKichHoat : Khởi tạo chiến dịch
    note left of ChoKichHoat
      Đồng hồ hiện tại
      SỚM HƠN thời điểm bắt đầu
    end note
    
    ChoKichHoat --> DangDienRa : Tới đúng thời điểm Sale
    
    DangDienRa --> DaKetThuc : Vượt qua thời điểm kết thúc
    note right of DaKetThuc
      Hệ thống tự khóa lại.
      Sản phẩm trở về giá gốc.
    end note
    
    DaKetThuc --> [*]
```

### 4. Thiết kế Cấu trúc file & Màn hình hiển thị
> Nơi các kỹ sư lưu trữ File và Giao diện phác thảo tương ứng.

```text
📁 CẤU TRÚC FOLDER THEO BCE:
 src/
  ╰─ main/java/com/cosmetics/flashsale/
      ├─ boundary/     ->  CustomerBoundary.ui (File thiết kế giao diện màu sắc)
      ├─ control/      ->  ProductCatalog.java (File điều chế kết nối)
      ╰─ entity/       ->  FlashSaleCampaign.java (File công thức tính toán thời gian)


🖥️ MÀN HÌNH WIREFRAME:
 ╭──────────────────────────────────────────────────────────╮
 │  < Quay lại               Chi tiết Son MAC Ruby Woo      │
 ├──────────────────────────────────────────────────────────┤
 │                                                          │
 │   ╭──────────────╮    [⚡ FLASH SALE ĐANG MỞ BÁN]       │
 │   │              │                                       │
 │   │      [📸]    │    Giá niêm yết: ~~1.500.000 VNĐ~~    │
 │   │   ẢNH CỦA    │    Giá k.mãi:    1.000.000 VNĐ        │
 │   │   SẢN PHẨM   │                                       │
 │   │              │   ╰ Đã tiết kiệm: 500.000 VNĐ ╯       │
 │   ╰──────────────╯                                       │
 │                                                          │
 │   ⏱ Đồng hồ hết hạn: 02:45:10      [ 🛒 THÊM VÀO GIỎ ]  │
 ╰──────────────────────────────────────────────────────────╯
```

---

## 🟢 US2: Xử lý tồn kho và thanh toán

### 1. Kiến trúc BCE
> **Giải thích:** Khi bấm giỏ hàng, thông tin truyền qua Bộ Check-out để đi xin cấp phép trừ kho ảo tại Cột Inventory (Nằm sâu và an toàn nhất).
```text
  [ BOUNDARY / GIAO DIỆN ]                ( CONTROL / XỬ LÝ )                 { ENTITY / LÕI NGHIỆP VỤ }
  
  ╔════════════════════╗               ┌────────────────────┐               ⟪──────────────────────⟫
  ║ GIAO DIỆN XÁC NHẬN ║ = Bấm mua ==> │ ĐIỀU PHỐI ĐƠN HÀNG │ === Lệnh ===> │ BỘ BẢO MẬT TỒN KHO   │
  ║ (CheckoutBoundary) ║               │   (OrderCheckout)  │               │ (FlashSaleInventory) │
  ╚════════════════════╝ <== T/C, Lỗi= └────────────────────┘ <== Trừ kho ==⟪──────────────────────⟫
```

### 2. Sơ đồ Kịch Bản (Sequence Diagram)
> **Giải thích:** Hai trường hợp khi Khách chốt deal; một là Tồn Kho cấp phép, hai là Cản lại vì hết hàng. 
```mermaid
sequenceDiagram
    autonumber
    actor KhachHang as Khách Hàng
    participant UI as Màn hình Thanh Toán
    participant Control as Lệnh Đặt Hàng
    participant Entity as Bộ Trữ Kho

    KhachHang->>UI: Bấm "Xác nhận đặt hàng"
    UI->>Control: Tiến hành làm thủ tục thanh toán
    Control->>Entity: Lệnh: Khóa & Trừ bớt Số Vị Trí (Ví dụ: -2 suất)
    
    alt Kho CÒN DƯ HÀNG 
        Entity-->>Control: Thành công (Trừ vào Database)
        Control-->>UI: Ra lệnh in Biên lai Hợp lệ
        UI-->>KhachHang: Màn hình "Cảm ơn quý khách!" 
    else KHO ĐÃ VỀ SỐ KHÔNG (0)
        Entity-->>Control: Ném Còi Báo Động (Lỗi Exception)
        Control-->>UI: Cảnh báo Hết Hàng
        UI-->>KhachHang: Hiện Panel Lỗi Trắng tay, load lại Giỏ nguyên giá
    end
```

### 3. Thiết kế Cấu trúc file & Màn hình hiển thị
```text
📁 CẤU TRÚC FOLDER:
 src/
  ╰─ main/java/.../
      ├─ boundary/     ->  CheckoutBoundary.html (Nút bấm thanh toán)
      ├─ control/      ->  OrderCheckout.java (Điều khiển giao tiếp giữ chỗ lệnh)
      ╰─ entity/       ->  FlashSaleInventory.java (Giữ khư khư thông tin Số Lượng)

🖥️ MÀN HÌNH WIREFRAME:
 ╭──────────────────────────────────────────────────────────╮
 │  Thanh toán giỏ hàng                                     │
 ├──────────────────────────────────────────────────────────┤
 │                                                          │
 │  Son MAC Ruby Woo                                        │
 │  SL: [ - ]  2  [ + ]   ................... 2.000.000 VNĐ │
 │                                                          │
 │  Tổng cộng (đã giảm):                    2.000.000 VNĐ   │
 │                                                          │
 │   ╔══════════════════════════════════════════════════╗   │
 │   ║ ⚠️ LỖI BÁO TỪ MÁY CHỦ:                           ║   │ <--- (Hiển thị mượt mà)
 │   ║    Rất tiếc! Số suất Flash Sale đã hết.          ║   │
 │   ╚══════════════════════════════════════════════════╝   │
 │                                                          │
 │                     [ XÁC NHẬN ĐẶT HÀNG ]                │
 ╰──────────────────────────────────────────────────────────╯
```

---

## 🟢 US3: Thiết lập chiến dịch (Dành cho Quản trị viên)

### 1. Kiến trúc BCE
> **Giải thích:** Quản lý làm việc với biểu mẫu, Controller truyền tới lõi, nếu % lớn hơn kịch trần, Entity sẽ "cạch mặt" từ chối lưu.
```text
  [ BOUNDARY / GIAO DIỆN ]                ( CONTROL / XỬ LÝ )                 { ENTITY / LÕI NGHIỆP VỤ }
  
  ╔════════════════════╗               ┌────────────────────┐               ⟪──────────────────────⟫
  ║ GIAO DIỆN ADMIN    ║ = Nhấn Lưu => │ BAN QUẢN LÝ TỰ ĐỘNG│ === Lệnh ===> │ BỘ NHIỆM VỤ SINH MỚI │
  ║   (AdminForm)      ║               │  (CampaignManager) │               │ (FlashSaleCampaign)  │
  ╚════════════════════╝ <== T/C, Lỗi= └────────────────────┘ <== Báo lỗi ==⟪──────────────────────⟫
```

### 2. Sơ đồ Kịch Bản (Sequence Diagram)
> **Giải thích:** Tình huống thực tiễn mô tả cách hệ thống chặn "Quyền Lực" của Admin khi vi phạm các giới hạn an toàn.
```mermaid
sequenceDiagram
    autonumber
    actor Admin
    participant UI as Bảng Điều Khiển Admin
    participant Control as Trình Ghi Chiến Dịch
    participant Entity as Bộ Kiểm Quản Sinh Object

    Admin->>UI: Kê số giờ & Mức siêu giảm 60%
    UI->>Control: createCampaign(60%)
    Control->>Entity: Cố ép hệ thống cho ra khuôn 60%
    
    alt Bị quá giới hạn 50%
        Entity-->>Control: Nhào lộn Exception (Quá % cho phép)!
        Control-->>UI: Thất bại, dừng quá trình lưu trữ
        UI-->>Admin: Hiện bọt đỏ (Tooltip) "Cấm giảm hơn 50%"
    else Biên độ tốt chuẩn
        Entity-->>Control: Định hình khối Campaign mới xong
        Control-->>UI: Lưu giữ Object thành công
        UI-->>Admin: "Kế hoạch đã được cài hẹn giờ chạy"
    end
```

### 3. Thiết kế Cấu trúc file & Màn hình hiển thị
```text
📁 CẤU TRÚC FOLDER:
 src/
  ╰─ main/java/.../
      ├─ boundary/     ->  AdminFormBoundary.html (Bảng nhập giá trị)
      ├─ control/      ->  CampaignManager.java (Đóng vai thư ký lưu hồ sơ)
      ╰─ entity/       ->  FlashSaleCampaign.java (Các quy định cứng)

🖥️ MÀN HÌNH WIREFRAME:
 ╭──────────────────────────────────────────────────────────╮
 │  [ADMIN] Tạo mới chiến dịch Flash Sale                   │
 ├──────────────────────────────────────────────────────────┤
 │                                                          │
 │  📅 Ngày & Giờ bắt đầu:  [ 2026-04-20 08:00 ▾]         │
 │  📅 Ngày & Giờ kết thúc: [ 2026-04-20 12:00 ▾]         │
 │                                                          │
 │  🔥 Điền mức sale (%):                                   │
 │   ╭──────────────────────────────────╮                   │
 │   │ 60                               │ ❌ Không hộp lệ  │
 │   ╰──────────────────────────────────╯                   │
 │  [ LƯU CHIẾN DỊCH KHUYẾN MÃI ]                           │
 │                                                          │
 │  > _Mức giảm cho phép vượt rào kịch trần là 50%_         │
 ╰──────────────────────────────────────────────────────────╯
```

---

## 🟢 US4: Báo cáo hiệu quả thời gian thực

### 1. Kiến trúc BCE
> **Giải thích:** Admin ngồi xem Dashboard, hệ thống phải liên tục móc qua Analytics nhẩm tỷ lệ. Đề phòng máy nhẩm lỗi nếu Cấu hình chưa được nhập từ trước.
```text
  [ BOUNDARY / GIAO DIỆN ]                ( CONTROL / XỬ LÝ )                 { ENTITY / LÕI NGHIỆP VỤ }
  
  ╔════════════════════╗               ┌────────────────────┐               ⟪──────────────────────⟫
  ║ TẤM NỀN CHARTIST   ║ = Yêu cầu ==> │ CỘNG TÁC VIÊN ĐỌC  │ === Chọc ===> │ MÁY QUÉT KPI NHÀ KHO │
  ║(DashboardBoundary) ║               │ (DashboardControl  │               │   (SaleAnalytics)    │
  ╚════════════════════╝ <== Đồ Thị == └────────────────────┘ <== Kết quả ==⟪──────────────────────⟫
```

### 2. Sơ đồ Kịch Bản (Sequence Diagram)
> **Giải thích:** Giao diện cần xin 2 số liệu từ máy lõi.
```mermaid
sequenceDiagram
    autonumber
    actor Admin
    participant UI as Bảng Dashboard
    participant Control as DashboardController
    participant Entity as Bộ Máy Thống Kê Toán Học

    Admin->>UI: Refresh lại trang nội bộ
    UI->>Control: Đòi ngay số liệu Tỉ Lệ Tiêu Thụ Hàng
    Control->>Entity: Cậu chạy hàm cho ra % Bán! (getPercentage)
    
    alt Dự án chưa nhập tổng kho / Lỗi chia cho Không (0)
        Entity-->>Control: Văng Exception Tới Tấp
        Control-->>UI: Cảnh báo "Chưa có thiết lập Tồn kho"
        UI-->>Admin: Che mờ biểu đồ 0%, rải khung đỏ cảnh báo
    else Đã có số hợp lệ
        Entity-->>Control: 80% Tổng Số Lượng
        Control->>Entity: Xin thêm Tổng Doanh Thu đính kèm
        Entity-->>Control: 50 triệu VNĐ
        Control-->>UI: Đóng gói (80%, 50M) gửi bảng hiển thị
        UI-->>Admin: Vẽ cái Chart Cột rất đẹp bằng JS
    end
```

### 3. Thiết kế Cấu trúc file & Màn hình hiển thị
```text
📁 CẤU TRÚC FOLDER:
 src/
  ╰─ main/java/.../
      ├─ boundary/     ->  DashboardBoundary.html (Biểu đồ)
      ├─ control/      ->  DashboardController.java (Tính toán đầu cuối)
      ╰─ entity/       ->  SaleAnalytics.java (Chứa các biến cộng dồn Total)

🖥️ MÀN HÌNH WIREFRAME:
 ╭──────────────────────────────────────────────────────────╮
 │  [ADMIN] Real-time Két Sắt Số Liệu (Live)                │
 ├──────────────────────────────────────────────────────────┤
 │                                                          │
 │   Tỷ lệ xả hàng (%):                                     │
 │   [██████████████████████             ] 80.0%            │
 │                                                          │
 │   Doanh thu đạt được đến trưa nay:                       │
 │   💰 50,000,000 VNĐ                                      │
 │                                                          │
 │   [ Cập Nhật Lại ]          Tình trạng: Mượt mà ✔        │
 ╰──────────────────────────────────────────────────────────╯
```

---

## 🟢 US5: Quản lý Sản phẩm và Combo Sale

### 1. Kiến trúc BCE
> **Giải thích:** Liên kết nhiều SP vào chung một khay mang tên là `Combo`.
```text
  [ BOUNDARY / GIAO DIỆN ]                ( CONTROL / XỬ LÝ )                 { ENTITY / LÕI NGHIỆP VỤ }
  
  ╔════════════════════╗               ┌────────────────────┐               ⟪──────────────────────⟫
  ║ GIAO DIỆN KẾT HỢP  ║ = Kéo thẻ ==> │ BAN QUẢN LÝ NHÂN SỰ│ === Nhét ===> │ LẴNG COMBO TỔNG HỢP  │
  ║  (ComboBoundary)   ║               │   (ComboManager)   │               │   (FlashSaleCombo)   │
  ╚════════════════════╝ <== Lưu OK == └────────────────────┘ <== Xét Duyệt=⟪──────────────────────⟫
```

### 2. Sơ đồ Kịch Bản (Sequence Diagram)
> **Giải thích:** Vòng lặp đệ quy trong hệ thống để lục soát, bắt lỗi nếu Admin bỏ sót nhét 1 sản phẩm hết hạn/rỗng vào giỏ Combo.
```mermaid
sequenceDiagram
    autonumber
    actor Admin
    participant UI as Bảng Tùy Chỉnh Combo
    participant Control as ComboManager
    participant Entity as Gói Combo Mới Build

    Admin->>UI: Đặt tên, thêm Son, thêm Nước Hoa, chọn chiết khấu!
    UI->>Control: Lập danh sách: Create("Mùa Hè", ds_mat_hang)
    Control->>Entity: Cố nhồi danh sách vào Class cấu trúc
    
    loop Xét qua từng mã hàng được thêm
        Entity->>Entity: Sản vật A còn ko?
        Entity->>Entity: Sản vật B còn ko?
    end
    
    alt Có Nước Hoa (Tồn Kho = 0)
        Entity-->>Control: Bắn ngay Lỗi: "Nước hoa hết sạch mà đòi gộp"
        Control-->>UI: Cancel quá trình đóng danh sách
        UI-->>Admin: Hất văng bảng đỏ chỉ điểm đúng Nước Hoa đã hết
    else Tất cả đều hợp lệ
        Entity-->>Control: Thành lập Box đóng gói Combo thành công
        Control-->>UI: Cho lưu Combo chung vào CSDL
        UI-->>Admin: Hiện túi thẻ Combo Mùa Hè ngay bảng chọn
    end
```

### 3. Thiết kế Cấu trúc file & Màn hình hiển thị
```text
📁 CẤU TRÚC FOLDER:
 src/
  ╰─ main/java/.../
      ├─ boundary/     ->  ComboBoundary.html (Tích ô chọn vật phẩm)
      ├─ control/      ->  ComboManager.java (Đẩy các Box vào Kho Quản trị)
      ╰─ entity/       ->  FlashSaleCombo.java (Nhét tất cả vào 1 Entity tổ)

🖥️ MÀN HÌNH WIREFRAME:
 ╭──────────────────────────────────────────────────────────╮
 │  [ADMIN] Tạo Hộp Nhóm - Combo Sale                       │
 ├──────────────────────────────────────────────────────────┤
 │ Tên Combo đặt: [ Combo Mùa Hè 2026 .............. ]      │
 │                                                          │
 │ Mặt hàng đem vào gói Group:                              │
 │ [x] Son MAC Ruby Woo (Tồn: 10 cái)                       │
 │ [x] Nước hoa Chanel (Tồn: 0 cái)  <-- Vô tình tích nhầm  │
 │                                                          │
 │ Trị giá cắt máu (% giảm hộp): [ 30 ]                     │
 │                                                          │
 │ [ TIẾN HÀNH ĐÓNG GÓI! ]                                  │
 │                                                          │
 │ 🚫 Lỗi kẹt kho: Thằng "Nước hoa Chanel" rỗng rồi sếp ơi! │
 ╰──────────────────────────────────────────────────────────╯
```
