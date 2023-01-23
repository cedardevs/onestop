import React, {useState, useEffect} from 'react'
import _ from 'lodash'

import RadioButtonSet from '../input/RadioButtonSet'

import {FilterColors} from '../../../style/defaultStyles'
import {consolidateStyles} from '../../../utils/styleUtils'

const styleTabPanel = {justifyContent: 'center'}

const styleTab = {
  cursor: 'pointer',
  display: 'inline-block',
  backgroundColor: '#277CB2',
  padding: '0.309em 0.618em',
  borderWidth: '1px',
  borderStyle: 'solid',
  borderColor: FilterColors.DARK,
  color: FilterColors.INVERSE_TEXT,
}

const styleTabSelected = {
  backgroundColor: FilterColors.DARK,
}

const styleTabFocused = {
  textDecoration: 'underline',
}

const styleTabFirst = isFirst => {
  return isFirst
    ? {
        borderRight: '0',
        borderRadius: '0.309em 0 0 0.309em',
      }
    : null
}

const styleTabLast = isLast => {
  return isLast ? {borderRadius: '0 0.309em 0.309em 0'} : null
}

const styleTabMiddle = isMiddle => {
  return isMiddle
    ? {
        borderRight: '0',
      }
    : null
}

const styleHideInput = {
  // keeps hidden without messing up 508 considerations
  // in practice, the input will still have focus, but can be used to style the label when combined with styleLabelFocused on the option
  opacity: 0,
  position: 'fixed',
  width: 0,
}

/*
`options` should be an array. Each option in the array should be a map with label, value and a view. (Same as RadioButtonSet requirements, plus `view`.)
<RadioButtonSet options={[{label: 'First', value: 1, view: <div>...</div>}, {label: 'Next', value: 2, view: <Button/>}]}
props.defaultSelection is the default value, so no need to wire up things to when it changes.
*/
const TabPanels = ({options, name, defaultSelection, onSelectionChanged}) => {
  const [ selectedValue, setSelectedValue ] = useState(null)

  // expand basic radio options to include tab-specific styling instructions
  let internalOptions = _.map(options, (option, index) => {
    const first = index == 0
    const last = index == options.length - 1
    const middle = !first && !last
    return {
      ...option,
      ...{
        styleLabel: consolidateStyles(
          styleTab,
          styleTabFirst(first),
          styleTabMiddle(middle),
          styleTabLast(last)
        ),
        styleLabelSelected: styleTabSelected,
        styleLabelFocused: styleTabFocused,
        styleInput: styleHideInput,
      },
    }
  })

  const onSelectionChange = selectedValue => {
    let selected = _.find(options, (option, index) => {
      return option.value == selectedValue
    })
    if (selected) {
      setSelectedValue(selected.value) // set primary control for which tab is selected
      onSelectionChanged(selected.value) // callback for side effects
    }
  }

  const views = []
  _.each(options, (option, index) => {
    /*
    Render all views, using display:none to hide the inactive views. This prevents some bugs due to rerendering the component by only having a single active view as a child to the div
    */
    const selected = selectedValue == option.value
    const styleView = selected ? {} : {display: 'none'}
    views.push(
      <div style={styleView} key={`View::${name}::${option.value}`}>
        {option.view}
      </div>
    )
  })

  return (
    <div>
      <RadioButtonSet
        name={name}
        options={internalOptions}
        onSelectionChange={onSelectionChange}
        showLabelsOnly={true}
        defaultSelection={defaultSelection}
        stylePanel={styleTabPanel}
        interItemPadding={0}
      />
      {views}
    </div>
  )
}
export default TabPanels
