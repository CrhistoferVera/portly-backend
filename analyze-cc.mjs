import { readFileSync } from 'fs';

const xml = readFileSync('target/pmd.xml', 'utf8');
const fileMatches = [...xml.matchAll(/<file name="([^"]+)">([\s\S]*?)<\/file>/g)];
const complexities = [];

for (const [, filePath, content] of fileMatches) {
  const fname = filePath.replace(/\\/g, '/').split('/').at(-1);
  const violations = [...content.matchAll(/<violation[^>]+method="([^"]+)"[^>]*>([\s\S]*?)<\/violation>/g)];
  for (const [, method, msg] of violations) {
    const m = msg.match(/complexity of (\d+)/);
    if (m) complexities.push({ cc: parseInt(m[1]), method, file: fname });
  }
}

if (!complexities.length) {
  // No violations above threshold — parse all methods from the raw report
  console.log('Sin violaciones sobre el umbral (10). Mostrando muestra del XML:');
  console.log(xml.substring(0, 800));
} else {
  complexities.sort((a, b) => b.cc - a.cc);
  const total = complexities.reduce((s, x) => s + x.cc, 0);
  const avg = (total / complexities.length).toFixed(1);
  console.log(`Metodos con CC > umbral : ${complexities.length}`);
  console.log(`Complejidad maxima      : CC=${complexities[0].cc}  ${complexities[0].method} (${complexities[0].file})`);
  console.log(`Promedio (alta)         : ${avg}`);
  console.log('\nTop 10 metodos mas complejos:');
  complexities.slice(0, 10).forEach(x =>
    console.log(`  CC=${String(x.cc).padStart(3)}  ${x.method}  (${x.file})`)
  );
}
