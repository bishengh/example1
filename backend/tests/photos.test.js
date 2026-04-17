const request = require('supertest');
const fs = require('fs');
const app = require('../src/app');
const { db, run } = require('../src/db');
const { getSavedFilePath } = require('../src/storage');

describe('照片接口', () => {
  let cookie;
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
    db.close();
  });

  it('未登录用户访问照片列表应返回 401', async () => {
    const response = await request(app).get('/api/photos');
    expect(response.status).toBe(401);
  });

  it('登录用户应能上传照片', async () => {
    const response = await request(app)
      .post('/api/photos')
      .set('Cookie', cookie)
      .attach('image', Buffer.from([0x89, 0x50, 0x4e, 0x47]), 'photo.png')
      .field('title', '测试照片')
      .field('description', '这是一个测试照片');

    expect(response.status).toBe(201);
    expect(response.body.title).toBe('测试照片');
    expect(response.body.url).toContain('/uploads/');

    testPhotoId = response.body.id;
    testPhotoFilename = response.body.filename;
  });

  it('上传照片时没有文件应返回 400', async () => {
    const response = await request(app)
      .post('/api/photos')
      .set('Cookie', cookie)
      .field('title', '无文件测试');

    expect(response.status).toBe(400);
    expect(response.body.error).toBeDefined();
  });

  it('上传非图片文件应返回 400', async () => {
    const response = await request(app)
      .post('/api/photos')
      .set('Cookie', cookie)
      .attach('image', Buffer.from('this is a text file'), 'document.txt')
      .field('title', '错误格式测试');

    expect(response.status).toBe(400);
    expect(response.body.error).toContain('只支持');
  });

  it('登录用户应能查看照片列表', async () => {
    const response = await request(app)
      .get('/api/photos')
      .set('Cookie', cookie);

    expect(response.status).toBe(200);
    expect(Array.isArray(response.body)).toBe(true);
  });

  it('应能获取单张照片详情', async () => {
    const response = await request(app)
      .get(`/api/photos/${testPhotoId}`)
      .set('Cookie', cookie);

    expect(response.status).toBe(200);
    expect(response.body.id).toBe(testPhotoId);
    expect(response.body.url).toContain('/uploads/');
    expect(Array.isArray(response.body.tags)).toBe(true);
  });

  it('获取不存在的照片应返回 404', async () => {
    const response = await request(app)
      .get('/api/photos/999999')
      .set('Cookie', cookie);

    expect(response.status).toBe(404);
  });

  it('应能更新照片信息', async () => {
    const response = await request(app)
      .put(`/api/photos/${testPhotoId}`)
      .set('Cookie', cookie)
      .send({ title: '更新后的标题', description: '更新后的描述', tags: 'tag1, tag2' });

    expect(response.status).toBe(200);
    expect(response.body.title).toBe('更新后的标题');
    expect(response.body.description).toBe('更新后的描述');
    expect(response.body.tags).toEqual(['tag1', 'tag2']);
  });

  it('更新不存在的照片应返回 404', async () => {
    const response = await request(app)
      .put('/api/photos/999999')
      .set('Cookie', cookie)
      .send({ title: '不存在的照片' });

    expect(response.status).toBe(404);
  });
});
