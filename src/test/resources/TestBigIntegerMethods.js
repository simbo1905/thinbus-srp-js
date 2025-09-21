// Test all BigInteger methods needed by JSRunner tests
// Load modern polyfill and browser.js bundle
load("src/main/resources/js-modern/graalvm-polyfill.js");
load("src/main/resources/js-modern/browser.js");

tests({

    testBigIntegerEquals: function() {
        console.log("Testing BigInteger equals method");
        
        var a = new BigInteger("123");
        var b = new BigInteger("123");
        var c = new BigInteger("456");
        
        if (!a.equals(b)) {
            throw new Error("BigInteger equals failed: 123 should equal 123");
        }
        
        if (a.equals(c)) {
            throw new Error("BigInteger equals failed: 123 should not equal 456");
        }
        
        if (a.equals(null)) {
            throw new Error("BigInteger equals failed: should return false for null");
        }
        
        console.log("✓ BigInteger equals method working");
    },

    testBigIntegerCompareTo: function() {
        console.log("Testing BigInteger compareTo method");
        
        var a = new BigInteger("123");
        var b = new BigInteger("123");
        var c = new BigInteger("456");
        var d = new BigInteger("100");
        
        if (a.compareTo(b) !== 0) {
            throw new Error("BigInteger compareTo failed: 123 should equal 123 (return 0)");
        }
        
        if (a.compareTo(c) >= 0) {
            throw new Error("BigInteger compareTo failed: 123 should be less than 456 (return < 0)");
        }
        
        if (a.compareTo(d) <= 0) {
            throw new Error("BigInteger compareTo failed: 123 should be greater than 100 (return > 0)");
        }
        
        console.log("✓ BigInteger compareTo method working");
    },

    testBigIntegerToString: function() {
        console.log("Testing BigInteger toString method");
        
        var a = new BigInteger("123");
        var hex = new BigInteger("ff", 16);
        
        if (a.toString() !== "123") {
            throw new Error("BigInteger toString failed: expected '123', got '" + a.toString() + "'");
        }
        
        if (hex.toString(16) !== "ff") {
            throw new Error("BigInteger toString(16) failed: expected 'ff', got '" + hex.toString(16) + "'");
        }
        
        console.log("✓ BigInteger toString method working");
    },

    testBigIntegerArithmetic: function() {
        console.log("Testing BigInteger arithmetic methods");
        
        var a = new BigInteger("10");
        var b = new BigInteger("5");
        var zero = new BigInteger("0");
        
        // Test add
        var sum = a.add(b);
        if (sum.toString() !== "15") {
            throw new Error("BigInteger add failed: 10 + 5 should equal 15, got " + sum.toString());
        }
        
        // Test multiply  
        var product = a.multiply(b);
        if (product.toString() !== "50") {
            throw new Error("BigInteger multiply failed: 10 * 5 should equal 50, got " + product.toString());
        }
        
        // Test mod
        var mod = a.mod(b);
        if (mod.toString() !== "0") {
            throw new Error("BigInteger mod failed: 10 % 5 should equal 0, got " + mod.toString());
        }
        
        // Test modPow
        var modPow = a.modPow(b, new BigInteger("7"));
        // 10^5 % 7 = 100000 % 7 = 5
        if (modPow.toString() !== "5") {
            throw new Error("BigInteger modPow failed: 10^5 % 7 should equal 5, got " + modPow.toString());
        }
        
        console.log("✓ BigInteger arithmetic methods working");
    },

    testBigIntegerZeroComparison: function() {
        console.log("Testing BigInteger zero comparison");
        
        var jsbn = require('jsbn');
        console.log("jsbn module:", jsbn);
        console.log("jsbn.BigInteger type:", typeof jsbn.BigInteger);
        
        var positive = new jsbn.BigInteger("123");
        console.log("positive object keys:", Object.keys(positive));
        console.log("positive._java exists:", positive._java !== undefined);
        console.log("positive has compareTo:", typeof positive.compareTo);
        
        var negative = new jsbn.BigInteger("-456");
        var zero = new jsbn.BigInteger("0");
        console.log("zero object keys:", Object.keys(zero));
        console.log("zero._java exists:", zero._java !== undefined);
        console.log("zero has compareTo:", typeof zero.compareTo);
        
        if (zero.compareTo(zero) !== 0) {
            throw new Error("Zero should equal zero");
        }
        
        if (positive.compareTo(zero) <= 0) {
            throw new Error("Positive number should be greater than zero");
        }
        
        if (negative.compareTo(zero) >= 0) {
            throw new Error("Negative number should be less than zero");
        }
        
        console.log("✓ BigInteger zero comparison working");
    }

});