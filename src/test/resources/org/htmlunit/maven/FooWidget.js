FooWidget = function (container) {
  return {
    render: function () {
      container.innerHTML = PROP_FOO;
    }
  };
};
