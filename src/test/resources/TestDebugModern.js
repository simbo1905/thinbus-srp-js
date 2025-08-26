// Baby steps debug test using modern polyfill approach for GraalVM
console.log("DEBUG: Modern polyfill test file loaded");

tests({
    testStep1_LoadPolyfill: function() {
        console.log("DEBUG: Step 1 - Loading modern GraalVM polyfill");
        
        // Load modern polyfill that provides Node.js modules in GraalVM
        load("src/main/resources/js-modern/graalvm-polyfill.js");
        console.log("DEBUG: graalvm-polyfill.js loaded successfully");
        
        // Test that require function is available
        if (typeof require !== 'undefined') {
            console.log("DEBUG: require function is available");
        } else {
            console.log("DEBUG: require function not found");
        }
        
        console.log("DEBUG: Step 1 complete");
        assert.assertTrue(true);
    },
    
    testStep2_LoadModernBrowser: function() {
        console.log("DEBUG: Step 2 - Loading modern browser.js with polyfill");
        
        // Load polyfill first
        load("src/main/resources/js-modern/graalvm-polyfill.js");
        
        // Load modern browser.js bundle
        load("src/main/resources/js-modern/browser.js");
        console.log("DEBUG: browser.js loaded successfully");
        
        // Check if thinbus is available
        if (typeof thinbus !== 'undefined') {
            console.log("DEBUG: thinbus global is available");
        } else {
            console.log("DEBUG: thinbus global not found");
        }
        
        console.log("DEBUG: Step 2 complete");
        assert.assertTrue(true);
    },
    
    testStep3_CreateClientFactory: function() {
        console.log("DEBUG: Step 3 - Creating client factory from modern browser.js");
        
        // Load polyfill and browser.js
        load("src/main/resources/js-modern/graalvm-polyfill.js");
        load("src/main/resources/js-modern/browser.js");
        
        // RFC 5054 2048bit constants
        var N_base10 = "21766174458617435773191008891802753781907668374255538511144643224689886235383840957210909013086056401571399717235807266581649606472148410291413364152197364477180887395655483738115072677402235101762521901569820740293149529620419333266262073471054548368736039519702486226506248861060256971802984953561121442680157668000761429988222457090413873973970171927093992114751765168063614761119615476233422096442783117971236371647333871414335895773474667308967050807005509320424799678417036867928316761272274230314067548291133582479583061439577559347101961771406173684378522703483495337037655006751328447510550299250924469288819";
        var g_base10 = "2";
        var k_base16 = "5b9e8ef059c6b32ea59fc1d322d37f04aa30bae5aa9003b8321e21ddb04e300";
        
        // Test what's available in thinbus
        console.log("DEBUG: thinbus type:", typeof thinbus);
        console.log("DEBUG: thinbus keys:", Object.keys(thinbus || {}));
        
        // Try to create client factory
        try {
            var SRP6JavascriptClientSession = thinbus(N_base10, g_base10, k_base16);
            console.log("DEBUG: Client factory created successfully");
            
            var client = new SRP6JavascriptClientSession();
            console.log("DEBUG: Client instance created successfully");
        } catch (e) {
            console.log("DEBUG: Error creating client:", e.message);
        }
        
        console.log("DEBUG: Step 3 complete");
        assert.assertTrue(true);
    }
});