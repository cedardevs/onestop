module.exports = function (api) {

  const presetsDefault = [
    ["@babel/preset-env", {"modules": false}],
    "@babel/preset-react",
  ]

  const presetsTest = [
    ["@babel/preset-env"],
    "@babel/preset-react",
  ]

  const presets = api.env('test') ? presetsTest : presetsDefault

  const plugins = [
    // Stage 2
    ["@babel/plugin-proposal-decorators", { "legacy": true }],
    "@babel/plugin-proposal-function-sent",
    "@babel/plugin-proposal-export-namespace-from",
    "@babel/plugin-proposal-numeric-separator",
    "@babel/plugin-proposal-throw-expressions",

    // Stage 3
    "@babel/plugin-syntax-dynamic-import",
    "@babel/plugin-syntax-import-meta",
    ["@babel/plugin-proposal-class-properties", { "loose": false }],
    "@babel/plugin-proposal-json-strings"
  ]

  return {
    presets: presets,
    plugins: plugins,
  }

}