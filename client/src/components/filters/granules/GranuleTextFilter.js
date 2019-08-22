import React, {useEffect, useRef, useState} from 'react'

import Button from '../../common/input/Button'
import FlexColumn from '../../common/ui/FlexColumn'
import FlexRow from '../../common/ui/FlexRow'

import FilterFieldset from '../FilterFieldset'

import {FilterColors} from '../../../style/defaultStyles'

const styleBreathingRoom = {
  marginTop: '1em',
}

const styleTextFilter = {
  ...{padding: '0.618em'},
}

const styleButton = {
  width: '35%',
}

const styleButtonRow = {
  display: 'flex',
  flexDirection: 'row',
  alignItems: 'center',
  justifyContent: 'space-around',
  // marginBottom: '0.5em',
}

const styleField = {
  margin: '2px',
  alignItems: 'center',
  justifyContent: 'space-around',
  // marginBottom: '0.25em',
}

const styleLabel = {
  // marginBottom: '0.25em',
}

const styleInput = {
  color: FilterColors.TEXT,
  height: '100%',
  margin: 0,
  padding: '0 0.309em',
  border: `1px solid ${FilterColors.LIGHT_SHADOW}`,
  borderRadius: '0.309em',
}

const styleInputWrapper = {
  height: '2em',
}

const GranuleTextFilter = props => {
  const [ query, setQuery ] = useState(props.query)
  const textQuery = useRef(null)
  useEffect(
    () => {
      setQuery(props.query)
    },
    [ props.query ]
  ) // on props change, update internal state

  const submit = event => {
    event.preventDefault()
    props.submit(textQuery.current.value) // TODO can this be {query}?
  }
  const clear = event => {
    event.preventDefault()
    props.clear()
  }

  const applyButton = (
    <Button
      key="TextFilter::apply"
      text="Apply"
      title="Apply text filter"
      onClick={submit}
      style={styleButton}
    />
  )
  const clearButton = (
    <Button
      key="TextFilter::clear"
      text="Clear"
      title="Clear text filter"
      onClick={clear}
      style={styleButton}
    />
  )
  const id = 'TextFilter::input'
  const input = (
    <div key="TextFilter::input::wrapper" style={styleInputWrapper}>
      <input
        ref={textQuery}
        id={id}
        type="text"
        value={query}
        onChange={e => setQuery(e.target.value)}
        style={styleInput}
      />
    </div>
  )

  return (
    <div style={styleTextFilter}>
      <FlexColumn
        items={[
          <div key="TextFilterInput::all" style={styleBreathingRoom}>
            <form onSubmit={submit}>
              <FilterFieldset>
                <FlexRow
                  style={styleField}
                  items={[
                    <label
                      key="TextFilter::input::label"
                      style={styleLabel}
                      htmlFor={id}
                    >
                      Contains
                    </label>,
                    input,
                  ]}
                />
              </FilterFieldset>
            </form>
          </div>,

          <FlexRow
            key="TextFilter::InputColumn::Buttons"
            style={styleButtonRow}
            items={[ applyButton, clearButton ]}
          />,
        ]}
      />
    </div>
  )
}
export default GranuleTextFilter
