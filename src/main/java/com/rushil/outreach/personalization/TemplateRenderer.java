package com.rushil.outreach.personalization;

import com.rushil.outreach.lead.Lead;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.HashMap;
import java.util.Map;

@Component
public class TemplateRenderer {

    public static final String OPENER = "personalized_opener";

    public String renderSubject(String template, Lead lead, Map<String, String> extra) {
        return fill(template, values(lead, extra), false);
    }

    public String renderBody(String template, Lead lead, Map<String, String> extra) {
        return fill(template, values(lead, extra), true);
    }

    private Map<String, String> values(Lead lead, Map<String, String> extra) {
        Map<String, String> values = new HashMap<>();
        values.put("contact_name", nullSafe(lead.getContactName()));
        values.put("company_name", nullSafe(lead.getCompanyName()));
        values.put("industry", nullSafe(lead.getIndustry()));
        values.put("city", nullSafe(lead.getCity()));
        values.put(OPENER, "");
        if (extra != null) {
            values.putAll(extra);
        }
        return values;
    }

    private String fill(String template, Map<String, String> values, boolean escapeHtml) {
        String result = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String value = escapeHtml ? HtmlUtils.htmlEscape(entry.getValue()) : entry.getValue();
            result = result.replace("{{" + entry.getKey() + "}}", value);
        }
        return result;
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}