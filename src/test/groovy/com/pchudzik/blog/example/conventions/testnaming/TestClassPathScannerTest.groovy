package com.pchudzik.blog.example.conventions.testnaming

import com.pchudzik.blog.example.conventions.testnaming.TestNamingConventionITest.TestClassPathScanner
import com.pchudzik.blog.example.conventions.testnaming.classpathscanner.ClassWithBrokenStaticInitializerBlock
import com.pchudzik.blog.example.conventions.testnaming.classpathscanner.NonTestClass
import com.pchudzik.blog.example.conventions.testnaming.classpathscanner.nested.NestedClass
import spock.lang.Specification

class TestClassPathScannerTest extends Specification {

    def "works when scanning non existing package"() {
        given:
        final scanner = new TestClassPathScanner(packages: ["com.pchudzik.blog.example.conventions.testnaming.classpathscanner.nonexistingpacakge"])

        expect:
        scanner.findAllTestClasses().isEmpty()
    }

    def "finds classes only from test scope"() {
        given:
        final scanner = new TestClassPathScanner(packages: ["com.pchudzik.blog.example.conventions.testnaming.classpathscanner"])

        when:
        final foundClasses = scanner.findAllTestClasses()

        then:
        !foundClasses.contains(NonTestClass)
    }

    def "recursively scans for classes"() {
        given:
        final scanner = new TestClassPathScanner(packages: ["com.pchudzik.blog.example.conventions.testnaming.classpathscanner"])

        when:
        final foundClasses = scanner.findAllTestClasses()

        then:
        foundClasses.contains(NestedClass)
    }

    def "doesnt fail when class loading fails"() {
        given:
        final scanner = new TestClassPathScanner(packages: ["com.pchudzik.blog.example.conventions.testnaming.classpathscanner"])

        when:
        final foundClasses = scanner.findAllTestClasses()

        then:
        !foundClasses.contains(ClassWithBrokenStaticInitializerBlock)
    }
}
