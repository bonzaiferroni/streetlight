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

function parseCondition(spec, flow) {
    const at = spec.lastIndexOf('@');
    if (at === -1) return { variant: spec, url: flow.url };

    const variant = spec.slice(0, at);
    const target = spec.slice(at + 1);
    const url = /^https?:\/\//.test(target)
        ? target
        : new URL(flow.url).origin.replace(/:\d+$/, '') + `:${target}/`;

    return { variant, url };
}

const args = process.argv.slice(2);
const headed = args.includes('--headed');
const positional = args.filter(a => !a.startsWith('--'));

const name = positional[0];

if (!name) {
    console.log('usage: node bench.js <flow> [variant[@port|@url] ...] [--headed]\n');
    console.log('flows with their own file:');
    for (const flow of available()) console.log(`  ${flow}`);
    console.log('\nany other name runs the default flow, titled from the name given.');
    console.log('give two or more variants to interleave them in randomized order:');
    console.log('  node bench.js flow-transition scoped@8080 always-on@8081 --headed');
    process.exit(0);
}

const { flow, isDefault } = loadFlow(name);

if (isDefault) {
    console.log(`no flow file for "${name}" — using the default flow\n`);
}

const specs = positional.slice(1);
const conditions = specs.length > 0
    ? specs.map(s => parseCondition(s, flow))
    : [{ variant: 'default', url: flow.url }];

run(flow, conditions, { headless: !headed }).catch(err => {
    console.error(err);
    process.exit(1);
});