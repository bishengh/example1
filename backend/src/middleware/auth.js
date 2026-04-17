function requireLogin(req, res, next) {
  if (req.session && req.session.user) {
    return next();
  }
  res.status(401).json({ error: '未登录，需要先登录' });
}

module.exports = { requireLogin };
