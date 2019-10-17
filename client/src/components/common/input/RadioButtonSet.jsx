import React, {useState, useEffect, useRef} from 'react'
import _ from 'lodash'

import FlexRow from '../ui/FlexRow'
import RadioButton from './RadioButton'

const stylePanelDefault = {
  margin: '0.309em auto',
  padding: '0.309em',
}

/*
`options` should be an array. Each option in the array should be a map with label, value, and description.

Each option may also specify: styleLabel, styleLabelSelected, styleLabelFocused, styleInput

<RadioButtonSet options={[{label: 'First', value: 1, description: 'Accessible description indicating side effects.'}, {label: 'Next', value: 2, description: 'Also shows on mouse hover.'}]}
*/
const RadioButtonSet = ({
  name,
  options,
  onSelectionChange,
  showLabelsOnly,
  ariaExpanded, // function (selected) => ...
  defaultSelection,
  stylePanel,
  interItemPadding, // allows override of default margin between items
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

    const first = index == 0 // for interItemPadding

    radioButtons.push(
      <RadioButton
        key={`RadioButton::${name}::${option.value}`}
        id={id}
        name={name}
        description={option.description}
        label={option.label}
        value={option.value}
        selected={selected}
        ariaExpanded={ariaExpanded ? ariaExpanded(selected) : null}
        setSelection={setSelectedValue}
        styleContainer={
          !first ? interItemPadding != null ? (
            {marginLeft: interItemPadding}
          ) : (
            {marginLeft: '0.618em'}
          ) : (
            {}
          )
        }
        styleLabel={option.styleLabel ? option.styleLabel : {}}
        styleLabelSelected={
          option.styleLabelSelected ? option.styleLabelSelected : {}
        }
        styleLabelFocused={
          option.styleLabelFocused ? option.styleLabelFocused : {}
        }
        styleInput={option.styleInput ? option.styleInput : {}}
      />
      // TODO just pass through option.styleLabel directly without ?: operator and let RadioButton handle it?
    )
  })

  return (
    <div>
      <FlexRow
        style={{...stylePanelDefault, ...(stylePanel ? stylePanel : {})}}
        items={radioButtons}
      />
    </div>
  )
}
export default RadioButtonSet
