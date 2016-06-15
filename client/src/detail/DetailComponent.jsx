import React, { PropTypes } from 'react'
import FlipCard from '../../node_modules/react-flipcard/lib/main'
import styles from './detail.css'


const Detail = (props) => {

  return (
        <FlipCard disabled={true} className={styles.reactFlipCard} flipped={props.flipped}>

          <div className={styles.reactFlipCard__Front} onClick={props.onClick}>
            <div>Front</div>
            <div>Title: {props.title} </div>
            <div></div>
          </div>
          <div className={styles.reactFlipCard__Back} onClick={props.onClick}>
            <div>Summary: {props.summary}</div>
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
};

Detail.defaultProps = {
  id: '',
  details: {
    title: '',
    summary: '',
    links: []
  },
  flipped: false
};

Detail.shouldComponentUpdate = (nextProps, nextState) => typeof nextProps.id !== 'undefined';

export default Detail
