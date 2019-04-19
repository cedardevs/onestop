# Feature Toggles
These are the current features in OneStop, their location, and default values.

## API-metadata 
### onestop/api-metadata/src/main/resources/application.yml

#### Auth feature
* apply the profile securityenabled in application.yml
* update security values as needed - see documentation on the private repo

#### Sitemap feature
* [Sitemap](/docs/deployment/feature-toggle/sitemap.md) (default: false)

## API-search
### onestop/api-search/src/main/resources/application.yml

* [Trending Searches](/docs/deployment/feature-toggle/trending-searches.md) (default: false)
* [Download Cart](/docs/deployment/feature-toggle/download-cart.md) (default: not enabled)