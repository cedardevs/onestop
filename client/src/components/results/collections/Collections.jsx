import React, {useState, useEffect} from 'react'
import PropTypes from 'prop-types'
import CollectionGridItem from './CollectionGridItem'
import ListView from '../../common/ui/ListView'
import Meta from '../../helmet/Meta'
import CollectionListItem from './CollectionListItem'
import {fontFamilySerif} from '../../../utils/styleUtils'
import {asterisk, SvgIcon} from '../../common/SvgIcon'
import {decodePathAndQueryString, PAGE_SIZE} from '../../../utils/queryUtils'
import defaultStyles from '../../../style/defaultStyles'
import saveIcon from 'fa/bookmark-o.svg'
import alreadySavedIcon from 'fa/bookmark.svg'
import _ from 'lodash'

const styleCollections = {
  color: '#222',
}

const styleListHeading = {
  fontFamily: fontFamilySerif(),
  fontSize: '1.2em',
}

const styleShowMore = {
  margin: '1em auto 1.618em auto',
}

const styleShowMoreFocus = {
  outline: '2px dashed #5C87AC',
  outlineOffset: '.118em',
}

export default function Collections(props){
  const {
    searchTerms,
    results,
    totalHits,
    fetchResultPage,
    selectCollection,
    collectionDetailFilter,
    loading,
    saveSearch,
    savedSearchUrl,
    isAuthenticatedUser,
    savedSearches, // todo use this to figure out if their current filter is already saved
    collectionFilter,
  } = props

  const queryText = props.collectionDetailFilter.queryText
  const [ offset, setOffset ] = useState(0)
  const [ currentPage, setCurrentPage ] = useState(1)
  const [ headingMessage, setHeadingMessage ] = useState(null)
  const [searchSaved, setSearchSaved] = useState(false)

  function handleSave(savedSearchUrl, urlToSave, filter) {
    const queryStringIndex = urlToSave.indexOf('?')
    const queryString = urlToSave.slice(queryStringIndex)
    const decodedSavedSearch = decodePathAndQueryString('', queryString)
    saveSearch(savedSearchUrl, urlToSave, decodedSavedSearch.filters.queryText, filter)
    setSearchSaved(true)
  }

  const currentSearchAlreadySaved = searchSaved //todo identify if they have this search saved already
  const bookmarkIcon = currentSearchAlreadySaved ? alreadySavedIcon : saveIcon
  const title = currentSearchAlreadySaved ? 'Search already saved' : 'Save search'
  const text = currentSearchAlreadySaved ? 'Unsave' : 'Save'
  const notification = text

  const saveSearchAction =  isAuthenticatedUser ? [
    {
      text: text,
      title: title,
      icon: bookmarkIcon,
      showText: false,
      handler: () => {handleSave(savedSearchUrl, window.location.href, collectionFilter)},
      notification: notification,
    }
  ] : []

  useEffect(
    () => {
      if (loading) {
        setHeadingMessage(
          <span>
            <SvgIcon
              style={{fill: 'white', animation: 'rotation 2s infinite linear'}}
              path={asterisk}
              size=".9em"
              verticalAlign="unset"
            />&nbsp;Loading collections
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
        setHeadingMessage(
          <span>
            <span>Showing {offset + 1} </span>
            {thru}{' '}
            <span>
              {offset + size} of {totalHits.toLocaleString()} collection results
              matching '{searchTerms}'
            </span>
          </span>
        )
      }
      else {
        setHeadingMessage(`No collection results matched '${searchTerms}'`)
      }
    },
    [ loading ]
  )
  const listHeading = (
    <h2 key="Collections::listHeading" style={styleListHeading}>
      <span role="alert" aria-live="polite">
        {headingMessage}
      </span>
    </h2>
  )

  const propsForItem = (item, itemId) => {
    return {
      onSelect: key => {
        selectCollection(key, collectionDetailFilter)
      },
    }
  }

  return (
    <div style={styleCollections}>
      <Meta
        title={'Collection Search Results for ' + queryText}
        formatTitle={true}
        robots="noindex"
      />
      <ListView
        totalRecords={totalHits}
        items={results}
        ListItemComponent={CollectionListItem}
        GridItemComponent={CollectionGridItem}
        propsForItem={propsForItem}
        heading={listHeading}
        showAsGrid={true}
        setOffset={offset => {
          setOffset(offset)
          fetchResultPage(offset, PAGE_SIZE)
        }}
        currentPage={currentPage}
        setCurrentPage={page => setCurrentPage(page)}
        customActions={saveSearchAction}
      />
      {/*{showMoreButton}*/}
    </div>
  )
}

Collections.propTypes = {
  results: PropTypes.object.isRequired,
  totalHits: PropTypes.number.isRequired,
  returnedHits: PropTypes.number.isRequired,
  pageSize: PropTypes.number,
}
