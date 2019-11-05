import React, {useState, useEffect} from 'react'
import _ from 'lodash'
import moment from 'moment/moment'

import {FilterColors, SiteColors} from '../../../../style/defaultStyles'

import FilterFieldset from '../../FilterFieldset'

import RadioButtonSet from '../../../common/input/RadioButtonSet'

const styleDate = {
  display: 'flex',
  flexDirection: 'row',
}

const FORMAT_OPTIONS = [
  {
    value: 'CE',
    label: <abbr title="Common Era">CE</abbr>,
  },
  {
    value: 'BP',
    label: (
      <span>
        <abbr title="Before Present">BP</abbr>(0 = 1950{' '}
        <abbr title="Common Era">CE</abbr>)
      </span>
    ),
  },
]

const GeologicFormatFieldset = ({geologicFormat, onFormatChange}) => {
  const legendText = 'Year Format'

  return (
    <FilterFieldset legendText={legendText}>
      <div style={styleDate}>
        <RadioButtonSet
          name="yearformat"
          options={FORMAT_OPTIONS}
          onSelectionChange={onFormatChange}
        />
      </div>
    </FilterFieldset>
  )
}
export default GeologicFormatFieldset
