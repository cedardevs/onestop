import React from 'react'
import Button from '../input/Button'
import gridIcon from 'fa/th.svg'
import listIcon from 'fa/th-list.svg'
import expandIcon from 'fa/expand.svg'
import collapseIcon from 'fa/compress.svg'
import {mapFromObject} from '../../../utils/objectUtils'

const styleController = {
  display: 'flex',
  justifyContent: 'space-evenly',
  padding: '0.618em',
  borderRadius: '0.309em',
}

const styleControlButton = {
  margin: '0 0.105em',
}

const styleControlButtonIcon = withText => {
  return {
    width: '1em',
    height: '1em',
    padding: withText ? 'initial' : '0.309em 0',
    marginRight: withText ? '0.309em' : 0,
  }
}

export default function ListViewController(props){
  const {
    itemsMap,
    previousItemsMap,
    propsForItem,
    focusedKey,
    ListItemComponent,
    GridItemComponent,
    showAsGrid,
    toggleGrid,
    expandAll,
    collapseAll,
    customActions,
  } = props

  // initialize vars for control elements
  let controller = null
  let controlButtons = []

  const numItems = itemsMap ? itemsMap.size : 0
  const numPreviousItems = previousItemsMap ? previousItemsMap.size : 0

  // if both list and grid components are provided,
  // we can show a toggle between views
  const toggleGridAvailable = ListItemComponent && GridItemComponent
  if (toggleGridAvailable) {
    let buttonTitle = showAsGrid ? 'List View' : 'Grid View'
    controlButtons.push(
      <Button
        key={buttonTitle}
        title={buttonTitle}
        icon={showAsGrid ? listIcon : gridIcon}
        style={styleControlButton}
        styleIcon={styleControlButtonIcon(false)}
        onClick={toggleGrid}
      />
    )
  }

  // if a list component is provided and we are currently showing as a list
  const expandCollapseAvailable =
    ListItemComponent && !showAsGrid && numItems > 0
  if (expandCollapseAvailable) {
    let buttonExpandTitle = 'Expand All'
    controlButtons.push(
      <Button
        key={buttonExpandTitle}
        title={buttonExpandTitle}
        icon={expandIcon}
        style={styleControlButton}
        styleIcon={styleControlButtonIcon(false)}
        onClick={expandAll}
      />
    )
    let buttonCollapseTitle = 'Collapse All'
    controlButtons.push(
      <Button
        key={buttonCollapseTitle}
        title={buttonCollapseTitle}
        icon={collapseIcon}
        style={styleControlButton}
        styleIcon={styleControlButtonIcon(false)}
        onClick={collapseAll}
      />
    )
  }

  // add any provided custom actions to the control element
  // e.g. -
  // {
  //   "Clear All": {
  //     icon: clearIcon, // `import clearIcon from 'fa/remove.svg'`
  //     handler: ({
  //       itemsMap,
  //       previousItemsMap,
  //       focusedKey,
  //       ListItemComponent,
  //       GridItemComponent,
  //       showAsGrid
  //     }) => { ... } // action handler for when button is activated
  //   }
  // }
  const customActionsMap = mapFromObject(customActions)
  if (customActionsMap && customActionsMap.size > 0) {
    customActionsMap.forEach((action, key) => {
      controlButtons.push(
        <Button
          key={key}
          title={action.title}
          text={action.showText || !action.icon ? key : null}
          icon={action.icon}
          style={styleControlButton}
          styleIcon={styleControlButtonIcon(action.showText)}
          onClick={() =>
            action.handler({
              itemsMap,
              previousItemsMap,
              focusedKey,
              propsForItem,
              ListItemComponent,
              GridItemComponent,
              showAsGrid,
            })}
        />
      )
    })
  }

  // if any control buttons are available to show, show them
  if (controlButtons.length > 0) {
    controller = <div style={styleController}>{controlButtons}</div>
  }

  return <div>{controller}</div>
}
