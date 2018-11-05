module.exports = {
  verbose: true,
  coverageDirectory: "./coverage/",
  collectCoverage: true,
  collectCoverageFrom: [
    "**/*.{js,jsx}"
  ],
  coverageReporters: [
    "text-summary",
    "html",
    "lcov"
  ],
  coveragePathIgnorePatterns: [
    "/node_modules/",
    "/test/",
  ],
  coverageThreshold: {
    // "global": {
    //   "branches": 50,
    //   "functions": 50,
    //   "lines": 50,
    //   "statements": 50
    // },
    // "./src/reducers/**/*.js": {
    //   "statements": 90
    // },
    // "./src/utils/**/*.js": {
    //   "branches": 70,
    //   "functions": 80,
    //   "lines": 70,
    //   "statements": 70
    // },
  },
  testMatch: [ "**/test/**/*.test.js?(x)"],
  reporters: [
    "default",
    [ "jest-junit", { outputDirectory: "./coverage/junit" } ]
  ]
};
