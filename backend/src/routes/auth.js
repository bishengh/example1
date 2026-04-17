const express = require('express');
const bcrypt = require('bcrypt');
const { get } = require('../db');

const router = express.Router();

router.post('/login', async (req, res) => {
  const { username, password } = req.body;
  if (!username || !password) {
    return res.status(400).json({ error: '用户名和密码不能为空' });
  }

  const user = await get('SELECT id, username, password FROM users WHERE username = ?', [username]);
  if (!user || !(await bcrypt.compare(password, user.password))) {
    return res.status(401).json({ error: '用户名或密码错误' });
  }

  req.session.user = { id: user.id, username: user.username };
  res.json({ message: '登录成功', user: { id: user.id, username: user.username } });
});

router.post('/logout', (req, res) => {
  req.session.destroy(() => {
    res.json({ message: '已退出登录' });
  });
});

router.get('/me', (req, res) => {
  if (req.session && req.session.user) {
    return res.json({ user: req.session.user });
  }
  res.status(401).json({ error: '未登录' });
});

module.exports = router;
