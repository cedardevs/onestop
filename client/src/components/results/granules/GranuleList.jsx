import React, {useState} from 'react'
import PropTypes from 'prop-types'
import Button from '../../common/input/Button'
import ListView from '../../common/ui/ListView'
import {SiteColors} from '../../../style/defaultStyles'
import Meta from '../../helmet/Meta'
import _ from 'lodash'
import cartIcon from '../../../../img/font-awesome/white/svg/shopping-cart.svg'
import GranuleListItem from './GranuleListItem'
import {fontFamilySerif} from '../../../utils/styleUtils'
import {Link} from 'react-router-dom'

const styleCenterContent = {
  display: 'flex',
  justifyContent: 'center',
}

const styleGranuleListWrapper = {
  maxWidth: '80em',
  width: '80em',
  color: '#222',
}

const styleListHeading = {
  fontFamily: fontFamilySerif(),
  fontSize: '1.2em',
}

const styleLink = focusing => {
  return {
    color: SiteColors.LINK_LIGHT,
    textDecoration: 'underline',
    outline: focusing ? '2px dashed white' : 'none',
    outlineOffset: focusing ? '0.309em' : 'initial',
  }
}

const styleShowMore = {
  margin: '1em auto 1.618em auto',
}
const styleShowMoreFocus = {
  outline: '2px dashed #5C87AC',
  outlineOffset: '.118em',
}

const styleWarning = warning => {
  if (_.isEmpty(warning)) {
    return {
      display: 'none',
    }
  }
  else {
    return {
      backgroundColor: SiteColors.WARNING,
      color: 'white',
      fontWeight: 'bold',
      fontSize: '1.15em',
      margin: '0 1.618em 1em 0',
      padding: '0.618em',
      display: 'flex',
      justifyContent: 'center',
      borderRadius: '0.309em',
    }
  }
}

export default function GranuleList(props){
  const {
    results,
    returnedHits,
    totalHits,
    fetchMoreResults,
    addFilteredGranulesToCart,
    addFilteredGranulesToCartWarning,
    collectionId,
    collectionTitle,
    granuleFilter,
    selectedGranules,
    selectGranule,
    deselectGranule,
  } = props

  const [ focusingCollectionLink, setFocusingCollectionLink ] = useState(false)

  const isGranuleSelected = itemId => {
    const checkIt = Object.keys(selectedGranules).includes(itemId)
    return checkIt
  }

  const handleCheckboxChange = (itemId, item) => {
    return checkbox => {
      if (checkbox.checked) {
        selectGranule(item, itemId)
      }
      else {
        deselectGranule(itemId)
      }
    }
  }

  const propsForItem = (item, itemId, setFocusedKey) => {
    return {
      onSelect: () => {},
      showLinks: true,
      showTimeAndSpace: true,
      handleCheckboxChange: handleCheckboxChange,
      checkGranule: isGranuleSelected(itemId),
    }
  }

  const showMoreButton =
    returnedHits < totalHits ? (
      <Button
        text="Show More Results"
        onClick={() => fetchMoreResults()}
        style={styleShowMore}
        styleFocus={styleShowMoreFocus}
      />
    ) : null

  let message = 'No file results'
  if (totalHits > 0) {
    message = `Showing ${returnedHits.toLocaleString()} of ${totalHits.toLocaleString()} matching files`
  }
  const listHeading = (
    <h2 key="GranuleList::listHeading" style={styleListHeading}>
      {message} within&nbsp;
      <Link
        style={styleLink(focusingCollectionLink)}
        to={`/collections/details/${collectionId}`}
        title={'Return to collection details.'}
        onFocus={() => setFocusingCollectionLink(true)}
        onBlur={() => setFocusingCollectionLink(false)}
      >
        collection
      </Link>
    </h2>
  )

  const granuleListCustomActions = {
    'Add Matching to Cart': {
      title: 'Add Matching to Cart',
      icon: cartIcon,
      showText: false,
      handler: () => addFilteredGranulesToCart(granuleFilter),
    },
  }

  let customMessage = addFilteredGranulesToCartWarning ? (
    <div
      key="GranuleList::Warning"
      style={styleWarning(addFilteredGranulesToCartWarning)}
      role="alert"
    >
      {addFilteredGranulesToCartWarning}
    </div>
  ) : null

  return (
    <div style={styleCenterContent}>
      <Meta
        title={`Files in Collection ${collectionTitle}`}
        formatTitle={true}
        robots="noindex"
      />

      <div style={styleGranuleListWrapper}>
        <ListView
          items={results}
          ListItemComponent={GranuleListItem}
          GridItemComponent={null}
          propsForItem={propsForItem}
          heading={listHeading}
          customActions={granuleListCustomActions}
          customMessage={customMessage}
        />
        {showMoreButton}
      </div>
    </div>
  )
}

GranuleList.propTypes = {
  results: PropTypes.object.isRequired,
  totalHits: PropTypes.number.isRequired,
  returnedHits: PropTypes.number.isRequired,
}
