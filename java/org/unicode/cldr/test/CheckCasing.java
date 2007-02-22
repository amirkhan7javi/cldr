package org.unicode.cldr.test;

import org.unicode.cldr.test.CheckCLDR.CheckStatus;
import org.unicode.cldr.util.CLDRFile;
import org.unicode.cldr.util.XPathParts;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.BreakIterator;
import com.ibm.icu.util.ULocale;

import java.text.CharacterIterator;
import java.util.List;
import java.util.Map;

public class CheckCasing extends CheckCLDR {
  public enum Case {mixed, lowercase_words, titlecase_words, titlecase_firstword, verbatim;
  public static Case forString(String input) {
    return valueOf(input.replace('-','_'));
  }
  };  
  // remember to add this class to the list in CheckCLDR.getCheckAll
  // to run just this test, on just locales starting with 'nl', use CheckCLDR with -fnl.* -t.*Currencies.*
  
  XPathParts parts = new XPathParts(); // used to parse out a path
  ULocale uLocale = null;
  BreakIterator breaker = null;
  
  public CheckCLDR setCldrFileToCheck(CLDRFile cldrFileToCheck, Map options, List possibleErrors) {
    if (cldrFileToCheck == null) return this;
    super.setCldrFileToCheck(cldrFileToCheck, options, possibleErrors);
    uLocale = new ULocale(cldrFileToCheck.getLocaleID());
    breaker = BreakIterator.getWordInstance(uLocale);
    return this;
  }

  // If you don't need any file initialization or postprocessing, you only need this one routine
  public CheckCLDR handleCheck(String path, String fullPath, String value, Map<String, String> options, List<CheckStatus> result) {
    // it helps performance to have a quick reject of most paths
    if (fullPath.indexOf("casing") < 0) return this;
    
    // pick up the casing attributes from the full path
    parts.set(fullPath);
    
    Case caseTest = Case.mixed;
    for (int i = 0; i < parts.size(); ++i) {
      String casingValue = parts.getAttributeValue(i, "casing");
      if (casingValue == null) {
        continue;
      }
      caseTest = Case.forString(casingValue);
      if (caseTest == Case.verbatim) {
        return this; // we're done
      }
    }
    
    String newValue = value;
    switch (caseTest) {
      case lowercase_words: newValue = UCharacter.toLowerCase(uLocale, value); break;
      case titlecase_words: newValue = UCharacter.toTitleCase(uLocale, value, null); break;
      case titlecase_firstword: newValue = TitleCaseFirst(uLocale, value); break;
    }
    if (!newValue.equals(value)) {
      // the following is how you signal an error or warning (or add a demo....)
      result.add(new CheckStatus().setCause(this) // boilerplate
          .setType(CheckStatus.errorType) // typically warningType or errorType
          .setMessage("Casing incorrect: either should have casing=\"verbatim\" or be <{0}>", new Object[]{newValue})); // the message; can be MessageFormat with arguments
    }
    return this;
  }

  // -f(bg|cs|da|el|et|is|it|lt|ro|ru|sl|uk) -t(.*casing.*)
  
  private String TitleCaseFirst(ULocale locale, String value) {
    if (value.length() == 0) {
      return value;
    }
    breaker.setText(value);
    int first = breaker.first();
    int endOfFirstWord = breaker.next();
    return UCharacter.toTitleCase(uLocale, value.substring(0,endOfFirstWord), breaker) + value.substring(endOfFirstWord);
  }
  
}