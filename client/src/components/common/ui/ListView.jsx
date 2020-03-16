import React, {useEffect, useRef, useState} from 'react'
import PropTypes from 'prop-types'
import ListViewController from './ListViewController'
import {mapFromObject} from '../../../utils/objectUtils'
import FlexRow from './FlexRow'
import {FilterStyles} from '../../../style/defaultStyles'
import {LiveAnnouncer, LiveMessage} from 'react-aria-live'
import gridIcon from 'fa/th.svg'
import listIcon from 'fa/th-list.svg'
import expandIcon from 'fa/expand.svg'
import collapseIcon from 'fa/compress.svg'
import previousIcon from 'fa/arrow-left.svg'
import nextIcon from 'fa/arrow-right.svg'

import PageController, {usePaging} from './PageController'
import {PAGE_SIZE} from '../../../utils/queryUtils'
import Button from '../input/Button'

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

  return [ itemsMap, previousItemsMap ]
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
    // add any provided custom actions to the control element
    // e.g. -
    // [
    //   {
    //     text: "Clear All",
    //     notification: 'Clearing all.', // a11y feature to notifiy users of AT that something elsewhere on the page is changing
    //     icon: clearIcon, // `import clearIcon from 'fa/remove.svg'`
    //     handler: ({
    //       itemsMap,
    //       previousItemsMap,
    //       ListItemComponent,
    //       GridItemComponent,
    //       showAsGrid
    //     }) => { ... } // action handler for when button is activated
    //   }
    // ]
    customActions,
    customMessage,
    totalRecords,
    setOffset,
    currentPage,
    setCurrentPage,
  } = props

  const [ notification, setNotification ] = useState('')

  const [ itemsMap, previousItemsMap ] = useItems(items)
  const [ showAsGrid, setShowAsGrid ] = useState(
    !!props.showAsGrid && !!props.GridItemComponent
  )

  const [ focusingDefaultItem, setFocusingDefaultItem ] = useState(false)
  const [ expanded, cycleExpanded ] = useCycleState(null)

  const actions = []

  // if both list and grid components are provided,
  // we can show a toggle between views
  if (ListItemComponent && GridItemComponent) {
    actions.push({
      text: 'Toggle',
      title: showAsGrid ? 'List View' : 'Grid View',
      icon: showAsGrid ? listIcon : gridIcon,
      showText: false,
      handler: () => setShowAsGrid(!showAsGrid),
      notification: showAsGrid
        ? 'Displaying as list.'
        : 'Displaying as grid view.',
    })
  }

  // if a list component is provided and we are currently showing as a list
  const numItems = itemsMap ? itemsMap.size : 0
  if (ListItemComponent && !showAsGrid && numItems > 0) {
    actions.push({
      text: 'Expand',
      title: 'Expand All',
      icon: expandIcon,
      showText: false,
      handler: () => cycleExpanded(true),
      notification: 'Expanding all results in list.',
    })
    actions.push({
      text: 'Collapse',
      title: 'Collapse All',
      icon: collapseIcon,
      showText: false,
      handler: () => cycleExpanded(false),
      notification: 'Collapsing all results in list.',
    })
  }

  // use paging effect to keep track of paging conditions
  // - this result can be passed directly to the PageController component
  //   but you can make use of the values within for other conditional rendering (see actions pushed below)
  // paging = {
  //     totalRecords,
  //     totalPages,
  //     currentPage,
  //     handleSkipPrevious,
  //     handleSkipNext,
  //     numSkipPrevious,
  //     numSkipNext,
  // }
  const paging = usePaging({
    totalRecords,
    pageLimit: PAGE_SIZE,
    pageNeighbours: 2,
    setOffset,
    currentPage,
    setCurrentPage,
  })

  // if there is a 'previous' page, allow user to control from list view controller at top
  if (currentPage > 1) {
    actions.push({
      text: 'Previous Page',
      title: 'Go to previous page',
      icon: previousIcon,
      showText: false,
      handler: () => setCurrentPage(currentPage - 1),
      notification: 'Navigating to the previous page.',
    })
  }

  // if there is a 'next' page, allow user to control from list view controller at top
  if (currentPage + 1 <= paging.totalPages) {
    actions.push({
      text: 'Next Page',
      title: 'Go to next page',
      icon: nextIcon,
      showText: false,
      handler: () => setCurrentPage(currentPage + 1),
      notification: 'Navigating to the next page.',
    })
  }

  if (customActions) {
    customActions.forEach(action => {
      actions.push(action)
    })
  }

  // list view controller
  const controlElement = (
    <ListViewController
      key={'ListViewController'}
      itemsMap={itemsMap}
      previousItemsMap={previousItemsMap}
      propsForItem={propsForItem}
      ListItemComponent={ListItemComponent}
      GridItemComponent={GridItemComponent}
      showAsGrid={showAsGrid}
      notification={setNotification}
      actions={actions}
    />
  )

  const pageController = (
    <PageController
      paging={paging}
      ButtonComponent={({
        page,
        numSkip,
        onClick,
        style,
        styleHover,
        stylePress,
        styleFocus,
        styleDisable,
        config,
      }) => {
        const {isFirst, isPrevious, isNext, isLast} = config
        const previousText = `«`
        const nextText = `»`
        const title = isFirst
          ? 'Go to first page'
          : isPrevious
            ? `Skip to page ${page}`
            : isNext
              ? `Skip to page ${page}`
              : isLast ? `Go to last page - ${page}` : `Go to page ${page}`
        return (
          <Button
            onClick={onClick}
            title={title}
            style={style}
            styleHover={styleHover}
            stylePress={stylePress}
            styleFocus={styleFocus}
            styleDisable={styleDisable}
            config={config}
            ariaCurrent={config.isActive}
          >
            {isPrevious ? previousText : isNext ? nextText : page}
          </Button>
        )
      }}
    />
  )

  let itemElements = []
  itemsMap.forEach((item, key) => {
    let itemElement = null

    const itemProps = propsForItem ? propsForItem(item, key) : null

    // list item element
    if (!showAsGrid && ListItemComponent) {
      itemElement = (
        <ListItemComponent
          key={key}
          itemId={key}
          item={item}
          expanded={expanded}
          {...itemProps}
        />
      )
    }
    else if (showAsGrid && GridItemComponent) {
      // grid item element
      itemElement = (
        <GridItemComponent key={key} itemId={key} item={item} {...itemProps} />
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

      <LiveAnnouncer>
        <LiveMessage message={notification} aria-live="polite" />
      </LiveAnnouncer>
      {customMessage}
      <div style={showAsGrid ? styleGrid : styleList}>{itemElements}</div>
      {pageController}
    </div>
  )
}

ListView.propTypes = {
  items: PropTypes.object,
  showAsGrid: PropTypes.bool,
  ListItemComponent: PropTypes.func,
  GridItemComponent: PropTypes.func,
  propsForItem: PropTypes.func,
  customActions: PropTypes.array,
  totalHits: PropTypes.number,
  setOffset: PropTypes.func,
  currentPage: PropTypes.number,
  setCurrentPage: PropTypes.func,
}
