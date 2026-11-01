package org.apache.maven.archetypes.hotelflow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TestRunner {

    public static void main(String[] args) {
        Class<?>[] testClasses = {
                BookingConflictTest.class,
                DataStoreSyncTest.class,
                ReportServiceTest.class
        };

        int totalTests = 0;
        int passedTests = 0;
        int failedTests = 0;

        System.out.println("=================================================");
        System.out.println("   HotelFlow Automated Unit & Business Rule Tests");
        System.out.println("=================================================");

        for (Class<?> testClass : testClasses) {
            DisplayName classDisplayName = testClass.getAnnotation(DisplayName.class);
            String className = classDisplayName != null ? classDisplayName.value() : testClass.getSimpleName();
            System.out.println("\n[SUITE] " + className);

            List<Method> beforeMethods = new ArrayList<>();
            List<Method> testMethods = new ArrayList<>();

            for (Method m : testClass.getDeclaredMethods()) {
                if (m.isAnnotationPresent(BeforeEach.class)) {
                    beforeMethods.add(m);
                }
                if (m.isAnnotationPresent(Test.class)) {
                    testMethods.add(m);
                }
            }

            for (Method testMethod : testMethods) {
                totalTests++;
                DisplayName methodDisplayName = testMethod.getAnnotation(DisplayName.class);
                String testName = methodDisplayName != null ? methodDisplayName.value() : testMethod.getName();

                try {
                    Object instance = testClass.getDeclaredConstructor().newInstance();

                    for (Method b : beforeMethods) {
                        b.setAccessible(true);
                        b.invoke(instance);
                    }

                    testMethod.setAccessible(true);
                    testMethod.invoke(instance);

                    passedTests++;
                    System.out.println("  PASS: " + testName);
                } catch (Throwable t) {
                    failedTests++;
                    Throwable cause = t.getCause() != null ? t.getCause() : t;
                    System.err.println("  FAIL: " + testName);
                    System.err.println("        Error: " + cause.getMessage());
                }
            }
        }

        System.out.println("\n=================================================");
        System.out.printf("Results: Total: %d, Passed: %d, Failed: %d\n", totalTests, passedTests, failedTests);
        System.out.println("=================================================");

        if (failedTests > 0) {
            System.exit(1);
        }
    }
}
