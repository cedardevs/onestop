export const DateRange = {
  START_DATE: 'START_DATE',
  END_DATE: 'END_DATE'
}

export const startDate = (datetime) => {
  return {
    type: DateRange.START_DATE,
    datetime
  }
}

export const endDate = (datetime) => {
  return {
    type: DateRange.END_DATE,
    datetime
  }
}
