const request = require('supertest');
const app = require('../src/app');
const { db } = require('../src/db');

beforeAll(async () => {
  await app.ready;
});

afterAll(() => {
  db.close();
});

describe('认证接口', () => {
  it('应允许管理员使用默认凭据登录', async () => {
    const response = await request(app)
      .post('/api/auth/login')
      .send({ username: 'admin', password: 'password' });

    expect(response.status).toBe(200);
    expect(response.body.user).toBeDefined();
    expect(response.body.user.username).toBe('admin');
  });

  it('用户名或密码为空时应返回 400', async () => {
    const response1 = await request(app)
      .post('/api/auth/login')
      .send({ username: '', password: 'password' });
    expect(response1.status).toBe(400);

    const response2 = await request(app)
      .post('/api/auth/login')
      .send({ username: 'admin', password: '' });
    expect(response2.status).toBe(400);
  });

  it('密码错误时应返回 401', async () => {
    const response = await request(app)
      .post('/api/auth/login')
      .send({ username: 'admin', password: 'wrongpassword' });

    expect(response.status).toBe(401);
  });

  it('不存在的用户登录时应返回 401', async () => {
    const response = await request(app)
      .post('/api/auth/login')
      .send({ username: 'nonexistent', password: 'password' });

    expect(response.status).toBe(401);
  });

  it('未登录时访问 /me 应返回 401', async () => {
    const response = await request(app).get('/api/auth/me');
    expect(response.status).toBe(401);
  });

  it('登录后访问 /me 应返回用户信息', async () => {
    const loginResponse = await request(app)
      .post('/api/auth/login')
      .send({ username: 'admin', password: 'password' });

    const cookie = loginResponse.headers['set-cookie'];
    const response = await request(app).get('/api/auth/me').set('Cookie', cookie);

    expect(response.status).toBe(200);
    expect(response.body.user).toBeDefined();
    expect(response.body.user.username).toBe('admin');
  });

  it('登出后访问 /me 应返回 401', async () => {
    const loginResponse = await request(app)
      .post('/api/auth/login')
      .send({ username: 'admin', password: 'password' });

    const cookie = loginResponse.headers['set-cookie'];

    const logoutResponse = await request(app)
      .post('/api/auth/logout')
      .set('Cookie', cookie);
    expect(logoutResponse.status).toBe(200);

    const meResponse = await request(app)
      .get('/api/auth/me')
      .set('Cookie', cookie);
    expect(meResponse.status).toBe(401);
  });
});
