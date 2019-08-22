import React from 'react'
import {mount} from 'enzyme'
import _ from 'lodash'

import App from '../../src/App'
import store from '../../src/store' // create Redux store with appropriate middleware
import history from '../../src/history'
import ScriptDownloader from '../../src/components/cart/ScriptDownloader'
import mockCartItems from '../mocks/mockCartItems'
import {insertSelectedGranule} from '../../src/actions/CartActions'
import {insertGranule} from '../../src/utils/localStorageUtil'
import {toggleFeatures} from '../../src/actions/ConfigActions'

const debugStore = (label, path) => {
  const state = store.getState()
  const stateSelector = _.get(state, path, state)
  console.log('%s:\n\n%s', label, JSON.stringify(stateSelector, null, 4))
}

describe('The ScriptDownloader component', () => {
  const url = '/cart'
  let component = null

  beforeAll(async () => {
    // populate redux with mock selected granules
    _.forEach(mockCartItems, (item, itemId) => {
      insertGranule(itemId, item)
      store.dispatch(insertSelectedGranule(item, itemId))
    })

    // cart page will only be navigable and have the proper components when cart feature enabled
    const featuresList = [ 'cart' ]
    await store.dispatch(toggleFeatures(featuresList))

    // initialize history to be on the '/cart' route
    history.push(url)

    component = mount(App(store, history))
  })

  it('exists on the /cart page', () => {
    const scriptDownloader = component.find(ScriptDownloader)
    console.log('scriptDownloader:', JSON.stringify(scriptDownloader, null, 4))
    expect(scriptDownloader.length).toBe(1)
  })

  it('has expected downloadable selected granules to select from', () => {
    const scriptDownloader = component.find(ScriptDownloader)
    const props = scriptDownloader.props()
    expect(props.selectedGranules).toEqual(mockCartItems)
  })

  it('should proper non-empty state variables when selected granules are added', () => {
    const scriptDownloader = component.find(ScriptDownloader)

    const state = scriptDownloader.state()

    // these should be the expected unique sources and protocols related to our mock data
    // and in the format that the ScriptDownloader should form them into
    const expectedSourcesAndProtocols = [
      {
        protocol: 'HTTP',
        source: 'HTTP',
      },
      {
        protocol: 'FTP',
        source: 'FTP',
      },
      {
        protocol: 'UNIDATA:THREDDS',
        source: 'THREDDS(TDS)',
      },
      {
        protocol: 'OPeNDAP:Hyrax',
        source: 'THREDDS OPeNDAP',
      },
      {
        protocol: 'HTTP',
        source: 'THREDDS(TDS)',
      },
      {
        protocol: 'HTTP',
        source: 'THREDDS OPeNDAP',
      },
    ]
    expect(expectedSourcesAndProtocols).toEqual(
      expect.arrayContaining(state.sourcesAndProtocols)
    )

    // default selected index is 0
    expect(state.selectedSourceAndProtocol).toBe(0)
  })
})
