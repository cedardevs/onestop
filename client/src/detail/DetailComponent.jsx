import React, { PropTypes } from 'react'
import FlipCard from 'react-flipcard'
import styles from './detail.css'


const Detail = (props) => {
  return (
        <FlipCard disabled={false} className={styles.reactFlipCard} flipped={props.flipped}>

          <div className={styles.reactFlipCard__Front} onClick={props.onClick}>
            <div>Title: {props.title} </div>
            <div></div>
          </div>
          <div className={styles.reactFlipCard__Back} onClick={props.onClick}>
            <div>Description: {props.description}</div>
          </div>
        </FlipCard>
  )
}

Detail.propTypes = {
  id: PropTypes.string.isRequired,
  flipped: PropTypes.bool.isRequired,
  onClick: PropTypes.func.isRequired,
  details: PropTypes.shape({
    title: PropTypes.string.isRequired,
    summary: PropTypes.string.isRequired,
    links: PropTypes.arrayOf(PropTypes.shape({
      href: PropTypes.string.isRequired,
      type: PropTypes.string.isRequired
    })).isRequired
  })
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
