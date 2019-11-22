import React, {useState, useEffect} from 'react'
import _ from 'lodash'

import {useBoundingBox} from './BoundingBoxEffect'
import GeoFieldset from './GeoFieldset'
import FlexColumn from '../../common/ui/FlexColumn'
import FormSeparator from '../FormSeparator'
import Relation from '../Relation'
import GeoRelationIllustration from './GeoRelationIllustration'
import Checkbox from '../../common/input/Checkbox'
import Button from '../../common/input/Button'
import {Key} from '../../../utils/keyboardUtils'
import {SiteColors} from '../../../style/defaultStyles'
import mapIcon from '../../../../img/font-awesome/white/svg/globe.svg'
import {styleFilterPanel, styleFieldsetBorder} from '../common/styleFilters'
import ApplyClearRow from '../common/ApplyClearRow'
import {LiveAnnouncer, LiveMessage} from 'react-aria-live'

const styleMapFilter = {
  ...styleFilterPanel,
  ...{
    position: 'relative',
  },
}

const styleDescription = {
  margin: 0,
}
const styleButtonShowMap = {
  fontSize: '1.05em',
}

const warningStyle = valid => {
  if (valid) {
    return {
      display: 'none',
    }
  }
  else {
    return {
      color: SiteColors.WARNING,
      textAlign: 'center',
      margin: '0.75em 0 0.5em',
      fontWeight: 'bold',
      fontSize: '1.15em',
    }
  }
}

const MapFilter = ({
  bbox,
  geoRelationship,
  isOpen, // state of if this filter is open and not collapsed
  showMap, // state of if map is showing
  openMap,
  closeMap,
  excludeGlobal,
  handleNewGeometry,
  removeGeometry,
  toggleExcludeGlobal,
  updateGeoRelationship,
  submit,
}) => {
  const [ bounds ] = useBoundingBox(bbox)

  useEffect(() => {
    return () => {
      closeMap() // unmount
    }
  }, [])

  useEffect(
    () => {
      if (!isOpen) {
        closeMap() // closing expandable containing filter
      }
    },
    [ isOpen ]
  )

  const applyGeometry = () => {
    if (bounds.valid && bounds.asBbox()) {
      handleNewGeometry(bounds.asBbox())
      submit()
    }
  }

  const clearGeometry = () => {
    removeGeometry()
    submit()

    // need to clear local state, since it's not reflected in redux if there was an error:
    bounds.clear()
  }

  const toggleExcludeGlobalResults = () => {
    toggleExcludeGlobal()
    submit()
  }

  const showMapText = showMap ? 'Hide Map' : 'Show Map'

  const buttonShowMap = (
    <Button
      key="MapFilter::showMapToggle"
      icon={mapIcon}
      text={showMapText}
      onClick={() => {
        showMap ? closeMap() : openMap()
      }}
      style={styleButtonShowMap}
      styleIcon={{
        width: '1.618em',
        height: '1.618em',
        marginRight: '0.618em',
      }}
      ariaExpanded={showMap}
    />
  )
  //
  const inputBoundingBox = (
    <GeoFieldset
      key="MapFilter::InputColumn::Bounds"
      handleKeyDown={event => {
        if (event.keyCode === Key.ENTER) {
          event.preventDefault()
          applyGeometry()
        }
      }}
      bounds={bounds}
    />
  )

  const inputColumn = (
    <FlexColumn
      items={[
        inputBoundingBox,
        <ApplyClearRow
          key="MapFilter::InputColumn::Buttons"
          ariaActionDescription="location filter"
          applyAction={applyGeometry}
          clearAction={clearGeometry}
        />,
        <div
          key="MapFilter::InputColumn::Warning"
          style={warningStyle(bounds.valid)}
          aria-hidden={true}
        >
          {bounds.reason}
        </div>,
        <LiveAnnouncer key="alert::annoucement">
          <LiveMessage message={bounds.reason} aria-live="polite" />
        </LiveAnnouncer>,
        <FormSeparator key="MapFilter::InputColumn::OR" text="OR" />,
        buttonShowMap,
      ]}
    />
  )

  const excludeGlobalCheckbox = (
    <div style={{marginLeft: '0.309em'}}>
      <Checkbox
        label="Exclude Global Results"
        id="MapFilter::excludeGlobalCheckbox"
        checked={!!excludeGlobal}
        onChange={toggleExcludeGlobalResults}
      />
    </div>
  )

  const illustration = relation => {
    return (
      <GeoRelationIllustration
        relation={relation}
        excludeGlobal={excludeGlobal}
      />
    )
  }

  return (
    <div style={styleMapFilter}>
      <fieldset style={styleFieldsetBorder}>
        <legend id="mapFilterInstructions" style={styleDescription}>
          Type coordinates or draw on the map. Use the Clear button to reset the
          location filter.
        </legend>
        {inputColumn}
      </fieldset>
      <h4 style={{margin: '0.618em 0 0.618em 0.309em'}}>
        Additional Filtering Options:
      </h4>
      {excludeGlobalCheckbox}
      <Relation
        id="geoRelation"
        relation={geoRelationship}
        onUpdate={relation => {
          if (relation != geoRelationship) {
            updateGeoRelationship(relation)
          }
          if (!_.isEmpty(bbox)) {
            // TODO I think this doesn't require validation because those values are only set at this level if they've passed validation and been submitted...?
            submit()
          }
        }}
        illustration={illustration}
      />
    </div>
  )
}
export default MapFilter
