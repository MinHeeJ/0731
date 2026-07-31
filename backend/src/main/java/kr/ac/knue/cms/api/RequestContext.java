package kr.ac.knue.cms.api;

public final class RequestContext {
    private static final ThreadLocal<CurrentUser> CURRENT = new ThreadLocal<>();

    private RequestContext() {}

    public static void set(CurrentUser user) {
        CURRENT.set(user);
    }

    public static CurrentUser require() {
        CurrentUser user = CURRENT.get();
        if (user == null) {
            throw new ApiException(401, "UNAUTHORIZED", "인증이 필요합니다");
        }
        return user;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
