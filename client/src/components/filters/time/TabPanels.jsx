import React, {useState, useEffect} from 'react'
import _ from 'lodash'
import RadioButtonTabs from './RadioButtonTabs'

/*
`options` should be an array. Each option in the array should be a map with label, value, description and a view. (Same as RadioButtonTabs requirements, plus `view`.)
<RadioButtonTabs options={[{label: 'First', value: 1, description: 'accessible', view: <div>...</div>}, {label: 'Next', value: 2, description: 'etc', view: <Button/>}]}
*/
const TabPanels = ({options, name}) => {
  const [ view, setView ] = useState(null)
  const onSelectionChange = selectedValue => {
    let selected = _.find(options, (option, index) => {
      return option.value == selectedValue
    })
    if (selected) {
      setView(selected.view)
    }
  }

  return (
    <div>
      <RadioButtonTabs
        name={name}
        options={options}
        onSelectionChange={onSelectionChange}
        tabPanel={true}
      />
      {view}
    </div>
  )
}
export default TabPanels
