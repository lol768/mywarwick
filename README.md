My Warwick
=====

Setup
--------

You will need a recent JDK, 8 ideally. You will need `node` (with `npm`) installed in order to build the static assets.

To get Node dependencies run `npm install`.

Get a global `gulp` command with `npm install -g gulp-cli` in case you don't already have it.

Building and running
--------------------

To build assets, run `gulp assets`.
To continually build assets when they change, run `gulp wizard`.
To lint the JavaScript code, you can use `gulp lint`. You'll probably want to do this prior to a PR.

To start the app, run `./activator "run 8080"`. You can choose a different port but using 8080 might make it simpler
to re-use your Apache/Nginx proxy config that you have working for Tomcat.

You probably also need to run the `content-providers` app to generate content for your tiles.

Using the embedded worker
-------------------------

If you want to test any of the background worker tasks (e.g. sending emails for alerts), you'll need
to:

* Create a `conf/worker.conf` based on the `conf/worker-example.conf` file
* Add `include "worker"` to your `conf/application.conf` file under the
  existing `include "default"` line.

The worker should then run with the usual IDE run/debug configurations.

Version control
---------------

Commit messages should be prefixed with the JIRA ticket reference. Once merged, old branches
should be deleted.

CSRF and Forms
--------------

Forms are protected against cross-site request forgery by default, which means you'll need to
include the appropriate CSRF token in your forms and AJAX requests. For forms templated in Twirl,
you can include a form field with the following code:

```twirl
@context.csrfHelper.formField
```

You my need to add an `(implicit context: RequestContext)` to the view if it's not already there.

For JavaScript, use the `csrfToken` module with exported functions `getCsrfHeaderName` and
`getCsrfToken` *or* the `fetchWithCredentials`/`postJsonWithCredentials` helper functions which
have CSRF support baked in.

If there is an exceptional reason why your route needs to be exempt from the CSRF protection,
you can add a regexp-based path exception in the `mywarwick.csrfWhitelist` configuration list. By
default the SSO access control route is covered with this.

Functional tests
----------------

See functional-test/README.md.
