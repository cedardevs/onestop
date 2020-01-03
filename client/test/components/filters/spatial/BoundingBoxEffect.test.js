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
  describe('initial conditions', () => {
    it('default - no bbox provided', () => {
      const hook = init(null)
      let [ bounds ] = hook.current
      expect(bounds.east.value).toEqual('')
      expect(bounds.north.value).toEqual('')
      expect(bounds.south.value).toEqual('')
      expect(bounds.west.value).toEqual('')
    })

    it('with a bbox', () => {
      const hook = init({north: 34, south: -20, west: 17, east: -123})

      let [ bounds ] = hook.current
      expect(bounds.east.value).toEqual('-123')
      expect(bounds.north.value).toEqual('34')
      expect(bounds.south.value).toEqual('-20')
      expect(bounds.west.value).toEqual('17')
    })
  })

  it('simple interaction - should update value and basic side effects', () => {
    const hook = init(null)
    // before interacting TODO why isn't this working?
    // let [ bounds ] = hook.current
    // expect(bounds.west.isSet()).toBeFalsy()

    act(() => {
      let [ bounds ] = hook.current
      // this is currently how a component is expected to update values based on user interaction events:
      bounds.west.set('111')
    })

    let [ bounds ] = hook.current
    // primary effect: value is set
    expect(bounds.west.value).toEqual('111')
    // secondary effects:
    expect(bounds.west.number).toEqual(111)
    expect(bounds.west.isSet()).toBeTruthy()
  })

  describe('validation - field level', () => {
    // let hook = null
    // beforeAll(async () => {
    //   // init hook with defaults
    //   hook = init(null)
    // })

    const NAN_testCases = [
      {
        field: 'west',
        value: 'foo',
        error: 'West: Invalid coordinates entered.',
      },
      {
        field: 'east',
        value: '1-2',
        error: 'East: Invalid coordinates entered.',
      },
      {
        field: 'south',
        value: 'hi',
        error: 'South: Invalid coordinates entered.',
      },
      {
        field: 'north',
        value: '...',
        error: 'North: Invalid coordinates entered.',
      },
    ]

    const coordLimitTestCases = {
      valid: [
        {field: 'west', value: '-180'},
        {field: 'west', value: '180'},
        {field: 'west', value: '0'},
        {field: 'east', value: '-180'},
        {field: 'east', value: '180'},
        {field: 'east', value: '0'},
        {field: 'north', value: '-90'},
        {field: 'north', value: '90'},
        {field: 'north', value: '0'},
        {field: 'south', value: '-90'},
        {field: 'south', value: '90'},
        {field: 'south', value: '0'},
      ],
      invalid: [
        {
          field: 'west',
          value: '-181',
          error:
            'West: Invalid coordinates entered. Valid longitude are between -180 and 180.',
        }, // TODO the phrasing 'valid longitude' should be 'valid latitude coordinates'
        {
          field: 'east',
          value: '181',
          error:
            'East: Invalid coordinates entered. Valid longitude are between -180 and 180.',
        },
        {
          field: 'north',
          value: '91',
          error:
            'North: Invalid coordinates entered. Valid latitude are between -90 and 90.',
        },
        {
          field: 'south',
          value: '-91',
          error:
            'South: Invalid coordinates entered. Valid latitude are between -90 and 90.',
        },
      ],
    }

    _.each(NAN_testCases, c => {
      it(`not a number - for ${c.field}='${c.value}'`, function(){
        const hook = init(null)
        act(() => {
          let [ bounds ] = hook.current
          bounds[c.field].set(c.value)
        })

        let [ bounds ] = hook.current

        // generic invalid expectations
        expect(bounds[c.field].valid).toBeFalsy()

        // NaN expectations
        expect(bounds[c.field].number).toBeNull()
        expect(bounds[c.field].isSet()).toBeFalsy()

        // specific invalid expectations
        expect(bounds[c.field].value).toEqual(c.value)
        expect(bounds[c.field].error).toEqual(c.error)
      })
    })

    _.each(coordLimitTestCases.invalid, c => {
      it(`invalid limits - for ${c.field}='${c.value}'`, function(){
        const hook = init(null)

        act(() => {
          let [ bounds ] = hook.current
          bounds[c.field].set(c.value)
        })

        let [ bounds ] = hook.current

        // generic invalid expectations
        expect(bounds[c.field].valid).toBeFalsy()

        // coord limitations expectations
        expect(Number.isFinite(bounds[c.field].number)).toBeTruthy()
        expect(bounds[c.field].isSet()).toBeTruthy()

        // specific invalid expectations
        expect(bounds[c.field].value).toEqual(c.value)
        expect(bounds[c.field].error).toEqual(c.error)
      })
    })

    _.each(coordLimitTestCases.valid, c => {
      it(`valid limits - for ${c.field}='${c.value}'`, function(){
        const hook = init(null)

        act(() => {
          let [ bounds ] = hook.current
          bounds[c.field].set(c.value)
        })

        let [ bounds ] = hook.current

        console.log(`for ${c.field}='${c.value}'`, bounds[c.field])
        // generic invalid expectations
        expect(bounds[c.field].valid).toBeTruthy()

        // coord limitations expectations
        expect(Number.isFinite(bounds[c.field].number)).toBeTruthy()
        expect(bounds[c.field].isSet()).toBeTruthy()

        // specific invalid expectations
        expect(bounds[c.field].value).toEqual(c.value)
        expect(bounds[c.field].error).toEqual('')
      })
    })
  })
})
