# Functional tests

## Requirements

* The machine current needs to be on the SSO whitelist in order to look up users.
* You need a browser installed. You can run on a headless machine by installing xfrb
  and putting `xfrb-run` before `./activator` in the commands below.

## Running

There is a config called `fun` that has all the same tasks as the regular `test` config, so
you can run the `test` task or the `test-only` task etc.

Run all functional tests in chrome and firefox

    TEST_BROWSERS=chrome,firefox ./activator fun:test
    
Run a single test using the default browser (firefox)

    ./activator "fun:test-only BasicFuncTest"

Build updated assets and run a functional test

    ./activator "gulp assets" "fun:test-only BasicFuncTest"
    
You can also start `sbt` instead of `activator` if you prefer, in the usual way.
     
## Chrome driver

To run tests with Chrome, you'll need to download the driver:

[Getting Started with ChromeDriver](https://sites.google.com/a/chromium.org/chromedriver/getting-started)

It's easiest to drop it somewhere on your PATH.