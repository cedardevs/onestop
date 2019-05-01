import {layout, initialState} from '../../src/reducers/layout'
// import {push} from 'react-router-redux'
import {LOCATION_CHANGE} from 'react-router-redux'

const pushMock = descriptor => {
  // using push from react-router-redux does not directly trigger the LOCATION_CHANGE action, it triggers a history action
  return {
    type: LOCATION_CHANGE,
    payload: descriptor,
  }
}

describe('The layout reducer', function(){
  it('sets showLeft to true for collections', function(){
    const locationDescriptor = {
      pathname: '/collections',
      search: '?q=dem',
    }
    const action = pushMock(locationDescriptor)
    const result = layout(initialState, action)
    expect(result.showLeft).toBeTruthy()
  })

  it('sets showLeft to false for details', function(){
    const locationDescriptor = {
      pathname: '/collections/details/ASDF',
      search: '?q=dem',
    }
    const action = pushMock(locationDescriptor)
    const result = layout(initialState, action)
    expect(result.showLeft).toBeFalsy()
  })
})
