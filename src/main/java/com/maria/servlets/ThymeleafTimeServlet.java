package com.maria.servlets;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JavaxServletWebApplication;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@WebServlet (value = "/time")
public class ThymeleafTimeServlet extends HttpServlet {
    private final Instant instant = Instant.now();
    private TemplateEngine engine;

    @Override
    public void init() {
        engine = new TemplateEngine();

        JavaxServletWebApplication swa =
                JavaxServletWebApplication.buildApplication(this.getServletContext());

        WebApplicationTemplateResolver
                resolver = new WebApplicationTemplateResolver(swa);

        resolver.setPrefix("/WEB-INF/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html; charset=utf-8");
        HttpSession session = req.getSession(true);

        Context simpleContext = new Context(
                req.getLocale(),
                Map.of("dateTimes", getLastTimeZones(req, resp))
        );

        engine.process("time", simpleContext, resp.getWriter());
        resp.getWriter().close();
    }

    private List<String> getLastTimeZones(HttpServletRequest req, HttpServletResponse resp) {
        List<String> cookiesValues = new ArrayList<>();
        List<String> dateTimesFromCookies = new ArrayList<>();
        if (getCookies(req).size() == 0) {
            return new ArrayList<>(getDateTimes(req, resp));
        } else {
            for (Map.Entry<String, String> entry : Objects.requireNonNull(getCookies(req)).entrySet()) {
                if ("lastTimezone".equals(entry.getKey())) {
                    cookiesValues.add(entry.getValue());
                }
            }
            for (String cookieValue : Objects.requireNonNull(cookiesValues)) {
                dateTimesFromCookies.add(instant.atZone(ZoneId.of(cookieValue))
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
            }
            return dateTimesFromCookies;
        }
    }

    private Map<String, String> getCookies(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        Map<String, String> cookiesMap = new HashMap<>();
        if (cookies == null) {
            return Collections.emptyMap();
        }
        for (Cookie cookie : cookies) {
            cookiesMap.put(cookie.getName(), cookie.getValue());
        }
        return cookiesMap;
    }

    private List<String> getDateTimes(HttpServletRequest req, HttpServletResponse resp) {
        String[] timezonesParams = req.getParameterValues("timezone");
        List<String> dateTimes = new ArrayList<>();
        String offset;
        if (timezonesParams != null) {
            for (String timezone : timezonesParams) {
                String sign = timezone.substring(3, 4);
                int hoursOffset = Integer.parseInt(timezone.substring(4));

                if (sign.equals("-")) {
                    offset = "-" + hoursOffset;
                } else {
                    offset = "+" + hoursOffset;
                }
                dateTimes.add(instant.atZone(ZoneId.of("UTC" + offset))
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
                resp.addCookie(new Cookie("lastTimezone", "UTC" + offset));
            }
        } else {
            dateTimes.add(instant.atZone(ZoneId.of("UTC"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
        }
        return dateTimes;
    }
}
