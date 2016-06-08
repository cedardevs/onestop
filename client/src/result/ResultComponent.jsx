import React, { PropTypes } from 'react'
import ListItem from '../../node_modules/material-ui/lib/lists/list-item';
import Avatar from '../../node_modules/material-ui/lib/avatar';

const Result = ({record, onClick}) => {
  const thumbnailLink = record.links.find(link => link.type === 'thumbnail');
  const thumbnailHref = thumbnailLink && thumbnailLink.href;

  const styles = {
    title: {
      whiteSpace: 'nowrap',
      overflow: 'hidden',
      textOverflow: 'ellipsis',
      color: 'white'
    }
  }

  const handleSelection = () => onClick(record.id);

  return <ListItem
      primaryText={<div style={styles.title}>{record.title}</div>}
      leftAvatar={<Avatar src={thumbnailHref || null}/>}
      onTouchTap={handleSelection}
  />
}

Result.propTypes = {
  record: PropTypes.shape({
    id: PropTypes.string.isRequired,
    title: PropTypes.string.isRequired,
    links: PropTypes.arrayOf(PropTypes.shape({
      href: PropTypes.string.isRequired,
      type: PropTypes.string.isRequired
    })).isRequired
  })
}

export default Result
