# language: vi
Tính năng: US1 - Hiển thị trạng thái Flash Sale trên sản phẩm
  Là khách hàng
  Tôi muốn thấy giá ưu đãi, số tiền tiết kiệm được và đồng hồ đếm ngược trên trang sản phẩm
  Để ra quyết định mua hàng nhanh hơn

  Kịch bản: Xem sản phẩm trong giờ Flash Sale (Happy Path)
    Cho chiến dịch "Siêu Sale" đang diễn ra, kết thúc lúc "23:59"
    Khi khách hàng xem sản phẩm "Son MAC Ruby Woo"
    Thì hệ thống hiển thị giá Flash Sale "1.000.000 VNĐ", nhãn "Tiết kiệm 500.000 VNĐ" và đồng hồ đếm ngược đến "23:59"

  Kịch bản: Xem sản phẩm khi chiến dịch đã kết thúc (Unhappy Path)
    Cho chiến dịch "Siêu Sale" đã kết thúc
    Khi khách hàng xem sản phẩm "Son MAC Ruby Woo"
    Thì hệ thống hiển thị giá gốc "1.500.000 VNĐ" và không có đồng hồ đếm ngược

  Kịch bản: Xem sản phẩm khi chiến dịch chưa bắt đầu (Unhappy Path)
    Cho chiến dịch "Sale Hè" sắp diễn ra
    Khi khách hàng xem sản phẩm "Son MAC Ruby Woo"
    Thì hệ thống hiển thị giá gốc và nhãn "Sắp diễn ra"
