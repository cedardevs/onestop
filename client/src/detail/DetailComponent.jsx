import React, { PropTypes } from 'react'

import FlipCard from '../../node_modules/react-flipcard/lib/main';


const Detail = (props) => {


  const cardHight = "300px";
  const cardWidth = "300px";

  const styles = {
    base: {
      display: props.id ? 'block' : 'none',
      margin: 20
    },
    reactFlipCard: {
      margin: "25px",
      textAlign: "center",
      width: cardWidth,
      height: cardHight
    },
    reactFlipCard__Front: {
      boxSizing: "border-box",
      width: cardWidth,
      height: cardHight,
      borderRadius: "5px",
      border: "1px solid #ccc",
      padding: "25px",
      backgroundColor: "#eee",
      color: "black"
    },
    reactFlipCard__Back: {
      boxSizing: "border-box",
      width: cardWidth,
      height: cardHight,
      borderRadius: "5px",
      border: "1px solid #ccc",
      padding: "25px",
      backgroundColor: "#cef",
      color: "black"
    }
  };


  return (

        <FlipCard disabled={true} style={styles.reactFlipCard} flipped={props.flipped}>

          <div style={styles.reactFlipCard__Front} onClick={props.onClick}>
            <div>Front</div>
            <div>Title: {props.title} </div>
            <div></div>
          </div>
          <div style={styles.reactFlipCard__Back} onClick={props.onClick}>
            <div>Summary: {props.summary}</div>
          </div>
        </FlipCard>

  )
};

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
