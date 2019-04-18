import {createBrowserHistory, createMemoryHistory} from 'history'

// - Calling createBrowserHistory w/no browser causes issues with tests, even using 'jsdom' and 'jsdom-global'.
// - This flag will never be set in prod because NODE_ENV would be undefined in our static bundle anyway.
// - Setting NODE_ENV to 'test' is done by the package.json test scripts, which actually utilize Node.js.
const isTest = process.env.NODE_ENV === 'test'

import {getBasePath} from './utils/urlUtils'

const history = isTest
  ? // memory history does not offer a basename, but this is okay for testing purposes anyway
    createMemoryHistory()
  : // setting a basename for browser history makes routing concise in app and simplifies web server configuration
    createBrowserHistory({basename: getBasePath()})

export default history
