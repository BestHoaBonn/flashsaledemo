## 🟢 US1: Hiển thị trạng thái Flash Sale trên sản phẩm

### 1. Kiến trúc BCE (Boundary - Control - Entity)
> **Giải thích:** Khách hàng (Boundary) không trực tiếp đòi dữ liệu từ hệ thống tính toán (Entity), mà bắt buộc phải thông qua Bộ lọc (Control) làm nhiệm vụ bảo vệ logic.
```text
  [ BOUNDARY / GIAO DIỆN ]                ( CONTROL / XỬ LÝ )                 { ENTITY / LÕI NGHIỆP VỤ }
  
  ╔════════════════════╗               ┌────────────────────┐               ⟪──────────────────────⟫
  ║ MÀN HÌNH MUA HÀNG  ║ = Lấy SP ===> │ TRÌNH QUẢN LÝ DANH │ == Truy vấn =>│ DỮ LIỆU CHIẾN DỊCH & │
  ║ (CustomerBoundary) ║               │ MỤC (ProductCatalog)│               │ MỨC GIẢM (Campaign)  │
  ╚════════════════════╝ <== Trả về == └────────────────────┘ <== Kết quả ==⟪──────────────────────⟫
   Là nơi khách xem, nhấn                Là nhân viên chạy bàn                Là nhà bếp nấu công thức
```

### 2. Sơ đồ Tuần Tự (Sequence Diagram)
> **Kỹ thuật:** Luồng xử lý lấy thông tin giá từ Layer Boundary qua Control tới Entity.

```mermaid
sequenceDiagram
    autonumber
    actor Customer
    participant Boundary as Giao diện Khách - CustomerBoundary (Layer: Boundary)
    participant Control as Trình quản lý Sản phẩm - ProductCatalog (Layer: Control)
    participant Entity as Dữ liệu Chiến dịch - FlashSaleCampaign (Layer: Entity)

    Customer->>Boundary: getDisplayedPrice(campaign, productName)
    Boundary->>Control: getProductPrice(productName)
    Control->>Entity: isActive(currentTime)
    
        Note right of Control: Luồng 1: Flash Sale Đang mở
        Entity-->>Control: trả về [true, discountPercent]
        Control-->>Boundary: calculatedPrice (Giá đã giảm)
        Boundary-->>Customer: Hiển thị Giá Sale & Đồng hồ

        Note right of Control: Luồng 2: Sale đã hết hạn
        Entity-->>Control: trả về [false]
        Control-->>Boundary: originalPrice (Giá gốc)
        Boundary-->>Customer: Hiển thị Giá gốc & Ẩn đồng hồ
```

**Chi tiết luồng dữ liệu (Data Flow):**
1. **Dữ liệu đầu vào (Input Artifacts):** Nhận `Campaign` (Đối tượng chiến dịch), thời gian hệ thống (`currentTime`) và tên sản phẩm.
2. **Tiến trình xử lý (Technical Processing):**
    * `CustomerBoundary` tiếp nhận yêu cầu từ người dùng, thực hiện ủy thác (delegation) việc xử lý dữ liệu cho `ProductCatalog`.
    * `ProductCatalog` (Control) đóng vai trò trung gian, phối hợp kiểm tra tính hiệu lực qua thực thể `FlashSaleCampaign`.
    * `FlashSaleCampaign` (Entity) thực hiện so khớp thời gian (Temporal matching) để xác định xem `currentTime` có nằm trong cửa sổ Sale hay không.
3. **Đặc tính kỹ thuật (Technical Specs):**
    * **Decoupling**: Logic hiển thị tách biệt hoàn toàn khỏi logic nghiệp vụ, đảm bảo dễ dàng thay đổi giao diện mà không ảnh hưởng tới cách tính thời gian.
    * **Temporal Validation**: Hệ thống sử dụng thời gian của máy chủ (Server-side) để ngăn chặn việc gian lận thời gian từ phía Client.
4. **Kết quả trả về (Output):** Trả về giá đã được tính toán (Calculated Price) kèm theo cờ trạng thái (`isActive`) để lớp Boundary quyết định việc render đồng hồ đếm ngược.

