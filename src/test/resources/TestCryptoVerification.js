// Baby steps test to verify crypto functionality with modern thinbus distribution
load('src/main/resources/js-modern/graalvm-polyfill.js');
load('src/main/resources/js-modern/browser.js');

// Test vectors for SHA-256 verification
var knownVectors = [
    { input: "hello", expected: "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824" },
    { input: "world", expected: "486ea46224d1bb4fb680f34f7c9ad96a8f24ec88be73ea8e5a6c65260e9cb8a7" },
    { input: "test", expected: "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08" }
];

tests({
    testJavaVectorSHA256: function() {
        console.log("=== Testing Java SHA-256 with known vectors ===");
        
        // Use Java crypto directly
        var MessageDigest = Java.type('java.security.MessageDigest');
        var StandardCharsets = Java.type('java.nio.charset.StandardCharsets');
        
        for (var i = 0; i < knownVectors.length; i++) {
            var vector = knownVectors[i];
            var digest = MessageDigest.getInstance("SHA-256");
            var inputBytes = Java.to(vector.input.split('').map(function(c) { return c.charCodeAt(0); }), 'byte[]');
            var hashBytes = digest.digest(inputBytes);
            
            // Convert to hex
            var hex = "";
            for (var j = 0; j < hashBytes.length; j++) {
                var b = hashBytes[j] & 0xff;
                hex += (b < 16 ? "0" : "") + b.toString(16);
            }
            
            console.log("Input: '" + vector.input + "' -> " + hex);
            assert.assertEquals(vector.expected, hex);
        }
        console.log("✓ Java SHA-256 vectors verified");
    },

    testRandomNumberGeneration: function() {
        console.log("=== Testing random number generation ===");
        
        var SecureRandom = Java.type('java.security.SecureRandom');
        var secureRandom = new SecureRandom();
        
        // Generate 100 random numbers and check basic properties
        var randoms = [];
        var sum = 0;
        
        for (var i = 0; i < 100; i++) {
            var randomBytes = secureRandom.generateSeed(4);
            var randomInt = 0;
            for (var j = 0; j < 4; j++) {
                randomInt = (randomInt << 8) | (randomBytes[j] & 0xFF);
            }
            randomInt = Math.abs(randomInt);
            randoms.push(randomInt);
            sum += randomInt;
        }
        
        // Basic randomness checks
        var average = sum / randoms.length;
        var uniqueCount = {};
        for (var i = 0; i < randoms.length; i++) {
            uniqueCount[randoms[i]] = true;
        }
        var uniqueValues = Object.keys(uniqueCount).length;
        
        console.log("Generated 100 random numbers");
        console.log("Average: " + average);
        console.log("Unique values: " + uniqueValues + "/100");
        
        // Should have reasonable uniqueness (at least 90% unique for 100 32-bit numbers)
        assert.assertTrue("Should have good uniqueness", uniqueValues >= 90);
        console.log("✓ Random number generation looks good");
    },

    testThinbusClientCreation: function() {
        console.log("=== Testing thinbus client creation with different verifiers ===");
        
        // Use RFC 5054 1024-bit parameters (N as decimal, k as hex)
        var N = "21766174458617435773191008891802753781907668374255538511144643224689886235383840957210909013086056401571399717235807266581649606472148410291413364152197364477180887395655483738115072677402235101762521901569820740293149529620419333266262073471054548368736039519702486226506248861060256971802984953561121442680157668000761429988222457090413873973970171927093992114751765168063614761119615476233422096442783117971236371647333871414335895773474667308967050807005509320424799678417036867928316761272274230314067548291133582479583061439577559347101961771406173684378522703483495337037655006751328447510550299250924469288819";
        var g = "2";
        var k = "5b9e8ef059c6b32ea59fc1d322d37f04aa30bae5aa9003b8321e21ddb04e300";
        
        // Create multiple clients and verify they generate different verifiers
        var verifiers = [];
        var username = "testuser";
        var password = "testpass";
        
        for (var i = 0; i < 5; i++) {
            var SRP6JavascriptClientSession = thinbus(N, g, k);
            var client = new SRP6JavascriptClientSession();
            
            var salt = client.generateRandomSalt();
            var verifier = client.generateVerifier(salt, username, password);
            
            console.log("Client " + (i+1) + " - Salt: " + salt.substring(0, 16) + "..., Verifier: " + verifier.substring(0, 16) + "...");
            
            verifiers.push(verifier);
            
            // Basic sanity checks
            assert.assertTrue("Salt should be hex string", /^[0-9A-Fa-f]+$/.test(salt));
            assert.assertTrue("Verifier should be hex string", /^[0-9A-Fa-f]+$/.test(verifier));
            assert.assertTrue("Salt should be reasonable length", salt.length >= 16);
            assert.assertTrue("Verifier should be reasonable length", verifier.length >= 32);
        }
        
        // Verify all verifiers are different (they should be due to random salts)
        var uniqueVerifiers = {};
        for (var i = 0; i < verifiers.length; i++) {
            uniqueVerifiers[verifiers[i]] = true;
        }
        
        assert.assertEquals("All verifiers should be unique", verifiers.length, Object.keys(uniqueVerifiers).length);
        console.log("✓ Thinbus client creation and verifier generation working");
    }
});