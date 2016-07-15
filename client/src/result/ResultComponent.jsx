import React, { PropTypes } from 'react'
import FlipCard from 'react-flipcard'
import styles from './result.css'

const Result = (props) => {
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
        <div className={styles.reactFlipCardFront} onClick={() => props.onCardClick(props.recordId)}>
          <div className={styles.titleText}>{props.title}</div>
        </div>
      </div>
      <div className={styles.reactFlipCardBack} onClick={() => props.onCardClick(props.recordId)}>
        <div>{props.description}</div>
      </div>
    </FlipCard>
  )
}

Result.propTypes = {
  id: PropTypes.string.isRequired,
  flipped: PropTypes.bool.isRequired,
  onCardClick: PropTypes.func.isRequired,
  title: PropTypes.string.isRequired
}

Result.defaultProps = {
  id: '',
  details: {
    title: '',
    summary: '',
    links: []
  },
  flipped: false
}

Result.shouldComponentUpdate = (nextProps, nextState) => typeof nextProps.id !== 'undefined'

export default Result
