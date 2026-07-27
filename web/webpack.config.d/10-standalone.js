const path = require("path");

const root = path.resolve(__dirname, "../../../..");

const executable = config.mode === "production"
    ? "passwordResetProductionExecutable"
    : "passwordResetDevelopmentExecutable";

config.entry = {
    web: config.entry.main,
    passwordReset: [
        path.resolve(
            root,
            "web/build/compileSync/js/passwordReset",
            executable,
            "kotlin/streetlight-web-passwordReset.js"
        )
    ]
};

config.resolve = config.resolve || {};
config.resolve.modules = config.resolve.modules || [];
config.resolve.modules.push(path.resolve(root, "build/js/node_modules"));

config.output.filename = "[name].js";
config.output.library = "[name]";
