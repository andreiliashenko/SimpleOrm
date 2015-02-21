package com.anli.simpleorm.queries.named;

import org.apache.commons.lang.StringUtils;

public class NamedQuery {

    protected static final String LIST_MACRO = "${list}";

    protected final String name;
    protected final String additionalJoins;
    protected final String criteria;
    protected final int macroCount;

    public static String getListMacro() {
        return LIST_MACRO;
    }

    public NamedQuery(String name, String additionalJoins, String criteria) {
        this.name = name;
        this.additionalJoins = additionalJoins != null ? additionalJoins : "";
        this.criteria = criteria != null ? criteria : "";
        this.macroCount = StringUtils.countMatches(this.additionalJoins, LIST_MACRO)
                + StringUtils.countMatches(this.criteria, LIST_MACRO);
    }

    public String getName() {
        return name;
    }

    public String getAdditionalJoins() {
        return additionalJoins;
    }

    public String getCriteria() {
        return criteria;
    }

    public boolean isTemplate() {
        return macroCount > 0;
    }

    public int getMacroCount() {
        return macroCount;
    }
}
