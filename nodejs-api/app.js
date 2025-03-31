// app.js
const express = require('express');
const { sql, connect } = require('./db'); // Kết nối với db.js

const app = express();
const port = 3000; // Cổng mặc định cho API

// Kết nối đến SQL Server
connect();

// Middleware để xử lý các request có dữ liệu JSON
app.use(express.json());

// API endpoint để lấy danh sách người dùng từ SQL Server
app.get('/users', async (req, res) => {
  try {
    const result = await sql.query`SELECT * FROM Users`; // Thực hiện truy vấn SQL
    res.json(result.recordset);  // Trả dữ liệu dưới dạng JSON
  } catch (err) {
    res.status(500).send({ message: 'Lỗi khi truy xuất dữ liệu', error: err });
  }
});

// API endpoint để thêm một người dùng mới
app.post('/users', async (req, res) => {
  const { name, email } = req.body;

  try {
    await sql.query`INSERT INTO Users (name, email) VALUES (${name}, ${email})`;
    res.status(201).send({ message: 'Người dùng đã được thêm thành công!' });
  } catch (err) {
    res.status(500).send({ message: 'Lỗi khi thêm người dùng', error: err });
  }
});

// Bắt đầu server
app.listen(port, () => {
  console.log(`Server đang chạy trên http://localhost:${port}`);
});
