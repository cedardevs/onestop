import React from 'react'
import Meta from 'react-helmet'
import ListView from '../common/ui/ListView'
import Button from '../common/input/Button'
import {boxShadow} from '../../style/defaultStyles'
import {identifyProtocol} from '../../utils/resultUtils'
import clearIcon from 'fa/ban.svg'

import {fontFamilySerif} from '../../utils/styleUtils'
import ScriptDownloader from './ScriptDownloader'
import {FEATURE_CART} from '../../utils/featureUtils'
import CartListItem from './CartListItem'

const SHOW_MORE_INCREMENT = 10

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

const styleShowMore = {
  margin: '1em auto 1.618em auto',
}
const styleShowMoreFocus = {
  outline: '2px dashed #5C87AC',
  outlineOffset: '.118em',
}

export default class Cart extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      numShownItems:
        props.numberOfGranulesSelected < SHOW_MORE_INCREMENT
          ? props.numberOfGranulesSelected
          : SHOW_MORE_INCREMENT,
    }
    this.props = props
  }

  propsForItem = (item, itemId, setFocusedKey) => {
    const {
      collectionDetailFilter,
      selectCollection,
      deselectGranule,
    } = this.props
    const collectionId = item.internalParentIdentifier
    return {
      onSelect: key => {
        selectCollection(collectionId, collectionDetailFilter)
      },
      setFocusedKey,
      deselectGranule,
    }
  }

  handleShowMore = () => {
    const {numberOfGranulesSelected} = this.props
    const {numShownItems} = this.state
    if (numShownItems < numberOfGranulesSelected) {
      const nextNumShownItems =
        numShownItems + SHOW_MORE_INCREMENT > numberOfGranulesSelected
          ? numberOfGranulesSelected
          : numShownItems + SHOW_MORE_INCREMENT

      this.setState(prevState => {
        return {
          ...prevState,
          numShownItems: nextNumShownItems,
        }
      })
    }
  }

  render() {
    const {
      featuresEnabled,
      // loading, TODO does this need to wire up specific loading state somewhere else correctly now?
      selectedGranules,
      numberOfGranulesSelected,
      deselectAllGranules,
    } = this.props

    if (!featuresEnabled.includes(FEATURE_CART)) {
      return null
    }

    const {numShownItems} = this.state
    const selectedGranulesCount = Object.keys(selectedGranules).length
    const shownGranules =
      selectedGranulesCount < numShownItems
        ? selectedGranulesCount
        : numShownItems
    // keep track of used protocols in results to avoid unnecessary legend keys
    const usedProtocols = new Set()

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

    const showMoreButton =
      numShownItems < numberOfGranulesSelected ? (
        <Button
          text="Show More"
          onClick={this.handleShowMore}
          style={styleShowMore}
          styleFocus={styleShowMoreFocus}
        />
      ) : null

    const cartActionsWrapper =
      selectedGranulesCount === 0 ? null : (
        <div style={styleCartActions}>
          <h1 style={styleCartActionsTitle}>Cart Actions</h1>
          <ScriptDownloader
            key="scriptDownloaderButton"
            selectedGranules={selectedGranules}
          />
        </div>
      )

    let message = 'No files selected for download'
    if (selectedGranulesCount > 0) {
      message = `Showing ${shownGranules.toLocaleString()} of ${selectedGranulesCount.toLocaleString()} files for download`
    }
    const listHeading = (
      <h2 key="Cart::listHeading" style={styleListHeading}>
        {message}
      </h2>
    )

    const cartListCustomActions = {
      'Clear All': {
        title: 'Clear All Files from Cart',
        icon: clearIcon,
        showText: false,
        handler: deselectAllGranules,
      },
    }

    // filter map down to size of results in cart we want (# shownGranules)
    const allowed = Object.keys(selectedGranules).slice(0, shownGranules)
    const subset = Object.keys(selectedGranules)
      .filter(key => allowed.includes(key))
      .reduce((obj, key) => {
        obj[key] = selectedGranules[key]
        return obj
      }, {})

    return (
      <div style={styleCenterContent}>
        <Meta title="File Access Cart" robots="noindex" />

        <div style={styleCartListWrapper}>
          {cartActionsWrapper}
          <ListView
            items={subset}
            ListItemComponent={CartListItem}
            GridItemComponent={null}
            propsForItem={this.propsForItem}
            heading={listHeading}
            customActions={cartListCustomActions}
          />
          {showMoreButton}
        </div>
      </div>
    )
  }
}
