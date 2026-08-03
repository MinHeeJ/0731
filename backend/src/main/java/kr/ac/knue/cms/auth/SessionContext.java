package kr.ac.knue.cms.auth;

public final class SessionContext {
    private static final ThreadLocal<SessionUser> CURRENT = new ThreadLocal<>();
    private SessionContext() {}
    public static void set(SessionUser user) { CURRENT.set(user); }
    public static SessionUser get() { return CURRENT.get(); }
    public static String currentUserId() { return CURRENT.get() == null ? "system" : CURRENT.get().userId(); }
    public static void clear() { CURRENT.remove(); }
}
