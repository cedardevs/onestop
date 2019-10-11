import React, {useState, useEffect} from 'react'
import _ from 'lodash'

import FilterFieldset from '../../FilterFieldset'

import {convertYearToCE, textToNumber} from '../../../../utils/inputUtils'

const ERAS = [
  // years defined in BP!
  {
    index: 0,
    label: 'Holocene',
    start: 11700,
    end: null,
  },
  {
    index: 1,
    label: 'Last Deglaciation',
    start: 19000,
    end: 11700,
  },
  {
    index: 2,
    label: 'Last Glacial Period',
    start: 115000,
    end: 11700,
  },
  {
    index: 3,
    label: 'Last Interglacial',
    start: 130000,
    end: 115000,
  },
  {
    index: 4,
    label: 'Pliocene',
    start: 5300000,
    end: 2600000,
  },
  {
    index: 5,
    label: 'Paleocene-Eocene Thermal Maximum (PETM)',
    start: 56000000,
    end: 55000000,
  },
]

const GeologicPresets = ({
  startYear,
  endYear,
  applyFilter,
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
        // TODO this use of textToNumber(convertYearToCE(...)) suggests I need a non-text version of convertYearToCE...
        return (
          textToNumber(convertYearToCE(`${preset.start}`, 'BP')) == startYear &&
          textToNumber(convertYearToCE(`${preset.end}`, 'BP')) == endYear
        )
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
        applyFilter(
          textToNumber(convertYearToCE(`${preset.start}`, 'BP')),
          textToNumber(convertYearToCE(`${preset.end}`, 'BP'))
        )
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
    <FilterFieldset legendText={legendText} styleFieldset={{marginBottom: 0}}>
      <form key="GeologicDateFilter::InputColumn::Presets" style={styleLayout}>
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
      </form>
    </FilterFieldset>
  )
}
export default GeologicPresets
