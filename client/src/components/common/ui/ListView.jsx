import React, {useEffect, useRef, useState} from 'react'
import PropTypes from 'prop-types'
import ListViewController from './ListViewController'
import {mapFromObject} from '../../../utils/objectUtils'
import FlexRow from './FlexRow'
import {FilterStyles} from '../../../style/defaultStyles'

const styleListView = {
  marginLeft: '1.618em',
}

const styleHeading = {
  justifyContent: 'space-between',
  alignItems: 'center',
  margin: '0 1.618em 0.618em 0',
  paddingLeft: '1em',
  borderRadius: '0.309em',
  ...FilterStyles.DARKEST,
}

const styleList = {
  display: 'flex',
  flexDirection: 'column',
  flexWrap: 'nowrap',
}

const styleGrid = {
  display: 'flex',
  flexDirection: 'row',
  flexWrap: 'wrap',
  justifyContent: 'center',
  alignItems: 'flex-start',
  alignContent: 'flex-start',
}

const styleDefaultItem = focusing => {
  const styleDefaultFocus = {
    outline: 'none',
    border: '.1em dashed white', // ems so it can be calculated into the total size easily - border + padding + margin of this style must total the same as padding in styleOverallHeading, or it will resize the element when focus changes
    padding: '.259em',
    margin: '.259em',
  }
  return {
    display: 'block',
    margin: '0 1.618em 0 0',
    ...(focusing ? styleDefaultFocus : {}),
  }
}

function usePrevious(value, defaultValue = undefined){
  const ref = useRef()
  useEffect(
    () => {
      ref.current = value
    },
    [ value ]
  )
  return ref.current ? ref.current : defaultValue
}

function useItems(items){
  const [ itemsMap, setItemsMap ] = useState(mapFromObject(items))

  // keep track of previous items map
  const previousItemsMap = usePrevious(itemsMap, new Map())

  const [ focusedKey, setFocusedKey ] = useState(undefined)
  const [ lastItem, setLastItem ] = useState(undefined)

  // this effect tracks when the items supplied to ListView changes
  useEffect(
    () => {
      // ensure our items are stored as a JavaScript `Map`
      // as a `Map` can iterate elements in insertion order and easily
      // retrieve the size of the items, without counting keys
      setItemsMap(mapFromObject(items))
    },
    [ items ]
  )

  // this effect tracks when the derived itemsMap changes
  useEffect(
    () => {
      const keysAfter = [ ...itemsMap.keys() ]

      const nextKey = keysAfter.indexOf(lastItem) + 1
      setFocusedKey(keysAfter[nextKey])

      let lastKey = undefined
      itemsMap.forEach((item, key) => {
        lastKey = key
      }) // I am having a brainfart day and can't figure out the right thing to get the last element without using the forEach to loop over them...
      setLastItem(lastKey)
    },
    [ itemsMap ]
  )

  return [ itemsMap, previousItemsMap, focusedKey, setFocusedKey ]
}

// TODO: eventually ListView won't take control over its own global expanded state, but will be stored
// in local storage (if available) and redux state to preserve expanded states between pages and refreshes, etc...
function useCycleState(steadyStateValue){
  const [ state, cycleValue ] = useState(steadyStateValue)
  useEffect(() => {
    if (state !== steadyStateValue) {
      cycleValue(steadyStateValue)
    }
  })
  return [ state, cycleValue ]
}

export default function ListView(props){
  const {
    items,
    ListItemComponent,
    GridItemComponent,
    propsForItem,
    heading,
    customActions,
    customMessage,
  } = props

  const [ itemsMap, previousItemsMap, focusedKey, setFocusedKey ] = useItems(
    items
  )
  const [ showAsGrid, setShowAsGrid ] = useState(
    !!props.showAsGrid && !!props.GridItemComponent
  )

  const [ focusingDefaultItem, setFocusingDefaultItem ] = useState(false)
  const [ expanded, cycleExpanded ] = useCycleState(null)

  // list view controller
  const controlElement = (
    <ListViewController
      key={'ListViewController'}
      itemsMap={itemsMap}
      previousItemsMap={previousItemsMap}
      propsForItem={propsForItem}
      focusedKey={focusedKey}
      ListItemComponent={ListItemComponent}
      GridItemComponent={GridItemComponent}
      showAsGrid={showAsGrid}
      toggleGrid={() => setShowAsGrid(!showAsGrid)}
      expandAll={() => cycleExpanded(true)}
      collapseAll={() => cycleExpanded(false)}
      customActions={customActions}
    />
  )

  let itemElements = []
  itemsMap.forEach((item, key) => {
    let itemElement = null

    const shouldFocus = key === focusedKey
    const itemProps = propsForItem
      ? propsForItem(item, key, setFocusedKey)
      : null

    // list item element
    if (!showAsGrid && ListItemComponent) {
      itemElement = (
        <ListItemComponent
          key={key}
          tabIndex={-1}
          itemId={key}
          item={item}
          expanded={expanded}
          shouldFocus={shouldFocus}
          {...itemProps}
        />
      )
    }
    else if (showAsGrid && GridItemComponent) {
      // grid item element
      itemElement = (
        <GridItemComponent
          key={key}
          tabIndex={-1}
          itemId={key}
          item={item}
          shouldFocus={shouldFocus}
          {...itemProps}
        />
      )
    }
    else {
      // default item element
      itemElement = (
        <div
          key={key}
          tabIndex={-1}
          style={styleDefaultItem(focusingDefaultItem)}
          onFocus={() => setFocusingDefaultItem(true)}
          onBlur={() => setFocusingDefaultItem(false)}
          children={key}
        />
      )
    }
    itemElements.push(itemElement)
  })

  return (
    <div style={styleListView}>
      <FlexRow style={styleHeading} items={[ heading, controlElement ]} />
      {customMessage}
      <div style={showAsGrid ? styleGrid : styleList}>{itemElements}</div>
    </div>
  )
}

ListView.propTypes = {
  items: PropTypes.object,
  showAsGrid: PropTypes.bool,
  ListItemComponent: PropTypes.func,
  GridItemComponent: PropTypes.func,
  propsForItem: PropTypes.func,
  customActions: PropTypes.object,
}
