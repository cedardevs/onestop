import React, {useEffect, useState, useRef, useCallback} from 'react'
import PropTypes from 'prop-types'
import ListViewController from './ListViewController'
import {mapFromObject} from '../../../utils/objectUtils'
import {useListViewItem} from "./ListViewItem";

const styleListView = {
  marginLeft: '1.618em',
}

const styleGrid = {
  display: 'flex',
  flexDirection: 'row',
  flexWrap: 'wrap',
  justifyContent: 'center',
  alignItems: 'flex-start',
  alignContent: 'flex-start',
}

const styleList = {
  display: 'flex',
  flexDirection: 'column',
  flexWrap: 'nowrap',
}

const styleFallbackItem = {
  display: 'block',
  margin: '0 1.618em 0 0',
}

const styleFocusDefault = {
  outline: 'none',
  border: '.1em dashed white', // ems so it can be calculated into the total size easily - border + padding + margin of this style must total the same as padding in styleOverallHeading, or it will resize the element when focus changes
  padding: '.259em',
  margin: '.259em',
}

function useHookWithRefCallback(setFocusKey){
  const ref = useRef(null)
  let focusKey = null

  const setRef = useCallback(node => {
    if (ref.current) {
      // Make sure to cleanup any events/references added to the last instance
    }

    if (node) {
      // console.log("node.props", node.props)
      // Check if a node is actually passed. Otherwise node would be null.
      // You can now do what you need to, addEventListeners, measure, etc.
      if (node.props && node.props.itemId && node.props.itemId) {
        if (setFocusKey) {
          setFocusKey(node.props.itemId)
        }
      }
    }

    // Save a reference to the node
    ref.current = node
  }, []) // TODO: set `node` id/key in inputs here? -- to take advantage of useCallback memoization

  return [ setRef ]
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
    },
    [ items ]
  )

  return [ itemsMap, itemsMapPrevious ]
}

export default function ListView(props){
  const {
    items,
    ListItemComponent,
    GridItemComponent,
    propsForItem,
    customActions,
  } = props
  const [ itemsMap, itemsMapPrevious ] = useItems(items)

  const [ showAsGrid, setShowAsGrid ] = useState(
    !!props.showAsGrid && !!props.GridItemComponent
  )

  // TODO: eventually ListView won't take control over its own global expanded state, but will be stored
  // in local storage (if available) and redux state to preserve expanded states between pages and refreshes, etc...
  const [expanded, setExpanded] = useState(null)

  useEffect(() => {
    if(expanded !== null) {
      setExpanded(null)
    }
  })

  // const [focusing, setFocusing] = useState(false)
  //
  // const [focusKey, setFocusKey] = useState(null)
  // const [focusItemRef] = useHookWithRefCallback(setFocusKey)

  const numItems = itemsMap ? itemsMap.size : 0
  const numItemsPrevious = itemsMapPrevious ? itemsMapPrevious.size : 0

  const cycleState = (setter, transientValue) => {
    setter(transientValue)
  }

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
      expandAll={() => cycleState(setExpanded, true)}
      collapseAll={() => cycleState(setExpanded, false)}
      customActions={customActions}
    />
  )

  console.log("expanded:", expanded)

  let itemElements = []
  itemsMap.forEach((item, key) => {
    // if(key === focusKey) {
    //   console.log("key === focusKey -> ", focusKey)
    // }

    // const isNextFocus = numItemsPrevious > 0 && numItemsPrevious === itemElements.length
    //
    // if(isNextFocus) {
    //   console.log(`isNextFocus:${isNextFocus}, focusKey:${focusKey}`)
    // }

    // const styleFocused = {
    //   ...(focusing ? styleFocusDefault : {}),
    // }

    const styleOverallItemApplied = {
      ...styleFallbackItem,
      // ...styleFocused,
    }

    let itemElement = null
    const isFocused = false // key === focusKey
    const itemProps = propsForItem ? propsForItem(item, key, isFocused) : null

    // list item element
    if (!showAsGrid && ListItemComponent) {
      itemElement = (
        <ListItemComponent
          itemId={key}
          item={item}
          key={key}
          //ref={isNextFocus ? focusItemRef : null}
          // make this a callback to allow the user control over the prop name so that it's not dictated by ListView
          // e.g. - listItemShouldFocus = key => {  }
          //shouldFocus={isNextFocus}
          expanded={expanded}
          {...itemProps}
        />
      )
    }
    else if (showAsGrid && GridItemComponent) {
      // grid item element
      itemElement = (
        <GridItemComponent
          itemId={key}
          item={item}
          key={key}
          //ref={isNextFocus ? focusItemRef : null}
          // make this a callback to allow the user control over the prop name so that it's not dictated by ListView
          //shouldFocus={isNextFocus}
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
          //ref={isNextFocus ? focusItemRef : null}
          style={styleOverallItemApplied}
          //onFocus={() => setFocusing(true)}
          //onBlur={() => setFocusing(false)}
        >
          {key}
        </div>
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
}
