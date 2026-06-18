package com.technote.log;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.technote.log.entity.OperationLogRecord;
import com.technote.log.mapper.OperationLogRecordMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 操作日志切面。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private static final int MAX_TEXT_LENGTH = 4000;

    private static final List<String> SENSITIVE_FIELD_KEYWORDS = List.of(
            "password",
            "token",
            "authorization",
            "secret",
            "credential"
    );

    private final OperationLogRecordMapper operationLogRecordMapper;

    private final ObjectMapper objectMapper;

    @Pointcut("@annotation(operationLog)")
    public void operationLogPointcut(OperationLog operationLog) {
    }

    @Around(value = "operationLogPointcut(operationLog)", argNames = "joinPoint,operationLog")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        Long userIdBeforeOperation = resolveLoginUserId();
        try {
            Object result = joinPoint.proceed();
            saveOperationLog(joinPoint, operationLog, userIdBeforeOperation, true, null);
            return result;
        } catch (Throwable ex) {
            saveOperationLog(joinPoint, operationLog, userIdBeforeOperation, false, ex.getMessage());
            throw ex;
        }
    }

    private void saveOperationLog(ProceedingJoinPoint joinPoint,
                                  OperationLog operationLog,
                                  Long userIdBeforeOperation,
                                  boolean success,
                                  String errorMessage) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes == null ? null : attributes.getRequest();
            OperationLogRecord record = new OperationLogRecord();
            record.setUserId(userIdBeforeOperation == null ? resolveLoginUserId() : userIdBeforeOperation);
            record.setModule(operationLog.module());
            record.setOperation(operationLog.operation());
            record.setRequestMethod(request == null ? "" : request.getMethod());
            record.setRequestUri(request == null ? "" : request.getRequestURI());
            record.setRequestParams(buildRequestParams(joinPoint.getArgs()));
            record.setIp(resolveClientIp(request));
            record.setUserAgent(truncate(request == null ? "" : request.getHeader("User-Agent")));
            record.setSuccessFlag(success ? 1 : 0);
            record.setErrorMessage(truncate(errorMessage));
            operationLogRecordMapper.insert(record);
        } catch (RuntimeException ex) {
            log.warn("operation log persist failed: {}", ex.getMessage());
        }
    }

    private String buildRequestParams(Object[] args) {
        ArrayNode arrayNode = objectMapper.createArrayNode();
        for (Object arg : args) {
            if (arg == null) {
                arrayNode.addNull();
                continue;
            }
            if (shouldSkipArg(arg)) {
                continue;
            }
            if (arg instanceof MultipartFile file) {
                ObjectNode fileNode = objectMapper.createObjectNode();
                fileNode.put("originalName", file.getOriginalFilename());
                fileNode.put("size", file.getSize());
                fileNode.put("contentType", file.getContentType());
                arrayNode.add(fileNode);
                continue;
            }
            JsonNode node = objectMapper.valueToTree(arg);
            maskSensitiveFields(node);
            arrayNode.add(node);
        }
        try {
            return truncate(objectMapper.writeValueAsString(arrayNode));
        } catch (JsonProcessingException ex) {
            return "";
        }
    }

    private boolean shouldSkipArg(Object arg) {
        return arg instanceof HttpServletRequest
                || arg instanceof HttpServletResponse
                || arg instanceof BindingResult;
    }

    private void maskSensitiveFields(JsonNode node) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            List<String> fieldNames = new ArrayList<>();
            while (fields.hasNext()) {
                fieldNames.add(fields.next().getKey());
            }
            for (String fieldName : fieldNames) {
                if (isSensitiveField(fieldName)) {
                    objectNode.put(fieldName, "***");
                } else {
                    maskSensitiveFields(objectNode.get(fieldName));
                }
            }
            return;
        }
        if (node.isArray()) {
            for (JsonNode item : node) {
                maskSensitiveFields(item);
            }
        }
    }

    private boolean isSensitiveField(String fieldName) {
        String lowerName = fieldName.toLowerCase(Locale.ROOT);
        return SENSITIVE_FIELD_KEYWORDS.stream().anyMatch(lowerName::contains);
    }

    private Long resolveLoginUserId() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        Object loginId = StpUtil.getLoginIdDefaultNull();
        return loginId == null ? null : Long.valueOf(String.valueOf(loginId));
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StrUtil.isNotBlank(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        return StrUtil.isNotBlank(realIp) ? realIp : request.getRemoteAddr();
    }

    private String truncate(String value) {
        if (value == null || value.length() <= MAX_TEXT_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_TEXT_LENGTH);
    }
}
