import React from 'react'
import {renderHook, act} from '@testing-library/react-hooks'

import {useBoundingBox} from '../../../../src/components/filters/spatial/BoundingBoxEffect'

const initHook = (hookFunction, ...hookArgs) => {
  // generic init hook function
  let hook = null
  act(() => {
    const {result} = renderHook(() => hookFunction(...hookArgs)) // TODO test ...hookArgs with hook that has more than one param
    hook = result
  })

  return hook
}

const init = bbox => {
  return initHook(useBoundingBox, bbox)
}

describe('The BoundingBoxEffect hook', () => {
  it('should set init value - no bbox', () => {
    const hook = init(null)
    let [ bounds ] = hook.current
    expect(bounds.east.value).toEqual('')
    expect(bounds.north.value).toEqual('')
    expect(bounds.south.value).toEqual('')
    expect(bounds.west.value).toEqual('')
  })
  it('should set init value - bbox', () => {
    const hook = init({north: 34, south: -20, west: 17, east: -123})

    let [ bounds ] = hook.current
    expect(bounds.east.value).toEqual('-123')
    expect(bounds.north.value).toEqual('34')
    expect(bounds.south.value).toEqual('-20')
    expect(bounds.west.value).toEqual('17')
  })

  it('should update', () => {
    const hook = init(null)
    act(() => {
      let [ bounds ] = hook.current
      bounds.west.set('111')
    })

    let [ bounds ] = hook.current
    expect(bounds.west.value).toEqual('111')
    expect(bounds.west.number).toEqual(111)
    expect(bounds.west.valid).toBeTruthy()
  })

  it('should update -  invalid', () => {
    const hook = init(null)
    act(() => {
      let [ bounds ] = hook.current
      bounds.west.set('abc')
    })

    let [ bounds ] = hook.current
    expect(bounds.west.value).toEqual('abc')
    expect(bounds.west.number).toBeNull()
    expect(bounds.west.valid).toBeFalsy()
  })
})
