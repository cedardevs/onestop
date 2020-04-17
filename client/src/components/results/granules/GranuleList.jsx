import React, {useState} from 'react'
import PropTypes from 'prop-types'
import ListView from '../../common/ui/ListView'
import {SiteColors} from '../../../style/defaultStyles'
import Meta from '../../helmet/Meta'
import _ from 'lodash'
import cartIcon from 'fa/cart-plus.svg'
import GranuleListItem from './GranuleListItem'
import {fontFamilySerif} from '../../../utils/styleUtils'
import {Link} from 'react-router-dom'
import {asterisk, SvgIcon} from '../../common/SvgIcon'
import {PAGE_SIZE} from '../../../utils/queryUtils'
import defaultStyles from '../../../style/defaultStyles'

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
    fetchResultPage,
    addFilteredGranulesToCart,
    addFilteredGranulesToCartWarning,
    collectionId,
    collectionTitle,
    granuleFilter,
    selectedGranules,
    selectGranule,
    deselectGranule,
    loading,
  } = props

  const [ focusingCollectionLink, setFocusingCollectionLink ] = useState(false)
  const [ offset, setOffset ] = useState(0)
  const [ currentPage, setCurrentPage ] = useState(1)

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

  const propsForItem = (item, itemId) => {
    return {
      onSelect: () => {},
      handleCheckboxChange: handleCheckboxChange,
      checkGranule: isGranuleSelected(itemId),
    }
  }

  let message = 'No file results'
  if (loading) {
    message = (
      <span>
        <SvgIcon
          style={{fill: 'white', animation: 'rotation 2s infinite linear'}}
          path={asterisk}
          size=".9em"
          verticalAlign="unset"
        />&nbsp;Loading files
      </span>
    )
  }
  else if (totalHits > 0) {
    var size = Object.keys(results).length
    var thru = (
      <span>
        <span aria-hidden="true">-</span>
        <span style={defaultStyles.hideOffscreen}>to</span>
      </span>
    )
    message = (
      <span>
        <span>Showing {offset + 1} </span>
        {thru}{' '}
        <span>
          {offset + size} of {totalHits.toLocaleString()} matching files
        </span>
      </span>
    )
  }
  const listHeading = (
    <h2 key="GranuleList::listHeading" style={styleListHeading}>
      <span role="alert" aria-live="polite">
        {message}
      </span>{' '}
      within&nbsp;
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

  const granuleListCustomActions = [
    {
      text: 'Add Matching to Cart',
      title: 'Add Matching to Cart',
      icon: cartIcon,
      showText: false,
      handler: () => addFilteredGranulesToCart(granuleFilter),
      notification: 'Adding all files matching this search to cart.',
    },
  ]

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
          totalRecords={totalHits}
          items={results}
          ListItemComponent={GranuleListItem}
          GridItemComponent={null}
          propsForItem={propsForItem}
          heading={listHeading}
          customActions={granuleListCustomActions}
          customMessage={customMessage}
          setOffset={offset => {
            setOffset(offset)
            fetchResultPage(offset, PAGE_SIZE)
          }}
          currentPage={currentPage}
          setCurrentPage={page => setCurrentPage(page)}
        />
      </div>
    </div>
  )
}

GranuleList.propTypes = {
  results: PropTypes.object.isRequired,
  totalHits: PropTypes.number.isRequired,
  returnedHits: PropTypes.number.isRequired,
}
