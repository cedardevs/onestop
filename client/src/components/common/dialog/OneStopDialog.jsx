import React, {useState} from 'react'
import {times, SvgIcon} from '../SvgIcon'
import Button from '../input/Button'
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

export const Confirmation = ({
  isOpen, // from Chakra useDisclosure
  onClose, // from Chakra useDisclosure
  title,
  question,
  yesAction,
  yesText,
  noAction,
  noText,
}) => {
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
          <ModalBody>{question ? question : 'Are you sure?'}</ModalBody>

          <ModalFooter>
            <Button
              style={styleButton}
              styleFocus={styleButtonFocus}
              onClick={() => {
                if (noAction) {
                  noAction()
                }
                onClose()
              }}
            >
              {noText ? noText : 'No'}
            </Button>
            <Button
              style={styleButton}
              styleFocus={styleButtonFocus}
              onClick={() => {
                if (yesAction) {
                  yesAction()
                }
                onClose()
              }}
            >
              {yesText ? yesText : 'Yes'}
            </Button>
          </ModalFooter>
        </ModalContent>
      </ModalOverlay>
    </Modal>
  )
}
