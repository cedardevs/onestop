import React, {useState, useEffect} from 'react'

import Select from 'react-select'
import FlexRow from '../common/ui/FlexRow'
import Expandable from '../common/ui/Expandable'
import Button from '../common/input/Button'
import {question_circle, SvgIcon} from '../common/SvgIcon'
import {FilterColors} from '../../style/defaultStyles'

const selectTheme = theme => {
  // TODO copy-pasta #3 - move this to defaultStyles (although actually there's just a lot of overlap in re-setting up the select....?)
  return {
    ...theme,
    borderRadius: '0.309em',
    colors: {
      ...theme.colors,
      primary: FilterColors.DARKEST,
      primary75: FilterColors.DARK,
      primary50: FilterColors.MEDIUM,
      primary25: FilterColors.LIGHT,
      danger: '#277CB2',
      dangerLight: '#277CB2',
    },
  }
}

const RELATION_OPTIONS = [
  {
    // TODO make these immutable like in GeologicPresets?
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

  const [ selectedRelation, setSelectedRelation ] = useState(
    RELATION_OPTIONS[0]
  )

  useEffect(
    () => {
      let matchingRelation = _.find(RELATION_OPTIONS, (relation, index) => {
        return relation.value == relation
      })
      if (matchingRelation) {
        setSelectedRelation(matchingRelation)
      }
      else {
        setSelectedRelation(RELATION_OPTIONS[0])
      }
    },
    [ relation ]
  )

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
                // setSelectedRelation(relation)
              }}
            />
          </div>,
          <div key="sentence::end" style={{marginLeft: '0.309em'}}>
            filter.
          </div>,
          <Button
            key="show-examples-button"
            title="Show relationship examples"
            aria-label="Show relationship examples"
            style={{
              marginLeft: '0.309em',
              padding: '0.309em',
              background: 'none',
              color: 'inherit',
              font: 'inherit',
            }}
            styleHover={{
              // unset default style until we genericize Button more
              background: 'none',
            }}
            aria-expanded={examplesOpen}
            onClick={() => {
              setExamplesOpen(!examplesOpen)
            }}
          >
            <SvgIcon path={question_circle} size="1em" />
          </Button>,
        ]}
      />

      <Expandable
        open={examplesOpen}
        content={illustration(selectedRelation.value)}
      />
    </div>
  )
}
export default Relation
