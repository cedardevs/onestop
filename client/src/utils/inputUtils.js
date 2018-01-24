import _ from 'lodash'

// if the input represents a finite number, coerces and returns it, else null
export const textToNumber = (text) => {
  const number = text ? _.toNumber(text) : null
  return _.isFinite(number) ? number : null
}
