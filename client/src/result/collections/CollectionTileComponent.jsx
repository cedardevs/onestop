import React, { PropTypes } from 'react'
import styles from './collectionTile.css'

class CollectionTile extends React.Component {
  constructor(props) {
    super(props)

    this.thumbnailUrl = this.props.thumbnail && this.props.thumbnail
            .replace(/^https?:/, '')
            .replace(/'/, '%27')
            .replace(/"/, '%22')
  }

  render() {
    return <div className={styles.tileContainer}>
      <div className={styles.tileContent} onClick={() => this.props.onCardClick()}>
        <h2>{this.props.title}</h2>
        <img src={this.thumbnailUrl}/>
      </div>
    </div>
  }

}

CollectionTile.propTypes = {
  onCardClick: PropTypes.func.isRequired,
  title: PropTypes.string.isRequired,
  thumbnail: PropTypes.string
}

export default CollectionTile
