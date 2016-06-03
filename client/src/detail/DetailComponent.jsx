import React, { PropTypes } from 'react'
import FlipCard from '../../node_modules/react-flipcard/lib/main'
import RaisedButton from '../../node_modules/material-ui/lib/raised-button'
import CSSModules from 'react-css-modules'
import { buttons } from 'purecss'

const Detail = (props, onCardClick) => {
  const cardHight = '300px'
  const cardWidth = '300px'

  const styles = {
    base: {
      display: props.id ? 'block' : 'none',
      margin: 20
    },
    reactFlipCard: {
      margin: '25px',
      textAlign: 'center'
    },
    reactFlipCard__Front: {
      boxSizing: 'border-box',
      width: cardWidth,
      height: cardHight,
      borderRadius: '5px',
      border: '1px solid #ccc',
      padding: '25px',
      backgroundColor: '#eee'
    },
    reactFlipCard__Back: {
      boxSizing: 'border-box',
      width: cardWidth,
      height: cardHight,
      borderRadius: '5px',
      border: '1px solid #ccc',
      padding: '25px',
      backgroundColor: '#cef'
    }
  }

  const thumbnailLink = props.links.find(link => link.type === 'thumbnail')
  const thumbnailHref = thumbnailLink && thumbnailLink.href || null

  const actions = props.links
      .filter(link => link.type !== 'thumbnail')
      .map(link => (
          <div
            styleName='pure-button'
              label={link.type}
              linkButton={true}
              href={link.href}
              key={link.href}
              primary={true}
          />
      ))

  return (
      //The `disabled` attribute allows turning off the auto-flip
      //on hover, or focus. This allows manual control over flipping.

      //The `flipped` attribute indicates whether to show the front,
      //or the back, with `true` meaning show the back.
      <div>
        <FlipCard
            disabled={true}
            style={styles.reactFlipCard}
            flipped={props.flipped}
            >


          <div style={styles.reactFlipCard__Front}  onClick={onCardClick}>
            <div>Front</div>
            <div>Title: {props.title} </div>
            <div></div>
          </div>
          <div style={styles.reactFlipCard__Back}>
            <div>Summary: {props.summary}</div>
          </div>
        </FlipCard>
      </div>
  )
}

Detail.propTypes = {
  id: PropTypes.string.isRequired,
  title: PropTypes.string.isRequired,
  summary: PropTypes.string.isRequired,
  links: PropTypes.arrayOf(PropTypes.shape({
    href: PropTypes.string.isRequired,
    type: PropTypes.string.isRequired
  })).isRequired,
  flipped: PropTypes.bool.isRequired,
  onCardClick: PropTypes.func.isRequired
}

Detail.defaultProps = {
  id: '',
  title: '',
  summary: '',
  links: [],
  flipped: false
}

Detail.shouldComponentUpdate = (nextProps, nextState) => typeof nextProps.id !== 'undefined'

export default Detail
