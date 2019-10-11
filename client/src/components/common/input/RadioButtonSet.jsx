import React, {useState, useEffect} from 'react'
import _ from 'lodash'

import FlexRow from '../ui/FlexRow'
import {FilterColors} from '../../../style/defaultStyles'

const styleRadioTab = {
  cursor: 'pointer',
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

const styleTabFocused = {
  textDecoration: 'underline',
}

const styleTabSelected = {
  backgroundColor: FilterColors.DARK,
}

const styleTabFirst = {
  borderRight: '0',
  borderRadius: '0.309em 0 0 0.309em',
}

const styleTabLast = {
  borderRadius: '0 0.309em 0.309em 0',
}

const styleTabMiddle = {
  borderRight: '0',
}

const styleHideInput = {
  // keeps hidden without messing up 508 considerations
  opacity: 0,
  position: 'fixed',
  width: 0,
}

const styleRadioButton = {marginLeft: '0.618em'}

/*
`options` should be an array. Each option in the array should be a map with label, value, and description.
<RadioButtonTabs options={[{label: 'First', value: 1, description: 'Accessible description indicating side effects.'}, {label: 'Next', value: 2, description: 'Also shows on mouse hover.'}]}
*/
const RadioButtonTabs = ({
  name,
  options,
  onSelectionChange,
  tabPanel,
  defaultSelection,
}) => {
  const DEFAULT = defaultSelection || options[0].value

  const [ selectedValue, setSelectedValue ] = useState(DEFAULT)
  const [ focus, setFocus ] = useState(null)

  useEffect(
    () => {
      onSelectionChange(selectedValue)
    },
    [ selectedValue ]
  )

  const radioButtons = []
  _.each(options, (option, index) => {
    const id = `RadioButton${name}${option.value}`
    const selected = selectedValue == option.value
    const focused = focus == option.value

    // styling works with 2+ options
    const first = index == 0
    const last = index == options.length - 1
    const middle = !first && !last

    const styleLabel = tabPanel
      ? {
          ...styleRadioTab,
          ...(selected ? styleTabSelected : {}),
          ...(focused ? styleTabFocused : {}),
          ...(first ? styleTabFirst : {}),
          ...(middle ? styleTabMiddle : {}),
          ...(last ? styleTabLast : {}),
        }
      : {
          ...(middle ? styleRadioButton : {}),
          ...(last ? styleRadioButton : {}),
        }

    const styleInput = tabPanel ? styleHideInput : {}
    radioButtons.push(
      <div key={`RadioButton::${name}::${option.value}`}>
        <label
          htmlFor={id}
          style={styleLabel}
          title={option.description}
          aria-label={`${option.description} ${option.label}`}
        >
          {option.label}
        </label>
        <input
          type="radio"
          id={id}
          style={styleInput}
          name={name}
          value={option.value}
          checked={selected}
          aria-expanded={tabPanel && selected}
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
