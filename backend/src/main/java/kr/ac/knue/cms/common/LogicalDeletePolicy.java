package kr.ac.knue.cms.common;

import org.springframework.stereotype.Component;

@Component
public class LogicalDeletePolicy {
    public String inactiveStatus() { return "INACTIVE"; }
    public String disabledUseYn() { return "N"; }
}
