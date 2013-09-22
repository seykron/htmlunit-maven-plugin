Lib = (function () {
  var callbacks = [];

  return {
    define: function (callback) {
      callbacks.push(callback);
    },
    getCallbacks: function () {
      return [].concat(callbacks);
    }
  };
}());
