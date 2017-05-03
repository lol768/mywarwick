# Functional tests

## Requirements

* You need a browser installed. You can run on a headless machine by installing xvfb
  and putting `xvfb-run` before `./activator` in the commands below.
* For tests that start an embedded app, the machine needs to be on the SSO whitelist 
  in order to look up users. (But at the time of writing we don't start any embedded apps.)

You need to populate `conf/functional-test.conf` based on the example file.

Most browsers typically don't support the standard WebDriver API directly, and
need a driver program as glue between Selenium and the browser.
Chrome requires `chromedriver` on the `PATH`, and Firefox requires `geckodriver`.
If either of them stop working, it's generally because these are missing or because
the version of the browser is not compatible with the version of the driver or
the version of Selenium, so try upgrading everything to the latest.

## Running

There is a config called `fun` that has all the same tasks as the regular `test` config, so
you can run the `test` task or the `test-only` task etc.

Run the mobile tests against Chrome in mobile emulation mode (probably the most useful)

    TEST_BROWSERS=chrome-mobile ./activator "fun:test-only -- -n uk.ac.warwick.MobileTest"

Run the desktop tests against Chrome and Firefox

    TEST_BROWSERS=chrome,firefox ./activator "fun:test-only -- -n uk.ac.warwick.DesktopTest"  

Run a single test using the default browser (firefox)

    ./activator "fun:test-only BasicFuncTest"
       
You can also start `sbt` instead of `activator` if you prefer, in the usual way.
     
## Chrome

To run tests with Chrome, you'll need to download the driver:

[Getting Started with ChromeDriver](https://sites.google.com/a/chromium.org/chromedriver/getting-started)

It's easiest to drop it somewhere on your PATH.

## Firefox

Download geckodriver and put it on PATH.