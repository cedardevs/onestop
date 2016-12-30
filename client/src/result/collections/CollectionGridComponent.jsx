import React, { PropTypes } from 'react'
import _ from 'lodash'
import CollectionTile from './CollectionTileComponent'
import { CardStatus } from '../../detail/DetailActions'
import styles from './collectionGrid.css'

class CollectionGrid extends React.Component {
  constructor(props) {
    super(props)
  }

  render() {
    const cards = []
    _.forOwn(this.props.results, (val, key) => {
      cards.push(<CollectionTile
            key={key}
            recordId={key}
            title={val.title}
            thumbnail={val.thumbnail}
            description={val.description}
            flipped={val.cardStatus != CardStatus.SHOW_FRONT}
            onCardClick={() => this.props.onCardClick(key)}
        />)
    })
    return <div>
      <div>
        Search returned {this.props.count} {(this.props.count !== 1) ? "results" : "result"}
      </div>
      <div className={styles.gridWrapper}>
        {cards}
      </div>
    </div>
  }
}

export default CollectionGrid
