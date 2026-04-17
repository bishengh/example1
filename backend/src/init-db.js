const { run, get } = require('./db');
const bcrypt = require('bcrypt');

async function initializeDatabase() {
  await run(`CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL
  )`);

  await run(`CREATE TABLE IF NOT EXISTS albums (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT DEFAULT ''
  )`);

  await run(`CREATE TABLE IF NOT EXISTS photos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    filename TEXT NOT NULL,
    title TEXT DEFAULT '',
    description TEXT DEFAULT '',
    tags TEXT DEFAULT '',
    album_id INTEGER,
    uploaded_at TEXT NOT NULL,
    FOREIGN KEY(album_id) REFERENCES albums(id)
  )`);

  const existingUser = await get(`SELECT id, password FROM users WHERE username = ?`, ['admin']);
  if (!existingUser) {
    const hashedPassword = await bcrypt.hash('password', 10);
    await run(`INSERT INTO users (username, password) VALUES (?, ?)`, ['admin', hashedPassword]);
  } else if (!existingUser.password.startsWith('$2')) {
    const hashedPassword = await bcrypt.hash(existingUser.password, 10);
    await run(`UPDATE users SET password = ? WHERE id = ?`, [hashedPassword, existingUser.id]);
  }
}

module.exports = { initializeDatabase };
