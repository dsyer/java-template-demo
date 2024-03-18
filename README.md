## Reflectionless Templates With Spring

A few Java libraries have shown up recently that use text templates, but compile to Java classes at build time. They can thus claim to some extent to be "reflection free". Together with potential benefits of runtime performance, they promise to be easy to use and integrate with GraalVM native image compilation, so they are quite interesting for people just getting started with that stack in Spring Boot 3.x. We take a look at a selection of libraries (Rocker, JTE and JStachio) and how to get them running with Spring Boot and GraalVM. 
