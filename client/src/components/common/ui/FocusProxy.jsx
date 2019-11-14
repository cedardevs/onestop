import React, {useRef, useState, useEffect} from 'react'
import {Key} from '../../../utils/keyboardUtils'

export const useTab = () => {
  const [ reverse, setReverse ] = useState(null)
  useEffect(() => {
    const handleKeyDown = e => {
      let key = e.which || e.keyCode
      if (key === Key.TAB && e.shiftKey) {
        setReverse(true)
      }
      else if (key === Key.TAB) {
        //if tab key is pressed then move next.
        setReverse(false)
      }
      else {
        setReverse(null)
      }
    }
    document.addEventListener('keydown', handleKeyDown)
    return () => {
      document.removeEventListener('keydown', handleKeyDown)
    }
  })
  return reverse
}

const proxyFocusables = proxyElement => {
  // assumes `proxyElement` provided to this function will be included in the results of this selector;
  // therefore, the element passed to this function *must* be focusable for this function to operate as intended
  const focusableSelector =
    'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
  const f = [ ...document.querySelectorAll(focusableSelector) ] // Array expansion because findIndex doesn't work on NodeList
  const nf = f.length
  // due to assumption above, we *should* find the exact node we provided in the list of all focusable things
  const i = f.findIndex(e => e.isSameNode(proxyElement))
  // in case we didn't give a focusable item to search relative to,
  // or the only focusable thing is the thing we provided, return `null`
  if (i === -1 || nf === 1) {
    return {previous: null, next: null}
  }
  return {previous: i > 0 ? f[i - 1] : null, next: i < nf - 1 ? f[i + 1] : null}
}

const targetFocusables = targetElement => {
  // unlike `proxyFocusables` the element here need not be focusable because we are specifically
  // leveraging this function to determine the first and last focusable elements *within* a more narrowed DOM context
  // in other words, we use this to determine the entry and exit of a DOM area we want to focus proxy in and out of automatically;
  // the first focusable element tells us where to focus as soon as the proxy is focused, and the last focusable element tells us
  // to return focus to after the proxy when it is blurred
  const f = targetElement.querySelectorAll(
    'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
  )
  const nf = f.length
  const first = nf > 0 ? f[0] : null
  const last = nf > 0 ? f[nf - 1] : null
  return {first, last}
}

export const useFocusProxy = (
  enabled = true,
  scrollOnProxy = true,
  scrollOnReturn = true
) => {
  // is the user `Shift+Tab`ing?
  const tabReverse = useTab()

  const proxyRef = useRef(null)
  const targetRef = useRef(null)
  const [ proxyFocusing, setProxyFocusing ] = useState(false)
  const [ targetBlurring, setTargetBlurring ] = useState(false)
  const [ previouslyActiveElement, setPreviouslyActiveElement ] = useState(null)

  // useEffect(() => {
  //   const {previous, next} = proxyFocusables(proxyRef.current)
  //
  // }, [proxyRef.current, proxyFocusing])

  useEffect(
    () => {
      if (enabled) {
        const {previous, next} = proxyFocusables(proxyRef.current)
        const {first, last} = targetFocusables(targetRef.current)
        const targetAvailable =
          targetRef.current &&
          targetRef.current.clientWidth > 0 &&
          targetRef.current.clientHeight > 0

        const leavingFromFirst =
          first && tabReverse && first.isEqualNode(previouslyActiveElement)
        const leavingFromLast =
          last && !tabReverse && last.isSameNode(previouslyActiveElement)
        setPreviouslyActiveElement(document.activeElement)

        // PROXY FOCUS AND ENTER TARGET
        // the proxy element was focused
        if (proxyFocusing) {
          // ensure we unset the state of the proxy focus
          setProxyFocusing(false)
          // when the target exists and is visible
          if (targetAvailable) {
            // focus on the first child element of the target element to provide context;
            // otherwise, we do the next best thing and scroll to the focusable element in question
            const firstChild = targetRef.current.firstChild
            let scrollElement = null

            // coming from after the proxy (focus should be placed on last focusable element in target)
            if (tabReverse) {
              // is there a "last" focusable thing to focus on?
              if (last) {
                // focus on the last focusable element in the target
                last.focus()
                scrollElement = firstChild ? firstChild : last
              }
            }
            else {
              // coming from before the proxy (focus should be placed on the first focusable element in target)
              // is there a "first" focusable thing to focus on?
              if (first) {
                // focus on the first focusable element in the target
                first.focus()
                scrollElement = firstChild ? firstChild : first
              }
            }
            if (scrollElement && scrollOnProxy) {
              scrollElement.scrollIntoView({behavior: 'smooth'})
            }
          }
          else {
            // target cannot be reached
            // skip focus over the proxy in the proper direction
            if (tabReverse) {
              previous && previous.focus()
            }
            else {
              next && next.focus()
            }
          }
        }

        if (targetBlurring) {
          // ensure we unset the state of the target blur
          setTargetBlurring(false)

          let scrollElement = null

          // return focus to just before or after proxy
          if (leavingFromFirst && proxyRef.current) {
            if (previous) {
              previous.focus()
              scrollElement = previous
            }
          }
          else if (leavingFromLast && proxyRef.current) {
            if (next) {
              next.focus()
              scrollElement = next
            }
          }

          if (scrollElement && scrollOnReturn) {
            scrollElement.scrollIntoView({behavior: 'smooth'})
          }
        }
      }
    },
    [
      enabled,
      proxyRef.current,
      targetRef.current,
      proxyFocusing,
      targetBlurring,
    ]
  )

  return {proxyRef, setProxyFocusing, targetRef, setTargetBlurring}
}

const FocusProxy = React.forwardRef((props, ref) => (
  <div ref={ref} tabIndex={0} onFocus={props.onFocus} />
))
export default FocusProxy

export const FocusTarget = React.forwardRef((props, ref) => {
  const {children} = props
  return (
    <div ref={ref} tabIndex={0} onBlur={props.onBlur}>
      {children}
    </div>
  )
})
