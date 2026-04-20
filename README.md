# TÀI LIỆU YÊU CẦU: TÍNH NĂNG FLASH SALE TOÀN DIỆN (BA + BDD)

## 1. Giới thiệu (Mục tiêu Nghiệp vụ)
Tính năng Flash Sale tạo ra sự khan hiếm chiến lược để thúc đẩy chốt đơn nhanh.
Hệ thống được xây dựng theo phương pháp Cắt dọc (Vertical Slicing), tuân thủ nghiêm ngặt mô hình kiến trúc **BCE (Boundary - Control - Entity)** và sử dụng **JSON Database** để quản lý dữ liệu khởi tạo.

---

## 2. Kiến trúc & Cơ sở dữ liệu

### Mô hình BCE (Boundary - Control - Entity)
Để đảm bảo tính linh hoạt và dễ kiểm thử, dự án được phân lớp rõ ràng:
*   **Boundary (Lớp Biên)**: `CustomerBoundary`, `AdminBoundary`. Đóng vai trò là "cánh cửa" tiếp nhận yêu cầu từ Giao diện (UI) hoặc các bộ kiểm thử (Tests).
*   **Control (Lớp Điều khiển)**: `CampaignManager`, `ProductCatalog`,... Chứa logic điều phối và xử lý quy trình nghiệp vụ.
*   **Entity (Lớp Thực thể)**: `FlashSaleCampaign`, `FlashSaleInventory`,... Chứa các quy tắc nghiệp vụ cốt lõi và dữ liệu gốc.

### Cơ sở dữ liệu JSON (In-Memory Reset)
Hệ thống sử dụng file [initial_data.json] làm nguồn dữ liệu gốc:
*   **Nạp dữ liệu**: Tự động nạp sản phẩm và chiến dịch mẫu khi khởi động.
*   **Cơ chế Reset**: Mọi thay đổi trong quá trình chạy (mua hàng, tạo sale) chỉ lưu trên RAM. Khi khởi động lại, hệ thống sẽ tự động reset về trạng thái gốc trong file JSON.

---

## 3. Chi tiết Use Cases & User Stories

### US1: Hiển thị trạng thái Flash Sale trên sản phẩm
**User Story**: Là khách hàng, tôi muốn thấy giá ưu đãi, số tiền tiết kiệm được và đồng hồ đếm ngược trên trang sản phẩm để ra quyết định mua hàng nhanh hơn.

- **Đảm bảo tính INVEST**:
  - **I (Independent)**: Tách biệt hoàn toàn với logic thanh toán; có thể chạy độc lập để thu hút khách.
  - **N (Negotiable)**: Cách hiển thị đồng hồ (giờ:phút hay giây) có thể thảo luận thêm.
  - **V (Valuable)**: Tạo sự khan hiếm và thúc đẩy tâm lý mua hàng (FOMO).
  - **E (Estimable)**: Dễ dàng ước lượng dựa trên việc so sánh thời gian hệ thống.
  - **S (Small)**: Chỉ tập trung vào trạng thái và hiển thị thông tin.
  - **T (Testable)**: Kiểm thử được bằng cách giả lập các mốc thời gian khác nhau.

**Quy tắc Nghiệp vụ (Business Rules):**
*   Hệ thống chỉ hiển thị các thông tin Flash Sale khi chiến dịch đang ở trạng thái hiệu lực (`Active`).
*   Dữ liệu đếm ngược phải được đồng bộ với thời gian thực của máy chủ (Server Time).

**Logic xử lý (Business Logic):**
*   Hệ thống đóng vai trò "người canh gác thời gian", liên tục đối soát đồng hồ thực tế với lịch trình đã cài đặt. Chỉ khi đồng hồ nằm trong khung giờ quy định, chế độ Flash Sale mới được kích hoạt.
*   Ngay khi hết giờ, hệ thống tự động gỡ bỏ đồng hồ đếm ngược và trả giá sản phẩm về mức bình thường mà không cần con người can thiệp thủ công.

