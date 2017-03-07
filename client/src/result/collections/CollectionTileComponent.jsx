import React, { PropTypes } from 'react'
import styles from './collectionTile.css'
import {processUrl} from '../../utils/urlUtils'
import MapThumbnailComponent from '../../common/MapThumbnailComponent'

class CollectionTile extends React.Component {
  constructor(props) {
    super(props)

    this.thumbnailUrl = processUrl(this.props.thumbnail)
  }

  render() {
    return <div className={styles.tileContainer}>
      <div className={styles.tileContent} onClick={() => this.props.onCardClick()}>
        <h2 className={styles.title}>{this.props.title}</h2>
        {this.renderThumbnail()}
      </div>
    </div>
  }

  renderThumbnail() {
    return this.thumbnailUrl ?
        <img className={styles.thumbnail} src={this.thumbnailUrl}/> :
        <div className={styles.thumbnail}>
          <MapThumbnailComponent geometry={this.props.geometry}/>
        </div>
  }

}

CollectionTile.propTypes = {
  onCardClick: PropTypes.func.isRequired,
  title: PropTypes.string.isRequired,
  thumbnail: PropTypes.string,
  geometry: PropTypes.object
}

export default CollectionTile
