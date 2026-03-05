#!/bin/bash
set -e

# ============================
# 動態產生 database.properties
# 覆蓋 WAR 內的預設配置
# ============================

PROPS_DIR="/usr/local/tomcat/webapps/seat-lottery/WEB-INF/classes"

# 等待 Tomcat 解壓 WAR（最多等 30 秒）
echo "等待 Tomcat 解壓 WAR 檔案..."
for i in $(seq 1 30); do
    if [ -d "$PROPS_DIR" ]; then
        break
    fi
    sleep 1
done

# 如果目錄還不存在，手動解壓 WAR
if [ ! -d "$PROPS_DIR" ]; then
    echo "手動解壓 WAR 檔案..."
    mkdir -p /usr/local/tomcat/webapps/seat-lottery
    cd /usr/local/tomcat/webapps/seat-lottery
    jar -xf /usr/local/tomcat/webapps/seat-lottery.war
fi

# 從環境變數產生 database.properties
cat > "$PROPS_DIR/database.properties" <<EOF
# Database Connection Properties (由 Docker entrypoint 自動產生)
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://${DB_HOST:-localhost}:${DB_PORT:-3306}/${DB_NAME:-seat_lottery}?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
db.username=${DB_USER:-root}
db.password=${DB_PASSWORD:-}

# Connection Pool Configuration
db.pool.maxSize=${DB_POOL_MAX_SIZE:-10}
db.pool.minIdle=${DB_POOL_MIN_IDLE:-5}
db.pool.idleTimeout=${DB_POOL_IDLE_TIMEOUT:-30000}
db.pool.connectionTimeout=${DB_POOL_CONNECTION_TIMEOUT:-30000}
db.pool.cachePrepStmts=true
db.pool.prepStmtCacheSize=250
db.pool.prepStmtCacheSqlLimit=2048
EOF

echo "database.properties 已從環境變數產生"
cat "$PROPS_DIR/database.properties"

# 啟動 Tomcat
exec "$@"
