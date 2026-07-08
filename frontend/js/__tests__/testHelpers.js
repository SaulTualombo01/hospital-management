const fs = require('fs');
const path = require('path');
const vm = require('vm');

function loadScript(fileName, exportNames, extraGlobals = {}) {
  const filePath = path.join(__dirname, '..', fileName);
  const source = fs.readFileSync(filePath, 'utf8');

  const sandbox = {
    console,
    document: global.document,
    window: global.window,
    navigator: global.navigator,
    alert: global.alert,
    fetch: global.fetch,
    setTimeout,
    clearTimeout,
    Date,
    Intl,
    JSON,
    Math,
    Promise,
    ...extraGlobals,
  };

  sandbox.globalThis = sandbox;
  sandbox.self = sandbox;

  vm.createContext(sandbox);
  vm.runInContext(
    `${source}\n;globalThis.__testExports = { ${exportNames.join(', ')} };`,
    sandbox,
    { filename: filePath }
  );

  return sandbox.__testExports;
}

module.exports = { loadScript };