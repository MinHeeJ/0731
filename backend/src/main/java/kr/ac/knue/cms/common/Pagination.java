package kr.ac.knue.cms.common;

public record Pagination(int page, int size, long totalElements, int totalPages) {
    public static Pagination of(int page, int size, long total) {
        int pages = size <= 0 ? 0 : (int)Math.ceil((double)total / size);
        return new Pagination(page, size, total, pages);
    }
}
