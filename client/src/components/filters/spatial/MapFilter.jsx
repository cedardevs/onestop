import React, {useState, useEffect} from 'react'
import _ from 'lodash'

import {useGeoJson} from './GeoJsonEffect'
import GeoFieldset from './GeoFieldset'
import FlexColumn from '../../common/ui/FlexColumn'
import FormSeparator from '../FormSeparator'
import Relation from '../Relation'
import GeoRelationIllustration from './GeoRelationIllustration'
import Checkbox from '../../common/input/Checkbox'
import Button from '../../common/input/Button'
import {Key} from '../../../utils/keyboardUtils'

import mapIcon from '../../../../img/font-awesome/white/svg/globe.svg'

import {styleFilterPanel, styleFieldsetBorder} from '../common/styleFilters'
import ApplyClearRow from '../common/ApplyClearRow'

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

const warningStyle = warning => {
  if (_.isEmpty(warning)) {
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
  geoJSON,
  geoRelationship,
  isOpen,
  showMap,
  openMap,
  closeMap,
  excludeGlobal,
  handleNewGeometry,
  removeGeometry,
  toggleExcludeGlobal,
  updateGeoRelationship,
  submit,
}) => {
  const [ bounds ] = useGeoJson(geoJSON)
  const [ warning, setWarning ] = useState('')
  useEffect(
    () => {
      setWarning('')
    },
    [ geoJSON ]
  )

  useEffect(() => {
    return () => {
      console.log('unmount', showMap, isOpen)
      console.log('closing map')
      closeMap()
      // if (showMap) {
      //   toggleMap()
      // }
    }
  }, [])
  useEffect(
    () => {
      if (!isOpen) {
        // from component will recieve props
        // console.log("?????", showMap, isOpen)
        //   toggleMap()
        closeMap()
        console.log('closing map (because not open)')
      }
    },
    [ isOpen ]
  )

  const applyGeometry = () => {
    if (bounds.asGeoJSON()) {
      // Validation of coordinates is performed in bbox to GeoJSON conversion (geoUtils)
      handleNewGeometry(bounds.asGeoJSON())
      submit()
    }
    else if (bounds.west && bounds.south && bounds.east && bounds.north) {
      setWarning(
        'Invalid coordinates entered. Valid longitudes are between -180 and 180. Valid latitudes are between -90 and 90, where North is always greater than South.'
      )
    }
    else {
      setWarning(
        'Incomplete coordinates entered. Ensure all four fields are populated.'
      )
    }
  }

  const clearGeometry = () => {
    removeGeometry()
    submit()

    bounds.clear() // TODO probably not needed, should cascade from removeGeometry()?
    setWarning('') // TODO probably not needed, should cascade from removeGeometry()?
  }
  const toggleExcludeGlobalResults = () => {
    toggleExcludeGlobal()
    submit()
  }

  const handleShowMap = () => {
    // if (!showMap && toggleMap) {
    //   toggleMap()
    // }
    openMap()
  }

  const handleHideMap = () => {
    // if (showMap && toggleMap) {
    //   toggleMap()
    // }
    closeMap()
  }

  const showMapText = showMap ? 'Hide Map' : 'Show Map'

  const buttonShowMap = (
    <Button
      key="MapFilter::showMapToggle"
      icon={mapIcon}
      text={showMapText}
      onClick={() => {
        showMap ? handleHideMap() : handleShowMap()
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
          style={warningStyle(warning)}
          role="alert"
        >
          {warning}
        </div>,
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
          if (!_.isEmpty(geoJSON)) {
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
