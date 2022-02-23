module.exports = {
  testEnvironment: "jsdom",
  verbose: true,
  coverageDirectory: "./build/coverage/",
  collectCoverage: true,
  collectCoverageFrom: [
    "src/**/*.{js,jsx}"
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
    [ "jest-junit", { outputDirectory: "./build/junit" } ]
  ],
  moduleNameMapper: {
    "\\.(jpg|jpeg|png|gif|eot|otf|webp|svg|ttf|woff|woff2|mp4|webm|wav|mp3|m4a|aac|oga)$": "<rootDir>/test/fileMock.js",
    "\\.(css|less)$": "<rootDir>/test/styleMock.js"
  },
  // fixes 'regeneratorRuntime is not defined' errors for async tests
  setupFiles: ["<rootDir>/node_modules/regenerator-runtime/runtime"],
  setupFilesAfterEnv: ["<rootDir>/test/setupTests.js"]
};
