import React, {useState, useEffect} from 'react'
import FlexRow from '../../common/ui/FlexRow'
import {FilterColors} from '../../../style/defaultStyles'

const styleRadioTab = {
  display: 'inline-block',
  backgroundColor: '#277CB2',
  padding: '0.309em 0.618em',
  borderWidth: '1px',
  borderStyle: 'solid',
  borderColor: FilterColors.DARK,
  color: FilterColors.INVERSE_TEXT,
}

const stylePanel = {
  margin: '0.309em auto',
  padding: '0.309em',
  justifyContent: 'center',
}

const styleFocused = {
  textDecoration: 'underline',
}

const styleSelected = {
  backgroundColor: FilterColors.DARK,
}

const styleFirst = {
  borderRight: '0',
  borderRadius: '0.309em 0 0 0.309em',
}

const styleLast = {
  borderRadius: '0 0.309em 0.309em 0',
}

const styleMiddle = {
  borderRight: '0',
}

const styleHideInput = {
  // keeps hidden without messing up 508 considerations
  opacity: 0,
  position: 'fixed',
  width: 0,
}

/*
OPTIONS should be an array of maps with a value and a label.
<RadioButtonTabs OPTIONS={[{label: 'First', value: 1}, {label: 'Next', value: 2}]}
*/
const RadioButtonTabs = ({inputName, OPTIONS, callback}) => {
  const DEFAULT = OPTIONS[0].value

  const [ selectedValue, setSelectedValue ] = useState(DEFAULT)
  const [ focus, setFocus ] = useState(null)

  useEffect(
    () => {
      callback(selectedValue)
    },
    [ selectedValue ]
  )

  const radioButtons = []
  _.each(OPTIONS, (option, index) => {
    const id = `RadioButton${inputName}${option.value}`
    const selected = selectedValue == option.value
    const focused = focus == option.value

    // styling works with 2+ options
    const first = index == 0
    const last = index == OPTIONS.length - 1
    const middle = !first && !last

    const style = {
      ...styleRadioTab,
      ...(selected ? styleSelected : {}),
      ...(focused ? styleFocused : {}),
      ...(first ? styleFirst : {}),
      ...(middle ? styleMiddle : {}),
      ...(last ? styleLast : {}),
    }
    // TODO make sure onBlur junk works correctly for other browsers!
    radioButtons.push(
      <div key={`RadioButton::${inputName}::${option.value}`}>
        <label htmlFor={id} style={style}>
          {option.label}
        </label>
        <input
          type="radio"
          id={id}
          style={styleHideInput}
          name={inputName}
          value={option.value}
          checked={selected}
          onChange={e => setSelectedValue(e.target.value)}
          onFocus={e => setFocus(e.target.value)}
          onBlur={e => setFocus(null)}
        />
      </div>
    )
  })

  return <FlexRow style={stylePanel} items={radioButtons} />
}
export default RadioButtonTabs
