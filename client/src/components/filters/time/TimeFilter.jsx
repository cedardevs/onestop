import React from 'react'
import DateTimeFilter from './standard/DateTimeFilter'
import GeologicTimeFilter from './geologic/GeologicTimeFilter'
import TabPanels from './TabPanels'

export default class TimeFilter extends React.Component {
  render() {
    const {
      startDateTime,
      endDateTime,
      updateDateRange,
      removeDateRange,
      startYear,
      endYear,
      updateYearRange,
      removeYearRange,
      submit,
    } = this.props

    const standardView = (
      <DateTimeFilter
        startDateTime={startDateTime}
        endDateTime={endDateTime}
        updateDateRange={updateDateRange}
        removeDateRange={removeDateRange}
        submit={submit}
      />
    )
    const geologicView = (
      <GeologicTimeFilter
        startYear={startYear}
        endYear={endYear}
        updateYearRange={updateYearRange}
        removeYearRange={removeYearRange}
        submit={submit}
      />
    )

    const VIEW_OPTIONS = [
      {
        value: 'standard',
        label: 'Datetime',
        view: standardView,
        description: 'Show standard date time filter.', // used in aria for 508, and title for slightly expanded instructions to sighted users on hover
      },
      {
        value: 'geologic',
        label: 'Geologic',
        view: geologicView,
        description: 'Show geologic year filter.',
      },
      // {
      //   value: 'periodic',
      //   label: 'Periodic',
      //   view: null,
      //   description: 'Show periodic time filter.',
      // },
    ]
    console.log('wtf?', startYear) // Somehow this is correct, but it's not showing up in the console log inside geologicView. I think moving that into a const and passing it to tab panels borked it somehow...?
    // putting the view as direct properties didn't help - maybe make each view an oldschool container to wire in the redux props? although that sux, since the granule and collection each have a different set of properties...

    // return <TabPanels name="timeFilter" options={VIEW_OPTIONS} standard={standardView} geologic={geologicView}/>
    return <TabPanels name="timeFilter" options={VIEW_OPTIONS} />
  }
}
