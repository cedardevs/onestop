import React, { PropTypes } from 'react'
import Result from './CollectionTileComponent'
import { CardStatus } from '../../detail/DetailActions'
import styles from './collectionGrid.css'

class CollectionGrid extends React.Component {
  constructor(props) {
    super(props)
  }

  render() {
    const cards = []
    this.props.results.forEach((value, key) => {
      cards.push(<div key={key} className={`${styles.grid}`}>
        <Result
            recordId={key}
            title={value.get('title')}
            thumbnail={value.get('thumbnail')}
            description={value.get('description')}
            flipped={value.get('cardStatus') != CardStatus.SHOW_FRONT}
            onCardClick={() => this.props.onCardClick(key)}
        />
      </div>)
    })
    return <div>
      <div>
        Search returned {this.props.count} {(this.props.count !== 1) ? "results" : "result"}
      </div>
      <div>
        {cards}
      </div>
    </div>
  }
}

export default CollectionGrid
