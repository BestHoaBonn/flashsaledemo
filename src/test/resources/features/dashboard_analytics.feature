# language: vi
Tính năng: US4 - Báo cáo hiệu quả thời gian thực
  Là Quản lý cửa hàng
  Tôi muốn xem doanh thu và tỷ lệ bán ra theo thời gian thực
  Để quyết định bổ sung hàng hoặc thay đổi chiến lược marketing

  Kịch bản: Xem báo cáo khi có đơn hàng (Happy Path)
    Cho chiến dịch đang diễn ra và có đơn hàng thành công
    Khi Quản lý mở Dashboard Flash Sale
    Thì hệ thống hiển thị doanh thu "50.000.000 VNĐ" và tỷ lệ bán ra "80%"

  Kịch bản: Xem báo cáo khi bị lỗi chia cho 0 (Unhappy Path)
    Cho chiến dịch bị lỗi cấu hình tổng sản phẩm ban đầu là "0"
    Khi Quản lý mở Dashboard
    Thì hệ thống hiển thị tỷ lệ bán ra "0%" và cảnh báo "Chưa cấu hình số lượng tổng"

  Kịch bản: Xem báo cáo khi chưa có giao dịch nào (Unhappy Path)
    Cho chiến dịch bị lỗi cấu hình tổng sản phẩm ban đầu là "100"
    Khi Quản lý mở Dashboard
    Thì hệ thống hiển thị doanh thu "0 VNĐ" và tỷ lệ bán ra "0%"
