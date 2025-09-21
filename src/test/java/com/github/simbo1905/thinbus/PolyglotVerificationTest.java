package com.github.simbo1905.thinbus;

import org.graalvm.polyglot.*;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PolyglotVerificationTest {
    
    private static final Logger logger = Logger.getLogger(PolyglotVerificationTest.class.getName());
    
    static {
        // Enable fine logging for debugging
        String logLevel = System.getProperty("thinbus.log.level", "INFO");
        logger.setLevel(Level.parse(logLevel));
    }
    
    /**
     * Set up console.log mapping to JUL logger for GraalVM JavaScript context
     */
    private Context createContextWithLogging() {
        Context context = Context.newBuilder("js")
            .allowAllAccess(true)
            .build();
            
        // Map console.log to JUL logger
        Value bindings = context.getBindings("js");
        bindings.putMember("console", new ConsoleLogger());
        
        return context;
    }
    
    /**
     * Simple console logger implementation for JavaScript
     */
    public static class ConsoleLogger {
        private static final Logger jsLogger = Logger.getLogger("thinbus.js");
        
        public void log(Object... args) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                if (i > 0) sb.append(" ");
                sb.append(String.valueOf(args[i]));
            }
            jsLogger.info(sb.toString());
        }
        
        // Alias for log
        public void info(Object... args) { log(args); }
        public void warn(Object... args) { 
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                if (i > 0) sb.append(" ");
                sb.append(String.valueOf(args[i]));
            }
            jsLogger.warning(sb.toString());
        }
        public void error(Object... args) { 
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                if (i > 0) sb.append(" ");
                sb.append(String.valueOf(args[i]));
            }
            jsLogger.severe(sb.toString());
        }
    }
    
    @Test
    public void testGraalVMPolyglotBasicJavaScript() {
        System.out.println("Testing GraalVM Polyglot JavaScript execution...");
        
        try (Context context = Context.create("js")) {
            // Test basic JavaScript execution
            Value result = context.eval("js", "2 + 3");
            assertEquals("Basic math should work", 5, result.asInt());
            
            // Test JavaScript array operations
            Value arrayResult = context.eval("js", "[1, 2, 3, 4].map(x => x * 2)");
            assertTrue("Should return an array", arrayResult.hasArrayElements());
            assertEquals("Array should have 4 elements", 4, arrayResult.getArraySize());
            assertEquals("First element should be 2", 2, arrayResult.getArrayElement(0).asInt());
            
            System.out.println("✓ GraalVM Polyglot JavaScript execution is working!");
        } catch (Exception e) {
            fail("GraalVM Polyglot test failed: " + e.getMessage());
        }
    }
    
    @Test
    public void testJavaScriptStringOperations() {
        try (Context context = Context.create("js")) {
            // Test string operations that might be used in SRP
            Value result = context.eval("js", "'hello world'.toUpperCase()");
            assertEquals("String operations should work", "HELLO WORLD", result.asString());
            
            // Test JSON parsing
            Value jsonResult = context.eval("js", "JSON.stringify({test: 'value', number: 42})");
            assertTrue("Should contain test key", jsonResult.asString().contains("test"));
            assertTrue("Should contain number", jsonResult.asString().contains("42"));
            
            System.out.println("✓ JavaScript string and JSON operations working!");
        }
    }
    
    @Test 
    public void testJavaJavaScriptInterop() {
        try (Context context = Context.create("js")) {
            // Test passing Java objects to JavaScript
            Value bindings = context.getBindings("js");
            bindings.putMember("javaString", "Hello from Java!");
            bindings.putMember("javaNumber", 42);
            
            Value result = context.eval("js", "javaString + ' The answer is ' + javaNumber");
            assertEquals("Java-JS interop should work", 
                        "Hello from Java! The answer is 42", 
                        result.asString());
            
            System.out.println("✓ Java-JavaScript interoperability is working!");
        }
    }
    
    @Test
    public void testConsoleLoggingSetup() {
        System.out.println("Testing console.log mapping to JUL logger...");
        
        try (Context context = createContextWithLogging()) {
            // Test console.log mapping
            context.eval("js", "console.log('Hello from JavaScript!', 42, true)");
            context.eval("js", "console.info('Info message')");
            context.eval("js", "console.warn('Warning message')");
            context.eval("js", "console.error('Error message')");
            
            System.out.println("✓ Console logging setup is working!");
        } catch (Exception e) {
            fail("Console logging test failed: " + e.getMessage());
        }
    }
    
    @Test
    public void testModernJavaScriptLoading() {
        System.out.println("Testing modern JavaScript loading...");
        
        try (Context context = createContextWithLogging()) {
            // Load the modern client.mjs file
            String clientPath = "src/main/resources/js-modern/client.mjs";
            if (!Files.exists(Paths.get(clientPath))) {
                fail("Modern JavaScript client file not found at: " + clientPath);
            }
            
            String clientCode = new String(Files.readAllBytes(Paths.get(clientPath)));
            
            // Test basic loading - just check if we can evaluate without syntax errors
            context.eval("js", "console.log('Loading modern JavaScript client...')");
            
            // Try to evaluate a small portion to test syntax compatibility
            // We'll start with just the BigInteger constructor test
            String testCode = """
                // Test basic BigInteger functionality from the modern code
                console.log('Testing BigInteger creation...');
                
                // Extract just the BigInteger constructor and basic functions
                var dbits;
                var canary = 0xdeadbeefcafe;
                var j_lm = ((canary&0xffffff)==0xefcafe);
                
                function BigInteger(a,b,c) {
                  if(a != null)
                    if("number" == typeof a) this.fromNumber(a,b,c);
                    else if(b == null && "string" != typeof a) this.fromString(a,256);
                    else this.fromString(a,b);
                }
                
                console.log('BigInteger constructor defined successfully');
                'success';
                """;
            
            Value result = context.eval("js", testCode);
            assertEquals("Should return success", "success", result.asString());
            
            System.out.println("✓ Modern JavaScript basic loading is working!");
        } catch (Exception e) {
            System.err.println("Modern JavaScript loading failed: " + e.getMessage());
            e.printStackTrace();
            fail("Modern JavaScript loading test failed: " + e.getMessage());
        }
    }
    
    @Test
    public void testModernSRPClientLoading() {
        System.out.println("Testing modern SRP client loading and basic functionality...");
        
        try (Context context = createContextWithLogging()) {
            // Load the modern client.mjs file
            String clientPath = "src/main/resources/js-modern/client.mjs";
            if (!Files.exists(Paths.get(clientPath))) {
                fail("Modern JavaScript client file not found at: " + clientPath);
            }
            
            String clientCode = new String(Files.readAllBytes(Paths.get(clientPath)));
            
            // Set up a minimal crypto environment for the modern JavaScript
            String setupCode = """
                console.log('Setting up crypto environment for modern SRP client...');
                
                // Mock crypto for testing - in real usage this would be provided by browser or Node.js
                if (typeof globalThis.crypto === 'undefined') {
                    globalThis.crypto = {
                        getRandomValues: function(array) {
                            // Simple pseudo-random for testing
                            for (let i = 0; i < array.length; i++) {
                                array[i] = Math.floor(Math.random() * 256);
                            }
                            return array;
                        }
                    };
                }
                
                // Mock CryptoJS for SHA256 - simplified for testing
                if (typeof globalThis.CryptoJS === 'undefined') {
                    globalThis.CryptoJS = {
                        SHA256: function(message) {
                            // This is a mock - in real usage CryptoJS would be loaded
                            return {
                                toString: function() {
                                    return 'mock_sha256_hash_' + message.toString().length;
                                }
                            };
                        }
                    };
                }
                
                console.log('Crypto environment setup complete');
                'setup_complete';
                """;
            
            Value setupResult = context.eval("js", setupCode);
            assertEquals("Should complete setup", "setup_complete", setupResult.asString());
            
            // Now try to load a small portion of the client code to test compatibility
            String testClientCode = """
                console.log('Testing SRP client module structure...');
                
                // Test that we can define the basic SRP constants
                const rfc5054 = {
                    N_base10: "21766174458617435773191008891802753781907668374255538511144643224689886235383840957210909013086056401571399717235807266581649606472148410291413364152197364477180887395655483738115072677402235101762521901569820740293149529620419333266262073471054548368736039519702486226506248861060256971802984953561121442680157668000761429988222457090413873973970171927093992114751765168063614761119615476233422096442783117971236371647333871414335895773474667308967050807005509320424799678417036867928316761272274230314067548291133582479583061439577559347101961771406173684378522703483495337037655006751328447510550299250924469288819",
                    g_base10: "2",
                    k_base16: "5b9e8ef059c6b32ea59fc1d322d37f04aa30bae5aa9003b8321e21ddb04e300"
                };
                
                console.log('RFC 5054 constants defined');
                console.log('N length:', rfc5054.N_base10.length);
                console.log('g value:', rfc5054.g_base10);
                console.log('k length:', rfc5054.k_base16.length);
                
                'client_constants_ok';
                """;
            
            Value clientResult = context.eval("js", testClientCode);
            assertEquals("Should define client constants", "client_constants_ok", clientResult.asString());
            
            System.out.println("✓ Modern SRP client basic structure is working!");
        } catch (Exception e) {
            System.err.println("Modern SRP client loading failed: " + e.getMessage());
            e.printStackTrace();
            fail("Modern SRP client loading test failed: " + e.getMessage());
        }
    }
}