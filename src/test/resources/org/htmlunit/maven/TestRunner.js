var popup;

TestRunner = {
  run: function () {
    var callbacks = Lib.getCallbacks();

    callbacks.forEach(function (callback) {
      callback();
    });

    setTimeout(function () {
      popup.close();
      window.close();
    }, 2000);
  }
};

if (window.addEventListener) {
  addEventListener('DOMContentLoaded', function () {
    popup = window.open("about:blank");
    TestRunner.run();
  }, false);
} else {
  attachEvent('onload', function () {
    TestRunner.run();
  });
}