### 3. Thiết kế Cấu trúc file & Màn hình hiển thị
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

### 2. Sơ đồ Tuần Tự (Sequence Diagram)
> **Kỹ thuật:** Luồng xử lý đặt hàng và trừ tồn kho an toàn (Thread-safe logic).

```mermaid
sequenceDiagram
    autonumber
    actor Customer
    participant Boundary as Giao diện Khách - CustomerBoundary (Layer: Boundary)
    participant Control as Điều phối Đơn hàng - OrderCheckout (Layer: Control)
    participant Entity as Bộ trữ Tồn kho - FlashSaleInventory (Layer: Entity)

    Customer->>Boundary: checkout(inventory, quantity)
    Boundary->>Control: processOrder(inventory, quantity)
    Control->>Entity: holdInventory(quantity)
    
        Note right of Control: Luồng 1: Còn hàng (Happy Path)
        Entity-->>Control: return true (Phép trừ Atomic)
        Control-->>Boundary: orderSuccess: true
        Boundary-->>Customer: Hiển thị "Thành công" & Hóa đơn

        Note right of Control: Luồng 2: Hết hàng (Unhappy Path)
        Entity-->>Control: throw IllegalStateException
        Control-->>Boundary: orderSuccess: false
        Boundary-->>Customer: Hiển thị Cảnh báo "Hết hàng"
```

**Chi tiết luồng dữ liệu (Data Flow):**
1. **Dữ liệu đầu vào (Input Artifacts):** `requestedQuantity` (Số lượng sản phẩm khách muốn mua) và Thực thể kho `FlashSaleInventory`.
2. **Tiến trình xử lý (Technical Processing):**
    * `OrderCheckout` (Control) triệu hồi phương thức `holdInventory` tại lớp Entity.
    * Thực thể kho kiểm tra xác thực số dư khả dụng (`availableQuantity >= quantity`). 
    * Nếu thỏa mãn, thực hiện biến đổi trạng thái (State mutation) bằng cách trừ trực tiếp vào bộ nhớ.
3. **Đặc tính kỹ thuật (Technical Specs):**
    * **Thread-safety**: Sử dụng cơ chế khóa đồng bộ (`synchronized`) đảm bảo tính nguyên tử (Atomicity), ngăn chặn tình trạng thất thoát kho hoặc bán quá mức (Overselling) khi có hàng nghìn người cùng mua một lúc.
    * **Fail-fast Logic**: Nếu kho không đủ, hệ thống ném `IllegalStateException` ngay lập tức để ngắt tiến trình (Interrupt process), đảm bảo không tạo đơn hàng khống.
4. **Đầu ra (Output):** Trả về trạng thái giao dịch thành công (Boolean) và cập nhật số dư kho thực tế trên RAM.

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

### 2. Sơ đồ Tuần Tự (Sequence Diagram)
> **Kỹ thuật:** Luồng nghiệp vụ cấu hình chiến dịch mới kèm theo các ràng buộc bảo vệ biên lợi nhuận.

```mermaid
sequenceDiagram
    autonumber
    actor Admin
    participant Boundary as Giao diện Admin - AdminBoundary (Layer: Boundary)
    participant Control as Trình ghi Chiến dịch - CampaignManager (Layer: Control)
    participant Entity as Lõi quy định - FlashSaleCampaign (Layer: Entity)

    Admin->>Boundary: scheduleCampaign(start, end, discount)
    Boundary->>Control: createCampaign(start, end, discount)
    Control->>Entity: new FlashSaleCampaign(start, end, discount)
    
        Note right of Entity: Kiểm tra điều kiện ràng buộc
        alt discountPercent > 50% OR startTime >= endTime
            Entity-->>Control: ném IllegalArgumentException
            Control-->>Boundary: error: Thông báo lỗi chi tiết
            Boundary-->>Admin: Hiển thị Tooltip cảnh báo
        end

        Note right of Entity: Khi dữ liệu hợp lệ
        Entity-->>Control: Đối tượng được khởi tạo thành công
        Control-->>Boundary: success: Đã lên lịch
        Boundary-->>Admin: Hiển thị "Thành công"
```

