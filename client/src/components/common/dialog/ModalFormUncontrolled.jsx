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
import {SiteColors} from '../../../style/defaultStyles'

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

const styleRequiredIndicator = {
  color: `${SiteColors.WARNING}`,
}

export function mapInputSpecToReactElements(inputSpec) {
  const label = inputSpec.label ? (
    <label
      key={`${inputSpec.name}::label`}
      htmlFor={inputSpec.id}
    >
      {inputSpec.label}
      {inputSpec.required ? <span style={styleRequiredIndicator}>*</span> : null}
    </label>
  ) : null

  const extraProps = inputSpec.extraProps || {}

  const requiredProps = inputSpec.required
    ? {required: true, 'aria-required': true}
    : {}

  const input = <input
    key={`${inputSpec.name}::input`}
    id={inputSpec.id}
    name={inputSpec.name}
    type={inputSpec.type}
    style={inputSpec.style || {}}
    defaultValue={inputSpec.initialValue}
    {...extraProps}
    {...requiredProps}
  />
  return [label, input]
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
      if (!form.reportValidity()) {
        return
      }
      if (onSubmit && form) {
        onSubmit(new FormData(form))
      }
      onClose()
    },
    [ onSubmit ]
  )

  const formInputs = inputs.map(inp => {
    const [label, input] = mapInputSpecToReactElements(inp)

    return (
      <FlexRow
        rowId={`${inp.name}Row`}
        key={`${inp.name}::row`}
        items={[
          label,
          input,
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
                    onClick={event => {
                      event.preventDefault()
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
