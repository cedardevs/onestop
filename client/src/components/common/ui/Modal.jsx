import React, {useRef, useState, useEffect} from 'react'
import {consolidateStyles} from '../../../utils/styleUtils'
import ResizeObserver from 'resize-observer-polyfill'
import AnimateHeight from 'react-animate-height'

export const ModalContext = () => {
  return React.createContext({})
}

export function useModal(){
  const modalRef = useRef(null)
  const relativeRef = useRef(null)
  const contentRef = useRef(null)

  const styleContent = (left, top, width) => {
    return {
      position: 'absolute',
      top: `${top}px`,
      left: `${left}px`,
      width: `${width}px`,
      transition: 'opacity 1s',
    }
  }

  const [ visible, setVisible ] = useState(false)
  const [ style, setStyle ] = useState(styleContent(0, 0, 0))
  const [ modalHeight, setModalHeight ] = useState(0)

  const resize = (modalElement, contentRelativeElement, contentElement) => {
    let localX = 0
    let localY = 0
    let width = 0
    if (modalElement && contentRelativeElement && contentElement) {
      console.log('VISIBLE', visible)

      // obtain the *viewport* coordinates of the modal region
      const modalRect = modalElement.getBoundingClientRect()
      console.log('MODALRECT', modalRect)

      // obtain the *viewport* coordinates of the content to be placed over the modal region
      const contentRelativeRect = contentRelativeElement.getBoundingClientRect()
      console.log('CONTENTRELATIVERECT', contentRelativeRect)

      const contentRect = contentElement.getBoundingClientRect()
      console.log('CONTENTRECT', contentRect)

      // now that our modal region position and modal content's position are in the same *viewport* coordinate system,
      // the difference translates the modal region into the coordinates relative to wherever the modal content actually
      // is (to preserve focus order without issue)
      localX = modalRect.left - contentRelativeRect.left
      localY = modalRect.top - contentRelativeRect.top

      // account for border of content (as `.getBoundingClientRect()` values are relative to outer-most boundary)
      const contentCSS = window.getComputedStyle(contentElement)
      const radix = 10

      // x-axis considerations
      const borderLeftWidth = parseInt(contentCSS.borderLeftWidth, radix)
      const borderRightWidth = parseInt(contentCSS.borderRightWidth, radix)
      const marginLeft = parseInt(contentCSS.marginLeft, radix)
      const marginRight = parseInt(contentCSS.marginRight, radix)
      const paddingLeft = parseInt(contentCSS.paddingLeft, radix)
      const paddingRight = parseInt(contentCSS.paddingRight, radix)

      // subtract left and right (margin+borderWidth+padding) to get effective width that won't overflow modal region
      width =
        -(marginLeft + borderLeftWidth + paddingLeft) +
        modalRect.width -
        (paddingRight + borderRightWidth + marginRight)

      // y-axis considerations
      const borderTopWidth = parseInt(contentCSS.borderTopWidth, radix)
      const borderBottomWidth = parseInt(contentCSS.borderBottomWidth, radix)
      const marginTop = parseInt(contentCSS.marginTop, radix)
      const marginBottom = parseInt(contentCSS.marginBottom, radix)
      const paddingTop = parseInt(contentCSS.paddingTop, radix)
      const paddingBottom = parseInt(contentCSS.paddingBottom, radix)

      console.log(
        'Translating position of modal region {',
        modalRect.left,
        ',',
        modalRect.top,
        ' ( width = ',
        modalRect.width,
        ') }',
        'relative to content element {',
        localX,
        ',',
        localY,
        '}'
      )
      setStyle(styleContent(localX, localY, width))
      setModalHeight(contentRect.height)
    }
  }

  const [ ro ] = useState(
    () =>
      new ResizeObserver(([ entry ]) => {
        // calculate new style for modal content when modal region has resized
        resize(modalRef.current, relativeRef.current, contentRef.current)
      })
  )

  // observe resize changes to the modal region
  useEffect(() => {
    if (modalRef.current) ro.observe(modalRef.current)
    return () => ro.disconnect()
  }, [])

  return {
    modalRef,
    relativeRef,
    contentRef,
    style,
    modalHeight,
    visible,
    setVisible,
  }
}

const ANIMATION_DURATION = 200

const styleModal = {
  padding: 0,
  margin: 0,
  border: 0,
}

export const Modal = props => {
  const {modal, open} = props

  console.log('Modal::modal', modal)

  const handleAnimationStart = newHeight => {
    if (modal.setVisible && !open) {
      modal.setVisible(false)
    }
  }

  const handleAnimationEnd = newHeight => {
    if (modal.setVisible && open) {
      modal.setVisible(true)
    }
  }
  console.log('SET HEIGHT:', modal.modalHeight)

  return (
    <AnimateHeight
      duration={ANIMATION_DURATION}
      height={open ? 'auto' : 0}
      style={styleModal}
      onAnimationStart={handleAnimationStart}
      onAnimationEnd={handleAnimationEnd}
    >
      <div
        style={{
          width: '100%',
          height: modal.modalHeight ? modal.modalHeight : '100%',
        }}
        ref={modal.modalRef}
      />
    </AnimateHeight>
  )
}

const styleModalRelative = {
  position: 'relative',
  top: '0',
  zIndex: 3,
}
const ModalContent = props => {
  const {context} = props
  if (context) {
    return (
      <context.Consumer>
        {modal => (
          <div style={styleModalRelative} ref={modal.relativeRef}>
            <div
              style={consolidateStyles(props.style, {
                ...modal.style,
                opacity: modal.visible ? 1 : 0,
              })}
              ref={modal.contentRef}
            >
              {props.children}
            </div>
          </div>
        )}
      </context.Consumer>
    )
  }
}

export default ModalContent
