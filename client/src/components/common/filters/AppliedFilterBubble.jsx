import React, {Component} from 'react'

import Button from '../input/Button'
import xIcon from 'fa/times.svg'

const style = (backgroundColor, borderColor) => {
  return {
    display: 'inline-flex',
    borderRadius: '0.1em 0.4em',
    padding: '.25em .1em .25em .5em',
    marginRight: '0.5em',
    marginBottom: '0.25em',
    background: backgroundColor,
    borderColor: borderColor,
    borderStyle: 'solid',
    borderWidth: '1px',
    fontSize: '1.1em',
  }
}

const styleHover = backgroundColor => {
  return {
    filter: 'brightness(120%)',
    background: backgroundColor,
  }
}

const styleFocus = {
  outline: '2px dashed #5C87AC',
  outlineOffset: '.118em',
}

const styleIcon = {
  width: '.8em',
  height: '.8em',
  margin: '0 .25em',
}

export default class AppliedFilterBubble extends Component {
  render() {
    const {text, onUnselect, backgroundColor, borderColor} = this.props

    return (
      <Button
        style={style(backgroundColor, borderColor)}
        styleHover={styleHover(backgroundColor)}
        styleFocus={styleFocus}
        onClick={onUnselect}
        title={`Remove ${text} Filter`}
        icon={xIcon}
        iconAfter={true}
        text={text}
        styleIcon={styleIcon}
      />
    )
  }
}
