package com.jpmorgan.ib.caonpd.ethereum.enterprise.config;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class JsonMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private static final Logger LOG = LoggerFactory.getLogger(JsonMethodArgumentResolver.class);

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface JsonBodyParam	{

        /**
         * The name of the request parameter to bind to.
         */
        String value() default "";

        /**
         * Whether the parameter is required.
         * <p>Default is {@code true}, leading to an exception thrown in case
         * of the parameter missing in the request. Switch this to {@code false}
         * if you prefer a {@code null} in case of the parameter missing.
         * <p>Alternatively, provide a {@link #defaultValue() defaultValue},
         * which implicitly sets this flag to {@code false}.
         */
        boolean required() default true;

        /**
         * The default value to use as a fallback when the request parameter value
         * is not provided or empty. Supplying a default value implicitly sets
         * {@link #required()} to false.
         */
        String defaultValue() default ValueConstants.DEFAULT_NONE;

    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return (parameter.getParameterAnnotation(JsonBodyParam.class) != null);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        if (!mavContainer.getModel().containsAttribute("_json_data")) {
            ObjectMapper mapper = new ObjectMapper();
            BufferedReader postReader = ((HttpServletRequest)webRequest.getNativeRequest()).getReader();
            Map<String,Object> data = null;
            try{
                data = mapper.readValue(postReader, Map.class);
            }catch(JsonMappingException ex){
            }
            mavContainer.addAttribute("_json_data", data);
        }
        Map<String,Object> data = (Map<String, Object>) mavContainer.getModel().get("_json_data");
        if(data == null){
            return null;
        }

        JsonBodyParam jsonParam = parameter.getParameterAnnotation(JsonBodyParam.class);
        String param = jsonParam.value();
        if (param == null || param.isEmpty() || param.equals(jsonParam.defaultValue())) {
            param = parameter.getParameterName(); // fallback to name of param itself
        }

        Class paramType = parameter.getParameterType();
        Object val = data.get(param);
        if (val == null || paramType == val.getClass()) {
            return val; // null or types match exactly
        }

        if (paramType.isArray()) {
            if (val.getClass().isArray()) {
                return val;
            }

            if (val instanceof List) {
                return ((List) val).toArray();
            }
        }

        LOG.warn("Param type mismatch for '" + parameter.getParameterName() + "'; got " + val.getClass().toString());
        return null;
    }

}
