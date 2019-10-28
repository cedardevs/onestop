import React, {useEffect, useState, useRef, useCallback} from 'react'
import PropTypes from 'prop-types'
import ListViewController from './ListViewController'
import {mapFromObject} from '../../../utils/objectUtils'

const styleListView = {
  marginLeft: '1.618em',
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

function usePrevious(value){
  const ref = useRef()
  useEffect(() => {
    ref.current = value
  })
  return ref.current
}

function useItems(items){
  // initial items map is empty
  const [ itemsMap, setItemsMap ] = useState(new Map())
  // keep track of previous items map
  const previous = usePrevious({itemsMap})
  // initial previous items map is also empty
  const [ itemsMapPrevious, setItemsMapPrevious ] = useState(new Map())

  const [ focusedKey, setFocusedKey ] = useState(null)
  const focusedRef = useRef(null)

  // this effect tracks when the items supplied to ListView changes
  useEffect(
    () => {
      // ensure our items are stored as a JavaScript `Map`
      // as a `Map` can iterate elements in insertion order and easily
      // retrieve the size of the items, without counting keys
      setItemsMap(mapFromObject(items))
      // we track our `previous` items after they've already been
      // converted to a `Map`, so there's no need to convert it here
      setItemsMapPrevious(previous ? previous.itemsMap : new Map())

      // focus on what next new item, or the first item if the previous focused key cannot be found; otherwise, previous focused item (if key to previous focused item is the last item),
      // let keyIterator = itemsMap.keys()
      // let done = false
      // let value = undefined
      // while(!done && value !== focusedKey) {
      //   let ki = keyIterator.next();
      //   done = ki.done;
      //   value = ki.value;
      // }
      // // focus on next new item because we found our previously focused key and it isn't the last key in the map
      // if(!done && value === focusedKey) {
      //   let firstNewItemKey = keyIterator.next().value
      //   console.log("firstNewItemKey:", firstNewItemKey)
      //   setFocusedKey(firstNewItemKey)
      // }
      // // focus on the first item in the map because the iterator finished without finding the previously focused key
      // if(done && value !== focusedKey) {
      //   let firstItemKey = itemsMap.keys().next().value
      //   console.log("firstItemKey:", firstItemKey)
      //   setFocusedKey(firstItemKey)
      // }
      // otherwise we must have found the previously focused key, and it *was* last, so we continue to focus on it
    },
    [ items ]
  )

  useEffect(() => {
    if (focusedRef.current) {
      focusedRef.current.focus()
    }
  })

  return [ itemsMap, itemsMapPrevious, focusedKey, setFocusedKey, focusedRef ]
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
    customActions,
  } = props

  const [
    itemsMap,
    itemsMapPrevious,
    focusedKey,
    setFocusedKey,
    focusedRef,
  ] = useItems(items)
  const [ showAsGrid, setShowAsGrid ] = useState(
    !!props.showAsGrid && !!props.GridItemComponent
  )

  const [ focusingDefaultItem, setFocusingDefaultItem ] = useState(false)
  const [ expanded, cycleExpanded ] = useCycleState(null)

  // list view controller
  const controlElement = (
    <ListViewController
      itemsMap={itemsMap}
      itemsMapPrevious={itemsMapPrevious}
      propsForItem={propsForItem}
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

    const isFocused = key === focusedKey
    console.log(`key=${key}, isFocused=${isFocused}`)
    const itemProps = propsForItem ? propsForItem(item, key, isFocused) : null

    // list item element
    if (!showAsGrid && ListItemComponent) {
      itemElement = (
        <ListItemComponent
          key={key}
          tabIndex={-1}
          ref={isFocused ? focusedRef : null}
          itemId={key}
          item={item}
          expanded={expanded}
          onFocus={() => {
            console.log('is this focusing?', key)
            setFocusedKey(key)
          }}
          onBlur={() => {
            console.log('is this blurring?')
            setFocusedKey(null)
          }}
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
          onFocus={() => setFocusedKey(key)}
          onBlur={() => setFocusedKey(null)}
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
      {controlElement}
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
