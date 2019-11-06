import {
  CLEAR_SELECTED_GRANULES,
  INSERT_MULTIPLE_SELECTED_GRANULES,
  INSERT_SELECTED_GRANULE,
  REMOVE_MULTIPLE_SELECTED_GRANULES,
  REMOVE_SELECTED_GRANULE,
} from './actions/CartActions'
import {GRANULES_FOR_CART_RESULTS_RECEIVED} from './actions/routing/GranuleSearchStateActions'
import {initialState as initialStateCart} from './reducers/cart'
import {localStorageMiddleware} from './localStorageMiddleware'

// middleware to handle observable actions which trigger cart reducer changes to local storage
export const cartMiddleware = localStorageMiddleware(
  'cart',
  [ 'selectedGranules' ],
  initialStateCart,
  [
    GRANULES_FOR_CART_RESULTS_RECEIVED, // add all matching to cart (final success action)
    INSERT_SELECTED_GRANULE,
    INSERT_MULTIPLE_SELECTED_GRANULES,
    REMOVE_SELECTED_GRANULE,
    REMOVE_MULTIPLE_SELECTED_GRANULES,
    CLEAR_SELECTED_GRANULES,
  ]
)
