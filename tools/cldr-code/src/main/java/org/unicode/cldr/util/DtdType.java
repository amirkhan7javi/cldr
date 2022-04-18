package org.unicode.cldr.util;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;

public enum DtdType {
    ldml("common/dtd/ldml.dtd", null, null,
        "main",
        "annotations",
        "annotationsDerived",
        "casing",
        "collation",
        "rbnf",
        "segments",
        "subdivisions"),
    ldmlICU("common/dtd/ldmlICU.dtd", ldml),
    supplementalData("common/dtd/ldmlSupplemental.dtd", null, null,
        "supplemental",
        "supplemental-temp",
        "transforms",
        "validity"),
    ldmlBCP47("common/dtd/ldmlBCP47.dtd", "1.7.2", null,
        "bcp47"),

    // keyboard 3.0
    keyboard("keyboards/dtd/ldmlKeyboard.dtd", "42.0", null,
        "../keyboards");

    public static final Set<DtdType> STANDARD_SET = ImmutableSet.of(
        ldmlBCP47, supplementalData, ldml, keyboard);

    static Pattern FIRST_ELEMENT = PatternCache.get("//([^/\\[]*)");

    public final String dtdPath;
    public final DtdType rootType;
    public final String firstVersion;
    public final Set<String> directories;

    private DtdType(String dtdPath) {
        this(dtdPath, null, null);
    }

    private DtdType(String dtdPath, DtdType realType) {
        this(dtdPath, null, realType);
    }

    private DtdType(String dtdPath, String firstVersion, DtdType realType, String... directories) {
        this.dtdPath = dtdPath;
        this.rootType = realType == null ? this : realType;
        this.firstVersion = firstVersion;
        this.directories = ImmutableSet.copyOf(directories);
    }

    public static DtdType fromPath(String elementOrPath) {
        Matcher m = FIRST_ELEMENT.matcher(elementOrPath);
        m.lookingAt();
        return DtdType.valueOf(m.group(1));
    }

    /**
     * Print a header for an XML file, where the generatedBy is normally MethodHandles.lookup().lookupClass().
     * The only time it needs to be changed is if it is not being called directly from the generating tool.
     * @param generatedBy
     * @return
     */
    public String header(Class<?> generatedBy) {
        String gline = "";
        if (generatedBy != null) {
            gline = "\n\tGENERATED DATA — do not manually update!"
                + "\n\t\tGenerated by tool:\t" + generatedBy.getSimpleName() + "\n";
            for (Annotation annotation : generatedBy.getAnnotations()) {
                if (annotation instanceof CLDRTool) {
                    gline += "\t\tTool documented on:\t" + ((CLDRTool) annotation).url() + "\n";
                    break;
                }
            }
        }

        return "<?xml version='1.0' encoding='UTF-8' ?>\n"
        + "<!DOCTYPE " + this + " SYSTEM '../../" + dtdPath + "'>\n" // "common/dtd/ldmlSupplemental.dtd"
        + "<!--\n"
        + CldrUtility.getCopyrightString("\t")
        + gline
        + " -->\n"
        + "<" + this + ">\n";
    }
}
