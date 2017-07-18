var rules = require("../../lib/index");
var RuleTester = require("eslint/lib/testers/rule-tester");
var ie11CompatTester = new RuleTester();

ie11CompatTester.run("ie11-compat", rules.rules['ie11-compat'], {
  valid: [
    "_.findIndex()"
  ],
  invalid: [
    {
      code: "someArray.findIndex()",
      errors: [
        {
          message: "Potential use of Array.prototype.findIndex, which isn't compatible with IE11.",
          type: "CallExpression"
        }
      ]
    }
  ]
});
