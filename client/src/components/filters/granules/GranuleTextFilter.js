import React, {useEffect, useRef, useState} from 'react'

import Checkbox from '../../common/input/Checkbox'
import FlexColumn from '../../common/ui/FlexColumn'
import FlexRow from '../../common/ui/FlexRow'

import FilterFieldset from '../FilterFieldset'

import {FilterColors} from '../../../style/defaultStyles'

import {styleFilterPanel, styleFieldsetBorder} from '../common/styleFilters'
import ApplyClearRow from '../common/ApplyClearRow'

const styleOptionsFilter = {
  ...styleFilterPanel,
  ...{
    position: 'relative',
  },
}

const styleField = {
  display: 'flex',
  flexDirection: 'row',
  margin: '0.618em 0',
  alignItems: 'center',
  justifyContent: 'space-between',
  width: '15em',
}

const styleInput = {
  color: FilterColors.TEXT,
  height: '100%',
  margin: 0,
  padding: '0 0.309em',
  border: `1px solid ${FilterColors.LIGHT_SHADOW}`,
  borderRadius: '0.309em',
  width: '10em',
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
    <div style={styleFilterPanel}>
      <FlexColumn
        style={styleFieldsetBorder}
        items={[
          <div key="TextFilterInput::all">
            <form onSubmit={submit}>
              <FilterFieldset>
                <FlexRow
                  style={styleField}
                  items={[
                    <label
                      key="TextFilter::input::label"
                      htmlFor={id}
                      style={{width: '4em'}}
                    >
                      Matches
                    </label>,
                    input,
                  ]}
                />
              </FilterFieldset>
            </form>
          </div>,
          <ApplyClearRow
            key="TextFilter::InputColumn::Buttons"
            ariaActionDescription="text filter"
            applyAction={submit}
            clearAction={clear}
          />,

          <div style={styleOptionsFilter}>
            <h4 style={{margin: '0.618em 0 0.618em 0.309em'}}>
              Additional Filtering Options:
            </h4>
            <Checkbox
              label="Match Any Terms"
              checked={!!!props.allTermsMustMatch}
              onChange={() => {
                props.toggleAllTermsMustMatch(props.query)
              }}
            />
          </div>,
        ]}
      />
    </div>
  )
}
export default GranuleTextFilter
