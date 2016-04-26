import React, { PropTypes } from 'react'
import Paper from 'material-ui/lib/paper';
import Card from 'material-ui/lib/card/card';
import CardActions from 'material-ui/lib/card/card-actions';
import CardHeader from 'material-ui/lib/card/card-header';
import CardMedia from 'material-ui/lib/card/card-media';
import CardTitle from 'material-ui/lib/card/card-title';
import RaisedButton from 'material-ui/lib/raised-button';
import CardText from 'material-ui/lib/card/card-text';

const Detail = (props) => {
  const styles = {
    base: {
      display: props.id ? 'block' : 'none',
      margin: 20,
    }
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
      <Paper style={styles.base}>
        <Card>
          <CardHeader title={props.title}/>
          <CardText>{props.summary}</CardText>
          <CardActions>{actions}</CardActions>
        </Card>
      </Paper>
  )
};

Detail.propTypes = {
  id: PropTypes.string.isRequired,
  title: PropTypes.string.isRequired,
  summary: PropTypes.string.isRequired,
  links: PropTypes.arrayOf(PropTypes.shape({
    href: PropTypes.string.isRequired,
    type: PropTypes.string.isRequired
  })).isRequired
};

Detail.defaultProps = {
  id: '',
  title: '',
  summary: '',
  links: []
};

Detail.shouldComponentUpdate = (nextProps, nextState) => typeof nextProps.id !== 'undefined';

export default Detail