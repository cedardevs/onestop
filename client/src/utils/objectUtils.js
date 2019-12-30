export function mapFromObject(obj){
  function* entries(obj){
    for (let key in obj) yield [ key, obj[key] ]
  }
  return new Map(entries(obj))
}
