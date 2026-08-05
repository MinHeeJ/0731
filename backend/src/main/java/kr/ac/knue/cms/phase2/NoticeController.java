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
public class NoticeController {
    private final NoticeService service;

    public NoticeController(NoticeService service) {
        this.service = service;
    }

    @GetMapping("/api/notices")
    public ApiResponse<List<Map<String, Object>>> list(@RequestParam Map<String, Object> filters) {
        return ApiResponse.ok(service.list(filters));
    }

    @GetMapping("/api/notices/{noticeId}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable long noticeId) {
        return ApiResponse.ok(service.detail(noticeId));
    }

    @PostMapping("/api/notices")
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok(service.create(body));
    }

    @PutMapping("/api/notices/{noticeId}")
    public ApiResponse<Map<String, Object>> update(@PathVariable long noticeId, @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(service.update(noticeId, body));
    }
}
