import React, {useEffect, useLayoutEffect, useRef, useState} from 'react'
import ResizeObserver from 'resize-observer-polyfill'

const ANIMATION_DURATION = 2000

export const ModalContext = () => {
  return React.createContext({})
}

const styleRelative = zIndex => {
  return {
    position: 'relative',
    top: 0,
    zIndex: zIndex ? zIndex : 0,
  }
}

const styleAbsolute = (left, top, width, visible) => {
  return {
    position: 'absolute',
    left: `${left}px`,
    top: `${top}px`,
    width: `${width}px`,
    height: `auto`,
    //transition: `height ${ANIMATION_DURATION}ms, width ${ANIMATION_DURATION}ms`,
    //debug the target area
    //visibility: visible ? 'visible' : 'hidden'

    // Animating width has visual side-effects with overflow and wrapping, so we must
    // prevent these effects while the modal is animating
    //overflow: animating ? 'hidden' : 'initial',
    //whiteSpace: animating ? 'nowrap' : 'initial',
  }
}

export function useModal(open){
  const targetRef = useRef(null)
  const relativeRef = useRef(null)
  const contentRef = useRef(null)

  // tracks the '100%' width (in `px`) of the target container (desired visual location of modal)
  // so that the absolutely positioned modal content knows the `px` width to render and appear as if it were actually there
  const [ width, setWidth ] = useState(0)
  const [ height, setHeight ] = useState(0)
  const [ styleContent, setStyleContent ] = useState(
    styleAbsolute(0, 0, 0, false)
  )

  const relativePosition = (targetRect, relativeRect) => {
    let position = {left: 0, top: 0} // fallback to relative origin
    if (targetRect && relativeRect) {
      // viewport coordinates of the target and proxy elements
      const {left: xTarget, top: yTarget} = targetRect
      const {left: xRelative, top: yRelative} = relativeRect
      // relative coordinates of target element
      position = {left: xTarget - xRelative, top: yTarget - yRelative}
    }
    return position
  }

  const effectiveDimensions = (contentElement, targetRect) => {
    //console.log(`effectiveDimensions::contentELement:${contentElement}, targetRect:${targetRect}`)
    // allow the target room for the content's margin, border width, and padding
    // `.getBoundingClientRect()` values are relative to outer-most boundary
    const contentCSS = window.getComputedStyle(contentElement)
    const radix = 10

    // x-axis considerations
    const borderLeftWidth = parseInt(contentCSS.borderLeftWidth, radix)
    const borderRightWidth = parseInt(contentCSS.borderRightWidth, radix)
    const marginLeft = parseInt(contentCSS.marginLeft, radix)
    const marginRight = parseInt(contentCSS.marginRight, radix)
    const paddingLeft = parseInt(contentCSS.paddingLeft, radix)
    const paddingRight = parseInt(contentCSS.paddingRight, radix)

    // y-axis considerations
    const borderTopWidth = parseInt(contentCSS.borderTopWidth, radix)
    const borderBottomWidth = parseInt(contentCSS.borderBottomWidth, radix)
    const marginTop = parseInt(contentCSS.marginTop, radix)
    const marginBottom = parseInt(contentCSS.marginBottom, radix)
    const paddingTop = parseInt(contentCSS.paddingTop, radix)
    const paddingBottom = parseInt(contentCSS.paddingBottom, radix)

    // subtract space to get effective dimensions of content that won't overflow target region
    const leftSpace = marginLeft + borderLeftWidth + paddingLeft
    const rightSpace = paddingRight + borderRightWidth + marginRight
    const topSpace = marginTop + borderTopWidth + paddingTop
    const bottomSpace = marginBottom + borderBottomWidth + paddingBottom

    return {
      width: targetRect.width - (leftSpace + rightSpace),
      height: targetRect.height - (topSpace + bottomSpace),
    }
  }

  const resize = (targetElement, relativeElement, contentElement, origin) => {
    if (targetElement && relativeElement && contentElement) {
      // obtain the *viewport* rect of the target, relative, and content refs
      const targetRect = targetElement.getBoundingClientRect()
      const relativeRect = relativeElement.getBoundingClientRect()
      const contentRect = contentElement.getBoundingClientRect()

      // get the relative coordinates of the target
      const {left, top} = relativePosition(targetRect, relativeRect)

      // get the effective width of the target
      // (accounting for content margin/border/padding)
      const {width} = effectiveDimensions(contentElement, targetRect)

      // whenever we resize (we should update the width and height)
      setWidth(width)
      console.log(`resize::height = ${contentRect.height}`)
      setHeight(contentRect.height)

      console.log('ORIGIN = ', origin)
      console.log(`left = ${left}, top = ${top}`)

      return styleAbsolute(left, top, width, false)
    }
  }

  const [ ro ] = useState(
    () =>
      new ResizeObserver(([ entry ]) => {
        // calculate new style for modal content when modal region has resized
        const resizeStyle = resize(
          targetRef.current,
          relativeRef.current,
          contentRef.current
        )
        setStyleContent(resizeStyle)
      })
  )

  // observe resize changes to the modal region
  useEffect(() => {
    if (targetRef.current) ro.observe(targetRef.current)
    return () => ro.disconnect()
  }, [])

  return {
    targetRef,
    relativeRef,
    contentRef,
    width,
    height,
    resize,
    styleContent,
    setStyleContent,
    open,
  }
}

const ModalComponent = props => {
  const {modal} = props
  const {
    targetRef,
    relativeRef,
    contentRef,
    resize,
    height,
    setStyleContent,
  } = modal

  // get the 100% width and corresponding height of the modal region intially (before render)
  // and update the style for the content
  useLayoutEffect(() => {
    const resizeStyle = resize(
      targetRef.current,
      relativeRef.current,
      contentRef.current,
      'target'
    )
    setStyleContent(resizeStyle)
  }, [])

  const styleTarget = {
    width: '100%',
    height: height ? height : 0,
    background: 'yellow',
    transition: `height ${ANIMATION_DURATION}ms`,
  }

  console.log('styleTarget:', styleTarget)
  return <div style={styleTarget} ref={modal.targetRef} />
}

const Modal = props => {
  const {context} = props
  if (context) {
    return (
      <context.Consumer>
        {modal => <ModalComponent modal={modal} />}
      </context.Consumer>
    )
  }
}
export default Modal

const ModalContentComponent = props => {
  const {modal, zIndex} = props
  const {
    targetRef,
    relativeRef,
    contentRef,
    resize,
    styleContent,
    setStyleContent,
  } = modal

  useLayoutEffect(() => {
    const resizeStyle = resize(
      targetRef.current,
      relativeRef.current,
      contentRef.current,
      'content'
    )
    setStyleContent(resizeStyle)
  }, [])

  console.log('styleContent', styleContent)
  return (
    <div style={styleRelative(zIndex)} ref={modal.relativeRef}>
      <div style={styleContent} ref={modal.contentRef}>
        {props.children}
      </div>
    </div>
  )
}

export const ModalContent = props => {
  const {context, zIndex, children} = props
  if (context) {
    return (
      <context.Consumer>
        {modal => (
          <ModalContentComponent
            modal={modal}
            zIndex={zIndex}
            children={children}
          />
        )}
      </context.Consumer>
    )
  }
}
