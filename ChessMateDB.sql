-- Tạo cơ sở dữ liệu ChessMateDB
CREATE DATABASE ChessMateDB;
GO

-- Sử dụng cơ sở dữ liệu ChessMateDB
USE ChessMateDB;
GO

-- Tạo bảng Users (Người dùng)
CREATE TABLE Users (
    user_id INT IDENTITY(1,1) PRIMARY KEY,        -- ID của người dùng
    username NVARCHAR(100) NOT NULL,              -- Tên người dùng
    email NVARCHAR(100) NOT NULL UNIQUE,          -- Email người dùng
    password_hash NVARCHAR(255) NOT NULL,        -- Mật khẩu đã mã hóa
    profile_picture NVARCHAR(255),                -- Đường dẫn ảnh đại diện
    created_at DATETIME DEFAULT GETDATE()         -- Thời gian tạo tài khoản
);
GO

-- Tạo bảng Games (Trận đấu)
CREATE TABLE Games (
    game_id INT IDENTITY(1,1) PRIMARY KEY,       -- ID của trận đấu
    player_white_id INT NOT NULL,                 -- Người chơi quân trắng
    player_black_id INT NOT NULL,                 -- Người chơi quân đen
    start_time DATETIME DEFAULT GETDATE(),       -- Thời gian bắt đầu
    end_time DATETIME,                            -- Thời gian kết thúc
    status NVARCHAR(50) DEFAULT 'in_progress',   -- Trạng thái của trận đấu
    winner_id INT,                                -- Người chiến thắng (nếu có)
    FOREIGN KEY (player_white_id) REFERENCES Users(user_id),
    FOREIGN KEY (player_black_id) REFERENCES Users(user_id),
    FOREIGN KEY (winner_id) REFERENCES Users(user_id)
);
GO

-- Tạo bảng Moves (Các nước đi)
CREATE TABLE Moves (
    move_id INT IDENTITY(1,1) PRIMARY KEY,       -- ID của nước đi
    game_id INT NOT NULL,                         -- Khóa ngoại đến bảng Games
    player_id INT NOT NULL,                       -- Người thực hiện nước đi
    move NVARCHAR(10) NOT NULL,                   -- Mô tả nước đi (ví dụ: e2-e4)
    timestamp DATETIME DEFAULT GETDATE(),         -- Thời gian thực hiện nước đi
    move_number INT NOT NULL,                     -- Số thứ tự của nước đi trong trận đấu
    FOREIGN KEY (game_id) REFERENCES Games(game_id),
    FOREIGN KEY (player_id) REFERENCES Users(user_id)
);
GO

-- Tạo bảng Friends (Bạn bè)
CREATE TABLE Friends (
    user_id INT NOT NULL,                         -- Người dùng
    friend_id INT NOT NULL,                       -- Bạn bè
    status NVARCHAR(50) DEFAULT 'pending',        -- Trạng thái (chờ xác nhận, từ chối, chấp nhận)
    PRIMARY KEY (user_id, friend_id),             -- Khóa chính là cặp (user_id, friend_id)
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (friend_id) REFERENCES Users(user_id)
);
GO

-- Tạo bảng Messages (Tin nhắn)
CREATE TABLE Messages (
    message_id INT IDENTITY(1,1) PRIMARY KEY,    -- ID của tin nhắn
    sender_id INT NOT NULL,                       -- Người gửi tin nhắn
    receiver_id INT NOT NULL,                     -- Người nhận tin nhắn
    message_content NVARCHAR(MAX) NOT NULL,       -- Nội dung tin nhắn
    timestamp DATETIME DEFAULT GETDATE(),         -- Thời gian gửi tin nhắn
    FOREIGN KEY (sender_id) REFERENCES Users(user_id),
    FOREIGN KEY (receiver_id) REFERENCES Users(user_id)
);
GO

-- Tạo bảng UserStats (Thống kê người dùng)
CREATE TABLE UserStats (
    user_id INT PRIMARY KEY,                       -- ID của người dùng
    total_games INT DEFAULT 0,                     -- Tổng số trận đấu
    total_wins INT DEFAULT 0,                      -- Số trận thắng
    total_losses INT DEFAULT 0,                    -- Số trận thua
    ranking INT DEFAULT 0,                         -- Xếp hạng của người dùng
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);
GO

-- Tạo bảng Leaderboard (Bảng xếp hạng)
CREATE TABLE Leaderboard (
    user_id INT PRIMARY KEY,                       -- ID của người dùng
    rank INT NOT NULL,                              -- Xếp hạng của người chơi
    total_points INT NOT NULL,                      -- Điểm tổng cộng
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);
GO
