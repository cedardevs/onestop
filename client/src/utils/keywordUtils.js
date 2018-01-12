import _ from 'lodash'
import Immutable from 'seamless-immutable'

const hierarchy = (category, facets) => {
  const reducer = (map, facet) => {
    const termHierarchy = facet.termHierarchy
    const facetMap = {
      // TODO make this immutable?
      id: facet.id,
      children: [],
      parent: map[termHierarchy.length].id,
    }
    map[termHierarchy.length + 1] = facetMap
    map[termHierarchy.length].children.push(facetMap)
    // Immutable.set(map[termHierarchy.length], 'children', Immutable(Immutable.asMutable(map[termHierarchy.length].children).push(facetMap)))
    return map
  }
  let initState = {}
  initState[0] = {id: null, children: []}
  return facets.reduce(reducer, initState)[0].children
}

const id = (category, term) => {
  const idParts = _.concat(_.words(category), _.words(term))
  return idParts.join('-')
}

const keyword = (termHierarchy, isHierarchy) => {
  if (isHierarchy) {
    return titleCaseKeyword(termHierarchy.pop())
  }
  return termHierarchy.pop()
}

const buildTermHierarchy = (term, isHierarchy) => {
  if (isHierarchy) {
    // Handling unfortunate instances of strings like "Spectral/Engineering >\t\t\t\t\t\t\tmicrowave"
    return term.split('>').map(e => e.trim())
  }
  return [ term ]
}

const buildFacet = (category, term, count, selected, isHierarchy) => {
  const termHierarchy = buildTermHierarchy(term, isHierarchy)
  return Immutable({
    count: count,
    category: category,
    term: term,
    id: id(category, term),
    selected: selected,
    termHierarchy: termHierarchy,
    keyword: keyword(termHierarchy, isHierarchy),
  })
}

const categoryName = category => {
  if (category === 'science') {
    return 'Data Theme'
  }
  return _.startCase(_.toLower(category.split(/(?=[A-Z])/).join(' ')))
}

const facets = (category, terms, selectedFacets) => {
  const selectedTerms = selectedFacets[category]

  return _.map(terms, (data, term) => {
    let selected = selectedTerms ? selectedTerms.includes(term) : false
    return buildFacet(
      category,
      term,
      data.count,
      selected,
      category === 'science'
    )
  })
}

export const buildKeywordHierarchyMap = (facetMap, selectedFacets) => {
  return _.map(facetMap, (terms, category) => {
    if (!_.isEmpty(terms)) {
      // Don't load categories that have no results
      const keywordFacets = facets(category, terms, selectedFacets)
      return {
        name: categoryName(category),
        id: categoryName(category).replace(' ', '-', 'g'),
        keywordFacets: keywordFacets,
        hierarchy: Immutable(hierarchy(category, keywordFacets)), // TODO make this immutable too
      }
    }
  }).filter(facetCategory => {
    return facetCategory
  }) // remove undefined?
}

// pulls out the last term in a GCMD-style keyword and attempts to maintain intended acronyms
export const titleCaseKeyword = term => {
  if (!term) {
    return null
  }
  const trimmed = term.split('>').pop().trim() // TODO no more need to split, combine with categoryName?
  return trimmed === trimmed.toUpperCase()
    ? _.startCase(trimmed.toLowerCase())
    : trimmed
}
