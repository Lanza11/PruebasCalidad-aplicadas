
import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.Test;

public class KarateTest {
    @Karate.Test
    Karate testAll() {
        return Karate.run("classpath:karate").relativeTo(getClass());
    }
}
