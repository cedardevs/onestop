import {renderHook, act} from '@testing-library/react-hooks'

export const initHook = (hookFunction, ...hookArgs) => {
  // generic init hook function (makes most tests easier to read, but does not help with tests requiring "rerender")
  let hook = null
  const {result} = renderHook(() => hookFunction(...hookArgs))
  hook = result
  return hook
}
