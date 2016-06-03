import React, { PropTypes } from 'react'
import Paper from '../../node_modules/material-ui/lib/paper';
import Card from '../../node_modules/material-ui/lib/card/card';
import CardActions from '../../node_modules/material-ui/lib/card/card-actions';
import CardHeader from '../../node_modules/material-ui/lib/card/card-header';
import CardMedia from '../../node_modules/material-ui/lib/card/card-media';
import CardTitle from '../../node_modules/material-ui/lib/card/card-title';
import RaisedButton from '../../node_modules/material-ui/lib/raised-button';
import CardText from '../../node_modules/material-ui/lib/card/card-text';

import FlipCard from '../../node_modules/react-flipcard/lib/main';




const Detail = (props, onClick) => {


  const cardHight = "300px";
  const cardWidth = "300px";

  const styles = {
    base: {
      display: props.id ? 'block' : 'none',
      margin: 20
    },
    reactFlipCard: {
      margin: "25px",
      textAlign: "center"
    },
    reactFlipCard__Front: {
      boxSizing: "border-box",
      width: cardWidth,
      height: cardHight,
      borderRadius: "5px",
      border: "1px solid #ccc",
      padding: "25px",
      backgroundColor: "#eee"
    },
    reactFlipCard__Back: {
      boxSizing: "border-box",
      width: cardWidth,
      height: cardHight,
      borderRadius: "5px",
      border: "1px solid #ccc",
      padding: "25px",
      backgroundColor: "#cef"
    }

  //
  //ReactFlipCard,
  //ReactFlipCard__Front,
  //ReactFlipCard__Back {
  //  box-sizing: border-box;
  //  width: 250px;
  //  height: 300px;
  //}
  //ReactFlipCard__Front,
  //ReactFlipCard__Back {
  //  border-radius: 10px;
  //  border: 1px solid #ccc;
  //  padding: 25px;
  //}
  //ReactFlipCard__Front {
  //  background-color: #eee;
  //}
  //ReactFlipCard__Back {
  //  background-color: #cef;
  //}
  //
  //  #example {
  //  text-align: center;
  //}
  };

  const thumbnailLink = props.links.find(link => link.type === 'thumbnail');
  const thumbnailHref = thumbnailLink && thumbnailLink.href || null;

  const actions = props.links
      .filter(link => link.type !== 'thumbnail')
      .map(link => (
          <RaisedButton
              label={link.type}
              linkButton={true}
              href={link.href}
              key={link.href}
              primary={true}
          />
      ));

  return (
      //<Paper style={styles.base}>
      //  <Card>
      //    <CardHeader title={props.title}/>
      //    <CardText>{props.summary}</CardText>
      //    <CardActions>{actions}</CardActions>
      //  </Card>
      //</Paper>


        <FlipCard disabled={true} style={styles.reactFlipCard} flipped={props.flipped}>

          <div style={styles.reactFlipCard__Front} onClick={onClick}>
            <div>Front</div>
            <div>Title: {props.title} </div>
            <div></div>
          </div>
          <div style={styles.reactFlipCard__Back} onClick={onClick}>
            <div>Summary: {props.summary}</div>
          </div>
        </FlipCard>

  )
};

Detail.propTypes = {
  id: PropTypes.string.isRequired,
  title: PropTypes.string.isRequired,
  summary: PropTypes.string.isRequired,
  links: PropTypes.arrayOf(PropTypes.shape({
    href: PropTypes.string.isRequired,
    type: PropTypes.string.isRequired
  })).isRequired,
  flipped: PropTypes.bool.isRequired,
  onClick: PropTypes.func.isRequired
};

Detail.defaultProps = {
  id: '',
  title: '',
  summary: '',
  links: [],
  flipped: false
};

Detail.shouldComponentUpdate = (nextProps, nextState) => typeof nextProps.id !== 'undefined';

export default Detail