import React from 'react'
import PropTypes from 'prop-types'
import styles from './collectionTile.css'
import {processUrl} from '../../utils/urlUtils'
import MapThumbnailComponent from '../../common/MapThumbnailComponent'

class CollectionTile extends React.Component {
  constructor(props) {
    super(props)

    this.thumbnailUrl = processUrl(this.props.thumbnail)
  }

  render() {
    return <div className={styles.tileContainer}
      onKeyPress={(e)=>this.handleKeyPress(e, this.props.onCardClick)}
      tabIndex={0}>
      <div className={styles.tileContent} style={this.thumbnailStyle()}>
        <div className={styles.overlay} onClick={() => this.props.onCardClick()}
          >
          <h2 className={styles.title}>{this.props.title}</h2>
          {this.renderThumbnailMap()}
        </div>
      </div>
    </div>
  }

  handleKeyPress(event, actionHandler) {
    if(event.key == 'Enter'){ actionHandler() }
  }

  renderThumbnailMap() {
    if (!this.thumbnailUrl) {
      return <div className={styles.mapContainer}>
        <MapThumbnailComponent geometry={this.props.geometry}/>
      </div>
    }
  }

  thumbnailStyle() {
    if (this.thumbnailUrl) {
      return {
        background: `url('${this.thumbnailUrl}')`,
        backgroundColor: 'black',
        backgroundRepeat: 'no-repeat',
        backgroundSize: 'cover',
        backgroundPosition: 'center center'
      }
    }
  }

}

CollectionTile.propTypes = {
  onCardClick: PropTypes.func.isRequired,
  title: PropTypes.string.isRequired,
  thumbnail: PropTypes.string,
  geometry: PropTypes.object
}

export default CollectionTile
