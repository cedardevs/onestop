# Download Cart Feature Toggle

## Configuration

This feature is disabled by default. It allows selection of granules, and viewing the Cart page.

While this is a client feature, it is enabled in the UI configuration within the Search API. Add:
```
ui:
  enabledFeatureToggles:
    - featureName: cart
```

## Requirements

There are currently no additional requirements for this feature.

## Status of this feature

* This feature is incomplete. Downloads cannot be made in bulk yet.
