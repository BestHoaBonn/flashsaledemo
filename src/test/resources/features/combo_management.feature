# language: vi
Tính năng: US5 - Quản lý Sản phẩm và Combo Sale
  Là Quản lý cửa hàng
  Tôi muốn gộp nhiều sản phẩm thành một Combo Flash Sale
  Để đẩy hàng tồn kho nhanh hơn

  Kịch bản: 5.1 - Tạo Combo hợp lệ (Happy Path)
    Cho Quản lý chọn "Son MAC Ruby Woo" và "Nước hoa Chanel N°5"
    Khi thiết lập giá Combo giảm "30%" và nhấn "Tạo"
    Thì hệ thống hiển thị "Combo Làm Đẹp" trong danh sách Flash Sale

  Kịch bản: 5.2 - Thêm sản phẩm đã hết hàng vào Combo (Unhappy Path)
    Cho sản phẩm "Nước hoa Chanel N°5" có tồn kho tổng là "0"
    Khi Quản lý cố gắng ghép "Nước hoa Chanel N°5" vào Combo và nhấn "Tạo"
    Thì hệ thống báo lỗi "Sản phẩm Nước hoa Chanel N°5 không đủ tồn kho để tạo Combo"

  Kịch bản: 5.3 - Tạo Combo không có sản phẩm nào (Unhappy Path)
    Khi thiết lập giá Combo giảm "30%" và nhấn "Tạo" nhưng không chọn sản phẩm nào
    Thì hệ thống báo lỗi "Combo phải có ít nhất 1 sản phẩm"
