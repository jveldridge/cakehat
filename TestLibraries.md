# Test Libraries #
cakehat makes use of a few libraries for testing. These libraries are not actually part of cakehat proper. They can only be referenced from within test code. When a release is built the test libraries are not included.

## EasyMock ##
http://easymock.org/

`easymock-3.0.jar` {NOT LATEST}

EasyMock allows for mocking objects. Mocking an object means to provided dummy implementations of methods. This functionality is used frequently in tests to mock the functionality of utilities and services. EasyMock is extensive, and has a bit of a learning curve but the website provides very good documentation.

## Code Generation Library (cglib) ##
http://cglib.sourceforge.net/

`cglib-nodep-2.2.jar` {NOT LATEST}

cglib is a dependency of EasyMock.

## Objenesis ##
http://objenesis.googlecode.com/svn/docs/index.html

`objenesis-1.2.jar`

Objenesis is a dependency of EasyMock.

## JUnit ##
http://www.junit.org/

`junit-4.8.2.jar` {NOT LATEST}

JUnit is the testing framework used by cakehat to write unit tests. It integrates very nicely with Netbeans. (Note: Netbeans can provide a built-in JUnit dependency, but this is intentionally not being used so that all developers of cakehat use the exact same version of JUnit instead of relying on a version provided by Netbeans.)