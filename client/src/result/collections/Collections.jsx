import React, {Component} from 'react'
import PropTypes from 'prop-types'
import CollectionCard from './CollectionCard'
import CollectionListResult from './CollectionListResult'
import Button from '../../common/input/Button'
import ListView from '../ListView'
import Meta from '../../helmet/Meta'

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

export default class Collections extends Component {
  render() {
    const {
      loading,
      results,
      returnedHits,
      totalHits,
      selectCollection,
      fetchMoreResults,
    } = this.props

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
        <Meta title="Collection Search Results" robots="noindex" />
        <ListView
          items={results}
          loading={!!loading}
          shown={returnedHits}
          total={totalHits}
          onItemSelect={selectCollection}
          ListItemComponent={null}
          GridItemComponent={CollectionCard}
          propsForItem={item => {
            return null
          }}
        />
        {showMoreButton}
      </div>
    )
  }
}

Collections.propTypes = {
  loading: PropTypes.number.isRequired,
  results: PropTypes.object.isRequired,
  totalHits: PropTypes.number.isRequired,
  returnedHits: PropTypes.number.isRequired,
  pageSize: PropTypes.number,
}
