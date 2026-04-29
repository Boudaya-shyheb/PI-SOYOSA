@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
class UserServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
