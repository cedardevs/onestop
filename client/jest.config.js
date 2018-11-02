module.exports = {
  // verbose: true,
  coverageDirectory: "./coverage/",
  collectCoverage: true,
  coverageReporters: [
    "text",
    "text-summary",
    "html",
    "lcov"
  ],
  reporters: [
    "default",
    ["./node_modules/jest-html-reporter", {
        "pageTitle": "Test Report",
        "outputPath": "./coverage/junit/index.html"
    }]
  ]
};
// {
//   "coverageDirectory": "./coverage/",
//   "collectCoverage": true,
//   "coverageReporters": [
//     "text",
//     "text-summary",
//     "html",
//     "lcov"
//   ],
//   "reporters": [
//     "default",
//     ["./node_modules/jest-html-reporter", {
//         "pageTitle": "Test Report",
//         "outputPath": "./coverage/junit/index.html"
//     }]
//   ]
// }
