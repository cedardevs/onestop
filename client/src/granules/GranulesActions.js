export const TOGGLE_GRANULE_FOCUS = 'toggle_granule_focus'

export const toggleGranuleFocus = (granuleId) => {
  return {
    type: TOGGLE_GRANULE_FOCUS,
    id: granuleId
  }
}