- **Acceptance Criteria (Tiêu chí chấp nhận) & Các Paths**:
  - **Scenario 1.1 (Happy Path) - Xem sản phẩm trong giờ Flash Sale**
    - **Given**: Chiến dịch "Siêu Sale" đang diễn ra, kết thúc lúc 23:59.
    - **When**: Khách hàng xem sản phẩm "Son MAC Ruby Woo".
    - **Then**: Hệ thống hiển thị giá Flash Sale "1.000.000 VNĐ", nhãn "Tiết kiệm 500.000 VNĐ" và đồng hồ đếm ngược đến 23:59.
  - **Scenario 1.2 (Unhappy Path) - Xem sản phẩm khi chiến dịch đã kết thúc**
    - **Given**: Chiến dịch "Siêu Sale" đã kết thúc.
    - **When**: Khách hàng xem sản phẩm "Son MAC Ruby Woo".
    - **Then**: Hệ thống hiển thị giá gốc "1.500.000 VNĐ" và không có đồng hồ đếm ngược.
  - **Scenario 1.3 (Unhappy Path) - Xem sản phẩm khi chiến dịch chưa bắt đầu (Coming Soon)**
    - **Given**: Chiến dịch "Sale Hè" bắt đầu vào ngày mai.
    - **When**: Khách hàng xem sản phẩm.
    - **Then**: Hệ thống hiển thị giá gốc và nhãn "Sắp diễn ra", không cho phép áp dụng giá Sale.



---

### US2: Xử lý tồn kho và thanh toán
**User Story**: Là khách hàng, tôi muốn hệ thống chốt đúng giá ưu đãi nếu tôi mua trong giới hạn số lượng cho phép, để đảm bảo tính công bằng.

- **Đảm bảo tính INVEST**:
  - **I (Independent)**: Logic trừ kho không phụ thuộc vào việc giao diện hiển thị như thế nào.
  - **N (Negotiable)**: Quy trình xử lý khi hết hàng đột ngột (Back-order hay Cancel) có thể linh hoạt.
  - **V (Valuable)**: Đảm bảo uy tín thương hiệu thông qua tính chính xác của tồn kho.
  - **E (Estimable)**: Xử lý dựa trên cơ chế đồng bộ hóa (Synchronized) tiêu chuẩn.
  - **S (Small)**: Tập trung duy nhất vào tính toàn vẹn của con số tồn kho.
  - **T (Testable)**: Kiểm thử được thông qua các kịch bản mua đồng thời (Concurrency).

**Quy tắc Nghiệp vụ (Business Rules):**
*   Việc kiểm tra và trừ tồn kho Flash Sale phải được thực hiện đồng bộ (Synchronized) để tránh tình trạng bán quá mức (Overselling).
*   Nếu tồn kho Flash Sale về 0, hệ thống phải tự động chuyển hướng người dùng về giá gốc của sản phẩm ngay tại bước thanh toán.

**Logic xử lý (Business Logic):**
*   Để tránh tình trạng "bán lố" khi có quá nhiều người cùng mua một lúc, hệ thống buộc các đơn hàng phải xếp hàng chờ xử lý tuần tự. Mỗi món hàng chỉ được trừ đi khi chắc chắn vẫn còn chỗ trong kho.
*   Nếu khách hàng đang thao tác mà hàng bất ngờ hết, hệ thống sẽ ngay lập tức thông báo và chuyển mức giá về giá gốc để đảm bảo tính minh bạch, tránh việc khách hàng mua hớ.

- **Acceptance Criteria (Tiêu chí chấp nhận) & Các Paths**:
  - **Scenario 2.1 (Happy Path) - Thanh toán hợp lệ**
    - **Given**: Kho Flash Sale còn "10" sản phẩm.
    - **When**: Khách hàng thêm "1" sản phẩm và thanh toán.
    - **Then**: Đơn hàng được tạo với giá Flash Sale và kho giảm còn "9".
  - **Scenario 2.2 (Unhappy Path) - Thanh toán khi hết kho khuyến mãi**
    - **Given**: Khách hàng đang ở trang thanh toán nhưng kho Flash Sale vừa về "0".
    - **When**: Khách nhấn "Xác nhận đặt hàng".
    - **Then**: Hệ thống báo lỗi "Sản phẩm đã hết suất Flash Sale" và cập nhật giỏ hàng về giá gốc.
  - **Scenario 2.3 (Unhappy Path) - Thanh toán với số lượng không hợp lệ**
    - **Given**: Khách hàng nhập số lượng mua là "0" hoặc số âm.
    - **When**: Khách nhấn "Thanh toán".
    - **Then**: Hệ thống chặn giao dịch và yêu cầu "Số lượng mua phải lớn hơn 0".



---

### US3: Thiết lập chiến dịch (Dành cho Quản lý)
**User Story**: Là Quản lý cửa hàng, tôi muốn lên lịch Ngày & Giờ và cấu hình mức giảm giá cho chiến dịch để hệ thống tự động chạy.

