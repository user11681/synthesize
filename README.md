jpp™: a collection of annotations for succinct code and boilerplate reduction.

### Why?
Because I like concision, multiple inheritance and ASM and find the Java language lacking.<br>
jpp is an inexorable consequence of this combination.

### Gradle setup
`build.gradle`:
```groovy
repositories {
    // . . .
    maven {url = "https://dl.bintray.com/user11681/maven"}
}
// . . .
dependencies {
    // . . .
    modApi("user11681:jpp:+")
}
```
### Annotations
The follwing annotations should work just as well with classes as with interfaces.

#### Declaring fields
```java
@Var(name = "energy", descriptor = "J")
@Var(name = "water", descriptor = "D")
public interface AnInterface {}
```

#### Declaring getters
```java
public interface AnInterface {
    @Getter("energy")
    default long getEnergy() {
        return 0;
    }
}
```

#### Declaring setters
```java
public interface AnInterface {
    @Setter("water")
    default void setWater(double ml) {}
}
```

#### Declaring *g s e t t e r s*
```java
public interface AnInterface {
    @Getter("water")
    @Setter("water")
    default double getAndSetWater(double ml) {
        return 0;
    }
}
```
Order matters. The above example will cause `water` to be loaded first, then set and returned.<br>

In the following example method, `water` will be set and then retrieved.
#### Declaring *s g e t t e r s*
```java
public class AClass implements AnInterface {
    @Setter("water")
    @Getter("water")
    public native double setAndGetWater(double ml);
}
```

### This is broken
Open an issue or ping `auoeke tjmnkrajyej#0633` in Fabric's Discord server.

