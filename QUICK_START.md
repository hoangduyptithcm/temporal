# 🚀 Quick Start Guide - Temporal Learning Project

## ⚡ Chạy nhanh trong 3 bước

### 1. Khởi động project
```bash
./start-temporal.sh
```

### 2. Test API
```bash
# Test async workflow
curl -X POST http://localhost:8081/api/workflows/greeting/async \
  -H "Content-Type: application/json" \
  -d '{"name": "Your Name"}'

# Test sync workflow  
curl -X POST http://localhost:8081/api/workflows/greeting/sync \
  -H "Content-Type: application/json" \
  -d '{"name": "Your Name"}'
```

### 3. Xem kết quả
- **Temporal Web UI**: http://localhost:8080
- **API Health**: http://localhost:8081/api/workflows/health

## 🛑 Dừng project
```bash
./stop-temporal.sh
```

## 📋 Các lệnh hữu ích

### Build project
```bash
mvn clean install
```

### Chạy tests
```bash
mvn test
```

### Xem logs
```bash
tail -f application.log
```

### Kiểm tra Temporal Server
```bash
docker ps | grep temporal
```

## 🔧 Troubleshooting nhanh

### Lỗi "Connection refused"
```bash
# Restart Temporal Server
cd temporal-docker
docker-compose restart
cd ..
```

### Lỗi port đã được sử dụng
```bash
# Kiểm tra process đang sử dụng port 8081
lsof -i :8081

# Kill process nếu cần
kill -9 <PID>
```

### Reset hoàn toàn
```bash
./stop-temporal.sh
docker-compose -f temporal-docker/docker-compose.yml down -v
rm -rf temporal-docker
./start-temporal.sh
```

## 📚 Đọc thêm
- **Chi tiết**: [README.md](README.md)
- **Temporal Docs**: https://docs.temporal.io/
- **Java SDK**: https://docs.temporal.io/dev-guide/java

---
💡 **Tip**: Luôn kiểm tra logs nếu có lỗi: `tail -f application.log`
