import React from 'react'
import Button from '../input/Button'
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
    ListItemComponent,
    GridItemComponent,
    showAsGrid,
    actions,
    notification,
  } = props

  // initialize vars for control elements
  let controller = null
  let controlButtons = []

  const actionsMap = mapFromObject(actions)
  if (actionsMap && actionsMap.size > 0) {
    actionsMap.forEach(action => {
      controlButtons.push(
        <Button
          key={action.text}
          title={action.title}
          text={action.showText || !action.icon ? action.text : null}
          icon={action.icon}
          style={styleControlButton}
          styleIcon={styleControlButtonIcon(action.showText)}
          onClick={() => {
            notification(action.notification)
            action.handler({
              itemsMap,
              previousItemsMap,
              propsForItem,
              ListItemComponent,
              GridItemComponent,
              showAsGrid,
            })
          }}
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
