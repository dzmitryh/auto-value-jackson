# auto-value-jackson

That extension is adding `JacksonProperty` to every getter in `AutoValue` class
as well as adding `JsonCreator` to constructor method.

Example:

Before:

```java
package test;
import com.google.auto.value.AutoValue;
import com.dzmitryh.auto.value.jackson.AutoJackson;

@AutoJackson 
@AutoValue 
public abstract class Test {
   public abstract String a();
}
```

After:

```java
package test;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Override;
import java.lang.String;

final class AutoValue_Test extends $AutoValue_Test {
    @JsonCreator
    AutoValue_Test(@JsonProperty("a") String a) {
        super(a);
    }
    
    @JsonProperty
    @Override
    public String a() {
    return super.a();
    }
}
```