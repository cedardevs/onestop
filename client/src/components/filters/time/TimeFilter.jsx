import React, {useState, useEffect} from 'react'
import _ from 'lodash'

import {LiveAnnouncer, LiveMessage} from 'react-aria-live'
import DateTimeFilter from './standard/DateTimeFilter'
import GeologicTimeFilter from './geologic/GeologicTimeFilter'
import TabPanels from '../../common/ui/TabPanels'
import FlexRow from '../../common/ui/FlexRow'
import Drawer from '../../common/ui/Drawer'
import {exclamation_triangle, SvgIcon} from '../../common/SvgIcon'
import {FilterColors} from '../../../style/defaultStyles'

const alertStyle = {
  alignItems: 'center',
  justifyContent: 'center',
  color: FilterColors.TEXT,
  backgroundColor: '#f3f38e',
  borderRadius: '0.618em',
  textAlign: 'center',
  margin: '0.618em',
  fontSize: '1.15em',
  padding: '0.309em',
}

const TimeFilter = ({
  timeRelationship,
  startDateTime,
  endDateTime,
  updateDateRange,
  removeDateRange,
  startYear,
  endYear,
  updateYearRange,
  removeYearRange,
  updateTimeRelation,
  submit,
}) => {
  const [ alert, setAlert ] = useState('')
  const [ showAlert, setShowAlert ] = useState(false)

  const standardView = (
    <DateTimeFilter
      startDateTime={startDateTime}
      endDateTime={endDateTime}
      timeRelationship={timeRelationship}
      updateTimeRelationship={relation => {
        if (relation != timeRelationship) updateTimeRelation(relation)
        if (!_.isEmpty(startDateTime) || !_.isEmpty(endDateTime)) {
          // TODO I think this doesn't require validation because those values are only set at this level if they've passed validation and been submitted...?
          submit()
        }
      }}
      applyFilter={(startDate, endDate) => {
        removeYearRange()
        updateDateRange(startDate, endDate)
        submit()
      }}
      clear={() => {
        removeDateRange()
        submit()
      }}
    />
  )
  const geologicView = (
    <GeologicTimeFilter
      startYear={startYear}
      endYear={endYear}
      timeRelationship={timeRelationship}
      updateTimeRelationship={relation => {
        if (relation != timeRelationship) updateTimeRelation(relation)
        if (startYear != null || endYear != null) {
          // TODO I think this doesn't require validation because those values are only set at this level if they've passed validation and been submitted...?
          submit()
        }
      }}
      applyFilter={(startYear, endYear) => {
        removeDateRange()
        updateYearRange(startYear, endYear)
        submit()
      }}
      clear={() => {
        removeYearRange()
        submit()
      }}
    />
  )

  const VIEW_OPTIONS = [
    {
      value: 'standard',
      label: (
        <div aria-label="Show standard datetime filter tab.">Datetime</div>
      ),
      view: standardView,
    },
    {
      value: 'geologic',
      label: <div aria-label="Show geologic year filter tab.">Geologic</div>,
      view: geologicView,
    },
    // {
    //   value: 'periodic',
    //   label: 'Periodic',
    //   view: null,
    // },
  ]

  // manage default selection based on what's set
  let defaultSelection = null
  if (!_.isEmpty(startDateTime) || !_.isEmpty(endDateTime))
    defaultSelection = 'standard'
  else if (startYear != null || endYear != null) defaultSelection = 'geologic'
  const [ currentTab, setCurrentTab ] = useState(defaultSelection) // note! this is not the master state of the tab, but used for computing the alert message internal to this component only

  const setAlertStatus = () => {
    let shouldShowAlert = false
    let alertText = ''
    let selection = currentTab

    if (
      selection == 'geologic' &&
      (!_.isEmpty(startDateTime) || !_.isEmpty(endDateTime))
    ) {
      alertText =
        'Datetime filters will be automatically removed by geologic filters.'
      shouldShowAlert = true
    }
    if (selection == 'standard' && (startYear != null || endYear != null)) {
      alertText =
        'Geologic filters will be automatically removed by datetime filters.'
      shouldShowAlert = true
    }

    if (shouldShowAlert) {
      setShowAlert(true)
      setAlert(alertText)
    }
    else {
      setShowAlert(false)
    }
  }

  useEffect(
    () => {
      setAlertStatus()
    },
    [ currentTab, startDateTime, endDateTime, startYear, endYear ]
  )

  const onSelectionChanged = selection => {
    setCurrentTab(selection)
  }

  const alertMessage = (
    <FlexRow
      key="GeologicDateFilter::InputColumn::Alert"
      style={alertStyle}
      items={[
        <SvgIcon
          key="alert::icon"
          size="1.4em"
          style={{marginLeft: '0.309em'}}
          path={exclamation_triangle}
        />,
        <div key="alert::message" aria-hidden="true">
          {alert}
        </div>,
        <LiveAnnouncer key="alert::annoucement">
          <LiveMessage message={alert} aria-live="polite" />
        </LiveAnnouncer>,
      ]}
    />
  )

  const clearAlert = () => {
    // set these back when closed so that announcements reannounce when they reappear (screen reader only announces it the first time it changes otherwise)
    setAlert('')
  }

  const handleDrawerAnimationEnd = open => {
    if (!open) {
      clearAlert()
    }
  }

  return (
    <div>
      <Drawer
        content={alertMessage}
        open={showAlert}
        onAnimationEnd={handleDrawerAnimationEnd}
      />
      <TabPanels
        name="timeFilter"
        options={VIEW_OPTIONS}
        defaultSelection={defaultSelection}
        onSelectionChanged={onSelectionChanged}
      />
    </div>
  )
}
export default TimeFilter
