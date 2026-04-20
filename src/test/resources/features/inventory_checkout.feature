# language: vi
Tính năng: US2 - Xử lý tồn kho và thanh toán
  Là khách hàng
  Tôi muốn hệ thống chốt đúng giá ưu đãi nếu tôi mua trong giới hạn số lượng cho phép
  Để đảm bảo tính công bằng

  Kịch bản: 2.1 - Thanh toán hợp lệ (Happy Path)
    Cho kho Flash Sale còn "10" sản phẩm
    Khi khách hàng thêm "1" sản phẩm và thanh toán
    Thì đơn hàng được tạo với giá Flash Sale và kho giảm còn "9"

  Kịch bản: 2.2 - Thanh toán khi hết kho khuyến mãi (Unhappy Path)
    Cho khách hàng đang ở trang thanh toán nhưng kho Flash Sale vừa về "0"
    Khi khách nhấn "Xác nhận đặt hàng"
    Thì hệ thống báo lỗi "Sản phẩm đã hết suất Flash Sale" và cập nhật giỏ hàng về giá gốc

  Kịch bản: 2.3 - Thanh toán với số lượng không hợp lệ (Unhappy Path)
    Cho kho Flash Sale còn "10" sản phẩm
    Khi khách hàng thêm "0" sản phẩm và thanh toán
    Thì hệ thống báo lỗi "Số lượng mua phải lớn hơn 0" và cập nhật giỏ hàng về giá gốc
