import React, {useState, useEffect} from 'react'

/**
  This effect keeps the internal state in sync with the props (in that a change in prop value such as those caused by a redux state change will overwrite the internal state).

  IMPORTANT!

  components using this must do
  const MyComponent = ({propA, propB}) => {
    const [ A, setA ] = useStateFromProps('', propA)
  }
  and *NOT*
  const MyComponent = (props) => {
    const [ A, setA ] = useStateFromProps('', props.propA) // BAD
  }
  or the update effect won't work!
**/
export const useStateFromProps = (propsValue, defaultValue) => {
  // TODO apply this to GranuleTextFilter!
  const [ value, setValue ] = useState(defaultValue)

  useEffect(
    () => {
      setValue(propsValue)
    },
    [ propsValue ]
  )

  return [ value, setValue ]
}
