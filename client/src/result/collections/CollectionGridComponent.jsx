import React, { PropTypes } from 'react'
import Result from './CollectionTileComponent'
import { CardStatus } from '../../detail/DetailActions'
import styles from './collectionGrid.css'

class CollectionGrid extends React.Component {
  constructor(props) {
    super(props)
    this.results = props.results
    this.onCardClick = props.onCardClick.bind(this)
  }

  render() {
    const cards = []
    this.results.forEach((value, key) => {
      cards.push(<div key={key} className={`${styles.grid}`}>
        <Result
            recordId={key}
            title={value.get('title')}
            thumbnail={value.get('thumbnail')}
            description={value.get('description')}
            flipped={value.get('cardStatus') != CardStatus.SHOW_FRONT}
            onCardClick={this.onCardClick}
        />
      </div>)
    })
    return <div>{cards}</div>
  }
}

export default CollectionGrid
