import React, {useState, useEffect} from 'react'
import _ from 'lodash'
import RadioButtonSet from '../input/RadioButtonSet'

/*
`options` should be an array. Each option in the array should be a map with label, value, description and a view. (Same as RadioButtonSet requirements, plus `view`.)
<RadioButtonSet options={[{label: 'First', value: 1, description: 'accessible', view: <div>...</div>}, {label: 'Next', value: 2, description: 'etc', view: <Button/>}]}
props.selected is the default value, so no need to wire up things to when it changes.
*/
const TabPanels = ({options, name, selected}) => {
  const [ selectedValue, setSelectedValue ] = useState(null)

  const onSelectionChange = selectedValue => {
    let selected = _.find(options, (option, index) => {
      return option.value == selectedValue
    })
    if (selected) {
      setSelectedValue(selected.value)
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
        options={options}
        onSelectionChange={onSelectionChange}
        tabPanel={true}
        selected={selected}
      />
      {views}
    </div>
  )
}
export default TabPanels
