import React, {Component} from 'react'
import ShowMore from 'react-show-more'
import { processUrl } from '../utils/urlUtils'
import MapThumbnailComponent from '../common/MapThumbnailComponent'
import styles from './DetailStyles.css'

export default class DescriptionView extends Component {

  render() {

  const { item } = this.props;

    // thumbnail might be undefined or an empty string, so check for both
    const thumbnail = item.thumbnail && item.thumbnail.length > 0 ? item.thumbnail : undefined;

    // geometry used, if available, to show map
    const geometry = item.spatialBounding;

    // provide default description
    const description = item.description ? item.description : "No description available"

    return (
      <div className={`pure-g`}>
        <div className={`pure-u-1-2`}>
          {this.renderCollectionImage(thumbnail, geometry)}
        </div>
        <div className={`pure-u-1-2 ${styles.descriptionText}`}>
          <ShowMore lines={10} anchorClass={`${styles.showMore}`}>
            {description}
          </ShowMore>
        </div>
      </div>
    )
  }

  renderCollectionImage(thumbnail, geometry) {
      const imgUrl = processUrl(thumbnail)
      if(imgUrl) {
          return <img className={styles.previewImg} src={imgUrl}/>
      }
      else if(geometry) {
          return (
              <div className={styles.previewMap}>
                  <MapThumbnailComponent geometry={geometry} interactive={false}/>
              </div>
          )
      }
      else {
          return (
              <div className={styles.previewMap}>
                  No preview image or map available.
              </div>
          )
      }
  }
}