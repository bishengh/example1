require('express-async-errors');
const path = require('path');
const express = require('express');
const session = require('express-session');
const cors = require('cors');
const authRoutes = require('./routes/auth');
const photoRoutes = require('./routes/photos');
const albumRoutes = require('./routes/albums');
const { initializeDatabase } = require('./init-db');

const app = express();
const port = process.env.PORT || 3000;

const initPromise = initializeDatabase();

app.use(cors({ origin: true, credentials: true }));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(
  session({
    secret: 'personal-photo-gallery-secret',
    resave: false,
    saveUninitialized: false,
    cookie: { maxAge: 24 * 60 * 60 * 1000 }
  })
);

app.use('/api/auth', authRoutes);
app.use('/api/photos', photoRoutes);
app.use('/api/albums', albumRoutes);

app.use('/uploads', express.static(path.join(__dirname, '../../storage')));
app.use('/', express.static(path.join(__dirname, '../../frontend')));

app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, '../../frontend/index.html'));
});

app.use((err, req, res, next) => {
  console.error(err);
  res.status(500).json({ error: '服务器内部错误' });
});

if (require.main === module) {
  app.listen(port, () => {
    console.log(`个人照片图库服务已启动，访问 http://localhost:${port}`);
  });
}

module.exports = app;
app.ready = initPromise;
