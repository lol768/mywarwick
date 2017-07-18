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
          message: "Potential use of Array.prototype.findIndex, which isn't compatible with IE11. Consider using lodash.",
          type: "CallExpression"
        }
      ]
    }
  ]
});

ie11CompatTester.run("ie11-compat", rules.rules['ie11-compat'], {
  valid: [
    "_.startsWith('foo', 'foo')",
    "_.endsWith('foo', 'foo')"
  ],
  invalid: [
    {
      code: "someStr.startsWith('foo')",
      errors: [
        {
          message: "Potential use of String.prototype.[starts/ends]With, which isn't compatible with IE11. Consider using lodash or indexOf.",
          type: "CallExpression"
        }
      ]
    }
  ]
});
