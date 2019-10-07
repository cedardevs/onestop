import React, {useState, useEffect} from 'react'
import _ from 'lodash'

import FilterFieldset from '../FilterFieldset'

const ERAS = [
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
  const [ presetIndex, setPresetIndex ] = useState('')

  useEffect(
    () => {
      // if startYear and endYear match a preset, show the name of the Era in the dropdown (ie: when reloading the page)
      let matchingPreset = _.find(ERAS, (preset, index) => {
        return preset.start == startYear && preset.end == endYear
      })
      if (matchingPreset) {
        setPresetIndex(matchingPreset.index)
      }
      else {
        setPresetIndex('') // reset to "None" if nothing matches
      }
    },
    [ startYear, endYear ]
  )

  useEffect(
    () => {
      let preset = ERAS[presetIndex]
      if (preset) {
        // setStart({year: preset.start, valid: true})
        // setEnd({year: preset.end, valid: true})
        // setDateRangeValid(true)
        // // TODO try activating this widget with keyboard to see if we need a preventDefault in there anywhere
        // applyDates()
        updateYearRange(preset.start, preset.end)
        submit()
      }
    },
    [ presetIndex ]
  )

  const options = [
    <option key="era::none" value="">
      (none)
    </option>,
  ]

  _.each(ERAS, (preset, index) => {
    options.push(
      <option key={`era::${preset.label}`} value={index}>
        {preset.label}
      </option>
    )
  })

  return (
    <FilterFieldset legendText={legendText}>
      <div key="GeologicDateFilter::InputColumn::Presets" style={styleLayout}>
        <select
          id="presets"
          name="presets"
          value={presetIndex}
          onChange={e => {
            setPresetIndex(e.target.value)
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
