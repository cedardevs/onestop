import React, {useState, useEffect} from 'react'

import Select from 'react-select'
import FlexRow from '../common/ui/FlexRow'
import Expandable from '../common/ui/Expandable'
import Button from '../common/input/Button'
import {question_circle, SvgIcon} from '../common/SvgIcon'
import {FilterColors, selectTheme} from '../../style/defaultStyles'
import {consolidateStyles} from '../../utils/styleUtils'

const RELATION_OPTIONS = [
  {
    value: 'intersects',
    label: 'intersects',
  },
  {
    value: 'contains',
    label: 'fully contains',
  },
  {
    value: 'within',
    label: 'is fully within',
  },
  {
    value: 'disjoint',
    label: 'is disjoint from',
  },
]

const Relation = ({id, relation, onUpdate, illustration}) => {
  const [ examplesOpen, setExamplesOpen ] = useState(false)
  const [ examplesButtonFocused, setExamplesButtonFocused ] = useState(false)

  const [ selectedRelation, setSelectedRelation ] = useState(
    RELATION_OPTIONS[0]
  )
  const diagram = illustration(relation)

  useEffect(
    () => {
      let matchingRelation = _.find(
        RELATION_OPTIONS,
        (relationOption, index) => {
          return relationOption.value == relation
        }
      )
      if (matchingRelation) {
        setSelectedRelation(matchingRelation)
      }
      else {
        setSelectedRelation(RELATION_OPTIONS[0])
      }
    },
    [ relation ]
  )

  // note: not using <Button> because that doesn't pass through attrs like aria-expanded
  return (
    <div style={{margin: '.618em'}}>
      <FlexRow
        style={{alignItems: 'center'}}
        items={[
          <div key="sentence::start" style={{marginRight: '0.309em'}}>
            Result
          </div>,
          <div key="sentence::middle" style={{flexGrow: 1}}>
            <Select
              id={id}
              name="relation"
              aria-label="Relationship"
              placeholder="Relationship"
              theme={selectTheme}
              value={selectedRelation}
              options={RELATION_OPTIONS}
              menuPlacement="auto"
              styles={{
                menu: styles => {
                  return {
                    ...styles,
                    zIndex: 101,
                  }
                },
              }}
              onChange={relation => {
                onUpdate(relation.value)
              }}
            />
          </div>,
          <div key="sentence::end" style={{marginLeft: '0.309em'}}>
            filter.
          </div>,
          <button
            key="show-examples-button"
            title="Show relationship examples"
            aria-label="Show relationship examples"
            style={consolidateStyles(
              {
                marginLeft: '0.309em',
                padding: '0.309em',
                background: 'none',
                color: 'inherit',
                font: 'inherit',
                border: 0,
                boxSizing: 'content-box',
                lineHeight: 'normal',
                // overflow: 'visible',
                // userSelect: 'none',
              },
              examplesButtonFocused
                ? {
                    outline: '2px dashed #00002c',
                  }
                : null
            )}
            aria-expanded={examplesOpen}
            onClick={() => {
              setExamplesOpen(!examplesOpen)
            }}
            onFocus={() => setExamplesButtonFocused(true)}
            onBlur={() => setExamplesButtonFocused(false)}
          >
            <SvgIcon path={question_circle} size="1em" />
          </button>,
        ]}
      />

      <Expandable open={examplesOpen} content={diagram} />
    </div>
  )
}
export default Relation
