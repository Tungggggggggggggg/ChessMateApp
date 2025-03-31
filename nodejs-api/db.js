// db.js
const sql = require('mssql');

// Cấu hình kết nối SQL Server
const config = {
  user: 'sa',            // Tên người dùng SQL Server (sử dụng mặc định hoặc của bạn)
  password: '123', // Mật khẩu của bạn
  server: 'localhost',   // Địa chỉ máy chủ (hoặc IP của SQL Server)
  database: 'your_database', // Tên cơ sở dữ liệu
  options: {
    encrypt: true,           // Nếu bạn sử dụng SSL
    trustServerCertificate: true // Đảm bảo kết nối an toàn
  }
};

// Kết nối SQL Server
async function connect() {
  try {
    await sql.connect(config);
    console.log("Kết nối đến SQL Server thành công!");
  } catch (err) {
    console.error("Kết nối cơ sở dữ liệu thất bại:", err);
  }
}

module.exports = { sql, connect };
