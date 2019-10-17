import React, {useState, useEffect, useRef} from 'react'
import _ from 'lodash'

import FlexRow from '../ui/FlexRow'
import RadioButton from './RadioButton'

import {consolidateStyles} from '../../../utils/styleUtils'

const stylePanelDefault = {
  margin: '0.309em auto',
  padding: '0.309em',
}

const styleContainer = (index, interItemPadding) => {
  const first = index == 0
  return !first
    ? interItemPadding != null
      ? {marginLeft: interItemPadding}
      : {marginLeft: '0.618em'}
    : {}
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
  ariaExpanded, // function (selected) => true/false
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
        styleContainer={styleContainer(index, interItemPadding)}
        styleLabel={option.styleLabel}
        styleLabelSelected={option.styleLabelSelected}
        styleLabelFocused={option.styleLabelFocused}
        styleInput={option.styleInput}
      />
    )
  })

  return (
    <div>
      <FlexRow
        style={consolidateStyles(stylePanelDefault, stylePanel)}
        items={radioButtons}
      />
    </div>
  )
}
export default RadioButtonSet
