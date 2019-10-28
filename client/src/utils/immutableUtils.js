const ls = {
  collections: {
    a: {
      value: 1,
      favorite: true,
    },
    b: {
      value: 2,
    },
    c: {
      value: 3,
    },
  },
  granules: {
    d: {
      value: 4,
      cart: true,
      hmm: [ 1, 2, 3, 4, 5 ],
    },
    e: {
      value: 5,
    },
    f: {
      value: 6,
      cart: true,
    },
  },
  services: {
    g: {
      value: 7,
    },
    h: {
      value: 8,
    },
    i: {
      value: 9,
    },
  },
}

const isObject = obj => {
  return typeof obj === 'object' && obj !== null
}

const isEmptyObject = obj => {
  return Object.entries(obj).length === 0 && obj.constructor === Object
}

const getKey = (obj, key) => {
  // return null if key cannot be found for whatever reason
  if (obj === null || obj === undefined || obj[key] === undefined) {
    return null
  }
  return isObject(obj) && !isEmptyObject(obj) && key in obj
    ? obj[key]
    : obj[key]
}

// immutable (returns a new object with new value at key -- or index, if array)
const setKey = (obj, key, value) => {
  // return new array with value at index (key) if obj is an array or key is an integer
  if (Array.isArray(obj) || Number.isInteger(key)) {
    return Object.assign([], obj, {[Number.parseInt(key)]: value})
  }
  // return sole key/value object if object DNE; otherwise, merge key/value
  return !obj ? {[key]: value} : {...obj, [key]: value}
}

export const getValue = (obj, path) => {
  if (!path || path.length === 0) {
    // return original object w/o path
    return obj
  }
  else if (path.length === 1) {
    // return the leaf at the end of the path
    return getKey(obj, path[0])
  }
  // branch into path
  const branch = getKey(obj, path.shift())
  return getValue(branch, path)
}

// immutable (returns a new version where the new value at path is set)
export const setValue = (obj, path, value) => {
  if (!path || path.length === 0) {
    // replace entire object w/o path
    return value
  }
  else if (path.length === 1) {
    if (value === undefined) {
      // undefined values means we want to prune this path (immutabbly)
      let {[path[0]]: omit, ...res} = obj
      return res
    }
    else {
      // set the leaf value at the end of the path
      return setKey(obj, path[0], value)
    }
  }

  // branch into path
  const branchKey = path.shift()
  const branch = getKey(obj, branchKey)
  return setKey(obj, branchKey, setValue(branch, path, value))
}
