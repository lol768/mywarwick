module.exports.rules = {
  "ie11-compat": {
    'create': function create(context) {
      return {
        CallExpression: function (node) {
          var callee = node.callee;
          if (callee.type === 'MemberExpression') {
            var calleeObj = callee.object;
            var calleeProp = callee.property;

            if (calleeObj.type !== 'Identifier' || calleeProp.type !== 'Identifier') {
              return;
            }

            if (calleeObj.name !== '_' && calleeProp.name === 'findIndex') {
              context.report(node, "Potential use of Array.prototype.findIndex, which isn't compatible with IE11.");
            }
          }
        },

      };
    }
  }
};



