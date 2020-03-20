import React, {useState} from 'react'
import {LiveAnnouncer, LiveMessage} from 'react-aria-live'
import Meta from 'react-helmet'
import ListView from '../common/ui/ListView'
import {boxShadow} from '../../style/defaultStyles'
import {identifyProtocol} from '../../utils/resultUtils'
import clearIcon from 'fa/ban.svg'

import {fontFamilySerif} from '../../utils/styleUtils'
import ScriptDownloader from './ScriptDownloader'
import {FEATURE_CART} from '../../utils/featureUtils'
import CartListItem from './CartListItem'
import {PAGE_SIZE} from '../../utils/queryUtils'

const styleCenterContent = {
  display: 'flex',
  justifyContent: 'center',
}

const styleCartListWrapper = {
  maxWidth: '80em',
  width: '80em',
  boxShadow: boxShadow,
  paddingTop: '1.618em',
  paddingBottom: '1.618em',
  backgroundColor: 'white',
  color: '#222',
}

const styleListHeading = {
  fontFamily: fontFamilySerif(),
  fontSize: '1.2em',
}

const styleCartActions = {
  margin: '0 1.618em 1.618em 1.618em',
}

const styleCartActionsTitle = {
  fontFamily: fontFamilySerif(),
  fontSize: '1.2em',
  margin: '0 0 0.618em 0',
  padding: 0,
}

// export default class Cart extends React.Component {
export default function Cart(props){
  const {
    featuresEnabled,
    // loading, TODO does this need to wire up specific loading state somewhere else correctly now?
    selectedGranules,
    numberOfGranulesSelected,
    deselectAllGranules,
  } = props

  if (!featuresEnabled.includes(FEATURE_CART)) {
    return null
  }

  // keep track of used protocols in results to avoid unnecessary legend keys
  const usedProtocols = new Set()

  const [ offset, setOffset ] = useState(0)
  const [ currentPage, setCurrentPage ] = useState(1)

  //only show granules for this page
  const allowed = Object.keys(selectedGranules).slice(
    offset,
    offset + PAGE_SIZE
  )

  const subset = Object.keys(selectedGranules)
    .filter(key => allowed.includes(key))
    .reduce((obj, key) => {
      obj[key] = selectedGranules[key]
      return obj
    }, {})

  const propsForItem = (item, itemId, setFocusedKey) => {
    const {collectionDetailFilter, selectCollection, deselectGranule} = props
    const collectionId = item.internalParentIdentifier
    return {
      onSelect: key => {
        selectCollection(collectionId, collectionDetailFilter)
      },
      setFocusedKey,
      deselectGranule,
    }
  }

  for (let key in selectedGranules) {
    if (selectedGranules.hasOwnProperty(key)) {
      const value = selectedGranules[key]
      _.forEach(value.links, link => {
        // if(link.linkFunction.toLowerCase() === 'download' || link.linkFunction.toLowerCase() === 'fileaccess') {
        return usedProtocols.add(identifyProtocol(link))
        // }
      })
    }
  }

  const cartActionsWrapper =
    numberOfGranulesSelected === 0 ? null : (
      <div style={styleCartActions}>
        <h1 style={styleCartActionsTitle}>Cart Actions</h1>
        <ScriptDownloader
          key="scriptDownloaderButton"
          selectedGranules={selectedGranules}
        />
      </div>
    )

  let message = 'No files selected for download'
  if (numberOfGranulesSelected > 0) {
    message = `Showing ${offset + 1} - ${offset +
      Object.keys(subset)
        .length} of ${numberOfGranulesSelected.toLocaleString()} files for download`
  }
  /**
  NOTE: this uses LiveAnnouncer instead of the following span, because the message does not toggle to "loading" in between, causing it to read changes incorrectly.
  <span role="alert" aria-live="polite">
    {message}
  </span>
  */
  const listHeading = (
    <h2 key="Cart::listHeading" style={styleListHeading}>
      <LiveAnnouncer>
        <LiveMessage message={message} aria-live="polite" />
      </LiveAnnouncer>
      <span aria-hidden="true">{message}</span>
    </h2>
  )

  const cartListCustomActions = [
    {
      text: 'Clear All',
      title: 'Clear All Files from Cart',
      icon: clearIcon,
      showText: false,
      handler: deselectAllGranules,
      notification: 'Clearing all files from cart',
    },
  ]

  return (
    <div style={styleCenterContent}>
      <Meta title="File Access Cart" robots="noindex" />

      <div style={styleCartListWrapper}>
        {cartActionsWrapper}
        <ListView
          totalRecords={numberOfGranulesSelected}
          items={subset}
          ListItemComponent={CartListItem}
          GridItemComponent={null}
          propsForItem={propsForItem}
          heading={listHeading}
          customActions={cartListCustomActions}
          setOffset={offset => {
            setOffset(offset)
          }}
          currentPage={currentPage}
          setCurrentPage={page => setCurrentPage(page)}
        />
      </div>
    </div>
  )
}
