import com.intuit.karate.junit5.Karate;

public class KarateTest {

    @Karate.Test
    Karate testAll() {
        return Karate.run("classpath:karate").relativeTo(getClass());
    }
}