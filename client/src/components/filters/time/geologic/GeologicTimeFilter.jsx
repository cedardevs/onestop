import React, {useState, useEffect} from 'react'
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
import {consolidateStyles} from '../../../../utils/styleUtils'
import {exclamation_triangle, SvgIcon} from '../../../common/SvgIcon'

import Relation from '../../Relation'
import TimeRelationIllustration from '../TimeRelationIllustration'

import {
  styleFilterPanel,
  styleFieldsetBorder,
  styleForm,
} from '../../common/styleFilters'
import ApplyClearRow from '../../common/ApplyClearRow'

const warningStyle = (startValid, endValid, errorMessage) => {
  return consolidateStyles(
    {
      color: SiteColors.WARNING,
      textAlign: 'center',
      fontWeight: 'bold',
      fontSize: '1.15em',
    },
    !startValid || !endValid || !_.isEmpty(errorMessage)
      ? {margin: '0.75em 0 0.5em'}
      : null
  )
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

  const [ start ] = useYear(startYear, format)
  const [ end ] = useYear(endYear, format)
  const [ warning, setWarning ] = useState('')

  const clearDates = () => {
    clear()
    start.clear()
    end.clear()
    setWarning('')
  }

  const validate = () => {
    let isValid = isValidYearRange(start.CE, end.CE)
    if (!isValid) {
      setWarning('Start year must be before end year.')
    }
    start.setValid(isValid)
    end.setValid(isValid)
    return isValid
  }

  useEffect(
    () => {
      start.setValid(true)
      end.setValid(true)
      setWarning('')
    },
    [ start.year, end.year ]
  )

  const applyDates = () => {
    if (start.valid && end.valid && validate()) {
      applyFilter(textToNumber(start.CE), textToNumber(end.CE))
    }
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
        <GeologicFieldset
          start={start}
          end={end}
          format={format}
          startYearErrorId={
            !_.isEmpty(warning) ? (
              'geologicDateFilterRangeErrors'
            ) : (
              'geologicDateFilterStartYearErrors'
            )
          }
          endYearErrorId={
            !_.isEmpty(warning) ? (
              'geologicDateFilterRangeErrors'
            ) : (
              'geologicDateFilterEndYearErrors'
            )
          }
        />
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

  const renderErrors = (name, errors) => {
    let {field, fieldset} = errors
    return (
      <div>
        {_.isEmpty(field) ? '' : `${name} ${field}.`}{' '}
        {_.isEmpty(fieldset) ? '' : `${name} ${fieldset}.`}
      </div>
    )
  }

  const warningMessage = (
    <div
      key="GeologicDateFilter::InputColumn::Warning"
      style={warningStyle(start.valid, end.valid, warning)}
      role="alert"
      aria-live="polite"
    >
      <div id="geologicDateFilterStartYearErrors">
        {renderErrors('Start', start.errors)}
      </div>
      <div id="geologicDateFilterEndYearErrors">
        {renderErrors('End', end.errors)}
      </div>
      <div id="geologicDateFilterRangeErrors">
        {!_.isEmpty(warning) ? warning : null}
      </div>
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
        idPrefix="geo"
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
