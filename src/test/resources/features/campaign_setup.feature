# language: vi
Tính năng: US3 - Thiết lập chiến dịch
  Là Quản lý cửa hàng
  Tôi muốn lên lịch thời gian và cấu hình mức giảm giá cho chiến dịch
  Để hệ thống tự động chạy

  Kịch bản: Tạo chiến dịch hợp lệ (Happy Path)
    Cho Quản lý đang ở form tạo chiến dịch
    Khi nhập thời gian bắt đầu "2026-04-20T08:00", kết thúc "2026-04-20T12:00" và giá giảm "20%", rồi nhấn "Lưu"
    Thì hệ thống lưu thành công và báo "Chiến dịch đã được lên lịch"

  Kịch bản: Mức giảm giá vượt quá biên lợi nhuận (Unhappy Path)
    Cho chính sách giới hạn giảm tối đa "50%"
    Khi Quản lý nhập mức giảm "60%" và nhấn "Lưu"
    Thì hệ thống chặn lưu và báo lỗi "Mức giảm không được vượt quá 50%"

  Kịch bản: Thiết lập thời gian sai (Unhappy Path)
    Khi nhập thời gian bắt đầu "2026-04-20T12:00", kết thúc "2026-04-20T08:00" và giá giảm "20%", rồi nhấn "Lưu"
    Thì hệ thống chặn lưu và báo lỗi "Thời gian kết thúc phải sau thời gian bắt đầu"
