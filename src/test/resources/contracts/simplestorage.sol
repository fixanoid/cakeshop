contract SimpleStorage {
    uint storedData;
    function SimpleStorage() {
    	storedData = 100;
    }
    function set(uint x) {
        storedData = x;
    }
    function get() constant returns (uint retVal) {
        return storedData;
    }
}