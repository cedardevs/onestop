import {getValue, saveReducerStateValue} from './stateManager'

// middleware to handle observable actions which trigger cart reducer changes to local storage
export const localStorageMiddleware = (
  reducerKey,
  reducerPath,
  initialState,
  observableActions
) => store => next => action => {
  // actions which are observed by and affect local storage cache
  const isObservable = observableActions.includes(action.type)

  // call the next dispatch method in the middleware chain.
  const returnValue = next(action)

  // move on without storing to local storage if the action is not observable
  if (!isObservable) {
    return returnValue
  }

  // get the value to save from the reducer after the action has been performed
  const saveReducerState = getValue(store.getState()[reducerKey], reducerPath)

  // save the part of the reducer we care about, merged into the initial state for that reducer
  saveReducerStateValue(
    reducerKey,
    [ 'selectedGranules' ],
    saveReducerState,
    initialState
  )

  return returnValue
}
