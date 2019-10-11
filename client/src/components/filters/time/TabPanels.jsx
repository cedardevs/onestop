import React, {useState, useEffect} from 'react'
import _ from 'lodash'
import RadioButtonTabs from './RadioButtonTabs'

/*
`options` should be an array. Each option in the array should be a map with label, value, description and a view. (Same as RadioButtonTabs requirements, plus `view`.)
<RadioButtonTabs options={[{label: 'First', value: 1, description: 'accessible', view: <div>...</div>}, {label: 'Next', value: 2, description: 'etc', view: <Button/>}]}
*/
const TabPanels = ({options, name}) => {
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
      <RadioButtonTabs
        name={name}
        options={options}
        onSelectionChange={onSelectionChange}
        tabPanel={true}
      />
      {views}
    </div>
  )
}
export default TabPanels
