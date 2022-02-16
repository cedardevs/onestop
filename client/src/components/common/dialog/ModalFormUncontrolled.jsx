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
} from '@chakra-ui/react'

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
    const label = inp.label ? (
      <label
        key={`${inp.name}::label`}
        htmlFor={inp.id}
        style={{marginRight: '0.5em'}}
      >
        {inp.label}
      </label>
    ) : null
    const extraProps = inp.extraProps || {}

    return (
      <FlexRow
        rowId={`${inp.name}Row`}
        key={`${inp.name}::row`}
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
          <form role="form" onSubmit={submitForm}>
            <ModalBody>{formInputs}</ModalBody>

            <ModalFooter>
              <FlexRow
                rowId="modalFormActionButtons"
                key="modalFormActionButtons"
                items={[
                  <Button
                    type="submit"
                    role="button"
                    key="modalFormActionButtons::submit"
                    text={submitText || 'Save'}
                    style={styleButton}
                    styleFocus={styleButtonFocus}
                  />,
                  <Button
                    type="button"
                    role="button"
                    key="modalFormActionButtons::cancel"
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
            </ModalFooter>
          </form>
        </ModalContent>
      </ModalOverlay>
    </Modal>
  )
}
