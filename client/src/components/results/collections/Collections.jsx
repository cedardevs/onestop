import React from 'react'
import PropTypes from 'prop-types'
import CollectionGridItem from './CollectionGridItem'
import Button from '../../common/input/Button'
import ListView from '../../common/ui/ListView'
import Meta from '../../helmet/Meta'
import CollectionListItem from './CollectionListItem'

const styleCollections = {
  color: '#222',
}

const styleShowMore = {
  margin: '1em auto 1.618em auto',
}
const styleShowMoreFocus = {
  outline: '2px dashed #5C87AC',
  outlineOffset: '.118em',
}

export default class Collections extends React.Component {
  propsForResult = (item, itemId) => {
    const {selectCollection, collectionDetailFilter} = this.props
    return {
      onSelect: key => {
        selectCollection(key, collectionDetailFilter)
      },
    }
  }

  render() {
    const {results, returnedHits, totalHits, fetchMoreResults} = this.props
    const queryText = this.props.collectionDetailFilter.queryText

    const showMoreButton =
      returnedHits < totalHits ? (
        <Button
          text="Show More Results"
          onClick={() => fetchMoreResults()}
          style={styleShowMore}
          styleFocus={styleShowMoreFocus}
        />
      ) : null

    return (
      <div style={styleCollections}>
        <Meta
          title={'Collection Search Results for ' + queryText}
          formatTitle={true}
          robots="noindex"
        />
        <ListView
          items={results}
          resultType="collections"
          searchTerms={queryText}
          shown={returnedHits}
          total={totalHits}
          onItemSelect={this.itemSelect}
          ListItemComponent={CollectionListItem}
          GridItemComponent={CollectionGridItem}
          propsForItem={this.propsForResult}
        />
        {showMoreButton}
      </div>
    )
  }
}

Collections.propTypes = {
  results: PropTypes.object.isRequired,
  totalHits: PropTypes.number.isRequired,
  returnedHits: PropTypes.number.isRequired,
  pageSize: PropTypes.number,
}
