# 1.1
## Features
* Added support to run on maven3.

## Bug fixes
* Debug mode generates invalid JavaScript to register configuration entries into
the test runner.

# 1.0
## Features
* Supports ant patterns to load resources from file system, network and
classpath.
* Allows to configure how class loaders are composed.
* Provides configurable runners, including default implementations for JavaScript
and single HTML files.
* Runner implementations are notified after each test file is processed and
they're able to perform assertions on the DOM.
* Integrated debug mode via embedded HTTP server.
* Configurable system properties.

## Bug fixes

