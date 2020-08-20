jpp tm

### why
yes

### how use this
in `build.gradle`:
```groovy
repositories {
    // . . .
    url "https://jitpack.io"
}
// . . .
dependencies {
    // . . .
    modImplementation "com.github.user11681:jpp:master-SNAPSHOT"
}
```

to declare fields:
```java
@Declare(name = "energy", descriptor = "J")
@Declare(name = "water", descriptor = "D")
public interface AnInterface {}
```

to make a getter:
```java
public interface AnInterface {
    @Getter("energy")
    default long getEnergy() {
        return 0;
    }
}
```

to make a setter:
```java
public interface AnInterface {
    @Setter("water")
    default void setWater(double ml) {}
}
```

to make a _s g e t t e r_:
```java
public interface AnInterface
    @Getter("water")
    @Setter("water")
    default double getAndSetWater(double ml) {
        return 0;
    }
}
```
note: order matters. The above example will cause `water` to be loaded first, then set and returned.


### help this no work
open an issue

