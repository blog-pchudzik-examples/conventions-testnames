package com.pchudzik.blog.example.conventions.testnaming.classpathscanner

class ClassWithBrokenStaticInitializerBlock {
    static {
        new FileReader("nonexistingfile.txt").read()
    }
}
