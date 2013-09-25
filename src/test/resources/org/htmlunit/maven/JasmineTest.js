describe("BarWidgetTest", function() {
  it("Should pass a basic truthiness test.", function() {
      expect(true).toEqual(true);
  });
  
  it("Should fail when it hits an inequal statement.", function() {
      expect(1+1).toEqual(2);
  });
});

describe("Another Suite", function() {
    it("Should pass this test as well.", function() {
        expect(0).toEqual(0);
    });
});
