import React from 'react'
import {renderHook, act} from '@testing-library/react-hooks'

import {useBoundingBox} from '../../../../src/components/filters/spatial/BoundingBoxEffect'

describe('The BoundingBoxEffect hook', () => {
  it('should set init value - no bbox', () => {
    let hook = null
    act(() => {
      const {result} = renderHook(() => useBoundingBox(null))

      hook = result
    })
    // let { hook } = wrapper.find('div').props();
    let [ bounds ] = hook.current
    expect(bounds.east.value).toEqual('')
    expect(bounds.north.value).toEqual('')
    expect(bounds.south.value).toEqual('')
    expect(bounds.west.value).toEqual('')
  })
  it('should set init value - bbox', () => {
    // let wrapper = null
    // act(() => {
    //   wrapper= mount(<HookWrapper hook={() => useBoundingBox({north: 34, south: -20, west: 17, east: -123})} />);
    // })
    let hook = null
    act(() => {
      const {result} = renderHook(() =>
        useBoundingBox({north: 34, south: -20, west: 17, east: -123})
      )

      hook = result
    })

    // let { hook } = wrapper.find('div').props();
    console.log('????', hook)
    let [ bounds ] = hook.current
    console.log('????', bounds)
    expect(bounds.east.value).toEqual('-123')
    expect(bounds.north.value).toEqual('34')
    expect(bounds.south.value).toEqual('-20')
    expect(bounds.west.value).toEqual('17')
  })

  it('should update', () => {
    let hook = null
    act(() => {
      const {result} = renderHook(() => useBoundingBox(null))

      hook = result
    })
    act(() => {
      let [ bounds ] = hook.current
      bounds.west.set('111')
    })

    // let { hook } = wrapper.find('div').props();
    let [ bounds ] = hook.current
    expect(bounds.west.value).toEqual('111')
    expect(bounds.west.number).toEqual(111)
    expect(bounds.west.valid).toBeTruthy()
  })

  it('should update -  invalid', () => {
    let hook = null
    act(() => {
      const {result} = renderHook(() => useBoundingBox(null))

      hook = result
    })
    act(() => {
      let [ bounds ] = hook.current
      bounds.west.set('abc')
    })

    // let { hook } = wrapper.find('div').props();
    let [ bounds ] = hook.current
    expect(bounds.west.value).toEqual('abc')
    expect(bounds.west.number).toBeNull()
    expect(bounds.west.valid).toBeFalsy()
  })
})
