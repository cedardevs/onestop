import React, {useState, useEffect} from 'react'

import Select from 'react-select'
import FlexRow from '../../common/ui/FlexRow'
import {FilterColors} from '../../../style/defaultStyles'
import TimelineRelationDisplay from './TimelineRelationDisplay' // TODO rename that to like... Illustration?

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
  }, // TODO display as: Result [dropdown] query
  {
    value: 'disjoint',
    label: 'is disjoint from',
  },
]

const TimeRelation = ({id, timeRelationship, hasStart, hasEnd}) => {
  // let defaultSelection = _.find(RELATION_OPTIONS, option => {
  //   return option.value == timeRelationship
  // })
  // if (!defaultSelection) {
  //   defaultSelection = RELATION_OPTIONS[0]
  // }
  // defaultSelection = defaultSelection.value

  const [ selectedRelation, setSelectedRelation ] = useState(
    RELATION_OPTIONS[0]
  )

  useEffect(
    () => {
      let matchingRelation = _.find(RELATION_OPTIONS, (relation, index) => {
        return relation.value == timeRelationship
      })
      if (matchingRelation) {
        setSelectedRelation(matchingRelation)
      }
      else {
        setSelectedRelation(RELATION_OPTIONS[0])
      }
    },
    [ timeRelationship ]
  )

  return (
    <div style={{marginTop: '.618em', marginBottom: '.618em'}}>
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
              onChange={relation => {
                setSelectedRelation(relation)
              }}
            />
          </div>,
          <div key="sentence::end" style={{marginLeft: '0.309em'}}>
            query.
          </div>,
        ]}
      />
      <TimelineRelationDisplay
        relation={selectedRelation.value}
        hasStart={hasStart}
        hasEnd={hasEnd}
      />
    </div>
  )
}
export default TimeRelation
