import React from 'react'
import Button from '../input/Button'
import gridIcon from 'fa/th.svg'
import listIcon from 'fa/th-list.svg'
import expandIcon from 'fa/expand.svg'
import collapseIcon from 'fa/compress.svg'
import {mapFromObject} from '../../../utils/objectUtils'

const styleController = {
  display: 'flex',
  justifyContent: 'space-around',
  padding: '0.618em',
  backgroundColor: 'rgba(0,0,0, 0.2)',
  borderRadius: '0.309em',
  margin: '0 1.618em 1em 0',
}

const styleControlButtonIcon = {
  width: '1em',
  height: '1em',
  marginRight: '0.309em',
}

export default function ListViewController(props){
  const {
    itemsMap,
    itemsMapPrevious,
    propsForItem,
    ListItemComponent,
    GridItemComponent,
    showAsGrid,
    focusKey,
    toggleGrid,
    expandAll,
    collapseAll,
    customActions,
  } = props

  // initialize vars for control elements
  let controller = null
  let controlButtons = []

  const numItems = itemsMap ? itemsMap.size : 0
  const numItemsPrevious = itemsMapPrevious ? itemsMapPrevious.size : 0

  // if both list and grid components are provided,
  // we can show a toggle between views
  const toggleGridAvailable = ListItemComponent && GridItemComponent
  if (toggleGridAvailable) {
    let buttonText = showAsGrid ? 'Show List' : 'Show Grid'
    controlButtons.push(
      <Button
        key={buttonText}
        text={buttonText}
        icon={showAsGrid ? listIcon : gridIcon}
        styleIcon={styleControlButtonIcon}
        onClick={toggleGrid}
      />
    )
  }

  // if a list component is provided and we are currently showing as a list
  const expandCollapseAvailable =
    ListItemComponent && !showAsGrid && numItems > 0
  if (expandCollapseAvailable) {
    let buttonExpandText = 'Expand All'
    controlButtons.push(
      <Button
        key={buttonExpandText}
        text={buttonExpandText}
        icon={expandIcon}
        styleIcon={styleControlButtonIcon}
        onClick={expandAll}
      />
    )
    let buttonCollapseText = 'Collapse All'
    controlButtons.push(
      <Button
        key={buttonCollapseText}
        text={buttonCollapseText}
        icon={collapseIcon}
        styleIcon={styleControlButtonIcon}
        onClick={collapseAll}
      />
    )
  }

  // add any provided custom actions to the control element
  // e.g. -
  // {
  //   "Clear All": {
  //     icon: clearIcon,       // `import clearIcon from 'fa/remove.svg'`
  //     handler: (itemsMap, itemsMapPrevious, ListItemComponent, GridItemComponent, showAsGrid, focusKey) => { ... } // action handler for when button is activated
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
          styleIcon={styleControlButtonIcon}
          onClick={() =>
            action.handler({
              itemsMap,
              itemsMapPrevious,
              propsForItem,
              ListItemComponent,
              GridItemComponent,
              showAsGrid,
              focusKey,
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
