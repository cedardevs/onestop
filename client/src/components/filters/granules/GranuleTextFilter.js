import React, {useEffect, useRef, useState} from 'react'

const GranuleTextFilter = props => {
  const [query, setQuery] = useState(props.query)
  const textQuery = useRef(null)
  useEffect(() => {
    setQuery(props.query)
  }, [props.query]) // on props change, update internal state

  // useEffect(
  //   () => {
  //     if (!ref.current || !ref.current.getBoundingClientRect().width) return
  //     props.callback(ref.current.getBoundingClientRect().width)
  //   },
  //   [ ref.current ]
  // )
  //
  // // render
  // const {content, open, visible} = props
  // const width = props.width ? props.width : defaultWidth
  //
  // if (!visible) {
  //   return null
  // }
  //
  // return (
  //   <div ref={ref} style={open ? styleOpen(width) : styleClosed(width)}>
  //     {content}
  //   </div>
  // )
  const submit = (event) => {
    event.preventDefault()
    props.submit(textQuery.current.value)
  }
  const clear = (event) => {
  event.preventDefault()
    props.clear()
  }

  return (
    <form onSubmit={submit}>
      <label>Title</label>
      <input ref={textQuery} type="text" value={query} onChange={e => setQuery(e.target.value)}/>
      <button onClick={submit}>Apply</button>
      <button onClick={clear}>Clear</button>
    </form>
  )
}
export default GranuleTextFilter
