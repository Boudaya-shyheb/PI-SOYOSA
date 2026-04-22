const mysql = require('mysql2/promise');

function buildDbConfig() {
  return {
    host: process.env.DB_HOST || 'localhost',
    port: Number(process.env.DB_PORT || 3306),
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || '',
    database: process.env.DB_NAME || 'ecommercedb',
    waitForConnections: true,
    connectionLimit: 5,
    queueLimit: 0
  };
}

async function createPool() {
  const config = buildDbConfig();
  return mysql.createPool(config);
}

module.exports = {
  createPool
};
