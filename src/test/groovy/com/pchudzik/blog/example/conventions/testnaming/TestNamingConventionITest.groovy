package com.pchudzik.blog.example.conventions.testnaming

import groovy.transform.TupleConstructor
import org.junit.Test
import org.junit.runners.Suite
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.util.ReflectionUtils
import spock.lang.Specification

import java.util.logging.Logger

class TestNamingConventionITest extends Specification {
    private static final testNamesConventions = [/.*Test$/, /.*ITest$/]

    def "tests names match convention"() {
        given:
        final allTestClasses = new TestClassPathScanner(packages: ["com.pchudzik"] as Set)
                .findAllTestClasses()
                .findAll { TestClassDetector.isTestClass(it) }

        when:
        final classesNotMatchingConventionTest = allTestClasses.findAll { breaksTestConventionName(it) }

        then:
        classesNotMatchingConventionTest.isEmpty()
    }

    private boolean breaksTestConventionName(Class clazz) {
        final clazzName = clazz.simpleName
        return !testNamesConventions.any { clazzName.matches(it) }
    }

    static class TestClassDetector {
        static isTestClass(Class clazz) {
            return Specification.class.isAssignableFrom(clazz) || isJunitTest(clazz)
        }

        private static boolean isJunitTest(Class clazz) {
            return hasAnyTestMethods(clazz) || isJunitTestSuite(clazz)
        }

        private static boolean isJunitTestSuite(Class clazz) {
            return Objects.nonNull(AnnotationUtils.findAnnotation(clazz, Suite.SuiteClasses.class))
        }

        private static boolean hasAnyTestMethods(Class clazz) {
            return Objects.nonNull(ReflectionUtils
                    .getAllDeclaredMethods(clazz)
                    .find { AnnotationUtils.findAnnotation(it, Test.class) != null })
        }
    }

    @TupleConstructor
    static class TestClassPathScanner {
        private static final log = Logger.getLogger(TestClassPathScanner.class.getName())
        private Collection<String> packages = []

        public Set<Class> findAllTestClasses() {
            return packages
                    .collect { findALlClasses(it) }
                    .flatten()
                    .toSet()
        }

        private Collection<Class> findALlClasses(String packageName) {
            final classLoader = Thread.currentThread().getContextClassLoader()
            return classLoader
                    .getResources(packageName.replace(".", File.separator)).toList()
                    .collect { new File(it.getFile()) }
                    .findAll { it.getAbsolutePath() ==~ /.*${File.separator}test.+classes${File.separator}.*/ }
                    .collect { findAllClasses(it, packageName) }
                    .flatten()
                    .toSet()
        }

        private Set<Class> findAllClasses(File directory, String packageName) {
            if (!directory.exists()) {
                return []
            }

            def result = []
            for (def file in directory.listFiles()) {
                if (file.isDirectory()) {
                    result += findAllClasses(file, packageName + "." + file.getName())
                } else {
                    final clazz = tryToLoadClass(packageName, file.getName())
                    result += clazz ?: []
                }
            }

            return result.toSet()
        }

        private Class tryToLoadClass(String packageName, String fileName) {
            final className = packageName + "." + fileName.replaceAll(/.class$/, "")
            try {
                return Class.forName(className)
            } catch (ClassNotFoundException | LinkageError ex) {
                handleClassInitializationError(fileName, ex)
            }
        }

        private void handleClassInitializationError(String fileName, Throwable ex) {
            //for example when there is an error while initializing static field
            log.warning("Ignoring class ${fileName}. Error while loading class ${ex.message}")
        }
    }
}
