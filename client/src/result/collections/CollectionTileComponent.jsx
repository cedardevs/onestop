import React, { PropTypes } from 'react'
import styles from './collectionTile.css'

const CollectionTile = (props) => {
  // Thumbnails are dynamically assigned so style's applied via JS
  let thumbnailUrl = props.thumbnail && props.thumbnail
          .replace(/^https?:/, '')
          .replace(/'/, '%27')
          .replace(/"/, '%22')

  const handleClick = () => {
    props.onCardClick(props.recordId)
  }

  return <div className={styles.tileContainer}>
    <div className={styles.tileContent}>
      <h2>{props.title}</h2>
      <img src={thumbnailUrl} onClick={handleClick}/>
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
