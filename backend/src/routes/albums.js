const express = require('express');
const { run, all, get } = require('../db');
const { requireLogin } = require('../middleware/auth');

const router = express.Router();
router.use(requireLogin);

router.get('/', async (req, res) => {
  const albums = await all('SELECT * FROM albums ORDER BY name ASC');
  res.json(albums);
});

router.post('/', async (req, res) => {
  const { name, description } = req.body;
  if (!name) {
    return res.status(400).json({ error: '相册名称不能为空' });
  }
  const result = await run('INSERT INTO albums (name, description) VALUES (?, ?)', [name, description || '']);
  const album = await get('SELECT * FROM albums WHERE id = ?', [result.lastID]);
  res.status(201).json(album);
});

router.put('/:id', async (req, res) => {
  const album = await get('SELECT * FROM albums WHERE id = ?', [req.params.id]);
  if (!album) {
    return res.status(404).json({ error: '相册未找到' });
  }
  const name = req.body.name || album.name;
  const description = req.body.description || album.description;
  await run('UPDATE albums SET name = ?, description = ? WHERE id = ?', [name, description, req.params.id]);
  const updatedAlbum = await get('SELECT * FROM albums WHERE id = ?', [req.params.id]);
  res.json(updatedAlbum);
});

router.delete('/:id', async (req, res) => {
  const album = await get('SELECT * FROM albums WHERE id = ?', [req.params.id]);
  if (!album) {
    return res.status(404).json({ error: '相册未找到' });
  }
  await run('UPDATE photos SET album_id = NULL WHERE album_id = ?', [req.params.id]);
  await run('DELETE FROM albums WHERE id = ?', [req.params.id]);
  res.json({ message: '相册已删除' });
});

module.exports = router;