**Chi tiết luồng dữ liệu (Data Flow):**
1. **Dữ liệu đầu vào (Input Artifacts):** `startTime`, `endTime` (Dạng LocalDateTime) và mức giảm giá `discountPercent`.
2. **Tiến trình xử lý (Technical Processing):**
    * `CampaignManager` (Control) đóng vai trò điều phối, khởi tạo thực thể `FlashSaleCampaign` mới.
    * Toàn bộ quy tắc kiểm soát biên lợi nhuận được thực thi ngay tại tầng khởi tạo (Constructor validation).
3. **Đặc tính kỹ thuật (Technical Specs):**
    * **Constructor Guard**: Ngăn chặn việc tạo ra các thực thể "không hợp lệ" ngay từ giai đoạn cấp phát vùng nhớ (ví dụ: Sale > 50% hoặc thời gian bắt đầu sau thời gian kết thúc).
    * **Business Rule Enforcement**: Chuyển đổi quy định hành chính sang quy định cứng trong mã nguồn (Hard-coded constraints), giảm thiểu sai sót do con người.
4. **Kết quả (Result):** Một thực thể chiến dịch hợp lệ được nạp vào danh sách quản lý, sẵn sàng hoạt động ngay khi tới giờ.

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
  ║(DashboardBoundary) ║               │ (DashboardControl)  │               │   (SaleAnalytics)    │
  ╚════════════════════╝ <== Đồ Thị == └────────────────────┘ <== Kết quả ==⟪──────────────────────⟫
```

### 2. Sơ đồ Tuần Tự (Sequence Diagram)
> **Kỹ thuật:** Luồng truy vấn báo cáo thời gian thực từ Entity Sales.

```mermaid
sequenceDiagram
    autonumber
    actor Admin
    participant Boundary as Giao diện Admin - AdminBoundary (Layer: Boundary)
    participant Control as Bộ máy Thống kê - DashboardController (Layer: Control)
    participant Entity as Máy quét số liệu - SaleAnalytics (Layer: Entity)

    Admin->>Boundary: getAnalyticsReport(analytics)
    Boundary->>Control: calculateReport(analytics)
    Control->>Entity: getSoldPercentage()
    
        Note right of Entity: Phòng thủ "Lỗi chia cho 0"
        alt initialTotalProduct == 0
            Entity-->>Control: throw IllegalStateException
            Control-->>Boundary: error: Thiếu cấu hình gốc
            Boundary-->>Admin: Cảnh báo "Cần nhập tồn kho ban đầu"
        end

        Note right of Entity: Dữ liệu hợp lệ
        Entity-->>Control: trả về soldPercentage (double)
        Control->>Entity: gọi getTotalRevenue()
        Entity-->>Control: trả về totalRevenue (double)
        Control-->>Boundary: mảng reportData [perc, rev]
        Boundary-->>Admin: Vẽ biểu đồ & Render KPI
