import React, { PropTypes } from 'react'
import CollectionTile from './CollectionTileComponent'
import { CardStatus } from '../../detail/DetailActions'
import styles from './collectionGrid.css'

class CollectionGrid extends React.Component {
  constructor(props) {
    super(props)
  }

  render() {
    const cards = []
    this.props.results.forEach((value, key) => {
      cards.push(<CollectionTile
            key={key}
            recordId={key}
            title={value.get('title')}
            thumbnail={value.get('thumbnail')}
            description={value.get('description')}
            flipped={value.get('cardStatus') != CardStatus.SHOW_FRONT}
            onCardClick={() => this.props.onCardClick(key)}
        />)
    })
    return <div>
      <div>
        Showing {this.props.returnedHits} of {this.props.totalHits} matching results
      </div>
      <div className={styles.gridWrapper}>
        {cards}
      </div>
    </div>
  }
}

export default CollectionGrid
