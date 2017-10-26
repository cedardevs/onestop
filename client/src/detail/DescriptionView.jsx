import React, {Component} from 'react'
import ShowMore from 'react-show-more'
import { processUrl } from '../utils/urlUtils'
import MapThumbnailComponent from '../common/MapThumbnailComponent'
import styles from './DetailStyles.css'

export default class DescriptionView extends Component {
  constructor(props) {
    super(props)

  }

  render() {
    return (
      <div className={`pure-g`}>
        <div className={`pure-u-1-2`}>
          {this.renderCollectionImage()}
        </div>
        <div className={`pure-u-1-2 ${styles.descriptionText}`}>
          <ShowMore lines={10} anchorClass={`${styles.showMore}`}>
            {this.props.item.description ? this.props.item.description : 'No description available'}
          </ShowMore>
        </div>
      </div>
    )
  }

  renderCollectionImage() {
    const imgUrl = processUrl(this.props.item.thumbnail)
    return imgUrl ?
      <img className={styles.previewImg} src={imgUrl}/> :
      <div className={styles.previewMap}>
        <MapThumbnailComponent geometry={this.props.geometry} interactive={false}/>
      </div>
  }
}