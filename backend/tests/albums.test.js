const request = require('supertest');
const fs = require('fs');
const app = require('../src/app');
const { db, run, get } = require('../src/db');
const { getSavedFilePath } = require('../src/storage');

describe('相册接口', () => {
  let cookie;
  let testAlbumId;
  let testPhotoId;
  let testPhotoFilename;

  beforeAll(async () => {
    await app.ready;
    const response = await request(app)
      .post('/api/auth/login')
      .send({ username: 'admin', password: 'password' });
    cookie = response.headers['set-cookie'];
  });

  afterAll(async () => {
    if (testPhotoId) {
      await run(`DELETE FROM photos WHERE id = ?`, [testPhotoId]);
    }
    if (testPhotoFilename) {
      try {
        fs.unlinkSync(getSavedFilePath(testPhotoFilename));
      } catch (e) {
        // 忽略文件不存在的错误
      }
    }
    if (testAlbumId) {
      await run(`DELETE FROM albums WHERE id = ?`, [testAlbumId]);
    }
    db.close();
  });

  it('未登录用户访问相册列表应返回 401', async () => {
    const response = await request(app).get('/api/albums');
    expect(response.status).toBe(401);
  });

  it('应能创建相册', async () => {
    const response = await request(app)
      .post('/api/albums')
      .set('Cookie', cookie)
      .send({ name: '测试相册', description: '测试描述' });

    expect(response.status).toBe(201);
    expect(response.body.name).toBe('测试相册');
    expect(response.body.description).toBe('测试描述');

    testAlbumId = response.body.id;
  });

  it('创建相册时名称为空应返回 400', async () => {
    const response = await request(app)
      .post('/api/albums')
      .set('Cookie', cookie)
      .send({ name: '', description: '描述' });

    expect(response.status).toBe(400);
    expect(response.body.error).toBeDefined();
  });

  it('应能查看相册列表', async () => {
    const response = await request(app).get('/api/albums').set('Cookie', cookie);

    expect(response.status).toBe(200);
    expect(Array.isArray(response.body)).toBe(true);
    const found = response.body.find(a => a.id === testAlbumId);
    expect(found).toBeDefined();
  });

  it('应能更新相册信息', async () => {
    const response = await request(app)
      .put(`/api/albums/${testAlbumId}`)
      .set('Cookie', cookie)
      .send({ name: '更新后的相册名', description: '更新后的描述' });

    expect(response.status).toBe(200);
    expect(response.body.name).toBe('更新后的相册名');
    expect(response.body.description).toBe('更新后的描述');
  });

  it('更新不存在的相册应返回 404', async () => {
    const response = await request(app)
      .put('/api/albums/999999')
      .set('Cookie', cookie)
      .send({ name: '不存在' });

    expect(response.status).toBe(404);
  });

  it('删除相册后，关联照片的 album_id 应变为 NULL', async () => {
    // 先上传一张照片并关联到测试相册
    const uploadResponse = await request(app)
      .post('/api/photos')
      .set('Cookie', cookie)
      .attach('image', Buffer.from([0x89, 0x50, 0x4e, 0x47]), 'album-photo.png')
      .field('title', '相册中的照片')
      .field('albumId', testAlbumId);

    expect(uploadResponse.status).toBe(201);
    testPhotoId = uploadResponse.body.id;
    testPhotoFilename = uploadResponse.body.filename;
    expect(uploadResponse.body.album_id).toBe(testAlbumId);

    // 删除相册
    const deleteResponse = await request(app)
      .delete(`/api/albums/${testAlbumId}`)
      .set('Cookie', cookie);

    expect(deleteResponse.status).toBe(200);

    // 查询照片，album_id 应为 NULL
    const photo = await get(`SELECT album_id FROM photos WHERE id = ?`, [testPhotoId]);
    expect(photo.album_id).toBeNull();

    testAlbumId = null;
  });

  it('删除不存在的相册应返回 404', async () => {
    const response = await request(app)
      .delete('/api/albums/999999')
      .set('Cookie', cookie);

    expect(response.status).toBe(404);
  });
});
