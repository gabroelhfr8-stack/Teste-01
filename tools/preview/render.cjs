#!/usr/bin/env node
/*
 * Renders Minecraft JSON models (selarium:block/..., selarium:item/...) to a PNG contact sheet.
 *
 *   npm install                       # once, in tools/preview
 *   node render.cjs --out sheet.png --cell 320 --cols 4 --view gui block/arcane_grinder block/mana_tank
 *
 * Views: gui (uses the model's own display.gui transform), front, north, top, gui2 (mirrored iso).
 * Textures from the minecraft: namespace are not available and show as magenta/black.
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
const opt = { out: 'preview.png', cell: 320, cols: 4, view: 'gui' };
const models = [];
for (let i = 0; i < args.length; i++) {
  const a = args[i];
  if (a === '--out') opt.out = args[++i];
  else if (a === '--cell') opt.cell = parseInt(args[++i], 10);
  else if (a === '--cols') opt.cols = parseInt(args[++i], 10);
  else if (a === '--view') opt.view = args[++i];
  else models.push(a);
}
if (!models.length) { console.error('no models given'); process.exit(2); }

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
  page.on('console', (m) => { if (m.type() === 'error') console.error('[page]', m.text()); });
  page.on('pageerror', (e) => console.error('[pageerror]', e.message));
  await page.goto(`http://127.0.0.1:${port}/tools/preview/preview.html`);
  await page.waitForFunction('window.previewReady === true', null, { timeout: 30000 });
  const specs = models.map((m) => {
    const [model, view] = m.split('@');
    const id = model.includes(':') ? model : `selarium:${model}`;
    return { model: id, label: model.replace('selarium:', ''), view: view || undefined };
  });
  const dataUrl = await page.evaluate(([s, c, n, v]) => window.renderSheet(s, c, n, v), [specs, opt.cell, opt.cols, opt.view]);
  fs.writeFileSync(opt.out, Buffer.from(dataUrl.split(',')[1], 'base64'));
  console.log(`wrote ${opt.out} (${specs.length} models)`);
  await browser.close();
  server.close();
})().catch((e) => { console.error(e); process.exit(1); });