```

**Chi tiết luồng dữ liệu (Data Flow):**
1. **Dữ liệu đầu vào (Input Artifacts):** Thực thể `SaleAnalytics` chứa tổng doanh thu lũy kế và số lượng hàng đã tiêu thụ.
2. **Tiến trình xử lý (Technical Processing):**
    * `DashboardController` (Control) truy xuất các trạng thái số liệu hiện hành từ Entity.
    * Thực hiện các phép toán tổng hợp (Aggregation) và chuyển đổi dữ liệu sang định dạng báo cáo.
3. **Đặc tính kỹ thuật (Technical Specs):**
    * **Division-by-Zero Protection**: Kiểm soát lỗi số học tại tầng Entity; nếu chưa cấu hình số lượng tổng ban đầu, hệ thống sẽ trả về 0% và cảnh báo thay vì gây sập ứng dụng.
    * **Real-time Observability**: Dữ liệu được tính toán tức thì từ dữ liệu thực trong RAM, đảm bảo Dashboard luôn phản ánh chính xác tình hình kinh doanh từng giây.
4. **Kết quả (Result):** Một cấu trúc dữ liệu báo cáo (Array/Object) chứa KPI Doanh thu và Tỷ lệ sẵn sàng để đẩy lên lớp giao diện.

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

### 2. Sơ đồ Tuần Tự (Sequence Diagram)
> **Kỹ thuật:** Luồng nghiệp vụ đóng gói Combo đa sản phẩm kèm bước kiểm tra tồn kho thành phần (validateStock).

```mermaid
sequenceDiagram
    autonumber
    actor Admin
    participant Boundary as Giao diện Admin - AdminBoundary (Layer: Boundary)
    participant Control as Ban quản lý Combo - ComboManager (Layer: Control)
    participant Entity as Gói Combo thực thể - FlashSaleCombo (Layer: Entity)

    Admin->>Boundary: createCombo(name, products, price, discount)
    Boundary->>Control: createCombo(name, products, price, discount)
    Control->>Entity: new FlashSaleCombo(name, products, price, discount)
    
    loop Kiểm tra từng sản phẩm (Validate Each)
        Entity->>Entity: verify product.availableQuantity > 0
    end
    
        Note right of Entity: Phát hiện SP hết hàng
        alt Component: Out of Stock
            Entity-->>Control: ném IllegalStateException
            Control-->>Boundary: error: SP thành phần lỗi
            Boundary-->>Admin: Đánh dấu đỏ SP lỗi trên giao diện
        end

        Note right of Entity: Tất cả SP hợp lệ
        Entity-->>Control: trả về đối tượng Combo
        Control-->>Boundary: success: Đã thêm vào danh sách
        Boundary-->>Admin: Hiển thị Combo trong Danh mục Sale
```

**Chi tiết luồng dữ liệu (Data Flow):**
1. **Dữ liệu đầu vào (Input Artifacts):** Tập hợp các thực thể kho (`List<FlashSaleInventory>`) và thông tin cấu hình giá/giảm giá.
2. **Tiến trình xử lý (Technical Processing):**
    * `ComboManager` điều phối việc gộp nhóm các thực thể đơn lẻ thành một thực thể phức hợp `FlashSaleCombo`.
    * Thực hiện vòng lặp kiểm tra tính khả dụng của toàn bộ danh sách thành phần trước khi cấp phép tạo Combo.
3. **Đặc tính kỹ thuật (Technical Specs):**
    * **Composite Logic**: Số lượng Combo khả dụng tuân theo nguyên tắc "Mắt xích yếu nhất" - được tính toán dựa trên mức tồn thấp nhất trong danh sách linh kiện.
    * **All-or-Nothing Validation**: Chỉ cần một sản phẩm thành phần hết hàng, toàn bộ quy trình tạo Combo sẽ bị hủy bỏ để đảm bảo tính nhất quán của gói hàng.
4. **Kết quả (Result):** Một thực thể Combo đa thành phần được đăng ký thành công vào danh sách quản lý.

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

---

## 🟢 US6: Quản lý Sản phẩm Flash Sale

### 1. Kiến trúc BCE
> **Giải thích:** Quản lý thao tác danh sách sản phẩm, Controller gửi yêu cầu tới Lõi quản lý sản phẩm để cập nhật danh mục khuyến mãi.
```text
  [ BOUNDARY / GIAO DIỆN ]                ( CONTROL / XỬ LÝ )                 { ENTITY / LÕI NGHIỆP VỤ }
  
  ╔════════════════════╗               ┌────────────────────┐               ⟪──────────────────────⟫
  ║ GIAO DIỆN QUẢN LÝ  ║ = Thêm/Xóa ==> │  BỘ ĐIỀU PHỐI SP   │ === Cập nhật =>│ DANH SÁCH SẢN PHẨM   │
  ║ (AdminBoundary)    ║               │ (ProductManager)   │               │ (FlashSaleProductList)│
  ╚════════════════════╝ <== Kết quả == └────────────────────┘ <== Xác nhận =⟪──────────────────────⟫
