import React, { PropTypes } from 'react'
import _ from 'lodash'
import CollectionTile from './CollectionTileComponent'
import styles from './collectionGrid.css'

class CollectionGrid extends React.Component {
  constructor(props) {
    super(props)

    this.renderShowMoreButton = this.renderShowMoreButton.bind(this)
  }

  renderShowMoreButton() {
    if(this.props.returnedHits < this.props.totalHits) {
      return <div className={styles.buttonContainer}>
          <button className={`pure-button ${styles.button}`} onClick={() => this.props.fetchMoreResults()}>Show More Results</button>
        </div>
    }
  }

  render() {
    const cards = []
    _.forOwn(this.props.results, (val, key) => {
      cards.push(<CollectionTile
            key={key}
            title={val.title}
            thumbnail={val.thumbnail}
            description={val.description}
            geometry={val.spatialBounding}
            onCardClick={() => this.props.onCardClick(key)}
        />)
    })
    return <div>
      <div className={styles.resultCount}>
        Showing {this.props.returnedHits} of {this.props.totalHits} matching results
      </div>
      <div className={styles.gridWrapper}>
        {cards}
      </div>
      {this.renderShowMoreButton()}
    </div>
  }
}

export default CollectionGrid
