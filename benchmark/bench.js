const fs = require('fs');
const path = require('path');
const { run } = require('./runner');

const flowDir = path.join(__dirname, 'flows');

function available() {
    return fs.readdirSync(flowDir)
        .filter(f => f.endsWith('.js'))
        .map(f => f.replace(/\.js$/, ''))
        .sort();
}

const name = process.argv[2];

if (!name) {
    console.log('usage: node bench.js <flow> [variant]\n');
    console.log('available flows:');
    for (const flow of available()) console.log(`  ${flow}`);
    process.exit(0);
}

const flowPath = path.join(flowDir, `${name}.js`);

if (!fs.existsSync(flowPath)) {
    console.error(`no flow named "${name}"\n`);
    console.error('available flows:');
    for (const flow of available()) console.error(`  ${flow}`);
    process.exit(1);
}

const variant = process.argv[3] || 'default';

run(require(flowPath), variant).catch(err => {
    console.error(err);
    process.exit(1);
});