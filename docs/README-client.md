# OneStop Browser Client

## Getting started
This guide focuses on running the OneStop browser client via `Node` for local development. To run the search API (used by the client) and other components, refer to the more expansive [Developer Guide](/docs/development).

1. Clone this repo
1. `cd client`
1. Run `npm install`
    - installs dependencies into `./node_modules`
1. Run one of the following:
  - `npm run dev` (search API on port 8097)
  - `npm run kub` (search API on port 30097)
  - Starts up `webpack-dev-server` hosting a hot-reload version of the client
  - Starts mocha in watch mode to automatically run the tests as you work
1. Go to http://localhost:9090/onestop/

## Format JavaScript+CSS with Prettier

Prettier is a code formatter . We actually use `prettier-miscellaneous` which is a fork allowing for more configuration. In particular, the `breakBeforeElse` option specified in our `.prettierrc.json` is [not supported](https://github.com/prettier/prettier/issues/840) by the default `prettier` package.

Formatting is a manual process, but our CI builds will warn when a commit is not formatted.

Ideally, these should be done before every commit by the developer.

### Dry-run with `--list-different`
`npm run formatCheck`

### Overwrite files with fixed formatting
`npm run format`

## Stack
### Development and Build Environment
  - npm
  - babel
  - webpack
  - es6

### Testing, and Code Quality
  - jest
  - enzyme
  - eslint

### Client-Side Libraries/Frameworks
  - react
  - redux
  - seamless-immutable
  - isomorphic-fetch
  - leaflet
  - etc...

## Project Structure

### Source Files

- **src/**
    - high-level entry points, configurations, and wrappers
- **src/actions/**
    - [Redux Actions](https://redux.js.org/basics/actions)
- **src/components/**
    - [React Components](https://reactjs.org/docs/react-component.html) and [Redux Containers](https://redux.js.org/basics/usage-with-react)
- **src/fonts/**
    - until we can use external fonts, we are forced to package our own nicer fonts (bloats our bundle!)
- **src/reducers/**
    - [Redux Reducers](https://redux.js.org/basics/reducers) specifying how the state changes in response to actions sent to the store.
- **src/style/**
    - CSS Stylesheets (where React inline styles are insufficient) and other style-related utilities.
- **src/utils/**
    - Common utility functions to facilitate easier unit testing outside the context of React lifecycles.

### Test Files
- ./test
    - TODO: explain test configs and etc...

### JSON schema validation
The requests to api are validated against a JSON schema.
Here are a few links related to validation and schema.
- [JSON Schema](http://json-schema.org/)
- [Sample Geometry Schema](https://github.com/fge/sample-json-schemas/blob/master/geojson/geometry.json)
- [JSON Schema Store](http://json.schemastore.org)
