import React, {Component} from 'react'
import PropTypes from 'prop-types'
import CollectionCard from './CollectionCard'
import Button from '../../common/input/Button'
import ListView from '../ListView'

const styleCollections = {
  color: '#222',
}

const styleShowMore = {
  margin: '1em auto 1.618em auto',
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
        />
      ) : null

    return (
      <div style={styleCollections}>
        <ListView
          items={results}
          loading={!!loading}
          shown={returnedHits}
          total={totalHits}
          enableGridToggle={true}
          onItemSelect={selectCollection}
          ListComponent={null}
          GridComponent={CollectionCard}
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
