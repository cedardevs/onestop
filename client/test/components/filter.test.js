import React from 'react'
import {mount} from 'enzyme'
import { shallow } from 'enzyme';
// import { act } from 'react-dom/test-utils';
import { renderHook, act } from '@testing-library/react-hooks'
import App from '../../src/App'
import store from '../../src/store' // create Redux store with appropriate middleware
import history from '../../src/history'
import {ProxyContext, useProxy} from '../../src/components/common/ui/Proxy'

// import MapFilter from '../../src/components/filters/spatial/MapFilter'
import MapFilter from '../../src/components/filters/spatial/MapFilterSmol'
import GeoFieldset from '../../src/components/filters/spatial/GeoFieldset'
import CoordinateTextbox from '../../src/components/filters/spatial/CoordinateTextbox'

import {useBoundingBox} from '../../src/components/filters/spatial//BoundingBoxEffect'

const debugStore = (label, path) => {
  const state = store.getState()
  const stateSelector = _.get(state, path, state)
  console.log('%s:\n\n%s', label, JSON.stringify(stateSelector, null, 4))
}

describe('The MapFilter component', () => {
  // const url = '/collections'
  // let component = null
  //
  // beforeAll(async () => {
  //   component = mount(App(store, history))
  //   history.push(url)
  //   // force a re-render after pushing to history so that our component hierarchy look as if we are on that page
  //   component.update()
  // })

  // it('exists', () => {
  //   const filter = component.find(MapFilter)
  //   console.log('scriptDownloader:', JSON.stringify(filter))
  //   expect(filter.length).toBe(1)
  //
  //
  // })

//   it('...', () => {
//
//
//   //   const MapProxyContext = ProxyContext()
//   //   const mapProxy = useProxy(false)
//   //   const component = mount(<MapProxyContext.Provider value={mapProxy}><Proxy context={MapProxyContext} /><MapFilter showMap={false} bbox="" geoRelationship="" excludeGlobal={false} updateGeoRelationship={()=> {console.log('update relationship')}}
//   //   toggleExcludeGlobal={()=> {console.log('toggle global')}}
//   //   submit={()=> {console.log('submit')}}
//   //   openMap={()=> {console.log('open map')}}
//   //   closeMap={()=> {console.log('close map')}}
//   //   removeGeometry={()=> {console.log('clear geometry')}}
//   //   handleNewGeometry={()=> {console.log('update geometry')}}  /> </MapProxyContext.Provider>)
//   // const input = component.find('input').at(0);
//   // input.instance().value = 'hello';
//   // input.simulate('change');
//   // expect(component.state().firstname).toEqual('hello');
//
//
//   const component = mount(<MapFilter showMap={false} bbox="" geoRelationship="" excludeGlobal={false} updateGeoRelationship={()=> {console.log('update relationship')}}
//   toggleExcludeGlobal={()=> {console.log('toggle global')}}
//   submit={()=> {console.log('submit')}}
//   openMap={()=> {console.log('open map')}}
//   closeMap={()=> {console.log('close map')}}
//   removeGeometry={()=> {console.log('clear geometry')}}
//   handleNewGeometry={()=> {console.log('update geometry')}}  /> )
//   console.log('wtf?', component)
//   expect(component.containsMatchingElement(GeoFieldset)).toBeTruthy()
//   expect(component.find(CoordinateTextbox).length).toEqual(4)
// const coordTextbox = component.find(CoordinateTextbox).at(0)
//   const input = coordTextbox.find('input').at(0)
//   console.log(input.html())
// input.instance().value = 'hello';
// // input.simulate('change', { target: { value: 'hello' } }); // TODO not doing anything
// // input.change({ target: { value: 'hello' } })
// // console.log(input.getDOMNode())
// console.log(coordTextbox.props())
// // coordTextbox.update()
// component.update()
// console.log(coordTextbox.props())
//
// expect(coordTextbox.prop('value')).toEqual('hello')
// expect(coordTextbox.props().valid).toBeFalsy();
//   // expect(input.getDOMNode())
// // expect(component.state().firstname).toEqual('hello');
//   })

function HookWrapper(props) {
  const hook = props.hook ? props.hook() : undefined;
  return <div hook={hook} />;
}

it('should set init value - no bbox', () => {
  // let wrapper = shallow(<HookWrapper hook={() => useBoundingBox(null)} />);
let wrapper = null
act(() => {
  wrapper= shallow(<HookWrapper hook={() => useBoundingBox(null)} />);
})
  let { hook } = wrapper.find('div').props();
  let [bounds] = hook;
  expect(bounds.east.value).toEqual('');
  expect(bounds.north.value).toEqual('');
  expect(bounds.south.value).toEqual('');
  expect(bounds.west.value).toEqual('');

});
it('should set init value - bbox', () => {
  // let wrapper = null
  // act(() => {
  //   wrapper= mount(<HookWrapper hook={() => useBoundingBox({north: 34, south: -20, west: 17, east: -123})} />);
  // })
  let hook = null
  act(() => {

  const { result } = renderHook(() => useBoundingBox({north: 34, south: -20, west: 17, east: -123}))

  hook = result

  })

  // let { hook } = wrapper.find('div').props();
  console.log('????', hook)
  let [bounds] = hook.current;
  console.log('????', bounds)
  expect(bounds.east.value).toEqual('-123');
  expect(bounds.north.value).toEqual('34');
  expect(bounds.south.value).toEqual('-20');
  expect(bounds.west.value).toEqual('17');
});

it('should update', () => {
  // let wrapper = null
  // act(() => {
  //   wrapper= mount(<HookWrapper hook={() => useBoundingBox({north: 34, south: -20, west: 17, east: -123})} />);
  // })
  let hook = null
  act(() => {

  const { result } = renderHook(() => useBoundingBox(null))

  hook = result

  })
  act(() => {
    let [bounds] = hook.current;
    bounds.west.set("111")
  })

  // let { hook } = wrapper.find('div').props();
  let [bounds] = hook.current;
  expect(bounds.west.value).toEqual('111');
  expect(bounds.west.number).toEqual(111);
  expect(bounds.west.valid).toBeTruthy();
});

it('should update -  invalid', () => {
  // let wrapper = null
  // act(() => {
  //   wrapper= mount(<HookWrapper hook={() => useBoundingBox({north: 34, south: -20, west: 17, east: -123})} />);
  // })
  let hook = null
  act(() => {

  const { result } = renderHook(() => useBoundingBox(null))

  hook = result

  })
  act(() => {
    let [bounds] = hook.current;
    bounds.west.set("abc")
  })

  // let { hook } = wrapper.find('div').props();
  let [bounds] = hook.current;
  expect(bounds.west.value).toEqual('abc');
  expect(bounds.west.number).toBeNull();
  expect(bounds.west.valid).toBeFalsy();
});
})
