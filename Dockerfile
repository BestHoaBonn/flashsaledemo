# Giai đoạn 1: Xây dựng (Build) ứng dụng Frontend
FROM node:18-alpine AS build

WORKDIR /app

# Sao chép file quản lý thư viện và cài đặt
COPY package*.json ./
RUN npm install

# Sao chép toàn bộ mã nguồn và biên dịch (Build)
COPY . .
RUN npm run build

# Giai đoạn 2: Máy chủ phục vụ (Serve) bằng Nginx
FROM nginx:stable-alpine

# Sao chép các file tĩnh từ giai đoạn build vào thư mục của Nginx
COPY --from=build /app/dist /usr/share/nginx/html

# Mở cổng 80 cho container
EXPOSE 80

# Khởi động Nginx
CMD ["nginx", "-g", "daemon off;"]
