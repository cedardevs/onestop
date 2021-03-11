import React from 'react'
import {mount} from 'enzyme'
import {useDisclosure, ModalHeader} from '@chakra-ui/core'

import ModalFormUncontrolled, {mapInputSpecToReactElements} from '../../../../src/components/common/dialog/ModalFormUncontrolled'

describe('ModalFormUncontrolled', () => {
  let component = null
  let wrapper = null
  const submitText = 'Submit'
  const cancelText = 'Cancel'
  const title = 'This Is A Title'
  const testInputs = [
    {
      label: 'Text Input1:',
      id: 'textInput1',
      name: 'textInput1',
      type: 'text',
      initialValue: 'textValue1',
      extraProps: {readOnly: true}, // Added 'readOnly' prop to appease React errors
    },
    {
      label: 'Radio Input1:',
      id: 'radioInput1',
      name: 'radioInput1',
      type: 'radio',
      initialValue: 'radioValue1',
      extraProps: {checked: true, readOnly: true},
    },
    {
      label: 'Check Input1:',
      id: 'checkInput1',
      name: 'checkInput1',
      type: 'checkbox',
      initialValue: 'checkValue1',
      extraProps: {checked: true, readOnly: true},
    },
    {
      label: 'Required Input1:',
      id: 'requiredInput1',
      name: 'requiredInput1',
      type: 'email',
      initialValue: 'foo@bar.com',
      required: true,
      extraProps: {readOnly: true}
    }
  ]

  const onSubmit = formData => {
    // This onSubmit function gets called when you run .simulate('submit') on the form element
    expect(Array.from(formData.entries())).toEqual(
      expect.arrayContaining([
        [ 'textInput1', 'textValue1' ],
        [ 'radioInput1', 'radioValue1' ],
        [ 'checkInput1', 'checkValue1' ],
      ])
    )
  }
  const onCancel = jest.fn(() => {})

  beforeAll(() => {
    // Create a wrapper component so that we can call the useDisclosure() hook
    const FormWrapper = () => {
      const {isOpen, onOpen, onClose} = useDisclosure()

      // Have a single useEffect to call onOpen() after this wrapper gets mounted
      React.useEffect(() => {
        onOpen()
      }, [])

      const props = {
        isOpen,
        onClose,
        onSubmit,
        onCancel,
        submitText,
        cancelText,
        title,
        inputs: testInputs,
      }
      return <ModalFormUncontrolled {...props} />
    }

    wrapper = mount(<FormWrapper />)
    wrapper.update()
    component = wrapper.find(ModalFormUncontrolled).at(0)
  })

  it('should output the provided string props as text', () => {
    const h1 = component.find('h1')
    expect(h1.length).toBe(1)
    expect(h1.text()).toBe(title)

    const body = component.find(ModalHeader)
    expect(body.length).toBe(1)
    expect(body.text()).toBe(title)

    const form = component.find('form')
    expect(form.length).toBe(1)

    const buttons = form.find('button')
    expect(buttons.length).toBe(2)
    const submitButton = buttons.at(0)
    const cancelButton = buttons.at(1)
    expect(submitButton.text()).toBe(submitText)
    expect(cancelButton.text()).toBe(cancelText)
  })

  it('should serialize its inputs as FormData onSubmit', () => {
    const form = component.find('form')
    form.simulate('submit')
  })

  it('should call the provided onCancel function when not submitted', () => {
    const form = component.find('form')
    const cancelButton = form.find('button').at(1)
    cancelButton.simulate('click')
    expect(onCancel).toBeCalledTimes(1)
  })

  it('should output the provided inputs correctly', () => {
    const form = component.find('form').at(0)
    const formInputs = form.find('input')
    expect(formInputs.length).toBe(testInputs.length)
    for (const input of testInputs) {
      const [reactLabel, reactInput] = mapInputSpecToReactElements(input)
      expect(form.containsMatchingElement(reactLabel)).toBeTruthy()
      expect(form.containsMatchingElement(reactInput)).toBeTruthy()
    }
  })
})
