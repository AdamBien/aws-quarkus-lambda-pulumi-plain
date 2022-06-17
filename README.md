# MicroProfile with Quarkus as AWS Lambda deployed with Pulumi for Java

# TL;DR

A Quarkus MicroProfile application:

```java

@Path("hello")
@ApplicationScoped
public class GreetingResource {

    @Inject
    Greeter greeter;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return this.greeter.greetings();
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public void hello(String message) {
        this.greeter.greetings(message);
    }
}
```
...with an additional dependency / [extension](https://quarkus.io/guides/amazon-lambda-http) for AWS REST APIs Gateway:

```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-amazon-lambda-rest</artifactId>
</dependency>
```

## Deploying MicroProfile / Quarkus Application as AWS Lambda with Pulumi and Java

[![Deploying MicroProfile / Quarkus Application as AWS Lambda with Java Pulumi](https://i.ytimg.com/vi/NA0WjIgp4CQ/mqdefault.jpg)](https://www.youtube.com/embed/NA0WjIgp4CQ?rel=0)