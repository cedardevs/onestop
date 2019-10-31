import React, {useState} from 'react'
import _ from 'lodash'

import FlexColumn from '../../../common/ui/FlexColumn'
import FlexRow from '../../../common/ui/FlexRow'
import Button from '../../../common/input/Button'
import {Key} from '../../../../utils/keyboardUtils'
import {isValidYearRange, textToNumber} from '../../../../utils/inputUtils'
import {SiteColors} from '../../../../style/defaultStyles'
import FormSeparator from '../../FormSeparator'
import GeologicFieldset from './GeologicFieldset'
import GeologicFormatFieldset from './GeologicFormatFieldset'
import GeologicPresets from './GeologicPresets'
import {useYear} from './GeologicYearEffect'

import {exclamation_triangle, SvgIcon} from '../../../common/SvgIcon'

import Relation from '../../Relation'
import TimeRelationIllustration from '../TimeRelationIllustration'

import {
  styleFilterPanel,
  styleFieldsetBorder,
  styleForm,
} from '../../common/styleFilters'
import ApplyClearRow from '../../common/ApplyClearRow'

const warningStyle = warning => {
  if (_.isEmpty(warning)) {
    return {
      display: 'none',
    }
  }
  else {
    return {
      color: SiteColors.WARNING,
      textAlign: 'center',
      margin: '0.75em 0 0.5em',
      fontWeight: 'bold',
      fontSize: '1.15em',
    }
  }
}

const GeologicTimeFilter = ({
  startYear,
  endYear,
  timeRelationship,
  updateTimeRelationship,
  clear,
  applyFilter,
}) => {
  const [ format, setFormat ] = useState('CE')

  const [ start ] = useYear(startYear, format, year => {
    setWarning('')
    setDateRangeValid(isValidYearRange(year, end.CE))
  })
  const [ end ] = useYear(endYear, format, year => {
    setWarning('')
    setDateRangeValid(isValidYearRange(start.CE, year))
  })
  const [ dateRangeValid, setDateRangeValid ] = useState(true)
  const [ warning, setWarning ] = useState('')

  const clearDates = () => {
    clear()
    start.clear()
    end.clear()
    setDateRangeValid(true)
    setWarning('')
  }

  const applyDates = () => {
    if (!start.valid || !end.valid || !dateRangeValid) {
      setWarning(createWarning())
    }
    else {
      applyFilter(textToNumber(start.CE), textToNumber(end.CE))
    }
  }

  const createWarning = () => {
    if (!start.valid && !end.valid) return 'Invalid start and end date.'
    if (!start.valid) return 'Invalid start date.'
    if (!end.valid) return 'Invalid end date.'
    if (!dateRangeValid) return 'Invalid date range.'
    return 'Unknown error'
  }

  const handleKeyDown = event => {
    if (event.keyCode === Key.ENTER) {
      event.preventDefault()
      applyDates()
    }
  }

  const onFormatChange = f => {
    setFormat(f)
  }

  const legendText = 'Geologic'
  const form = (
    <div key="GeologicDateFilterInput::all">
      <form
        style={styleForm}
        onKeyDown={handleKeyDown}
        aria-describedby="geologicTimeFilterInstructions"
      >
        <GeologicFormatFieldset
          geologicFormat={format}
          onFormatChange={onFormatChange}
        />
        <GeologicFieldset start={start} end={end} format={format} />
      </form>
    </div>
  )

  const buttons = (
    <ApplyClearRow
      key="GeologicDateFilter::InputColumn::Buttons"
      ariaActionDescription="time filters"
      applyAction={applyDates}
      clearAction={clearDates}
    />
  )

  const warningMessage = (
    <div
      key="GeologicDateFilter::InputColumn::Warning"
      style={warningStyle(warning)}
      role="alert"
    >
      {warning}
    </div>
  )

  const presets = (
    <div key="GeologicDateFilter::InputColumn::Presets">
      <FormSeparator text="OR" />

      <GeologicPresets
        startYear={startYear}
        endYear={endYear}
        applyFilter={applyFilter}
      />
    </div>
  )

  const illustration = relation => {
    return (
      <TimeRelationIllustration
        relation={relation}
        hasStart={!_.isEmpty(start.year)}
        hasEnd={!_.isEmpty(end.year)}
      />
    )
  }

  const relation = (
    <Relation
      id="geologicTimeRelation"
      relation={timeRelationship}
      onUpdate={updateTimeRelationship}
      illustration={illustration}
    />
  )

  return (
    <div style={styleFilterPanel}>
      <fieldset style={styleFieldsetBorder}>
        <legend id="geologicTimeFilterInstructions">
          Provide a start year, end year, or both. Future dates are not
          accepted. Values can be entered in{' '}
          <abbr title="International System of Units">SI</abbr> (<abbr title="kiloannum">ka</abbr>,{' '}
          <abbr title="megaannum">Ma</abbr>, <abbr title="gigaannum">Ga</abbr>).
        </legend>
        <FlexColumn items={[ form, buttons, warningMessage, presets ]} />
      </fieldset>
      <h4 style={{margin: '0.618em 0 0.618em 0.309em'}}>
        Additional Filtering Options:
      </h4>
      {relation}
    </div>
  )
}
export default GeologicTimeFilter
