import React, {useState, useEffect} from 'react'
import FlexRow from '../../common/ui/FlexRow'
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
`options` should be an array. Each option in the array should be a map with label, value, and description.
<RadioButtonTabs options={[{label: 'First', value: 1, description: 'Accessible description indicating side effects.'}, {label: 'Next', value: 2, description: 'Also shows on mouse hover.'}]}
*/
const RadioButtonTabs = ({
  name,
  options,
  onSelectionChange,
  tabPanel,
  selected,
}) => {
  const DEFAULT = selected || options[0].value

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
          ...(selected ? styleSelected : {}), // TODO rename all these styles to styleTabSelected, etc
          ...(focused ? styleFocused : {}),
          ...(first ? styleFirst : {}),
          ...(middle ? styleMiddle : {}),
          ...(last ? styleLast : {}),
        }
      : {
          ...(middle ? {marginLeft: '0.618em'} : {}), // TODO name these styles
          ...(last ? {marginLeft: '0.618em'} : {}),
        }

    const styleInput = tabPanel ? styleHideInput : {}
    // TODO make sure onBlur junk works correctly for other browsers!
    // TODO test out desc + label as aria value instead of just desc!!
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
