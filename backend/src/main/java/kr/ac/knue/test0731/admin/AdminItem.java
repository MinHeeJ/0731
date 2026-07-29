package kr.ac.knue.test0731.admin;

import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;
import java.util.UUID;

public class AdminItem {
    private UUID id;
    @NotBlank(message = "이름은 필수입니다")
    private String name;
    @NotBlank(message = "변경 사유는 필수입니다")
    private String changeReason;
    private String useStatus;
    private String effectiveStatus;
    private String permissionAction;
    private String displayStatus;
    private OffsetDateTime updatedAt;
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }
    public String getUseStatus() { return useStatus; }
    public void setUseStatus(String useStatus) { this.useStatus = useStatus; }
    public String getEffectiveStatus() { return effectiveStatus; }
    public void setEffectiveStatus(String effectiveStatus) { this.effectiveStatus = effectiveStatus; }
    public String getPermissionAction() { return permissionAction; }
    public void setPermissionAction(String permissionAction) { this.permissionAction = permissionAction; }
    public String getDisplayStatus() { return displayStatus; }
    public void setDisplayStatus(String displayStatus) { this.displayStatus = displayStatus; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
