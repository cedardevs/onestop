// -- setupTests.js --
// Called as item in `setupFilesAfterEnv` from `jest.config.js`
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

// when the leaflet map options have the `preferCanvas: true` option set,
// this allows us to mock leaflet for our tests. Refer to:
// https://github.com/Leaflet/Leaflet/issues/6297
Object.defineProperty(HTMLCanvasElement.prototype, 'getContext', {
  value: () => {
    return {
      fillRect: function(){},
      clearRect: function(){},
      putImageData: function(){},
      createImageData: function(){
        return []
      },
      setTransform: function(){},
      drawImage: function(){},
      save: function(){},
      fillText: function(){},
      restore: function(){},
      beginPath: function(){},
      moveTo: function(){},
      lineTo: function(){},
      closePath: function(){},
      stroke: function(){},
      translate: function(){},
      scale: function(){},
      rotate: function(){},
      arc: function(){},
      fill: function(){},
      transform: function(){},
      rect: function(){},
      clip: function(){},
    }
  },
})

//
// global define of lodash for tests
global._ = require('lodash')

// https://medium.com/@kayodeniyi/setting-up-tests-for-react-using-mocha-expect-and-enzyme-8f53af96fe7e
import Enzyme from 'enzyme/build'
import Adapter from 'enzyme-adapter-react-16/build'

Enzyme.configure({adapter: new Adapter()})

// we use `react-a11y-dialog` which leverages React portal and tells us to add a separate dialog root (e.g. - in index.js)
// because our tests use enzyme and mount `App` without this DOM structure, we need to add the #dialog id to the global body
// to ensure the Confirmation dialog is tested appropriately
// see: https://stackoverflow.com/questions/48094581/testing-react-portals-with-enzyme
const modalRoot = global.document.createElement('div')
modalRoot.setAttribute('id', 'dialog')
const body = global.document.querySelector('body')
body.appendChild(modalRoot)
