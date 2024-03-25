## Reflectionless Templates With Spring

A few Java libraries have shown up recently that use text templates, but compile to Java classes at build time. They can thus claim to some extent to be "reflection free". Together with potential benefits of runtime performance, they promise to be easy to use and integrate with GraalVM native image compilation, so they are quite interesting for people just getting started with that stack in Spring Boot 3.x. We take a look at a selection of libraries ([JStachio](https://github.com/jstachio/jstachio), [Rocker](https://github.com/fizzed/rocker), [JTE](https://github.com/casid/jte) and [ManTL](https://github.com/manifold-systems/manifold/tree/master/manifold-deps-parent/manifold-templates)) and how to get them running. 

The source code for the samples is in [GitHub](https://github.com/dsyer/java-template-demo) and each template engine has its own branch. The sample is intentionally very simple and doesn't use all the features of the template engines. The focus is on how to integrate them with Spring Boot and GraalVM.

## JStachio

Since it is my favourite, I will start with JStachio. It is very easy to use and has a very small footprint and is also very fast at runtime. The templates are plain text files written in [Mustache](https://mustache.github.io/) which are then compiled to Java classes at build time and rendered at runtime.

In the sample there is a template for the home page (`index.mustache`) that just prints a greeting and a visitor count:

```html
{{<layout}}{{$body}}
Hello {{name}}!
<br>
<br>
You are visitor number {{visits}}.
{{/body}}
{{/layout}}
```

It uses a trivial "layout" template (`layout.mustache`):

```html
<html>
  <head></head>
  <body>{{$body}}{{/body}}
  </body>
</html>
```

(The layout is not strictly necessary but it is a good way to show how to compose templates).

The JStachio APT processor will generate a Java class for each template it finds with a `@JStache` annotation which is used to identify the template file in the source code. In this case we have:

```java
@JStache(path = "index")
public class DemoModel {
	public String name;
	public long visits;

	public DemoModel(String name, long visits) {
		this.name = name;
		this.visits = visits;
	}
}
```

The `path` attribute of the `@JStache` annotation is the name of the template file without the extension (see below for how that gets stitched together). You could also use a Java record for the model which is neat, but since the other template engines don't support it we'll leave it out and make the samples more comparable.

### Build Configuration

To compile this to a Java class, you need to add some configuration to the compiler plugin in `pom.xml`:

```xml
<plugin>
	<artifactId>maven-compiler-plugin</artifactId>
	<configuration>
		<annotationProcessorPaths>
			<annotationProcessorPath>
				<groupId>io.jstach</groupId>
				<artifactId>jstachio-apt</artifactId>
				<version>${jstachio.version}</version>
			</annotationProcessorPath>
		</annotationProcessorPaths>
	</configuration>
</plugin>
```

JStachio comes with some Spring Boot integration, so you only need to add it to the classpath:

```xml
<dependency>
	<groupId>io.jstach</groupId>
	<artifactId>jstachio-spring-boot-starter-webmvc</artifactId>
	<version>${jstachio.version}</version>
</dependency>
```

### Controller

You can use the template in a controller, for example:

```java
@GetMapping("/")
public View view() {
	visitsRepository.add();
	return JStachioModelView.of(new DemoModel("World", visitsRepository.get()));
}
```

This controller returns a `View` constructed from a `DemoModel`. It could also just return the `DemoModel` directly and Spring Boot will wrap it in a `JStachioModelView` automatically.

### JStachio Configuration

There is also global configuration in the `DemoApplication` class:

```java
@JStachePath(prefix = "templates/", suffix = ".mustache")
@SpringBootApplication
public class DemoApplication {
	...
}
```

and a `package-info.java` file that points back to it (you need one of these per Java package that contains `@JStache` models):

```java
@JStacheConfig(using = DemoApplication.class)
package demo;
...
```

### Running the Sample

Run the application with `./mvnw spring-boot:run` (or in the IDE from the `main` method) and you should see the home page at `http://localhost:8080/`.

The generated sources after compilation are in `target/generated-sources/annotations` and you can see the generated Java class for the `DemoModel` there:

```
$ tree target/generated-sources/annotations/
target/generated-sources/annotations/
└── demo
    └── DemoModelRenderer.java
```

The sample also includes a [test main](https://docs.spring.io/spring-boot/docs/3.2.3/maven-plugin/reference/htmlsingle/#run.test-run-goal) so you can run from the command line with `./mvnw spring-boot:test-run` or via the test main in the IDE, and the application will restart when you make changes in the IDE. One of the disadvantages of the build-time compilation is that you have to force a recompile to see changes in the templates. The IDE won't do that automatically, so you might have to use another tool to trigger a recompile. I have had some success with using this to force the model class to recompile when the template changes:

```
$ while inotifywait src/main/resources/templates -e close_write; do \
  sleep 1; \
  find src/main/java -name \*Model.java -exec touch {} \;; \
done
```

The `inotifywait` command is a tool that waits for a file to be closed after a write. It is easy to install and use on any Linux distribution or on a Mac.

### Native Image

A native image can be generated with no additional configuration using `./mvnw -P native spring-boot:build-image` (or using the `native-image` plugin directly). The image starts up in less than 0.1s:

```
$ docker run -p 8080:8080 demo:0.0.1-SNAPSHOT

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.4)

2024-03-22T12:23:45.403Z  INFO 1 --- [           main] demo.DemoApplication                     : Starting AOT-processed DemoApplication using Java 17.0.10 with PID 1 (/workspace/demo.DemoApplication started by cnb in /workspace)
2024-03-22T12:23:45.403Z  INFO 1 --- [           main] demo.DemoApplication                     : No active profile set, falling back to 1 default profile: "default"
2024-03-22T12:23:45.418Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
2024-03-22T12:23:45.419Z  INFO 1 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2024-03-22T12:23:45.419Z  INFO 1 --- [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.19]
2024-03-22T12:23:45.429Z  INFO 1 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2024-03-22T12:23:45.429Z  INFO 1 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 26 ms
2024-03-22T12:23:45.462Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path ''
2024-03-22T12:23:45.462Z  INFO 1 --- [           main] demo.DemoApplication                     : Started DemoApplication in 0.069 seconds (process running for 0.073)
```

## Rocker

Rocker can be used in a similar way to JStachio. The templates are written in a custom language that is like HTML with additional Java features (a bit like JSP). The home page looks like this (`demo.rocker.html`):

```html
@import demo.DemoModel

@args(DemoModel model)

@templates.layout.template("Demo") -> {
	<h1>Demo</h1>
	<p>Hello @model.name!</p>
	<br>
	<br>
	<p>You are visitor number @model.visits.</p>
}
```

It imports the `DemoModel` object - the implementation is identical to the JStachio sample. The template also refers directly to its layout (calling a static method on `templates.layout`). The layout is a separate template file (`layout.rocker.html`):

```html
@args (String title, RockerBody content)

<html>
    <head>
        <title>@title</title>
    </head>
    <body>
    @content
    </body>
</html>
```

### Build Configuration

Rocker needs an APT processor and some manual addition of the generated sources to the build input. It can all be configured in the `pom.xml`:

```xml
<plugin>
	<groupId>com.fizzed</groupId>
	<artifactId>rocker-maven-plugin</artifactId>
	<version>1.2.1</version>
	<executions>
		<execution>
			<?m2e execute onConfiguration,onIncremental?>
			<id>generate-rocker-templates</id>
			<phase>generate-sources</phase>
			<goals>
				<goal>generate</goal>
			</goals>
			<configuration>
				<javaVersion>${java.version}</javaVersion>
				<templateDirectory>src/main/resources</templateDirectory>
				<outputDirectory>target/generated-sources/rocker</outputDirectory>
				<discardLogicWhitespace>true</discardLogicWhitespace>
				<targetCharset>UTF-8</targetCharset>
				<postProcessing>
					<param>com.fizzed.rocker.processor.LoggingProcessor</param>
					<param>com.fizzed.rocker.processor.WhitespaceRemovalProcessor</param>
				</postProcessing>
			</configuration>
		</execution>
	</executions>
</plugin>
<plugin>
	<groupId>org.codehaus.mojo</groupId>
	<artifactId>build-helper-maven-plugin</artifactId>
	<executions>
		<execution>
			<phase>generate-sources</phase>
			<goals>
				<goal>add-source</goal>
			</goals>
			<configuration>
				<sources>
					<source>${project.build.directory}/generated-sources/rocker</source>
				</sources>
			</configuration>
		</execution>
	</executions>
</plugin>
```

### Controller

The controller implementation is very conventional - it constructs a model and returns the name of the "demo" view:

```java
@GetMapping("/")
public String view(Model model) {
	visitsRepository.add();
	model.addAttribute("arguments", Map.of("model", new DemoModel("mystérieux visiteur", visitsRepository.get())));
	return "demo";
}
```

We are using a naming convention for "arguments" as a special model attribute. This is a detail of the `View` implementation that we will see later.

### Rocker Configuration

Rocker doesn't come with its own Spring Boot integration but it's not hard to implement, and you only have to do it once. The sample contains a `View` implementation, plus a `ViewResolver` and some configuration in `RockerAutoConfiguration`:

```java
@Configuration
public class RockerAutoConfiguration {
	@Bean
	public ViewResolver rockerViewResolver() {
		return new RockerViewResolver();
	}
}
```

The `RockerViewResolver` is a `ViewResolver` that uses the Rocker template engine to render the templates. The `View` implementation is a wrapper around the Rocker template class:

```java
public class RockerViewResolver implements ViewResolver, Ordered {

	private String prefix = "templates/";
	private String suffix = ".rocker.html";

	@Override
	@Nullable
	public View resolveViewName(String viewName, Locale locale) throws Exception {
		RockerView view = new RockerView(prefix + viewName + suffix);
		return view;
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 10;
	}

}
```

If you look at the implementation of `RockerView` you will see that it is a wrapper around the Rocker template class, and that it contains some reflection code to find the template parameter names. This could be a problem for the native image, so it is not ideal, but we will see how to fix it later. Rocker internally also uses reflection to bind the template parameters to the model, so it is not completely reflection free anyway.

### Running the Sample

If you run the sample with `./mvnw spring-boot:run` you will see the home page at `http://localhost:8080/`. The generated source code comes out as one Java class per template in `target/generated-sources/rocker/`: 

```
$ tree target/generated-sources/rocker/
target/generated-sources/rocker/
└── templates
    ├── demo.java
    └── layout.java
```

### Native Image

The native image would need some additional configuration to permit the reflection during rendering. We had a few attempts at this and it quickly became apparent that reflection is used all over the internals of Rocker and it would be a lot of effort to get it to work with GraalVM. Maybe worth coming back to one day.

## JTE

(The JTE sample is a direct copy from the project documentation. The other samples in this document only have the structure they do because they mirror this one.)

Like Rocker, JTE has a template language similar to HTML with additional Java features. The templates in the project documentation are in a `jte` directory alongside `java`, so we adopt the same convention. The home page looks like this (`demo.jte`):

```java
@import demo.DemoModel

@param DemoModel model

Hello ${model.name}!
<br>
<br>
You are visitor number ${model.visits}.
```

There is no layout template in this sample because JTE doesn't explicitly support composition of templates. The `DemoModel` is similar to the one we used for the other samples.

### Build Configuration

In the `pom.xml` you need to add the JTE compiler plugin:

```xml
<plugin>
	<groupId>gg.jte</groupId>
	<artifactId>jte-maven-plugin</artifactId>
	<version>${jte.version}</version>
	<configuration>
		<sourceDirectory>${basedir}/src/main/jte</sourceDirectory>
		<contentType>Html</contentType>
		<binaryStaticContent>true</binaryStaticContent>
	</configuration>
	<executions>
		<execution>
			<?m2e execute onConfiguration,onIncremental?>
			<phase>generate-sources</phase>
			<goals>
				<goal>generate</goal>
			</goals>
		</execution>
	</executions>
</plugin>
```

along with some source and resource copying:

```xml
<plugin>
	<groupId>org.codehaus.mojo</groupId>
	<artifactId>build-helper-maven-plugin</artifactId>
	<executions>
		<execution>
			<phase>generate-sources</phase>
			<goals>
				<goal>add-source</goal>
			</goals>
			<configuration>
				<sources>
					<source>${project.build.directory}/generated-sources/jte</source>
				</sources>
			</configuration>
		</execution>
	</executions>
</plugin>

<plugin>
	<artifactId>maven-resources-plugin</artifactId>
	<version>3.0.2</version>
	<executions>
		<execution>
			<id>copy-resources</id>
			<phase>process-classes</phase>
			<goals>
				<goal>copy-resources</goal>
			</goals>
			<configuration>
				<outputDirectory>${project.build.outputDirectory}</outputDirectory>
				<resources>
					<resource>
						<directory>${basedir}/target/generated-sources/jte</directory>
						<includes>
							<include>**/*.bin</include>
						</includes>
						<filtering>false</filtering>
					</resource>
				</resources>
			</configuration>
		</execution>
	</executions>
</plugin>
```

The runtime dependencies are:

```xml
<dependency>
	<groupId>gg.jte</groupId>
	<artifactId>jte</artifactId>
	<version>${jte.version}</version>
</dependency>
<dependency>
	<groupId>gg.jte</groupId>
	<artifactId>jte-spring-boot-starter-3</artifactId>
	<version>${jte.version}</version>
</dependency>
```

### Controller

The controller implementation is very conventional - in fact it is identical to the one we used for Rocker.

### JTE Configuration

JTE comes with its own Spring Boot autoconfiguration (we added it in the `pom.xml`), so you almost don't need to do anything else. There is one tiny thing you need to do to make it work with Spring Boot 3.x, which is to add a property to the `application.properties` file. For development time, especially if you are using [Spring Boot Devtools](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#using.devtools), you would want:

```
gg.jte.developmentMode=true
```

In production, switch that off with a Spring profile and use `gg.jte.usePrecompiledTemplates=true` instead.

### Running the Sample

If you run the sample with `./mvnw spring-boot:run` you will see the home page at `http://localhost:8080/`. The generated source code comes out as one Java class per template in `target/generated-sources/jte/`: 

```
$ tree target/generated-sources/jte/
target/generated-sources/jte/
└── gg
    └── jte
        └── generated
            └── precompiled
                ├── JtedemoGenerated.bin
                └── JtedemoGenerated.java
```

The `.bin` file is an efficient binary representation of the text template that is used at runtime, so it needs to be added to the classpath.

### Native Image

A native image can be generated with some additional configuration. We need to make sure the `.bin` files are available and also that the generated Java classes can be reflected on:

```java
@SpringBootApplication
@ImportRuntimeHints(DemoRuntimeHints.class)
public class DemoApplication {
	...
}

class DemoRuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
		hints.resources().registerPattern("**/*.bin");
		hints.reflection().registerType(JtedemoGenerated.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS);
	}
	
}
```

So JTE is not completely reflection free, but it is possible to configure it quite easily to work with GraalVM native.

## ManTL

ManTL (Manifold Template Language) is another template engine with Java-like syntax. The templates are compiled to Java classes at build time like with the other samples. The home page looks like this (`Demo.html.mtl`):

```html
<%@ import demo.DemoModel %>

<%@ params(DemoModel model) %>

Hello ${model.name}!
<br>
<br>
You are visitor number ${model.visits}.
```

where `DemoModel` is the same as in the other samples.

### Build Configuration

Manifold is a bit different to the other examples in that it uses a JDK compiler plugin, as opposed to an APT processor. The configuration in `pom.xml` is a bit more complex. There is the `maven-compiler-plugin`:

```xml
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-compiler-plugin</artifactId>
	<version>3.8.0</version>
	<configuration>
		<compilerArgs>
			<arg>-Xplugin:Manifold</arg>
		</compilerArgs>
		<annotationProcessorPaths>
			<path>
				<groupId>systems.manifold</groupId>
				<artifactId>manifold-templates</artifactId>
				<version>${manifold.version}</version>
			</path>
		</annotationProcessorPaths>
	</configuration>
```

and the runtime dependency:

```xml
<dependency>
	<groupId>systems.manifold</groupId>
	<artifactId>manifold-templates-rt</artifactId>
	<version>${manifold.version}</version>
</dependency>
```

### Controller

Our controller in this sample looks more like the JStachio one than the Rocker/JTE one:

```java
@GetMapping("/")
public View view(Model model, HttpServletResponse response) {
	visitsRepository.add();
	return new StringView(() -> Demo.render(new DemoModel("mystérieux visiteur", visitsRepository.get())));
}
```

where `StringView` is a convenience class that wraps the template and renders it:

```java
public class StringView implements View {

	private final Supplier<String> output;

	public StringView(Supplier<String> output) {
		this.output = output;
	}

	@Override
	public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String result = output.get();
		response.setContentType(MediaType.TEXT_HTML_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setContentLength(result.getBytes().length);

		response.getOutputStream().write(result.getBytes());
		response.flushBuffer();
	}
}
```

### Running the Sample

You can build and run the application on the command line using `./mvnw spring-boot:run` and inspect the result on `http://localhost:8080`. The generated source code comes out as a class and ancillary stuff per template:

```
$ tree target/classes/templates/
target/classes/templates/
├── Demo$LayoutOverride.class
├── Demo.class
└── Demo.html.mtl
```

ManTL only works in IntelliJ after installing a special plugin, and not at all in Eclipse or NetBeans or VSCode. You may be able to run the main method from those IDEs, but the code that refers to templates will have compiler errors because the compiler plugin is missing.

### Native Image

The compiler plugin is not supported by GraalVM, so you can't use ManTL with GraalVM native images.

## Summary

All the template engines we looked at here are reflection free in the sense that the templates are compiled to Java classes at build time. They are all easy to use and integrate with Spring, and they all have or can be provided with some kind of Spring Boot autoconfiguration. JStachio is the most lightweight and fastest at runtime, and it has the best support for GraalVM native images. Rocker is also very fast at runtime, but it uses reflection internally and it is not easy to get it to work with GraalVM. JTE is a bit more complex to configure, but it is also very fast at runtime and it is easy to get it to work with GraalVM. ManTL is the most complex to configure and it doesn't work with GraalVM at all. It also only works with IntelliJ as an IDE.

If you would like to see more samples then the each of the template engines has its own documentation, so follow the links above. My own work on JStachio has produced a few additional examples, for example the [Mustache PetClinic](https://github.com/spring-petclinic/spring-petclinic-mustache/tree/jstache), and also a [Todo MVC](https://github.com/dsyer/spring-todo-mvc/tree/jstachio) implementation, originally by [Ollie Drotbohm](https://github.com/odrotbohm) and adapted to various different template engines.

Dave Syer   
London 2024