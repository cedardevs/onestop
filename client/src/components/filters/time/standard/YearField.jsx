import React from 'react'
import _ from 'lodash'

import {consolidateStyles} from '../../../../utils/styleUtils'

const styleYear = {
  width: '2.618em',
  margin: 0,
  padding: '0 0.309em',
}

const YearField = props => {
  const {
    name,
    required,
    value,
    valid,
    onChange,
    label,
    styleLayout,
    styleLabel,
    styleLabelInvalid,
    styleRequiredIndicator,
    styleField,
    maxLength,
    placeholder,
    ariaPlaceholder,
    errorId,
  } = props
  const styleFieldApplied = {
    ...styleYear,
    ...styleField,
  }

  const id = `${name}DateYear`
  const ariaLabel = `year ${name}`
  const labelText = label ? label : 'Year'

  return (
    <div style={styleLayout}>
      <label
        style={consolidateStyles(styleLabel, valid ? null : styleLabelInvalid)}
        htmlFor={id}
      >
        {labelText}
        {required ? <span style={styleRequiredIndicator}>*</span> : null}
      </label>
      <input
        type="text"
        id={id}
        name={id}
        placeholder={_.isEmpty(placeholder) ? 'YYYY' : placeholder}
        aria-placeholder={
          _.isEmpty(ariaPlaceholder) ? 'Y Y Y Y' : ariaPlaceholder
        }
        value={value}
        aria-invalid={!valid}
        aria-required={required}
        aria-errormessage={errorId}
        onChange={onChange}
        maxLength={maxLength | 4}
        style={styleFieldApplied}
        aria-label={ariaLabel}
      />
    </div>
  )
}
export default YearField
