# ============================
# Stage 1: Build (Node.js)
# ============================
FROM node:18-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# 跳過 vue-tsc 型別檢查，直接用 vite build
RUN npx vite build

# ============================
# Stage 2: Runtime (Nginx)
# ============================
FROM nginx:alpine

# 複製 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 複製建置後的靜態檔案
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
