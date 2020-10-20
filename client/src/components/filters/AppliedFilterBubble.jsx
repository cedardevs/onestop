import React from 'react'
import _ from 'lodash'
import Button from '../common/input/Button'
import xIcon from 'fa/times.svg'

const style = (backgroundColor, borderColor, allowRemoveFilter) => {
  return {
    display: 'inline-flex',
    borderRadius: '0.1em 0.4em',
    padding: allowRemoveFilter
      ? '.25em .1em .25em .5em'
      : '.25em .5em .25em .5em',
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

export default class AppliedFilterBubble extends React.Component {
  render() {
    const {
      text,
      onUnselect,
      backgroundColor,
      borderColor,
      title,
      allowRemoveFilter,
    } = this.props
    // TODO arguably when allowRemoveFilter is false, we shouldn't use a button at all, but it's challenging to get the styling consistent then...
    return (
      <Button
        disabled={!allowRemoveFilter}
        styleDisabled={style(backgroundColor, borderColor, allowRemoveFilter)}
        style={style(backgroundColor, borderColor, allowRemoveFilter)}
        styleHover={styleHover(backgroundColor)}
        styleFocus={allowRemoveFilter ? styleFocus : {}}
        onClick={allowRemoveFilter ? onUnselect : () => {}}
        title={_.isEmpty(title) ? `Remove ${text} Filter` : title}
        icon={allowRemoveFilter ? xIcon : null}
        iconAfter={allowRemoveFilter}
        text={text}
        styleIcon={styleIcon}
      />
    )
  }
}
