import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';

const PORT = Number(process.env.DEFAULT_APP_PORT || 3000);
const ROOT = process.cwd();

const ARTIFACTS = {
  apk: {
    filename: 'scan2money-debug.apk',
    contentType: 'application/vnd.android.package-archive',
    candidates: [
      path.join(ROOT, 'src', 'scan2money-debug.apk'),
      path.join(ROOT, '.build-outputs', 'app-debug.apk'),
      path.join(ROOT, 'app', 'build', 'outputs', 'apk', 'debug', 'app-debug.apk'),
    ],
  },
  aab: {
    filename: 'scan2money-debug.aab',
    contentType: 'application/octet-stream',
    candidates: [
      path.join(ROOT, 'src', 'scan2money-debug.aab'),
      path.join(ROOT, 'app', 'build', 'outputs', 'bundle', 'debug', 'app-debug.aab'),
    ],
  },
};

function resolveArtifact(key) {
  const entry = ARTIFACTS[key];
  if (!entry) return null;
  for (const filePath of entry.candidates) {
    try {
      const stat = fs.statSync(filePath);
      if (stat.isFile() && stat.size > 0) {
        return { ...entry, filePath, sizeBytes: stat.size };
      }
    } catch {
      // Try next candidate path
    }
  }
  return null;
}

function formatMb(bytes) {
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function renderStatusHtml() {
  const apk = resolveArtifact('apk');
  const aab = resolveArtifact('aab');

  const apkMeta = apk ? formatMb(apk.sizeBytes) : 'Building...';
  const aabMeta = aab ? formatMb(aab.sizeBytes) : 'Run gradle bundleDebug';

  return `<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>SCAN2MONEY — Native Android Build Artifacts</title>
  <style>
    :root { color-scheme: dark; }
    * { box-sizing: border-box; }
    body {
      margin: 0;
      min-height: 100vh;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
      background: #0b1116;
      color: #f3f7fa;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 24px;
    }
    .card {
      width: 100%;
      max-width: 640px;
      background: #121a22;
      border: 1px solid #243240;
      border-radius: 16px;
      padding: 28px;
      box-shadow: 0 20px 50px rgba(0, 0, 0, 0.45);
    }
    .tag {
      display: inline-block;
      font-size: 12px;
      font-weight: 700;
      letter-spacing: 0.08em;
      text-transform: uppercase;
      color: #c8f135;
      margin-bottom: 10px;
    }
    h1 {
      margin: 0 0 10px;
      font-size: 24px;
      line-height: 1.2;
    }
    p {
      margin: 0 0 20px;
      color: #a9b8c6;
      font-size: 14px;
      line-height: 1.55;
    }
    .grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 12px;
      margin-bottom: 20px;
    }
    .btn {
      display: flex;
      flex-direction: column;
      gap: 4px;
      padding: 14px 16px;
      border-radius: 12px;
      text-decoration: none;
      border: 1px solid #2c3d4f;
      background: #18232e;
      color: #ffffff;
      transition: border-color 140ms ease, background 140ms ease;
    }
    .btn.primary {
      background: #c8f135;
      color: #0b1116;
      border-color: #c8f135;
    }
    .btn:hover { opacity: 0.94; }
    .btn strong { font-size: 15px; }
    .btn span { font-size: 12px; opacity: 0.85; }
    .btn.disabled { pointer-events: none; opacity: 0.5; }
    .specs {
      background: #0d141b;
      border: 1px solid #1e2b38;
      border-radius: 10px;
      padding: 14px 16px;
      font-size: 13px;
      color: #c2ced9;
    }
    .specs div {
      display: flex;
      justify-content: space-between;
      gap: 12px;
      padding: 5px 0;
      border-bottom: 1px solid #18232e;
    }
    .specs div:last-child { border-bottom: 0; }
    code {
      font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
      font-size: 12px;
      color: #9ae6b4;
    }
  </style>
</head>
<body>
  <main class="card">
    <div class="tag">Native Android App • Kotlin &amp; Jetpack Compose</div>
    <h1>SCAN2MONEY Build Downloads</h1>
    <p>
      All web app code has been removed. Download the compiled native Android binaries directly
      from <code>/src</code> or launch the Android Emulator preview.
    </p>
    <div class="grid">
      <a class="btn primary ${apk ? '' : 'disabled'}" href="/download/apk">
        <strong>Download Android APK</strong>
        <span>scan2money-debug.apk • ${apkMeta}</span>
      </a>
      <a class="btn ${aab ? '' : 'disabled'}" href="/download/aab">
        <strong>Download Play Bundle (AAB)</strong>
        <span>scan2money-debug.aab • ${aabMeta}</span>
      </a>
    </div>
    <section class="specs">
      <div><span>Package ID</span><code>com.aistudio.scan2money.k7m4px</code></div>
      <div><span>Stack</span><code>Kotlin 2.1 • Compose M3 • CameraX • ML Kit</code></div>
      <div><span>Backend</span><code>Firebase Auth + Firestore</code></div>
      <div><span>ADB Install</span><code>adb install src/scan2money-debug.apk</code></div>
    </section>
  </main>
</body>
</html>`;
}

const server = http.createServer((req, res) => {
  const urlPath = (req.url || '/').split('?')[0];

  if (req.method !== 'GET' && req.method !== 'HEAD') {
    res.writeHead(405, { 'Content-Type': 'text/plain; charset=utf-8' });
    res.end('Method Not Allowed');
    return;
  }

  if (urlPath === '/health') {
    res.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8' });
    res.end(JSON.stringify({ status: 'ok', app: 'SCAN2MONEY', platform: 'android' }));
    return;
  }

  if (urlPath === '/' || urlPath === '/index.html') {
    const html = renderStatusHtml();
    res.writeHead(200, {
      'Content-Type': 'text/html; charset=utf-8',
      'Cache-Control': 'no-cache',
    });
    if (req.method === 'HEAD') {
      res.end();
    } else {
      res.end(html);
    }
    return;
  }

  if (urlPath === '/download/apk' || urlPath === '/download/aab') {
    const key = urlPath === '/download/apk' ? 'apk' : 'aab';
    const artifact = resolveArtifact(key);
    if (!artifact) {
      res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
      res.end('Artifact not built yet.');
      return;
    }
    res.writeHead(200, {
      'Content-Type': artifact.contentType,
      'Content-Length': String(artifact.sizeBytes),
      'Content-Disposition': `attachment; filename="${artifact.filename}"`,
      'Cache-Control': 'no-store',
    });
    if (req.method === 'HEAD') {
      res.end();
      return;
    }
    fs.createReadStream(artifact.filePath).pipe(res);
    return;
  }

  res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
  res.end('Not Found');
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`SCAN2MONEY Android artifact server listening on port ${PORT}`);
});
