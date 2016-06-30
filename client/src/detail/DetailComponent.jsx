import React, { PropTypes } from 'react'
import FlipCard from 'react-flipcard'
import styles from './detail.css'


const Detail = (props) => {
  // Thumbnails are dynamically assigned so style's applied via JS
  var localStyles = {
    background: 'url(' + props.thumbnail + ')',
    backgroundRepeat: 'no-repeat',
    backgroundSize: 'cover',
    backgroundPosition: 'center center'
  }

  return (
        <FlipCard disabled={true} className={styles.reactFlipCard} flipped={props.flipped}>
          <div style={localStyles}>
            <div className={styles.reactFlipCard__Front} onClick={() => props.onCardClick(props.recordId)}>
              <div>Title: {props.title} </div>
            </div>
          </div>
          <div className={styles.reactFlipCard__Back} onClick={() => props.onCardClick(props.recordId)}>
            <div>Description: {props.description}</div>
          </div>
        </FlipCard>
  )
}

Detail.propTypes = {
  id: PropTypes.string.isRequired,
  flipped: PropTypes.bool.isRequired,
  onCardClick: PropTypes.func.isRequired,
  title: PropTypes.string.isRequired
}

Detail.defaultProps = {
  id: '',
  details: {
    title: '',
    summary: '',
    links: []
  },
  flipped: false
}

Detail.shouldComponentUpdate = (nextProps, nextState) => typeof nextProps.id !== 'undefined'

export default Detail