- **Đảm bảo tính INVEST**:
  - **I (Independent)**: Tính năng thiết lập cho Admin không ảnh hưởng đến luồng mua sắm hiện tại.
  - **N (Negotiable)**: Các ràng buộc (50%) có thể thay đổi tùy theo quy định của phòng Marketing.
  - **V (Valuable)**: Giảm sai sót thủ công và tiết kiệm thời gian vận hành cho Manager.
  - **E (Estimable)**: Sử dụng các form nhập liệu và validation cơ bản.
  - **S (Small)**: Chỉ quản lý vòng đời của một bản ghi chiến dịch.
  - **T (Testable)**: Kiểm thử qua Unit Test cho các điều kiện biên của dữ liệu đầu vào.

**Quy tắc Nghiệp vụ (Business Rules):**
*   Mức giảm giá tối đa cho phép là **50%** (Constraint theo quy định kinh doanh).
*   Thời gian bắt đầu (Start Time) phải luôn nhỏ hơn thời gian kết thúc (End Time).

**Logic xử lý (Business Logic):**
*   Hệ thống đóng vai trò một "bộ lọc thông minh". Nó sẽ từ chối ngay lập tức mọi yêu cầu tạo chiến dịch nếu phát hiện sai sót (như giảm giá quá sâu trên 50% hoặc thời gian bắt đầu muộn hơn thời gian kết thúc).
*   Cơ chế này đảm bảo dữ liệu của cửa hàng luôn sạch và không bao giờ xảy ra tình trạng hiển thị sai thông tin gây hiểu lầm cho khách hàng.

- **Acceptance Criteria (Tiêu chí chấp nhận) & Các Paths**:
  - **Scenario 3.1 (Happy Path) - Tạo chiến dịch hợp lệ**
    - **Given**: Quản lý đang ở form tạo chiến dịch.
    - **When**: Nhập ngày giờ bắt đầu "2026-04-20T08:00", kết thúc "2026-04-20T12:00" và giá giảm "20%", rồi nhấn "Lưu".
    - **Then**: Hệ thống lưu thành công và báo "Chiến dịch đã được lên lịch".
  - **Scenario 3.2 (Unhappy Path) - Mức giảm giá vượt quá biên lợi nhuận**
    - **Given**: Chính sách giới hạn giảm tối đa "50%".
    - **When**: Quản lý nhập mức giảm "60%" và nhấn "Lưu".
    - **Then**: Hệ thống chặn lưu và báo lỗi "Mức giảm không được vượt quá 50%".
  - **Scenario 3.3 (Unhappy Path) - Thiết lập thời gian sai (Ngược)**
    - **Given**: Quản lý nhập thời gian bắt đầu 2026-04-20T12:00", kết thúc "2026-04-20T08:00".
    - **When**: Nhấn "Lưu".
    - **Then**: Hệ thống báo lỗi "Thời gian kết thúc phải sau thời gian bắt đầu".



---

### US4: Báo cáo hiệu quả thời gian thực (Dành cho Quản lý)
**User Story**: Là Quản lý cửa hàng, tôi muốn xem doanh thu và tỷ lệ bán ra theo thời gian thực để quyết định bổ sung hàng hoặc thay đổi chiến lược marketing.

- **Đảm bảo tính INVEST**:
  - **I (Independent)**: Báo cáo chỉ đọc dữ liệu, không làm thay đổi trạng thái của đơn hàng hay kho.
  - **N (Negotiable)**: Hình thức biểu đồ (tròn, cột hay bảng) có thể tùy chọn thêm.
  - **V (Valuable)**: Cung cấp dữ liệu để ra quyết định kinh doanh ngay trong phiên sale.
  - **E (Estimable)**: Dựa trên các công thức toán học tính tổng và tỷ lệ đơn giản.
  - **S (Small)**: Gói gọn trong việc tổng hợp và hiển thị KPI.
  - **T (Testable)**: Kiểm soát được bằng cách đối soát con số tính toán với dữ liệu mẫu.

**Quy tắc Nghiệp vụ (Business Rules):**
*   Dữ liệu doanh thu và tỷ lệ bán ra phải được cập nhật ngay lập tức sau mỗi giao dịch thanh toán thành công (Real-time).
*   Hệ thống phải chặn việc tính toán tỷ lệ nếu tổng số lượng hàng ban đầu chưa được cấu hình (phòng lỗi chia cho 0).

**Logic xử lý (Business Logic):**
*   Hệ thống sử dụng cơ chế "ghi nhận tức thì". Mỗi khi có một đơn hàng thành công, số liệu doanh thu trên màn hình quản lý sẽ nhảy số ngay lập tức mà không cần tải lại trang.
*   Trong trường hợp có lỗi dữ liệu đầu vào (ví dụ số lượng sản phẩm bằng 0), hệ thống sẽ hiển thị 0% thay vì báo lỗi hệ thống, giúp màn hình quản lý luôn hoạt động ổn định.

