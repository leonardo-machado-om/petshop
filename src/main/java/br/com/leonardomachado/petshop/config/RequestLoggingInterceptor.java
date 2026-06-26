package br.com.leonardomachado.petshop.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final String INICIO_REQUISICAO = "INICIO_REQUISICAO";
    private static final String REQUEST_ID = "REQUEST_ID";

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {

        String requestId = UUID.randomUUID().toString();

        request.setAttribute(INICIO_REQUISICAO, System.nanoTime());
        request.setAttribute(REQUEST_ID, requestId);

        log.info(
                "Iniciando requisição. requestId={}, request={}",
                requestId,
                montarRequest(request));

        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception exception) {

        Long inicio = (Long) request.getAttribute(INICIO_REQUISICAO);
        String requestId = (String) request.getAttribute(REQUEST_ID);

        long duracaoMs = inicio == null
                ? 0
                : (System.nanoTime() - inicio) / 1_000_000;

        log.info(
                "Finalizando requisição. requestId={}, request={}, status={}, duracaoMs={}",
                requestId,
                montarRequest(request),
                response.getStatus(),
                duracaoMs);
    }

    private String montarRequest(HttpServletRequest request) {
        String queryString = request.getQueryString();

        if (queryString == null || queryString.isBlank()) {
            return request.getMethod() + " " + request.getRequestURI();
        }

        return request.getMethod()
                + " "
                + request.getRequestURI()
                + "?"
                + queryString;
    }
}