import React from 'react'
import Button from '../common/input/Button'
import trashIcon from 'fa/trash.svg'
import email from 'fa/envelope.svg'

const styleButton = color => {
  return {
    padding: '0.309em',
    borderRadius: '0.309em',
    background: color,
  }
}

const styleButtonHover = color => {
  return {
    background: `linear-gradient(black, ${color})`,
  }
}

const styleButtonFocus = color => {
  return {
    background: `linear-gradient(black, ${color})`,
    outline: '2px dashed black',
    outlineOffset: '2px',
  }
}

const styleIcon = {
  width: '1.309em',
  height: '1.309em',
}

const styleEmailButton = {
  ...styleButton('green'),
  margin: '0 0 0.309em 0',
  fontSize: '1em',
}
const styleEmailButtonHover = styleButtonHover('green')
const styleEmailButtonFocus = styleButtonFocus('green')

const styleDeleteButton = {...styleButton('#851A11'), fontSize: '1em'}
const styleDeleteButtonHover = styleButtonHover('#851A11')
const styleDeleteButtonFocus = styleButtonFocus('#851A11')

export default class CartListItemActions extends React.Component {
  render() {
    const {expanded, item, itemId, deselectGranule} = this.props

    const emailButton = (
      <Button
        key="emailButton"
        style={styleEmailButton}
        styleHover={styleEmailButtonHover}
        styleFocus={styleEmailButtonFocus}
        title={`Email ${item.title}`}
        icon={email}
        styleIcon={styleIcon}
        iconPadding={'0.309em'}
        onClick={() => {}}
      />
    )

    // TODO: populate expanded buttons array to show additional actions when cart item is expanded
    // e.g. - emailButton
    const expandableButtons = expanded ? [] : []

    const deleteButton = (
      <Button
        key="deleteButton"
        style={styleDeleteButton}
        styleHover={styleDeleteButtonHover}
        styleFocus={styleDeleteButtonFocus}
        title={`Remove ${item.title}`}
        icon={trashIcon}
        styleIcon={styleIcon}
        iconPadding={'0.309em'}
        onClick={() => {
          deselectGranule(itemId)
        }}
      />
    )

    return (
      <div>
        {expandableButtons}
        {deleteButton}
      </div>
    )
  }
}
