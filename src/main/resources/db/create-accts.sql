CREATE DATABASE dts;
CREATE USER 'dts'@'localhost' IDENTIFIED BY 'dts';
GRANT ALL PRIVILEGES ON dts.* TO 'dts'@'localhost';
GRANT ALL PRIVILEGES ON dts.* TO 'dts'@'%';
GRANT ALL PRIVILEGES ON dts.* TO 'dts'@'127.0.0.1';
