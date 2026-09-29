#!/usr/bin/env node
/*
 * Renders what the first-person hand shows for an item model (resting pose):
 *
 *   node hand.cjs --out hand.png [--hand left|right] [--display '{"rotation":[..],"translation":[..],"scale":[..]}'] selarium:item/selarium_codex_open
 */
const http = require('http');
const fs = require('fs');
const path = require('path');

let chromium;
try { ({ chromium } = require('playwright')); }
catch (e) {
  const root = require('child_process').execSync('npm root -g').toString().trim();
  ({ chromium } = require(path.join(root, 'playwright')));
}

const repoRoot = path.resolve(__dirname, '..', '..');
const args = process.argv.slice(2);
const opt = { out: 'hand.png', hand: 'right', display: null };
let model = null;
for (let i = 0; i < args.length; i++) {
  if (args[i] === '--out') opt.out = args[++i];
  else if (args[i] === '--hand') opt.hand = args[++i];
  else if (args[i] === '--display') opt.display = JSON.parse(args[++i]);
  else model = args[i];
}
if (!model) { console.error('no model given'); process.exit(2); }

const MIME = { '.html': 'text/html', '.js': 'text/javascript', '.mjs': 'text/javascript', '.json': 'application/json', '.png': 'image/png' };
const server = http.createServer((req, res) => {
  const file = path.join(repoRoot, decodeURIComponent(req.url.split('?')[0]));
  if (!file.startsWith(repoRoot) || !fs.existsSync(file) || fs.statSync(file).isDirectory()) { res.writeHead(404); res.end(); return; }
  res.writeHead(200, { 'Content-Type': MIME[path.extname(file)] || 'application/octet-stream' });
  fs.createReadStream(file).pipe(res);
});

(async () => {
  await new Promise((r) => server.listen(0, '127.0.0.1', r));
  const port = server.address().port;
  const launch = { args: ['--use-gl=swiftshader', '--enable-webgl', '--ignore-gpu-blocklist', '--enable-unsafe-swiftshader'] };
  if (process.env.CHROMIUM_PATH) launch.executablePath = process.env.CHROMIUM_PATH;
  const browser = await chromium.launch(launch);
  const page = await browser.newPage();
  page.on('pageerror', (e) => console.error('[pageerror]', e.message));
  await page.goto(`http://127.0.0.1:${port}/tools/preview/preview.html`);
  await page.waitForFunction('window.previewReady === true', null, { timeout: 30000 });
  const id = model.includes(':') ? model : `selarium:${model}`;
  const dataUrl = await page.evaluate(([m, h, d]) => window.renderHand(m, { hand: h, display: d }), [id, opt.hand, opt.display]);
  fs.writeFileSync(opt.out, Buffer.from(dataUrl.split(',')[1], 'base64'));
  console.log(`wrote ${opt.out}`);
  await browser.close();
  server.close();
})().catch((e) => { console.error(e); process.exit(1); });
