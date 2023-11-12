package com.maria.servlets;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.TimeZone;

@WebFilter(value = "/time")
public class TimezoneValidateFilter extends HttpFilter {

    @Override
    public void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        String[] timezonesParams = req.getParameterValues("timezone");

        if (timezonesParams != null && !allValidTimeZones(timezonesParams)) {
            resp.setContentType("text/html; charset=utf-8");
            resp.setStatus(400);
            resp.getWriter().write("<h1>Invalid timezone!</h1>");
        } else {
            chain.doFilter(req, resp);
        }
    }

    private static boolean allValidTimeZones(String[] timezonesParams) {
        boolean allValid = true;
        for (String timezone : timezonesParams) {
            if (timezone.startsWith("UTC")) {
                try {
                    int offsetHours = Integer.parseInt(timezone.substring(4));
                    long offsetMillis = (long) offsetHours * 60 * 60 * 1000;
                    String[] availableIDs = TimeZone.getAvailableIDs((int) offsetMillis);
                    allValid &= availableIDs != null && Arrays.asList(availableIDs).contains("Etc/GMT-" + offsetHours);
                } catch (NumberFormatException e) {
                    return false;
                }
            } else {
                allValid = false;
            }
        }
        return allValid;
    }
}

