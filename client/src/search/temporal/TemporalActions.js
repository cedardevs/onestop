export const UPDATE_DATE_RANGE = 'UPDATE_DATE_RANGE'

export const updateDateRange = (startDate, endDate) => {
  return {
    type: UPDATE_DATE_RANGE,
    startDate,
    endDate
  }
}
