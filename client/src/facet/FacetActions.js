export const METADATA_RECEIVED = 'METADATA_RECEIVED';


export const processMetadata = (metadata) => {
  return {
    type: METADATA_RECEIVED,
    metadata
  }
}