const path = require("path");
const fs = require("fs");

const root = path.resolve(__dirname, "../../../..");
const web = path.resolve(root, "web");
const mode = config.mode === "production" ? "Production" : "Development";

const standalones = fs.readFileSync(path.resolve(web, "standalones.txt"), "utf8")
    .split(/[,\n]/)
    .map(function (name) { return name.trim(); })
    .filter(Boolean);

const entries = { web: config.entry.main };
standalones.forEach(function (name) {
    entries[name] = [path.resolve(
        web,
        "build/compileSync/js",
        name,
        name + mode + "Executable",
        "kotlin",
        "streetlight-web-" + name + ".js"
    )];
});

config.entry = entries;
config.output.filename = "[name].js";
config.output.library = "[name]";

config.resolve = config.resolve || {};
config.resolve.modules = config.resolve.modules || [];
config.resolve.modules.push(path.resolve(root, "build/js/node_modules"));