```

### 2. Sơ đồ Tuần Tự (Sequence Diagram)
> **Kỹ thuật:** Luồng xử lý thêm sản phẩm vào danh mục sale hiện hành.

```mermaid
sequenceDiagram
    autonumber
    actor Admin
    participant Boundary as Giao diện Admin - AdminBoundary (Layer: Boundary)
    participant Control as Bộ điều phối - ProductManagerController (Layer: Control)
    participant Entity as Lõi danh sách - FlashSaleProductList (Layer: Entity)

    Admin->>Boundary: addFlashSaleProduct(id, price, qty)
    Boundary->>Control: addProductToCampaign(id, price, qty)
    Control->>Entity: addProduct(id, price, qty)
    
        Note right of Entity: Kiểm tra trùng lặp
        alt Sản phẩm đã tồn tại
            Entity-->>Control: throw IllegalArgumentException
            Control-->>Boundary: error: Sản phẩm đã tồn tại
            Boundary-->>Admin: Hiển thị thông báo lỗi
        else Sản phẩm mới
            Entity-->>Control: success
            Control-->>Boundary: success
            Boundary-->>Admin: Cập nhật bảng sản phẩm
        end
```

**Chi tiết luồng dữ liệu (Data Flow):**
1. **Dữ liệu đầu vào (Input Artifacts):** ID Sản phẩm, Giá Sale, và Giới hạn Tồn kho.
2. **Tiến trình xử lý (Technical Processing):**
    * `AdminBoundary` gửi yêu cầu cho `ProductManagerController`.
    * Controller uỷ nhiệm cho Entity `FlashSaleProductList` để thêm đối tượng.
    * Entity kiểm tra tồn tại (Unique Constraint Checks) trước khi thêm vào mảng.
3. **Đặc tính kỹ thuật (Technical Specs):**
    * **Data Integrity**: Ngăn chặn tình trạng trùng lặp sản phẩm trong một Campaign để tránh lỗi trừ kho và hiển thị kép.
4. **Kết quả (Result):** Sản phẩm được đưa lên quầy (hoặc ném lỗi cảnh báo).

### 3. Thiết kế Cấu trúc file & Màn hình hiển thị
```text
📁 CẤU TRÚC FOLDER:
 src/
  ╰─ main/java/.../
      ├─ boundary/     ->  AdminBoundary.html (Giao diện Quản lý SP)
      ├─ control/      ->  ProductManagerController.java (Đóng vai trò điều hướng)
      ╰─ entity/       ->  FlashSaleProductList.java (Danh sách quản lý cốt lõi)

🖥️ MÀN HÌNH WIREFRAME:
 ╭──────────────────────────────────────────────────────────╮
 │  [ADMIN] Quản lý Sản phẩm Flash Sale                     │
 ├──────────────────────────────────────────────────────────┤
 │                                                          │
 │  Chọn sản phẩm:  [ Son Dior Lip Glow ▾ ]                 │
 │  Giá sau Sale:   [ 800.000 ] VNĐ                         │
 │  Số lượng giới hạn:[ 50 ] Suất                           │
 │                                                          │
 │  [ + THÊM VÀO CHIẾN DỊCH ]                               │
 │                                                          │
 │  DANH SÁCH HIỆN CÓ:                                      │
 │  1. Son MAC (Giá sale: 520k - Tồn: 10)  [Xóa]            │
 │  2. Kem Chống Nắng (Giá sale: 200k)     [Xóa]            │
 ╰──────────────────────────────────────────────────────────╯
```

---

## 🟢 US7: Phân loại danh mục

### 1. Kiến trúc BCE
> **Giải thích:** Khách hàng chọn bộ lọc, Control truy vấn danh mục từ Entity và trả về danh sách đã được lọc.
```text
  [ BOUNDARY / GIAO DIỆN ]                ( CONTROL / XỬ LÝ )                 { ENTITY / LÕI NGHIỆP VỤ }
  
  ╔════════════════════╗               ┌────────────────────┐               ⟪──────────────────────⟫
  ║ TRANG CHỦ / BỘ LỌC ║ = Chọn DM ===> │ BỘ LỌC DANH MỤC    │ === Truy vấn =>│ DANH SÁCH DANH MỤC   │
  ║ (CustomerBoundary) ║               │  (ProductCatalog)  │               │  (ProductCategory)   │
  ╚════════════════════╝ <== List SP == └────────────────────┘ <== Dữ liệu ==⟪──────────────────────⟫
