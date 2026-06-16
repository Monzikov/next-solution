package edu.mai.nextsolution;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
class SearchController {
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkService() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "NextSolution service is working.");
        return ResponseEntity.ok(response);
    }

    private final StopListChecker stopListChecker;

    public SearchController(StopListChecker stopListChecker) {
        this.stopListChecker = stopListChecker;
    }

    @PostMapping("/search")
    public ResponseEntity<CheckResult> search(@RequestBody SearchRequest request) {
        return ResponseEntity.ok(stopListChecker.checkClient(request));
    }
}
