import React from 'react'

const styleMonth = {
  width: '7em',
  // color: FilterColors.TEXT,
  // height: '100%',
  margin: 0,
  padding: 0,
  // border: `1px solid ${FilterColors.LIGHT_SHADOW}`,
}

const MonthField = props => {
  // const {name, value, onChange, styleField, styleLabel, styleWrapper} = props // TODO rename style names to styleWrapper, styleField instead of 'styleYear'
  const {name, value, onChange, styleLayout, styleLabel, styleWrapper, styleField} = props // TODO rename style names to styleWrapper, styleField instead of 'styleYear'
  const styleFieldApplied = {
    ...styleMonth,
    ...styleField,
  }
  console.log('month styling', styleFieldApplied)
  const id = `${name}DateMonth`
  const label = `month ${name}`
  return (
    <div style={styleLayout}>
      <label style={styleLabel} htmlFor={id}>
        Month
      </label>
      <div style={styleWrapper}>
        <select
          id={id}
          name={id}
          value={value}
          onChange={onChange}
          style={styleFieldApplied}
          aria-label={label}
        >
          <option value="">(none)</option>
          <option value="0">January</option>
          <option value="1">February</option>
          <option value="2">March</option>
          <option value="3">April</option>
          <option value="4">May</option>
          <option value="5">June</option>
          <option value="6">July</option>
          <option value="7">August</option>
          <option value="8">September</option>
          <option value="9">October</option>
          <option value="10">November</option>
          <option value="11">December</option>
        </select>
      </div>
    </div>
  )
}
export default MonthField


/*
createMonthField = (name, value, onChange) => {
  const id = `${name}DateMonth`
  const label = `month ${name}`
  return (
    <div style={styleField}>
      <label style={styleLabel} htmlFor={id}>
        Month
      </label>
      <div style={styleMonthWrapper}>
        <select
          id={id}
          name={id}
          value={value}
          onChange={onChange}
          style={styleMonth}
          aria-label={label}
        >
          <option value="">(none)</option>
          <option value="0">January</option>
          <option value="1">February</option>
          <option value="2">March</option>
          <option value="3">April</option>
          <option value="4">May</option>
          <option value="5">June</option>
          <option value="6">July</option>
          <option value="7">August</option>
          <option value="8">September</option>
          <option value="9">October</option>
          <option value="10">November</option>
          <option value="11">December</option>
        </select>
      </div>
    </div>
  )
}
*/
