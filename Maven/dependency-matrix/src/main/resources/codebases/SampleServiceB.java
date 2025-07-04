package com.enterprise.dependency.codebases;

public class SampleServiceB {
    public void process() {
        // ...
    }
    public void callServiceC() {
        // Simulate a call to ServiceC
        SampleServiceC serviceC = new SampleServiceC();
        serviceC.handle();
    }
}
