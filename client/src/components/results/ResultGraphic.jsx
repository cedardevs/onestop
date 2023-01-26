import React from 'react'

import {isFTP, processUrl} from '../../utils/urlUtils'
import MapThumbnail from '../common/MapThumbnail'

const styleImageContainer = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  height: '100%',
}

const styleImage = height => {
  const styleImageDefault = {
    width: '100%',
    objectFit: 'contain',
  }
  return {
    ...styleImageDefault,
    // if height is unspecified, height of image will be contigent on flex content to left and right of graphic
    ...(height ? {height: height} : {}),
  }
}

const styleMap = height => {
  const styleMapDefault = {
    zIndex: 4,
    width: '100%',
    paddingTop: '0.25em',
  }
  return {
    ...styleMapDefault,
    // if height is unspecified, height of map will be contigent on flex content to left and right of graphic
    ...(height ? {height: height} : {height: '100%'}),
  }
}

export default function ResultGraphic(props){
  const {thumbnail, geometry, height} = props

  const imgUrl = processUrl(thumbnail)
  const isFTPUrl = isFTP(imgUrl)

  if (imgUrl && !isFTPUrl && !imgUrl.includes('maps.googleapis.com')) {
    return (
      <div style={styleImageContainer}>
        <img
          style={styleImage(height)}
          src={imgUrl}
          alt=""
          width="100px"
          height="100px"
        />
      </div>
    )
  }
  else {
    // return map image of spatial bounding or, if none, world map
    return (
      <div style={styleMap(height)}>
        <MapThumbnail geometry={geometry} interactive={true} />
      </div>
    )
  }
}
