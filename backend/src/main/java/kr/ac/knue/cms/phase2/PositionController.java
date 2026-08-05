package kr.ac.knue.cms.phase2;

import kr.ac.knue.cms.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class PositionController {
    private final PositionManagementService service;

    public PositionController(PositionManagementService service) {
        this.service = service;
    }

    @GetMapping("/api/positions")
    public ApiResponse<List<Map<String, Object>>> list(@RequestParam Map<String, Object> filters) {
        return ApiResponse.ok(service.list(filters));
    }

    @PostMapping("/api/positions")
    public ApiResponse<List<Map<String, Object>>> create(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok(service.create(body));
    }

    @PutMapping("/api/positions/{positionId}")
    public ApiResponse<List<Map<String, Object>>> update(@PathVariable long positionId, @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(service.update(positionId, body));
    }
}
