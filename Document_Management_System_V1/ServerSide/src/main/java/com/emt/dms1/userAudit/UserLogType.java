package com.emt.dms1.userAudit;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserLogType {
    Logged_in_successfully,
    Failed_login_attempt,
    Access_denied,
    Rolechanged
}
