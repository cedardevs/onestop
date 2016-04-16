import React, { PropTypes } from 'react'
import Paper from 'material-ui/lib/paper';
import Card from 'material-ui/lib/card/card';
import CardActions from 'material-ui/lib/card/card-actions';
import CardHeader from 'material-ui/lib/card/card-header';
import CardMedia from 'material-ui/lib/card/card-media';
import CardTitle from 'material-ui/lib/card/card-title';
import RaisedButton from 'material-ui/lib/raised-button';
import CardText from 'material-ui/lib/card/card-text';

const Result = ({record}) => {
  const thumbnailLink = record.links.find(link => link.type === 'thumbnail');
  const thumbnailHref = thumbnailLink && thumbnailLink.href;

  const actions = record.links.map(link => (
      <RaisedButton
          label={link.type}
          linkButton={true}
          href={link.href}
          key={link.href}
          primary={true}
      />
  ));

  const titleMaxLength = 150;
  const title = record.title.length < titleMaxLength
      ? record.title
      : record.title.substring(0, titleMaxLength) + '...';

  return (
      <Paper style={{margin: 20}}>
        <Card>
          <CardHeader
              title={title}
              avatar={thumbnailHref || null}
              actAsExpander={true}
              showExpandableButton={true}/>
          <CardText expandable={true}>{record.summary}</CardText>
          <CardActions expandable={true}>{actions}</CardActions>
        </Card>
      </Paper>
  )
};

Result.propTypes = {
  record: PropTypes.shape({
    id: PropTypes.string.isRequired,
    title: PropTypes.string.isRequired,
    summary: PropTypes.string.isRequired,
    links: PropTypes.arrayOf(PropTypes.shape({
      href: PropTypes.string.isRequired,
      type: PropTypes.string.isRequired
    })).isRequired
  })
};

export default Result