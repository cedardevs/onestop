import React, { PropTypes } from 'react'
import ReactDOM from 'react-dom'
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

  componentDidUpdate() {
    const focusCard = ReactDOM.findDOMNode(this.focusCard)
    if (_.isNull(focusCard)) {
      ReactDOM.findDOMNode(this.resultCount).focus()
    } else {
      focusCard.focus()
    }
  }

  render() {
    const cards = []
    const { returnedHits, totalHits, pageSize } = this.props
    let focusCardNum = returnedHits - pageSize
    _.forOwn(this.props.results, (val, key) => {
      let cTileProps = {
            key: key,
            title: val.title,
            thumbnail:val.thumbnail,
            description:val.description,
            geometry:val.spatialBounding,
            onCardClick:() => this.props.onCardClick(key)
          }
      if (focusCardNum-- == 0  && pageSize !== returnedHits) {
        cTileProps.ref = focusCard=>this.focusCard=focusCard
      }
      cards.push(<CollectionTile {...cTileProps} />)
    })
    return <div>
      <div className={styles.resultCount} tabIndex={0}
        ref={resultCount=>this.resultCount=resultCount}>
        <h1>Search Results (showing {returnedHits} of {totalHits})</h1>
      </div>
      <div className={styles.gridWrapper}>
        {cards}
      </div>
      {this.renderShowMoreButton()}
    </div>
  }
}

export default CollectionGrid