```

### 2. Sơ đồ Tuần Tự (Sequence Diagram)
> **Kỹ thuật:** Luồng truy vấn sản phẩm theo tags danh mục và cơ chế fallback gợi ý.

```mermaid
sequenceDiagram
    autonumber
    actor Customer
    participant Boundary as Giao diện Khách - CustomerBoundary (Layer: Boundary)
    participant Control as Trình quản lý SP - ProductCatalog (Layer: Control)
    participant Entity as Lõi Phân loại - ProductCategory (Layer: Entity)

    Customer->>Boundary: filterByCategory(categoryName)
    Boundary->>Control: getProductsByCategory(categoryName)
    Control->>Entity: filter(categoryName)
    Entity-->>Control: List<Products>
    
    alt Danh sách rỗng
        Control->>Entity: getBestSellers()
        Entity-->>Control: List<BestSelling>
        Control-->>Boundary: suggest(BestSelling)
        Boundary-->>Customer: Hiển thị gợi ý "Sản phẩm Bán Chạy"
    else Có sản phẩm
        Control-->>Boundary: returnedProducts
        Boundary-->>Customer: Hiển thị danh sách SP theo Mục
    end
```

**Chi tiết luồng dữ liệu (Data Flow):**
1. **Dữ liệu đầu vào (Input Artifacts):** Tên/ID Danh mục mà khách muốn lọc (`categoryName`).
2. **Tiến trình xử lý (Technical Processing):**
    * Request từ Bộ lọc UI tiến thẳng tới `ProductCatalog`.
    * Control tiến hành yêu cầu Entity `ProductCategory` trả về các món thỏa mãn điều kiện.
    * Gặp Fallback logic: Nếu rỗng, yêu cầu Entity trả lại danh sách thay thế.
3. **Đặc tính kỹ thuật (Technical Specs):**
    * **Fallback Mechanism (Cơ chế Phòng hờ)**: Thuật toán không bao giờ trả về màn hình trắng tinh, gây trải nghiệm xấu. Áp dụng Fallback để đẩy SP bán chạy ra hỗ trợ Sale.
4. **Kết quả (Result):** Một mảng sản phẩm tương ứng, kèm theo chỉ báo UI thích hợp.

### 3. Thiết kế Cấu trúc file & Màn hình hiển thị
```text
📁 CẤU TRÚC FOLDER:
 src/
  ╰─ main/java/.../
      ├─ boundary/     ->  CustomerBoundary.html (Các Tab Danh mục)
      ├─ control/      ->  ProductCatalog.java (Điều khiển lọc Filter)
      ╰─ entity/       ->  ProductCategory.java (Logic nhận diện danh mục)

🖥️ MÀN HÌNH WIREFRAME:
 ╭──────────────────────────────────────────────────────────╮
 │  [🛍️] TRANG CHỦ MUA SẮM                                  │
 ├──────────────────────────────────────────────────────────┤
 │                                                          │
 │  Bộ Lọc: [Tất Cả] [Son Môi] [Nước Hoa] [Kem Dưỡng]       │
 │                                                          │
 │  ╔══════════════════════════════════════════════════╗    │
 │  ║ ⚠️ Danh mục Nước Hoa hiện đang trống.             ║    │
 │  ║ 💡 Nhưng bạn có thể xem các Sản Phẩm Bán Chạy 👇  ║    │
 │  ╚══════════════════════════════════════════════════╝    │
 │                                                          │
 │  [ SP Bán Chạy 1 ]   [ SP Bán Chạy 2 ]   [ SP Bán Chạy 3]│
 ╰──────────────────────────────────────────────────────────╯
