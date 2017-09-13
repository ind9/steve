[![Build Status](https://travis-ci.org/ind9/steve.svg?branch=master)](https://travis-ci.org/ind9/steve)

# steve

<img src="docs/logo.svg" alt="alt text" width="169px" height="169px">

Steve is a Job state server that holds information regarding your job and it's associated items.

## Modules
There are 3 modules that are part of the project

- `steve-core` - Steve Core contains the core entities.
- `steve-client` (TBD) - Steve Client contains the HTTPClient that other systems can use to talk to Steve Server.
- `steve-server` - Steve Server exposes the steve-core implementation via an API (backed by Dropwizard). 

## License
https://www.apache.org/licenses/LICENSE-2.0
