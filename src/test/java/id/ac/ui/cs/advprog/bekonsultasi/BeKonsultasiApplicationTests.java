package id.ac.ui.cs.advprog.bekonsultasi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BeKonsultasiApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        // Verify that the Spring application context loads successfully
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void testMainMethod() {
        BeKonsultasiApplication.main(new String[]{});
        assertThat(true).isTrue();
    }
}