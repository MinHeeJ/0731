package kr.ac.knue.cms.phase2;

import kr.ac.knue.cms.auth.SessionContext;
import kr.ac.knue.cms.common.AdminMapper;
import kr.ac.knue.cms.common.BusinessException;
import kr.ac.knue.cms.common.ChangeHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class NoticeService {
    private final AdminMapper mapper;
    private final ChangeHistoryService history;

    public NoticeService(AdminMapper mapper, ChangeHistoryService history) {
        this.mapper = mapper;
        this.history = history;
    }

    public List<Map<String, Object>> list(Map<String, Object> filters) {
        return mapper.listNotices(filters, SessionContext.currentUserId());
    }

    public Map<String, Object> detail(long noticeId) {
        Map<String, Object> notice = mapper.getNotice(noticeId);
        if (notice == null) throw new BusinessException(404, "공지사항을 찾을 수 없습니다.", Map.of("noticeId", "존재하지 않습니다."));
        notice.put("attachments", mapper.listNoticeAttachments(noticeId));
        return notice;
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> body) {
        validate(body);
        mapper.insertNotice(body, SessionContext.currentUserId());
        long noticeId = Long.parseLong(String.valueOf(body.get("noticeId")));
        replaceAttachments(noticeId, body);
        history.record("notice", String.valueOf(noticeId), "CREATE", null, body.toString(), SessionContext.currentUserId(), Phase2Validation.text(body, "changeReason"));
        return detail(noticeId);
    }

    @Transactional
    public Map<String, Object> update(long noticeId, Map<String, Object> body) {
        validate(body);
        int updated = mapper.updateNotice(noticeId, body);
        if (updated == 0) throw new BusinessException(400, "존재하지 않는 공지사항입니다.", Map.of("noticeId", "존재하지 않는 공지사항입니다."));
        replaceAttachments(noticeId, body);
        history.record("notice", String.valueOf(noticeId), "UPDATE", null, body.toString(), SessionContext.currentUserId(), Phase2Validation.text(body, "changeReason"));
        return detail(noticeId);
    }

    @SuppressWarnings("unchecked")
    private void replaceAttachments(long noticeId, Map<String, Object> body) {
        mapper.deleteNoticeAttachments(noticeId);
        Object raw = body.get("attachments");
        if (raw instanceof List<?> list) {
            for (Object item : list) if (item instanceof Map<?, ?> map) mapper.insertNoticeAttachment(noticeId, (Map<String, Object>) map);
        } else if (!Phase2Validation.text(body, "attachmentFileName").isBlank()) {
            mapper.insertNoticeAttachment(noticeId, Map.of(
                "originalFileName", Phase2Validation.text(body, "attachmentFileName"),
                "storedFilePath", Phase2Validation.text(body, "attachmentPath").isBlank() ? "/notice/metadata-only" : Phase2Validation.text(body, "attachmentPath"),
                "fileSize", Phase2Validation.text(body, "attachmentSize").isBlank() ? "0" : Phase2Validation.text(body, "attachmentSize")
            ));
        }
    }

    private void validate(Map<String, Object> body) {
        Phase2Validation.require(body, "title", "content", "postingStartDate", "postingEndDate");
        Phase2Validation.period(body, "postingStartDate", "postingEndDate");
        String roleCode = Phase2Validation.text(body, "targetRoleCode");
        if (!roleCode.isBlank() && mapper.countRole(roleCode) == 0) {
            throw new BusinessException(400, "공지 대상을 확인해 주세요.", Map.of("targetRoleCode", "존재하지 않는 역할코드입니다."));
        }
        String orgCode = Phase2Validation.text(body, "targetOrganizationCode");
        if (!orgCode.isBlank() && mapper.countOrganization(orgCode) == 0) {
            throw new BusinessException(400, "공지 대상을 확인해 주세요.", Map.of("targetOrganizationCode", "존재하지 않는 조직입니다."));
        }
    }
}
