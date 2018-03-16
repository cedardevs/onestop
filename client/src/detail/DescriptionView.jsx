import React, {Component} from 'react'
import {processUrl} from '../utils/urlUtils'
import MapThumbnail from '../common/MapThumbnail'

const styleContainer = {
  padding: '1.618em',
  color: "#222",
  backgroundColor: "#F9F9F9"
}

const styleImageContainer = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
}

const styleImage = {
  margin: '0 0 0.618em 0',
  width: '72%',
}

const styleMap = {
  margin: '0 0 0.618em 0',
  width: '100%',
}

const styleDescription = {
  margin: '0 0 0.618em 0',
}

export default class DescriptionView extends Component {
  render() {
    const {item} = this.props

    // thumbnail might be undefined or an empty string, so check for both
    const thumbnail =
      item.thumbnail && item.thumbnail.length > 0 ? item.thumbnail : undefined

    // geometry used, if available, to show map
    const geometry = item.spatialBounding

    // provide default description
    const description = item.description
      ? item.description
      : 'No description available'

    const collectionImage = this.renderCollectionImage(thumbnail, geometry)

    return (
      <div style={styleContainer}>
        {collectionImage}
        <div style={styleDescription}>{description}</div>
      </div>
    )
  }

  renderCollectionImage(thumbnail, geometry) {
    const imgUrl = processUrl(thumbnail)
    if (imgUrl) {
      return (
        <div style={styleImageContainer}>
          <img
            style={styleImage}
            src={imgUrl}
            alt="collection result image"
            aria-hidden="true"
          />
        </div>
      )
    }
    else if (geometry) {
      return (
        <div style={styleMap}>
          <MapThumbnail geometry={geometry} interactive={false} />
        </div>
      )
    }
    else {
      return <div style={styleMap}>No preview image or map available.</div>
    }
  }
}
