package com.enterprise.dependency.codebases;

public class SampleServiceA {
    public void callServiceB() {
        // Simulate a call to ServiceB
        SampleServiceB serviceB = new SampleServiceB();
        serviceB.process();
    }
    public void process() {
        // ...
    }
}
