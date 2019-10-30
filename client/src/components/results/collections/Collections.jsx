import React from 'react'
import PropTypes from 'prop-types'
import CollectionGridItem from './CollectionGridItem'
import Button from '../../common/input/Button'
import ListView from '../../common/ui/ListView'
import Meta from '../../helmet/Meta'
import CollectionListItem from './CollectionListItem'
import {fontFamilySerif} from '../../../utils/styleUtils'

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
    results,
    returnedHits,
    totalHits,
    fetchMoreResults,
    selectCollection,
    collectionDetailFilter,
  } = props
  const queryText = props.collectionDetailFilter.queryText

  let message = 'No collection results'
  if (totalHits > 0) {
    message = `Showing ${returnedHits.toLocaleString()} of ${totalHits.toLocaleString()} collection results`
  }
  const listHeading = (
    <h2 key="Collections::listHeading" style={styleListHeading}>
      {message}
    </h2>
  )

  const showMoreButton =
    returnedHits < totalHits ? (
      <Button
        text="Show More Results"
        onClick={() => fetchMoreResults()}
        style={styleShowMore}
        styleFocus={styleShowMoreFocus}
      />
    ) : null

  const propsForItem = (item, itemId, setFocusedKey) => {
    return {
      onSelect: key => {
        selectCollection(key, collectionDetailFilter)
      },
      setFocusedKey,
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
        items={results}
        ListItemComponent={CollectionListItem}
        GridItemComponent={CollectionGridItem}
        propsForItem={propsForItem}
        heading={listHeading}
        showAsGrid={true}
      />
      {showMoreButton}
    </div>
  )
}

Collections.propTypes = {
  results: PropTypes.object.isRequired,
  totalHits: PropTypes.number.isRequired,
  returnedHits: PropTypes.number.isRequired,
  pageSize: PropTypes.number,
}
