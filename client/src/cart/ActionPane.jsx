import React from 'react'
import {boxShadow} from '../common/defaultStyles'
import Button from '../common/input/Button'
import remove from 'fa/remove.svg'
import email from 'fa/envelope.svg'
import {storageAvailable, removeGranuleFromLocalStorage} from '../utils/localStorageUtil'

const styleActionPanel = {
  display: 'flex',
  flexDirection: 'column',
  justifyContent: 'center',
  boxShadow: boxShadow,
  padding: '0.309em',
  borderRadius: '0 0.309em 0.309em 0',
}

const styleButton = color => {
  return {
    // boxShadow: boxShadow,
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

const styleEmailButton = {...styleButton('green'), margin: '0 0 0.309em 0'}
const styleEmailButtonHover = styleButtonHover('green')
const styleEmailButtonFocus = styleButtonFocus('green')

const styleDeleteButton = styleButton('#851A11')
const styleDeleteButtonHover = styleButtonHover('#851A11')
const styleDeleteButtonFocus = styleButtonFocus('#851A11')

export default class ActionPane extends React.Component {
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
        onClick={() => {
          console.log(`click on email button: ${itemId}`)
        }}
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
        icon={remove}
        styleIcon={styleIcon}
        iconPadding={'0.309em'}
        onClick={() => {
          if(storageAvailable('localStorage')){
              deselectGranule(itemId)
          }
        }}
      />
    )

    return (
      <div style={styleActionPanel}>
        {expandableButtons}
        {deleteButton}
      </div>
    )
  }
}
