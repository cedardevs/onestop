import React, {useState, useEffect} from 'react'

import Select from 'react-select'
import FlexRow from '../../common/ui/FlexRow'
import Expandable from '../../common/ui/Expandable'
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

const TimeRelation = ({id, timeRelationship, hasStart, hasEnd, onUpdate}) => {
  const [ examplesOpen, setExamplesOpen ] = useState(false)
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
        console.log(
          'changing selected relation matching',
          timeRelationship,
          matchingRelation
        )
        setSelectedRelation(matchingRelation)
      }
      else {
        console.log(
          'changing selected relation default',
          timeRelationship,
          matchingRelation
        )
        setSelectedRelation(RELATION_OPTIONS[0])
      }
    },
    [ timeRelationship ]
  )

  // useEffect(
  //   () => {
  //     console.log('changing selected relation', selectedRelation.value)
  //     onUpdate(selectedRelation.value)
  //   },
  //   [ selectedRelation ]
  // )

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
        ]}
      />

      <Expandable
        open={examplesOpen}
        onToggle={({open}) => {
          setExamplesOpen(open)
        }}
        showArrow={true}
        heading="show example"
        styleHeading={{color: 'inherit', marginTop: '0.309em'}}
        content={
          <TimelineRelationDisplay
            relation={selectedRelation.value}
            hasStart={hasStart}
            hasEnd={hasEnd}
          />
        }
      />
    </div>
  )

  // TODO move 'advanced' expandable out of TimeRelation, which should just be a child of it in case we have other advanced options as well
  // return (
  //   <Expandable
  //     open={true}
  //     showArrow={true}
  //     heading="Advanced"
  //     styleHeading={{color: 'inherit', marginTop: '0.309em'}}
  //     content={content}
  //   />
  // )
}
export default TimeRelation
