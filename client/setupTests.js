// -- setupTests.js --
// Called as `setupTestFrameworkScriptFile` from `jest.config.js`
// See: https://jestjs.io/docs/en/configuration.html#setuptestframeworkscriptfile-string

// const jsdom = require('jsdom').jsdom
// require('jsdom-global')()
// process.env.NODE_ENV = 'test'
//
// // shim for tests to avoid this warning:
// // "React depends on requestAnimationFrame. Make sure that you load a polyfill in older browsers."
// global.requestAnimationFrame = cb => {
//   setTimeout(cb, 0)
// }
// global.cancelAnimationFrame = af => {}
//
// // global define for leaflet-draw use of `L` in tests
// global.L = require('leaflet')
//
// global define of lodash for tests
global._ = require('lodash')

// https://medium.com/@kayodeniyi/setting-up-tests-for-react-using-mocha-expect-and-enzyme-8f53af96fe7e
import Enzyme from 'enzyme'
import Adapter from 'enzyme-adapter-react-16'

Enzyme.configure({ adapter: new Adapter() })