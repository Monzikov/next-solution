package edu.mai.nextsolution;

import org.springframework.beans.factory.annotation.Qualifier;
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

    private final StopListChecker labseChecker;
    private final StopListChecker bgeChecker;

    public SearchController(@Qualifier("labseChecker") StopListChecker labseChecker,
                            @Qualifier("bgeChecker") StopListChecker bgeChecker) {
        this.labseChecker = labseChecker;
        this.bgeChecker = bgeChecker;
    }

    @PostMapping("/search-labse")
    public ResponseEntity<CheckResult> searchLabse(@RequestBody SearchRequest request) {
        return ResponseEntity.ok(labseChecker.checkClient(request));
    }

    @PostMapping("/search-bge")
    public ResponseEntity<CheckResult> searchBge(@RequestBody SearchRequest request) {
        return ResponseEntity.ok(bgeChecker.checkClient(request));
    }
}
