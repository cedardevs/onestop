import React from 'react'
import {mount} from 'enzyme'

import App from '../../src/App'
import store from '../../src/store' // create Redux store with appropriate middleware
import history from '../../src/history'
import ScriptDownloader from '../../src/cart/ScriptDownloader'
import mockCartItems from './mockCartItems'

const debugStore = (label, path) => {
  const state = store.getState()
  const stateSelector = _.get(state, path, state)
  console.log('%s:\n\n%s', label, JSON.stringify(stateSelector, null, 4))
}

describe('The ScriptDownloader component', () => {
  const url = '/'
  let component = null

  beforeAll(() => {
    history.push(url)
    component = mount(App(store, history))
  })

  it('should render once', () => {
    const scriptDownloader = component.find(ScriptDownloader)
    expect(scriptDownloader.length).toBe(1)
  })

  it('should have proper initial state variables when no selected granules', () => {
    const scriptDownloader = component.find(ScriptDownloader)
    const state = scriptDownloader.state()
    expect(state.sourcesAndProtocols).arrayContaining([])
    expect(state.selectedSourceAndProtocol).toBe(0)
  })

  it('should proper non-empty state variables when selected granules are added', () => {
    const scriptDownloader = component.find(ScriptDownloader)

    // add selected granules
    const selectedGranules = mockCartItems
    scriptDownloader.setProps({selectedGranules: selectedGranules})

    const state = scriptDownloader.state()

    expect(state.sourcesAndProtocols).arrayContaining([])
    expect(state.selectedSourceAndProtocol).toBe(0)
  })
})
