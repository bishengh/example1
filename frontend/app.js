const loginScreen = document.getElementById('login-screen');
const galleryScreen = document.getElementById('gallery-screen');
const uploadScreen = document.getElementById('upload-screen');
const albumsScreen = document.getElementById('albums-screen');
const loginForm = document.getElementById('login-form');
const loginMessage = document.getElementById('login-message');
const uploadForm = document.getElementById('upload-form');
const uploadMessage = document.getElementById('upload-message');
const albumForm = document.getElementById('album-form');
const albumMessage = document.getElementById('album-message');
const photoList = document.getElementById('photo-list');
const photoDetail = document.getElementById('photo-detail');
const albumList = document.getElementById('album-list');
const albumFilter = document.getElementById('album-filter');
const uploadAlbumSelect = document.getElementById('upload-album');
const filterText = document.getElementById('filter-text');

const screens = {
  login: loginScreen,
  gallery: galleryScreen,
  upload: uploadScreen,
  albums: albumsScreen
};

function escapeHtml(str) {
  if (typeof str !== 'string') return str;
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

function showScreen(name) {
  Object.values(screens).forEach(screen => screen.classList.remove('active'));
  screens[name].classList.add('active');
}

async function request(url, options = {}) {
  const response = await fetch(url, { credentials: 'include', ...options });
  return response.json();
}

async function fetchCurrentUser() {
  const result = await request('/api/auth/me');
  if (result.error) {
    showScreen('login');
  } else {
    showScreen('gallery');
    loadAppData();
  }
}

async function loadAppData() {
  await Promise.all([loadAlbums(), loadPhotos()]);
}

function renderAlbumOptions(albums) {
  albumFilter.innerHTML = '<option value="">所有相册</option>';
  uploadAlbumSelect.innerHTML = '<option value="">无相册</option>';
  albums.forEach(album => {
    const option = document.createElement('option');
    option.value = album.id;
    option.textContent = album.name;
    albumFilter.appendChild(option);
    uploadAlbumSelect.appendChild(option.cloneNode(true));
  });
}

async function loadAlbums() {
  const result = await request('/api/albums');
  if (result.error) {
    return;
  }
  renderAlbumOptions(result);
  albumList.innerHTML = result.map(album => `
    <div class="album-item">
      <div>
        <strong>${escapeHtml(album.name)}</strong><br />
        <small>${escapeHtml(album.description || '无描述')}</small>
      </div>
      <button data-id="${album.id}" class="delete-album">删除</button>
    </div>
  `).join('');
}

let currentPhotos = [];

async function loadPhotos() {
  const result = await request('/api/photos');
  if (result.error) {
    return;
  }
  currentPhotos = result;
  renderPhotos();
}

function renderPhotos() {
  const search = filterText.value.trim().toLowerCase();
  const albumId = albumFilter.value;
  const filtered = currentPhotos.filter(photo => {
    const matchesText = [photo.title, photo.description, (photo.tags || []).join(' ')].some(value => value.toLowerCase().includes(search));
    const matchesAlbum = !albumId || photo.albumId === Number(albumId);
    return matchesText && matchesAlbum;
  });

  photoList.innerHTML = filtered.map(photo => `
    <div class="photo-card">
      <img src="${escapeHtml(photo.url)}" alt="${escapeHtml(photo.title || '照片')}" />
      <h3>${escapeHtml(photo.title || '未命名')}</h3>
      <p>${escapeHtml(photo.description || '无描述')}</p>
      <p>上传时间：${escapeHtml(new Date(photo.uploadedAt).toLocaleString())}</p>
      <button data-id="${photo.id}" class="view-photo">查看详情</button>
    </div>
  `).join('');
}

function renderPhotoDetail(photo) {
  const tags = (photo.tags || []).map(tag => `<span>${escapeHtml(tag)}</span>`).join(' ');
  photoDetail.innerHTML = `
    <h3>${escapeHtml(photo.title || '未命名')}</h3>
    <img src="${escapeHtml(photo.url)}" alt="${escapeHtml(photo.title || '照片')}" />
    <p>${escapeHtml(photo.description || '无描述')}</p>
    <p>标签：${tags || '无'}</p>
    <p>上传时间：${escapeHtml(new Date(photo.uploadedAt).toLocaleString())}</p>
  `;
  photoDetail.classList.remove('hidden');
}

loginForm.addEventListener('submit', async event => {
  event.preventDefault();
  const username = document.getElementById('login-username').value;
  const password = document.getElementById('login-password').value;
  const result = await request('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  });
  if (result.error) {
    loginMessage.textContent = result.error;
    return;
  }
  loginMessage.textContent = '';
  showScreen('gallery');
  loadAppData();
});

uploadForm.addEventListener('submit', async event => {
  event.preventDefault();
  const image = document.getElementById('upload-image').files[0];
  const title = document.getElementById('upload-title').value;
  const description = document.getElementById('upload-description').value;
  const tags = document.getElementById('upload-tags').value;
  const albumId = document.getElementById('upload-album').value;
  if (!image) {
    uploadMessage.textContent = '请选择一张照片上传。';
    return;
  }

  const form = new FormData();
  form.append('image', image);
  form.append('title', title);
  form.append('description', description);
  form.append('tags', tags);
  if (albumId) {
    form.append('albumId', albumId);
  }

  const response = await fetch('/api/photos', {
    method: 'POST',
    credentials: 'include',
    body: form
  });
  const result = await response.json();
  if (result.error) {
    uploadMessage.textContent = result.error;
    return;
  }

  uploadMessage.textContent = '上传成功！';
  uploadForm.reset();
  await loadPhotos();
});

albumForm.addEventListener('submit', async event => {
  event.preventDefault();
  const name = document.getElementById('album-name').value;
  const description = document.getElementById('album-description').value;
  const result = await request('/api/albums', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, description })
  });
  if (result.error) {
    albumMessage.textContent = result.error;
    return;
  }
  albumMessage.textContent = '相册创建成功。';
  albumForm.reset();
  await loadAlbums();
});

photoList.addEventListener('click', async event => {
  if (!event.target.matches('.view-photo')) {
    return;
  }
  const id = event.target.dataset.id;
  const detail = await request(`/api/photos/${id}`);
  if (detail.error) {
    return;
  }
  renderPhotoDetail(detail);
});

albumList.addEventListener('click', async event => {
  if (!event.target.matches('.delete-album')) {
    return;
  }
  const id = event.target.dataset.id;
  await fetch(`/api/albums/${id}`, {
    method: 'DELETE',
    credentials: 'include'
  });
  await loadAlbums();
  await loadPhotos();
});

filterText.addEventListener('input', renderPhotos);
albumFilter.addEventListener('change', renderPhotos);

document.getElementById('nav-gallery').addEventListener('click', () => showScreen('gallery'));
document.getElementById('nav-upload').addEventListener('click', () => showScreen('upload'));
document.getElementById('nav-albums').addEventListener('click', () => showScreen('albums'));
document.getElementById('nav-logout').addEventListener('click', async () => {
  await request('/api/auth/logout', { method: 'POST' });
  showScreen('login');
});

fetchCurrentUser();
