import React, {useEffect, useRef, useState} from 'react'
import ResizeObserver from 'resize-observer-polyfill' // handle browsers not supporting ResizeObserver
import '../../../utils/polyfillDOMRect'

const ANIMATION_DURATION = 200

export const ProxyContext = () => {
  return React.createContext({})
}

const styleFixed = (left, top, width) => {
  return {
    position: 'fixed',
    left: `${left}px`,
    top: `${top}px`,
    width: `${width}px`,
    height: `auto`,
    transition: `opacity ${ANIMATION_DURATION}ms, width ${ANIMATION_DURATION}ms, top ${ANIMATION_DURATION}ms`,
  }
}

export function useProxy(open){
  const targetRef = useRef(null)
  const contentRef = useRef(null)

  // tracks the fully open height of the content, given it spans the same '100%' width of the target
  const [ contentHeight, setContentHeight ] = useState(null)

  const [ ticking, setTicking ] = useState(false)

  const [ styleContent, setStyleContent ] = useState(() => {
    styleFixed(0, 0, 0)
  })

  const calculateTargetRect = () => {
    const targetElement = targetRef.current
    let targetRect = new DOMRect()
    if (targetElement) {
      targetRect = targetElement.getBoundingClientRect()
    }
    return targetRect
  }

  const calculateContentHeight = () => {
    const contentElement = contentRef.current
    // allow the target room for the content's margin, border width, and padding
    // `.getBoundingClientRect()` values are relative to outer-most boundary
    let space = 0
    let contentRect = new DOMRect()
    if (contentElement) {
      const contentCSS = window.getComputedStyle(contentElement)
      const radix = 10

      // y-axis considerations
      const borderTopWidth = parseInt(contentCSS.borderTopWidth, radix)
      const borderBottomWidth = parseInt(contentCSS.borderBottomWidth, radix)
      const marginTop = parseInt(contentCSS.marginTop, radix)
      const marginBottom = parseInt(contentCSS.marginBottom, radix)
      const paddingTop = parseInt(contentCSS.paddingTop, radix)
      const paddingBottom = parseInt(contentCSS.paddingBottom, radix)

      // subtract space to get effective dimensions of content that won't overflow target region
      const topSpace = marginTop + borderTopWidth + paddingTop
      const bottomSpace = marginBottom + borderBottomWidth + paddingBottom
      space = topSpace + bottomSpace
      contentRect = contentElement.getBoundingClientRect()
    }
    return contentRect.height - space
  }

  const updateStyleContent = () => {
    if (contentRef.current && targetRef.current) {
      const targetRect = calculateTargetRect()
      const contentHeight = calculateContentHeight()
      setContentHeight(contentHeight)
      const newStyle = styleFixed(
        targetRect.left,
        targetRect.top,
        targetRect.width
      )
      setStyleContent(newStyle)
    }
  }

  const updateScroll = event => {
    if (!ticking) {
      window.requestAnimationFrame(() => {
        updateStyleContent()
        setTicking(false)
      })
    }
    setTicking(true)
  }

  useEffect(
    () => {
      // when the open state, contentRef or targetRef change, re-evaluate the content style
      updateStyleContent()
    },
    [ open, contentRef.current, targetRef.current ]
  )

  const [ roc ] = useState(() => {
    return new ResizeObserver((entries, observer) => {
      // when the content has resized, re-evaluate the content style
      updateStyleContent()
    })
  })

  const [ rot ] = useState(
    () =>
      new ResizeObserver((entries, observer) => {
        // when the target has resized, re-evaluate the content style
        updateStyleContent()
      })
  )

  useEffect(() => {
    window.addEventListener('scroll', updateScroll)
    return () => {
      window.removeEventListener('scroll', updateScroll)
    }
  }, [])

  // observe resize changes to the content
  useEffect(() => {
    if (contentRef.current) roc.observe(contentRef.current)
    return () => roc.disconnect()
  }, [])

  // observe resize changes to the target
  useEffect(() => {
    if (targetRef.current) rot.observe(targetRef.current)
    return () => rot.disconnect()
  }, [])

  return {
    // open is a passthrough param to `useProxy` which triggers the overall state of the proxy
    // the internal animation state is handled automatically
    open,

    // proxy refs
    targetRef,
    contentRef,

    // proxy content height
    contentHeight,
    setContentHeight,

    // proxy style content
    styleContent,
    setStyleContent,
    updateStyleContent,
  }
}

const ProxyComponent = ({targetRef, open, contentHeight}) => {
  const styleTarget = {
    width: '100%',
    height: open ? `${contentHeight}px` : 0,
    transition: `height ${ANIMATION_DURATION}ms`,
  }
  return <div style={styleTarget} ref={targetRef} />
}

const Proxy = props => {
  const {context} = props
  if (context) {
    return (
      <context.Consumer>
        {proxy => <ProxyComponent {...proxy} />}
      </context.Consumer>
    )
  }
}
export default Proxy

const ProxyContentComponent = ({
  open,
  zIndex,
  children,
  contentRef,
  styleContent,
  updateStyleContent,
}) => {
  useEffect(() => {
    // update the content style when content is mounted initially
    updateStyleContent()
  }, [])

  const handleTransitionStart = event => {
    // update the content style when the content style transitions start
    updateStyleContent()
  }

  useEffect(() => {
    if (contentRef.current) {
      contentRef.current.addEventListener(
        'transitionstart',
        handleTransitionStart
      )
    }
    return () => {
      if (contentRef.current) {
        contentRef.current.removeEventListener(
          'transitionstart',
          handleTransitionStart
        )
      }
    }
  })

  return (
    <div
      style={{...styleContent, opacity: open ? 1 : 0, zIndex: zIndex}}
      ref={contentRef}
    >
      {open ? children : null}
    </div>
  )
}

export const ProxyContent = props => {
  const {context, zIndex, children} = props
  if (context) {
    return (
      <context.Consumer>
        {proxy => (
          <ProxyContentComponent
            zIndex={zIndex}
            children={children}
            {...proxy}
          />
        )}
      </context.Consumer>
    )
  }
}
