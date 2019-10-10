import React, {useState, useEffect} from 'react'
import _ from 'lodash'
import moment from 'moment/moment'

import {FilterColors, SiteColors} from '../../../../style/defaultStyles'

import FilterFieldset from '../../FilterFieldset'

import RadioButtonTabs from '../RadioButtonTabs'

const styleDate = {
  display: 'flex',
  flexDirection: 'row',
}

const FORMAT_OPTIONS = [
  {
    value: 'CE',
    label: 'CE',
    description: 'Current Era',
  },
  {
    value: 'BP',
    label: 'BP (0 = 1950 CE)',
    description: 'Before Present',
  },
]

const GeologicFormatFieldset = ({geologicFormat, onFormatChange}) => {
  const legendText = 'Year Format'

  return (
    <FilterFieldset legendText={legendText}>
      <div style={styleDate}>
        <RadioButtonTabs
          name="yearformat"
          options={FORMAT_OPTIONS}
          onSelectionChange={onFormatChange}
        />
      </div>
    </FilterFieldset>
  )
}
export default GeologicFormatFieldset
