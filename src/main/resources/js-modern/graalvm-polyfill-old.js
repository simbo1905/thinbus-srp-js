// Modern GraalVM polyfill for Node.js modules required by thinbus-srp browser.js
// Provides crypto-js, jsbn, and random-strings modules in GraalVM context

// Polyfill for crypto-js/sha256
if (typeof require === 'undefined') {
    // Create a minimal require function for GraalVM
    var require = function(moduleName) {
        if (moduleName === 'crypto-js/sha256') {
            // Use Web Crypto API or fallback to built-in SHA-256
            return function(message) {
                // Convert to string if needed
                var str = message.toString();
                
                // Use GraalVM's built-in MessageDigest
                var MessageDigest = Java.type('java.security.MessageDigest');
                var digest = MessageDigest.getInstance('SHA-256');
                var bytes = Java.to(str.split('').map(function(c) { return c.charCodeAt(0); }), 'byte[]');
                var hash = digest.digest(bytes);
                
                // Convert to hex string
                var hex = '';
                for (var i = 0; i < hash.length; i++) {
                    var b = hash[i] & 0xFF;
                    hex += (b < 16 ? '0' : '') + b.toString(16);
                }
                
                return {
                    toString: function() { return hex; }
                };
            };
        }
        
        if (moduleName === 'jsbn') {
            // Provide BigInteger implementation
            function BigIntegerConstructor(value, radix) {
                // Ensure this is called with 'new'
                if (!(this instanceof BigIntegerConstructor)) {
                    return new BigIntegerConstructor(value, radix);
                }
                
                // Use GraalVM's built-in BigInteger
                var JavaBigInteger = Java.type('java.math.BigInteger');
                
                if (typeof value === 'string') {
                    this._java = new JavaBigInteger(value, radix || 10);
                } else if (typeof value === 'number') {
                    this._java = JavaBigInteger.valueOf(value);
                } else {
                    throw new Error("Invalid BigInteger value: " + value);
                }
                    
                    // Add methods needed by thinbus-srp
                    this.toString = function(radix) {
                        return this._java.toString(radix || 10);
                    };
                    
                    this.add = function(other) {
                        var result = {};
                        result._java = this._java.add(other._java);
                        // Copy methods to result
                        for (var prop in this) {
                            if (typeof this[prop] === 'function') {
                                result[prop] = this[prop];
                            }
                        }
                        return result;
                    };
                    
                    this.multiply = function(other) {
                        var result = {};
                        result._java = this._java.multiply(other._java);
                        // Copy methods to result
                        for (var prop in this) {
                            if (typeof this[prop] === 'function') {
                                result[prop] = this[prop];
                            }
                        }
                        return result;
                    };
                    
                    this.modPow = function(exp, mod) {
                        var result = {};
                        result._java = this._java.modPow(exp._java, mod._java);
                        // Copy methods to result
                        for (var prop in this) {
                            if (typeof this[prop] === 'function') {
                                result[prop] = this[prop];
                            }
                        }
                        return result;
                    };
                    
                    this.mod = function(mod) {
                        var result = {};
                        result._java = this._java.mod(mod._java);
                        // Copy methods to result
                        for (var prop in this) {
                            if (typeof this[prop] === 'function') {
                                result[prop] = this[prop];
                            }
                        }
                        return result;
                    };
                    
                    this.equals = function(other) {
                        if (!other || !other._java) return false;
                        return this._java.equals(other._java);
                    };
                    
                    this.compareTo = function(other) {
                        if (!other) throw new Error("Cannot compare to null or undefined");
                        if (!other._java) throw new Error("Cannot compare to object without _java property");
                        return this._java.compareTo(other._java);
                    };
                    
                    this.toString = function(radix) {
                        if (radix) {
                            return this._java.toString(radix);
                        }
                        return this._java.toString();
                    };
                }
            
            return {
                BigInteger: BigIntegerConstructor
            };
        }
        
        if (moduleName === 'random-strings') {
            // Provide random string generation with hex method
            return {
                hex: function(length) {
                    var SecureRandom = Java.type('java.security.SecureRandom');
                    var random = new SecureRandom();
                    var chars = '0123456789abcdef';
                    var result = '';
                    for (var i = 0; i < length; i++) {
                        result += chars.charAt(random.nextInt(chars.length));
                    }
                    return result;
                }
            };
        }
        
        throw new Error('Module not found: ' + moduleName);
    };
    
    // Make BigInteger available globally for direct use
    if (typeof BigInteger === 'undefined') {
        var jsbnModule = require('jsbn');
        BigInteger = jsbnModule.BigInteger;
        console.log("Set global BigInteger to:", BigInteger);
        console.log("Global BigInteger type:", typeof BigInteger);
    }
}