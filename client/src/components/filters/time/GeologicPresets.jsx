import React, {useState, useEffect} from 'react'
import _ from 'lodash'

import FilterFieldset from '../FilterFieldset'

const presetValues = [
  {index: 0, label: 'Holocene', start: 1950 - 11700, end: null},
  {
    index: 1,
    label: 'Last Deglaciation',
    start: 1950 - 19000,
    end: 1950 - 11700,
  },
  {
    index: 2,
    label: 'Last Glacial Period',
    start: 1950 - 115000,
    end: 1950 - 11700,
  },
  {
    index: 3,
    label: 'Last Interglacial',
    start: 1950 - 130000,
    end: 1950 - 115000,
  },
  {index: 4, label: 'Pliocene', start: 1950 - 5300000, end: 1950 - 2600000},
  {
    index: 5,
    label: 'Paleocene-Eocene Thermal Maximum (PETM)',
    start: 1950 - 56000000,
    end: 1950 - 55000000,
  },
]

const GeologicPresets = ({
  startYear,
  endYear,
  updateYearRange,
  submit,
  styleLayout,
  styleLabel,
  styleField,
}) => {
  const legendText = 'Eras'
  const [ preset, setPreset ] = useState('') // TODO tons of stuff to do with this widget

  useEffect(
    () => {
      // if startYear and endYear match a preset, show the name of the Era in the dropdown (ie: when reloading the page)
      let matchingPreset = _.find(presetValues, (pv, k) => {
        return pv.start == startYear && pv.end == endYear
      })
      setPreset(matchingPreset.index)
    },
    [ startYear, endYear ]
  )

  useEffect(
    () => {
      let pv = presetValues[preset] // TODO clear form should also reset the presets to (none)
      console.log('found', pv, 'from', preset)
      if (pv) {
        // setStart({year: pv.start, valid: true})
        // setEnd({year: pv.end, valid: true})
        // setDateRangeValid(true)
        // // TODO try activating this widget with keyboard to see if we need a preventDefault in there anywhere
        // applyDates()
        updateYearRange(pv.start, pv.end)
        submit()
      }
    },
    [ preset ]
  )

  const options = [
    <option key="era::none" value="">
      (none)
    </option>,
  ]

  _.each(presetValues, (pv, k) => {
    options.push(
      <option key={`era::${pv.label}`} value={k}>
        {pv.label}
      </option>
    )
  })

  return (
    <FilterFieldset legendText={legendText}>
      <div key="GeologicDateFilter::InputColumn::Presets" style={styleLayout}>
        <select
          id="presets"
          name="presets"
          value={preset}
          onChange={e => {
            setPreset(e.target.value)
          }}
          style={styleField}
          aria-label="Era Presets"
        >
          {options}
        </select>
      </div>
    </FilterFieldset>
  )
}
export default GeologicPresets
