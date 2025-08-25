package com.github.simbo1905.thinbus;

import org.graalvm.polyglot.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class PolyglotVerificationTest {
    
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
}