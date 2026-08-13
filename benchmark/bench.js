const fs = require('fs');
const path = require('path');
const { run } = require('./runner');

const flowDir = path.join(__dirname, 'flows');

function available() {
    return fs.readdirSync(flowDir)
        .filter(f => f.endsWith('.js') && f !== 'default.js')
        .map(f => f.replace(/\.js$/, ''))
        .sort();
}

function titleize(id) {
    return id.split(/[-_]/).filter(Boolean)
        .map(word => word.charAt(0).toUpperCase() + word.slice(1))
        .join(' ');
}

function loadFlow(id) {
    const flowPath = path.join(flowDir, `${id}.js`);
    if (fs.existsSync(flowPath)) {
        return { flow: require(flowPath), isDefault: false };
    }
    const fallback = require(path.join(flowDir, 'default.js'));
    return { flow: { ...fallback, benchmark: titleize(id) }, isDefault: true };
}

const args = process.argv.slice(2);
const headed = args.includes('--headed');
const positional = args.filter(a => !a.startsWith('--'));

const name = positional[0];
const variant = positional[1] || 'default';

if (!name) {
    console.log('usage: node bench.js <flow> [variant] [--headed]\n');
    console.log('flows with their own file:');
    for (const flow of available()) console.log(`  ${flow}`);
    console.log('\nany other name runs the default flow, titled from the name given.');
    process.exit(0);
}

const { flow, isDefault } = loadFlow(name);

if (isDefault) {
    console.log(`no flow file for "${name}" — using the default flow\n`);
}

run(flow, variant, { headless: !headed }).catch(err => {
    console.error(err);
    process.exit(1);
});