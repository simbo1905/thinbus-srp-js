// GraalVM polyfill for Node.js modules and browser APIs
// This provides compatibility shims for running modern JavaScript in GraalVM

if (typeof require === 'undefined') {
    // Polyfill require() for GraalVM
    var require = function(moduleName) {
        if (moduleName === 'jsbn') {
            // Provide BigInteger implementation using GraalVM's Java BigInteger
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
            }
            
            // Add methods to prototype for proper inheritance
            BigIntegerConstructor.prototype.toString = function(radix) {
                return this._java.toString(radix || 10);
            };
            
            BigIntegerConstructor.prototype.add = function(other) {
                var result = new BigIntegerConstructor("0");
                result._java = this._java.add(other._java);
                return result;
            };
            
            BigIntegerConstructor.prototype.multiply = function(other) {
                var result = new BigIntegerConstructor("0");
                result._java = this._java.multiply(other._java);
                return result;
            };
            
            BigIntegerConstructor.prototype.modPow = function(exponent, modulus) {
                var result = new BigIntegerConstructor("0");
                result._java = this._java.modPow(exponent._java, modulus._java);
                return result;
            };
            
            BigIntegerConstructor.prototype.mod = function(modulus) {
                var result = new BigIntegerConstructor("0");
                result._java = this._java.mod(modulus._java);
                return result;
            };
            
            BigIntegerConstructor.prototype.equals = function(other) {
                if (!other || !other._java) {
                    throw new Error("Cannot compare to object without _java property");
                }
                return this._java.equals(other._java);
            };
            
            BigIntegerConstructor.prototype.compareTo = function(other) {
                if (!other || !other._java) {
                    throw new Error("Cannot compare to object without _java property");
                }
                return this._java.compareTo(other._java);
            };
            
            BigIntegerConstructor.prototype.valueOf = function() {
                return this._java.toString();
            };
            
            return {
                BigInteger: BigIntegerConstructor
            };
        }
        
        if (moduleName === 'crypto-js/sha256') {
            // Provide SHA-256 implementation using Java's MessageDigest
            return function(message) {
                var MessageDigest = Java.type('java.security.MessageDigest');
                var StandardCharsets = Java.type('java.nio.charset.StandardCharsets');
                var JavaString = Java.type('java.lang.String');
                
                var digest = MessageDigest.getInstance('SHA-256');
                // Convert JavaScript string to Java String and then get bytes
                var javaString = new JavaString(message.toString());
                var hash = digest.digest(javaString.getBytes(StandardCharsets.UTF_8));
                
                // Convert to hex string
                var hexString = '';
                for (var i = 0; i < hash.length; i++) {
                    var hex = (hash[i] & 0xff).toString(16);
                    if (hex.length === 1) {
                        hexString += '0';
                    }
                    hexString += hex;
                }
                
                return {
                    toString: function() {
                        return hexString;
                    }
                };
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
    
    // Note: BigInteger will be available via require('jsbn').BigInteger
}

// Polyfill console.log to use Java logging
if (typeof console === 'undefined') {
    var console = {
        log: function() {
            var Logger = Java.type('java.util.logging.Logger');
            var logger = Logger.getLogger('JavaScript');
            var message = Array.prototype.slice.call(arguments).join(' ');
            logger.info(message);
        }
    };
}

// Polyfill global object
if (typeof global === 'undefined') {
    var global = this;
}

// Polyfill globalThis
if (typeof globalThis === 'undefined') {
    var globalThis = this;
}

// Add getBytes method to String prototype for GraalVM compatibility
// Note: This implementation avoids recursion by using a simple character code approach
if (typeof String.prototype.getBytes === 'undefined') {
    String.prototype.getBytes = function(charset) {
        // Simple implementation that converts each character to its UTF-8 byte representation
        var bytes = [];
        for (var i = 0; i < this.length; i++) {
            var code = this.charCodeAt(i);
            if (code < 0x80) {
                bytes.push(code);
            } else if (code < 0x800) {
                bytes.push(0xc0 | (code >> 6));
                bytes.push(0x80 | (code & 0x3f));
            } else if (code < 0xd800 || code >= 0xe000) {
                bytes.push(0xe0 | (code >> 12));
                bytes.push(0x80 | ((code >> 6) & 0x3f));
                bytes.push(0x80 | (code & 0x3f));
            } else {
                // Surrogate pair - simplified handling
                bytes.push(0xef, 0xbf, 0xbd); // replacement character
            }
        }
        return Java.to(bytes, 'byte[]');
    };
}