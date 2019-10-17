import React, {useState, useEffect, useRef} from 'react'
import _ from 'lodash'

import FlexRow from '../ui/FlexRow'
import {FilterColors} from '../../../style/defaultStyles'
import RadioButton from './RadioButton'

import {Key} from '../../../utils/keyboardUtils'

// const styleHideFocus = {
//   outline: 'none', // The focused state is being passed to a child component to display
// }

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

// // const styleTabFocused = {
//   textDecoration: 'underline',
// }

const styleTabSelected = {
  backgroundColor: FilterColors.DARK,
}
// const styleRadioFocused = {
//   outline: 'solid red 1px',
// }

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
<RadioButtonSet options={[{label: 'First', value: 1, description: 'Accessible description indicating side effects.'}, {label: 'Next', value: 2, description: 'Also shows on mouse hover.'}]}
*/
const RadioButtonSet = ({
  name,
  options,
  onSelectionChange,
  tabPanel,
  defaultSelection,
}) => {
  const DEFAULT = defaultSelection || options[0].value

  const [ selectedValue, setSelectedValue ] = useState(DEFAULT)

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
    // const focused = focus == option.value

    // styling works with 2+ options
    const first = index == 0
    const last = index == options.length - 1
    const middle = !first && !last

    const styleLabel = tabPanel
      ? {
          ...styleRadioTab,
          ...(selected ? styleTabSelected : {}), // TODO migrate tab styling to parameters passed by TabPanels
          // ...(focus && selected ? styleTabFocused : {}),
          ...(first ? styleTabFirst : {}),
          ...(middle ? styleTabMiddle : {}),
          ...(last ? styleTabLast : {}),
        }
      : {
          ...(middle ? styleRadioButton : {}),
          ...(last ? styleRadioButton : {}),
        }

    const styleInput = {
      ...(tabPanel ? styleHideInput : {}),
    }

    radioButtons.push(
      <RadioButton
        key={`RadioButton::${name}::${option.value}`}
        id={id}
        name={name}
        description={option.description}
        label={option.label}
        value={option.value}
        selected={selected}
        ariaExpanded={tabPanel && selected}
        labelGetsFocus={tabPanel}
        setSelection={setSelectedValue}
        styleInput={styleInput}
        styleLabel={styleLabel}
      />
    )
  })

  return (
    <div>
      <FlexRow style={stylePanel} items={radioButtons} />
    </div>
  )
}
export default RadioButtonSet
