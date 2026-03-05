FROM node:18-alpine

WORKDIR /app

# 先複製 package 檔案以利用 Docker 快取
COPY package.json package-lock.json ./

# 安裝生產環境依賴
RUN npm ci --omit=dev

# 複製應用程式原始碼
COPY . .

EXPOSE 3030

# 使用 node 而非 nodemon（生產環境）
CMD ["node", "app.js"]
