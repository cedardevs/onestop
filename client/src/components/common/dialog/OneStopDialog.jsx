import React, {useState} from 'react'
import Dialog from 'react-a11y-dialog'
import './OneStopDialog.css'
import Close from './OneStopDialogClose'
import Button from '../input/Button'

const dialogClassNames = {
  base: 'onestop-dialog-base',
  overlay: 'onestop-dialog-overlay',
  element: 'onestop-dialog-element',
  document: 'onestop-dialog-document',
  title: 'onestop-dialog-title',
  closeButton: 'onestop-dialog-closeButton',
}

export function useConfirmation({
  title,
  question,
  yesAction,
  yesText,
  noAction,
  noText,
}){
  const [ dialog, setDialog ] = useState()
  return {
    dialog,
    confirmation: {
      dialog,
      setDialog,
      title,
      question,
      yesAction,
      yesText,
      noAction,
      noText,
    },
  }
}

const styleQuestion = {
  marginBottom: '1.618em',
}

const styleButtons = {
  display: 'flex',
  justifyContent: 'space-evenly',
}

const styleButtonFocus = {
  outline: '2px dashed black',
}

const styleYesButtonHover = {
  background: 'linear-gradient(#277CB2, #28323E)',
}

const styleYesButton = {
  background: '#277CB2',
  fontSize: '1em',
}

const styleNoButtonHover = {
  background: 'linear-gradient(#277CB2, #28323E)',
}

const styleNoButton = {
  background: '#277CB2',
  fontSize: '1em',
}

export const Confirmation = ({confirmation}) => {
  const {
    dialog,
    setDialog,
    title,
    question,
    yesAction,
    yesText,
    noAction,
    noText,
  } = confirmation

  return (
    <Dialog
      id="my-accessible-dialog"
      appRoot="#app"
      dialogRoot="#dialog"
      dialogRef={dialog => setDialog(dialog)}
      title={title}
      classNames={dialogClassNames}
      closeButtonContent={<Close />}
    >
      <div style={styleQuestion}>{question ? question : 'Are you sure?'}</div>

      <div style={styleButtons}>
        <Button
          onClick={() => (noAction ? noAction(dialog) : undefined)}
          style={styleNoButton}
          styleFocus={styleButtonFocus}
          styleHover={styleNoButtonHover}
        >
          {noText ? noText : 'No'}
        </Button>
        <Button
          onClick={() => (yesAction ? yesAction(dialog) : undefined)}
          style={styleYesButton}
          styleFocus={styleButtonFocus}
          styleHover={styleYesButtonHover}
        >
          {yesText ? yesText : 'Yes'}
        </Button>
      </div>
    </Dialog>
  )
}
