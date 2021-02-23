import React from 'react'
import {times, SvgIcon} from '../SvgIcon'
import Button from '../input/Button'
import FlexRow from '../ui/FlexRow'
import {
  Modal,
  ModalOverlay,
  ModalContent,
  ModalHeader,
  ModalFooter,
  ModalBody,
  ModalCloseButton,
} from '@chakra-ui/core'

const styleButtonFocus = {
  outline: '2px dashed black',
  outlineOffset: '2px',
}

const styleButton = {
  padding: '0.309em',
  margin: '0.105em',
  borderRadius: '0.309em',
  fontSize: '1em',
}

export default function ModalFormUncontrolled({
  isOpen, // from Chakra useDisclosure
  onClose, // from Chakra useDisclosure
  onSubmit,
  onCancel,
  question,
  submitText,
  cancelText,
  title,
  inputs,
}){
  const submitForm = React.useCallback(
    event => {
      event.preventDefault()
      const form = event.currentTarget
      if (onSubmit && form) {
        onSubmit(new FormData(form))
      }
      onClose()
    },
    [ onSubmit ]
  )

  const formInputs = inputs.map(inp => {
    const label = inp.label ? <label key={`${inp.name}::label`} htmlFor={inp.id}>{inp.label}</label> : null
    const extraProps = inp.extraProps || {}

    return (
      <FlexRow
        rowId={`${inp.name}Row`}
        key={`${inp.name}::row`}
        style={{justifyContent: 'center'}}
        items={[
          label,
          <input
            key={`${inp.name}::input`}
            id={inp.id}
            name={inp.name}
            type={inp.type}
            style={inp.style || {}}
            value={inp.initialValue}
            {...extraProps}
          />,
        ]}
      />
    )
  })

  return (
    <Modal isOpen={isOpen} onClose={onClose} isCentered>
      <ModalOverlay backgroundColor="#5d5d5d94">
        <ModalContent maxWidth="28em" borderRadius="0.309em" padding="0.309em">
          <ModalHeader>
            <h1>{title}</h1>
          </ModalHeader>
          <ModalCloseButton
            backgroundColor="#00000000"
            border="none"
            _focus={styleButtonFocus}
          >
            <SvgIcon size="1em" path={times} />
          </ModalCloseButton>
          <ModalBody>{question}</ModalBody>

          <ModalFooter>
            <form role="form" onSubmit={submitForm} style={{width: '100%'}}>
              {formInputs}
              <FlexRow
                rowId='modalFormActionButtons'
                key='modalFormActionButtons'
                style={{justifyContent: 'center'}}
                items={[
                  <Button
                    role="button"
                    key='modalFormActionButtons::submit'
                    text={submitText || 'Save'}
                    style={styleButton}
                    styleFocus={styleButtonFocus}
                  />,
                  <Button
                    role="button"
                    key='modalFormActionButtons::cancel'
                    text={cancelText || 'Cancel'}
                    style={styleButton}
                    styleFocus={styleButtonFocus}
                    onClick={() => {
                      if (onCancel) {
                        onCancel()
                      }
                      onClose()
                    }}
                  />,
                ]}
              />
            </form>
          </ModalFooter>
        </ModalContent>
      </ModalOverlay>
    </Modal>
  )
}