```

---

## 🟢 US8: Lịch sử đơn hàng và Thống kê tiết kiệm

### 1. Kiến trúc BCE
> **Giải thích:** Khách hàng xem lại các giao dịch đã thực hiện trong quá khứ và tổng kết tiền lời.
```text
  [ BOUNDARY / GIAO DIỆN ]                ( CONTROL / XỬ LÝ )                 { ENTITY / LÕI NGHIỆP VỤ }
  
  ╔════════════════════╗               ┌────────────────────┐               ⟪──────────────────────⟫
  ║ MỤC LỊCH SỬ MUA    ║ = Yêu cầu ===> │ BỘ QUẢN LÝ ĐƠN     │ === Truy vấn =>│ KHO ĐƠN HÀNG ĐÃ LƯU  │
  ║ (CustomerBoundary) ║               │(OrderHistoryManager)│               │    (OrderHistory)    │
  ╚════════════════════╝ <== Chi tiết = └────────────────────┘ <== Dữ liệu ==⟪──────────────────────⟫
```

### 2. Sơ đồ Tuần Tự (Sequence Diagram)
> **Kỹ thuật:** Luồng tính toán tiền tiết kiệm (Saved Amount) cho người dùng.

```mermaid
sequenceDiagram
    autonumber
    actor Customer
    participant Boundary as Giao diện Khách - CustomerBoundary (Layer: Boundary)
    participant Control as Quản lý đơn - OrderHistoryManager (Layer: Control)
    participant Entity as Lõi dữ liệu - OrderHistory (Layer: Entity)

    Customer->>Boundary: viewOrderHistory()
    Boundary->>Control: getCustomerOrders()
    Control->>Entity: fetchOrders()
    Entity-->>Control: List<Orders>
    
    alt Không có đơn hàng
        Control-->>Boundary: emptyList
        Boundary-->>Customer: Hiển thị "Bạn chưa mua gì"
    else Có đơn hàng
        Control->>Entity: calculateTotalSavings()
        Entity-->>Control: totalSavedAmount (VND)
        Control-->>Boundary: List<Orders> + totalSavedAmount
        Boundary-->>Customer: Render bảng lịch sử & Nút khoe "Tiết kiệm"
    end
```

**Chi tiết luồng dữ liệu (Data Flow):**
1. **Dữ liệu đầu vào (Input Artifacts):** Yêu cầu xem lịch sử.
2. **Tiến trình xử lý (Technical Processing):**
    * Request từ Khách hàng vào Control `OrderHistoryManager`.
    * Control yêu cầu Entity cung cấp danh sách.
    * Control gọi hàm đặc biệt `calculateTotalSavings` để tính chênh lệch giá Gốc - giá Sale của mọi đơn.
3. **Đặc tính kỹ thuật (Technical Specs):**
    * **Gamification Logic**: Việc tính tiền tiết kiệm đóng vai trò giữ chân người dùng (Gamification) - làm họ thấy "Lời" khi mua sắm. Logic này xử lý nhanh trong RAM.
4. **Kết quả (Result):** Danh sách hóa đơn + Tổng số tiền hời.

### 3. Thiết kế Cấu trúc file & Màn hình hiển thị
```text
📁 CẤU TRÚC FOLDER:
 src/
  ╰─ main/java/.../
      ├─ boundary/     ->  CustomerBoundary.html (Giao diện lịch sử)
      ├─ control/      ->  OrderHistoryManager.java (Đọc đơn Hàng)
      ╰─ entity/       ->  OrderHistory.java (Biên lai lưu trữ)

🖥️ MÀN HÌNH WIREFRAME:
 ╭──────────────────────────────────────────────────────────╮
 │  📋 LỊCH SỬ ĐƠN HÀNG CỦA BẠN                             │
 ├──────────────────────────────────────────────────────────┤
 │                                                          │
 │  1. Son MAC Ruby Woo (2 món x 520.000) = 1.040.000       │
 │  2. Kem chống nắng (1 món x 200.000)   = 200.000         │
 │                                                          │
 │  --------------------------------------------------      │
 │  💰 TỔNG SỐ TIỀN BẠN ĐÃ TIẾT KIỆM ĐƯỢC NHỜ FLASH SALE:   │
 │                                                          │
 │                 [ 🔥 560.000 VNĐ 🔥 ]                    │
 │                                                          │
 ╰──────────────────────────────────────────────────────────╯
```
