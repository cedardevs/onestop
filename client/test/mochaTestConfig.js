// mocha mochaTestSetup.js "components/*.spec.js"

const jsdom = require('jsdom').jsdom
require('jsdom-global')()
process.env.NODE_ENV = 'test'

// drop-in replacement of localStorage for tests
// if (typeof localStorage === "undefined" || localStorage === null) {
//   let LocalStorage = require('node-localstorage').LocalStorage;
//   global.localStorage = new LocalStorage('./scratch');
// }

// shim for tests to avoid this warning:
// "React depends on requestAnimationFrame. Make sure that you load a polyfill in older browsers."
global.requestAnimationFrame = cb => {
  setTimeout(cb, 0)
}
global.cancelAnimationFrame = af => {}

// global define for leaflet-draw use of `L` in tests
global.L = require('leaflet')

// global define of lodash for tests
global._ = require('lodash')

// https://medium.com/@kayodeniyi/setting-up-tests-for-react-using-mocha-expect-and-enzyme-8f53af96fe7e
const enzyme = require('enzyme')
const Adapter = require('enzyme-adapter-react-16')
enzyme.configure({adapter: new Adapter()})

// disable webpack-specific loader imports for tests
const disabledImportExtensions = [
  '.css',
  '.scss',
  '.png',
  '.jpg',
  '.svg',
  '.xml',
  '.ttf',
]
disabledImportExtensions.forEach(ext => {
  require.extensions[ext] = () => null
})
