import React, { PropTypes } from 'react'
import FlipCard from 'react-flipcard'
import styles from './result.css'

const Result = (props) => {
  // Thumbnails are dynamically assigned so style's applied via JS
  var localStyles = {
    background: 'url(' + props.thumbnail.replace(/^https?:/, '') + ')',
    backgroundColor: 'black',
    backgroundRepeat: 'no-repeat',
    backgroundSize: 'cover',
    backgroundPosition: 'center center'
  }

  const handleClick = () => {
    props.onCardClick(props.recordId)
  }

  return (
    <FlipCard disabled={true} className={styles.reactFlipCard} flipped={false}>
      <div style={localStyles}>
        <div className={styles.reactFlipCardFront} onClick={handleClick}>
          <div className={styles.titleText}>{props.title}</div>
        </div>
      </div>
      <div className={styles.reactFlipCardBack} onClick={handleClick}>
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
  flipped: false
}

Result.shouldComponentUpdate = (nextProps, nextState) => typeof nextProps.id !== 'undefined'

export default Result
