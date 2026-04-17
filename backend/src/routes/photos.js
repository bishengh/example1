const express = require('express');
const multer = require('multer');
const { run, all, get } = require('../db');
const { requireLogin } = require('../middleware/auth');
const { saveUploadedFile, buildFileUrl } = require('../storage');

const upload = multer({ storage: multer.memoryStorage() });
const router = express.Router();

router.use(requireLogin);

router.get('/', async (req, res) => {
  const photos = await all(`SELECT p.id, p.title, p.description, p.tags, p.uploaded_at as uploadedAt, p.album_id as albumId, p.filename FROM photos p ORDER BY p.uploaded_at DESC`);
  res.json(photos.map(photo => ({
    ...photo,
    url: buildFileUrl(photo.filename),
    tags: photo.tags ? photo.tags.split(',').filter(Boolean) : []
  })));
});

router.get('/:id', async (req, res) => {
  const photo = await get(`SELECT * FROM photos WHERE id = ?`, [req.params.id]);
  if (!photo) {
    return res.status(404).json({ error: '照片未找到' });
  }
  res.json({
    ...photo,
    url: buildFileUrl(photo.filename),
    tags: photo.tags ? photo.tags.split(',').filter(Boolean) : []
  });
});

router.post('/', upload.single('image'), async (req, res) => {
  const file = req.file;
  if (!file) {
    return res.status(400).json({ error: '请上传照片' });
  }

  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif'];
  if (!allowedTypes.includes(file.mimetype)) {
    return res.status(400).json({ error: '只支持 JPG、PNG、GIF 格式' });
  }

  const filename = saveUploadedFile(file);
  const title = req.body.title || '';
  const description = req.body.description || '';
  const tags = (req.body.tags || '').split(',').map(tag => tag.trim()).filter(Boolean).join(',');
  const albumId = req.body.albumId || null;
  const uploadedAt = new Date().toISOString();

  const result = await run(
    `INSERT INTO photos (filename, title, description, tags, album_id, uploaded_at) VALUES (?, ?, ?, ?, ?, ?)`,
    [filename, title, description, tags, albumId, uploadedAt]
  );

  const photo = await get('SELECT * FROM photos WHERE id = ?', [result.lastID]);
  res.status(201).json({
    ...photo,
    url: buildFileUrl(photo.filename),
    tags: photo.tags ? photo.tags.split(',').filter(Boolean) : []
  });
});

router.put('/:id', async (req, res) => {
  const photo = await get('SELECT * FROM photos WHERE id = ?', [req.params.id]);
  if (!photo) {
    return res.status(404).json({ error: '照片未找到' });
  }

  const title = req.body.title || photo.title;
  const description = req.body.description || photo.description;
  const tags = (req.body.tags || photo.tags || '').split(',').map(tag => tag.trim()).filter(Boolean).join(',');
  const albumId = req.body.albumId || photo.album_id;

  await run(
    `UPDATE photos SET title = ?, description = ?, tags = ?, album_id = ? WHERE id = ?`,
    [title, description, tags, albumId, req.params.id]
  );

  const updatedPhoto = await get('SELECT * FROM photos WHERE id = ?', [req.params.id]);
  res.json({
    ...updatedPhoto,
    url: buildFileUrl(updatedPhoto.filename),
    tags: updatedPhoto.tags ? updatedPhoto.tags.split(',').filter(Boolean) : []
  });
});

module.exports = router;
