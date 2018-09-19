package org.cedar.psi.manager.config

class Constants {

  // Application Info
  static final String APP_ID = 'stream-manager'
  static final String BOOTSTRAP_DEFAULT = 'localhost:9092'

  // SME Splitting Info
  static final String SPLIT_FIELD = 'source'
  static final List<String> SPLIT_VALUES = new ArrayList<>([
      'common-ingest'
  ])
}
