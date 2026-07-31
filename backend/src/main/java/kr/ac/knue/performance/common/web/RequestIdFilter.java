package kr.ac.knue.performance.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestIdFilter extends OncePerRequestFilter {
  public static final String ATTRIBUTE = "requestId";
  private static final Pattern SAFE_REQUEST_ID = Pattern.compile("\\A[A-Za-z0-9._~-]{1,64}\\z");

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
    String requestId = firstSafeRequestId(request);
    request.setAttribute(ATTRIBUTE, requestId);
    response.setHeader("X-Request-Id", requestId);
    chain.doFilter(request, response);
  }

  private static String firstSafeRequestId(HttpServletRequest request) {
    Enumeration<String> values = request.getHeaders("X-Request-Id");
    if (values != null && values.hasMoreElements()) {
      String candidate = values.nextElement();
      if (candidate != null && SAFE_REQUEST_ID.matcher(candidate).matches()) {
        return candidate;
      }
    }
    return UUID.randomUUID().toString();
  }
}