- **Acceptance Criteria (Tiêu chí chấp nhận) & Các Paths**:
  - **Scenario 4.1 (Happy Path) - Xem báo cáo khi có đơn hàng**
    - **Given**: Chiến dịch đang diễn ra và có đơn hàng thành công.
    - **When**: Quản lý mở Dashboard Flash Sale.
    - **Then**: Hệ thống hiển thị doanh thu "50.000.000 VNĐ" và tỷ lệ bán ra "80%".
  - **Scenario 4.2 (Unhappy Path) - Xem báo cáo khi bị lỗi chia cho 0**
    - **Given**: Chiến dịch bị lỗi cấu hình tổng sản phẩm ban đầu là "0".
    - **When**: Quản lý mở Dashboard.
    - **Then**: Hệ thống hiển thị tỷ lệ bán ra "0%" và cảnh báo "Chưa cấu hình số lượng tổng".
  - **Scenario 4.3 (Unhappy Path) - Xem báo cáo khi chưa có giao dịch nào (Trạng thái rỗng)**
    - **Given**: Chiến dịch mới bắt đầu, chưa có khách hàng nào mua.
    - **When**: Quản lý mở Dashboard.
    - **Then**: Hệ thống hiển thị Doanh thu "0 VNĐ" và tỷ lệ "0%".



---

### US5: Quản lý Sản phẩm và Combo Sale
**User Story**: Là Quản lý cửa hàng, tôi muốn gộp nhiều sản phẩm thành một Combo Flash Sale để đẩy hàng tồn kho nhanh hơn.

- **Đảm bảo tính INVEST**:
  - **I (Independent)**: Sản phẩm Combo được quản lý như một thực thể mới, tách biệt đơn hàng lẻ.
  - **N (Negotiable)**: Số lượng sản phẩm tối đa trong 1 combo có thể điều chỉnh.
  - **V (Valuable)**: Giải quyết bài toán hàng tồn kho chậm và tăng giá trị đơn hàng (AOV).
  - **E (Estimable)**: Ứng dụng mô hình Composite để nhóm các đối tượng.
  - **S (Small)**: Tập trung vào logic kiểm tra chéo tồn kho giữa các thành phần.
  - **T (Testable)**: Kiểm thử được bằng cách rút cạn 1 trong các sản phẩm thành phần.

**Quy tắc Nghiệp vụ (Business Rules):**
*   Mỗi gói Combo phải chứa ít nhất một mã sản phẩm hợp lệ.
*   Không cho phép tạo Combo nếu một trong các sản phẩm thành phần đã hết hàng trong kho.
*   Số lượng Combo khả dụng được tính tự động dựa trên sản phẩm có tồn kho thấp nhất trong gói.

**Logic xử lý (Business Logic):**
*   Hệ thống tự động tính toán dựa trên "mắt xích yếu nhất". Ví dụ: Một Combo gồm 1 son và 1 nước hoa, nếu chỉ còn 2 thỏi son nhưng có 10 chai nước hoa, hệ thống sẽ báo chỉ còn 2 bộ Combo khả dụng.
*   Khi một sản phẩm được bán lẻ (không nằm trong combo), hệ thống cũng sẽ tự động tính toán lại số lượng của tất cả các bộ Combo có chứa sản phẩm đó để khách hàng không bao giờ đặt nhầm hàng đã hết.

- **Acceptance Criteria (Tiêu chí chấp nhận) & Các Paths**:
  - **Scenario 5.1 (Happy Path) - Tạo Combo hợp lệ**
    - **Given**: Quản lý chọn "Son MAC Ruby Woo" và "Nước hoa Chanel N°5".
    - **When**: Thiết lập giá Combo giảm "30%" và nhấn "Tạo".
    - **Then**: Hệ thống hiển thị "Combo Làm Đẹp" trong danh sách Flash Sale.
  - **Scenario 5.2 (Unhappy Path) - Thêm sản phẩm đã hết hàng vào Combo**
    - **Given**: Sản phẩm "Nước hoa Chanel N°5" có tồn kho tổng là "0".
    - **When**: Quản lý cố gắng ghép "Nước hoa Chanel N°5" vào Combo và nhấn "Tạo".
    - **Then**: Hệ thống báo lỗi "Sản phẩm Nước hoa Chanel N°5 không đủ tồn kho để tạo Combo".
  - **Scenario 5.3 (Unhappy Path) - Tạo Combo không có sản phẩm nào**
    - **Given**: Quản lý chưa chọn sản phẩm nào từ danh sách.
    - **When**: Nhấn "Tạo Combo".
    - **Then**: Hệ thống báo lỗi "Combo phải có ít nhất 1 sản phẩm".


---

## 4. Hướng dẫn chạy dự án

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
