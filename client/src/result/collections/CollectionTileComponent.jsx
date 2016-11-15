import React, { PropTypes } from 'react'
import styles from './collectionTile.css'

const CollectionTile = (props) => {
  // Thumbnails are dynamically assigned so style's applied via JS
  let thumbnailUrl = props.thumbnail && props.thumbnail
          .replace(/^https?:/, '')
          .replace(/'/, '%27')
          .replace(/"/, '%22')
  var backgroundImageStyles = {
    background: `url('${thumbnailUrl}')`,
    backgroundColor: 'black',
    backgroundRepeat: 'no-repeat',
    backgroundSize: 'cover',
    backgroundPosition: 'center center'
  }

  const handleClick = () => {
    props.onCardClick(props.recordId)
  }

  return <div className={styles.tileContainer}>
    <div style={backgroundImageStyles}>
      <div className={styles.tileContent} onClick={handleClick}>
        <div className={styles.titleText}>{props.title}</div>
      </div>
    </div>
  </div>
}

CollectionTile.propTypes = {
  id: PropTypes.string.isRequired,
  onCardClick: PropTypes.func.isRequired,
  title: PropTypes.string.isRequired
}

CollectionTile.defaultProps = {
  id: ''
}

CollectionTile.shouldComponentUpdate = (nextProps, nextState) => typeof nextProps.id !== 'undefined'

export default CollectionTile
