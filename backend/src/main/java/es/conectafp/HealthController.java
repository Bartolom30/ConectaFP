package es.conectafp;

import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final JdbcTemplate jdbc;

    public HealthController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/api/health")
    public Map<String, String> health() {
        Integer result = jdbc.queryForObject("SELECT 1", Integer.class);
        return Map.of(
                "app", "ConectaFP",
                "status", "UP",
                "database", (result != null && result == 1) ? "UP" : "DOWN");
    }
}
