import express from 'express';
import multer from 'multer';
import FormData from 'form-data';
import fetch from 'node-fetch';

const app = express();
const upload = multer({
  fileFilter: (req, file, cb) => {
    if (file.mimetype.startsWith('image/')) cb(null, true);
    else cb(new Error('只允许上传图片文件'));
  },
});

app.post('/upload', upload.single('image'), (req, res) => {
  if (!req.file) return res.status(400).json({ msg: 'no file' });
  res.json({ file: req.file });
});

const server = app.listen(9999, async () => {
  const form = new FormData();
  form.append('image', Buffer.from('fake'), { filename: 'a.jpg', contentType: 'image/*' });
  try {
      const res = await fetch('http://localhost:9999/upload', { method: 'POST', body: form });
      console.log(res.status, await res.text());
  } catch (e) {
      console.error(e);
  }
  server.close();
});
