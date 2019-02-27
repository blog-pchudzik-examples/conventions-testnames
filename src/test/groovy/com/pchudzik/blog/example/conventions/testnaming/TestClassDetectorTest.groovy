package com.pchudzik.blog.example.conventions.testnaming

import com.pchudzik.blog.example.conventions.testnaming.TestNamingConventionTest.TestClassDetector
import org.junit.Test
import org.junit.runners.Suite
import spock.lang.Specification
import spock.lang.Unroll

class TestClassDetectorTest extends Specification {
    def "detects specification class"() {
        expect:
        TestClassDetector.isTestClass(TestClassDetectorTest.class)
    }

    @Unroll
    def "detects junit tests"() {
        expect:
        TestClassDetector.isTestClass(testClass)

        where:
        testClass << [
                SimpleJunitTest,
                InheritsTest,
                SuiteTest
        ]
    }

    @Suite.SuiteClasses([SimpleJunitTest])
    private static class SuiteTest {

    }

    private static class SimpleJunitTest {
        @Test
        void testMethod() {
        }
    }

    private static class InheritsTest extends SimpleJunitTest {
    }
}
