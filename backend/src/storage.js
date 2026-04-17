const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const storagePath = path.resolve(__dirname, '../../storage');

if (!fs.existsSync(storagePath)) {
  fs.mkdirSync(storagePath, { recursive: true });
}

function buildFileUrl(filename) {
  return `/uploads/${filename}`;
}

function getSavedFilePath(filename) {
  return path.join(storagePath, filename);
}

function saveUploadedFile(file) {
  const ext = path.extname(file.originalname).toLowerCase();
  const safeExt = ext || '.bin';
  const uniqueName = `${crypto.randomUUID()}${safeExt}`;
  const destination = getSavedFilePath(uniqueName);
  fs.writeFileSync(destination, file.buffer);
  return uniqueName;
}

module.exports = { buildFileUrl, saveUploadedFile, getSavedFilePath };
