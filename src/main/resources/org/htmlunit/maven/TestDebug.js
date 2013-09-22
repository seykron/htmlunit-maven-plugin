(function () {
  var disconnect = function () {
    var xhr;
    if (window.XMLHttpRequest) {
      xhr = new XMLHttpRequest();
    } else {
      try {
        xhr = new ActiveXObject("Msxml2.XMLHTTP");
      } catch (e) {
        xhr = new ActiveXObject("Microsoft.XMLHTTP");
      }
    }
    if (!xhr) {
      throw new Error("Browser not supported");
    }
    xhr.open("GET", "/disconnect/", false);
    xhr.send();
  };

  if (window.addEventListener) {
    window.addEventListener("beforeunload", disconnect);
  } else if (window.attachEvent) {
    window.attachEvent("onbeforeunload", disconnect);
  } else {
    throw new Error("Unsupported browser");
  }
}());
