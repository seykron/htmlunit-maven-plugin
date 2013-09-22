package org.htmlunit.maven;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

/** Represents an Ant path expression.
 *
 * <p>The expression matches URLs using the following rules:
 * <ul>
 *  <li>? matches one character</li>
 *  <li>* matches zero or more characters</li>
 *  <li>** matches zero or more 'directories' in a path</li>
 *  <li>~ at the beginning of the expression indicates an exclusion
 *    pattern</li>
 * </ul>
 */
public class AntExpression {

  /** Original ant expression; it's never null or empty. */
  private final String expression;

  /** Indicates if the expression is a exclusion. */
  private boolean exclusion;

  /** Expression protocol part, if any; null if the protocol doesn't exist
   * in the expression. */
  private String protocol;

  /** Part of the expression that represents the path before the pattern;
   * never null after parse(). */
  private String rootDir;

  /** Expression pattern; never null after parse(). */
  private String pattern;

  /** Creates a new ant pattern and sets the pattern expression.
   *
   * @param theExpression A valid an pattern expression. Cannot be null or
   *    empty.
   */
  public AntExpression(final String theExpression) {
    Validate.notEmpty(theExpression, "The expression cannot be null or empty");
    expression = theExpression.trim();
    parse();
  }

  /** Returns the original expression.
   * @return Returns the original ant expression, never returns null or empty.
   */
  public String getExpression() {
    return expression;
  }

  /** Indicates whether this expression is an exclusion pattern.
   * @return true if the expression is an exclusion pattern, false otherwise.
   */
  public boolean isExclusion() {
    return exclusion;
  }

  /** Returns the protocol part of the expression, if any.
   * @return The expression protocol, or null if there's no protocol in the
   *    expression.
   */
  public String getProtocol() {
    return protocol;
  }

  /** Returns the pattern part of the expression.
   * @return Returns a valid ant pattern.
   */
  public String getPattern() {
    return pattern;
  }

  /** Returns the root directory of the expression.
   *
   * @return The root directory. Returns empty if the expression is relative
   *  to the current path. Never returns null.
   */
  public String getRootDir() {
    return rootDir;
  }

  /** Parses the current ant expression.
   */
  private void parse() {
    exclusion = expression.startsWith("~");

    if (expression.indexOf(":") > -1) {
      protocol = StringUtils.substringBefore(expression, ":");

      if (exclusion) {
        protocol = protocol.substring(1);
      }
    }

    String fullPath;

    if (protocol != null) {
      fullPath = FilenameUtils.normalize(
          StringUtils.substringAfter(expression, ":"), true);
    } else {
      if (exclusion) {
        fullPath = FilenameUtils.normalize(expression.substring(1), true);
      } else {
        fullPath = FilenameUtils.normalize(expression, true);
      }
    }
    String[] parts = fullPath.split("/");
    boolean isPattern = false;

    rootDir = "";
    pattern = "";

    for (String pathPart : parts) {
      if (!isPattern && (pathPart.contains("*")
          || pathPart.contains("?")
          || pathPart.equals(parts[parts.length - 1]))) {
        isPattern = true;
      }
      if (isPattern) {
        pattern += pathPart + "/";
      } else {
        rootDir += pathPart + "/";
      }
    }

    if (rootDir.length() > 1) {
      rootDir = StringUtils.stripEnd(rootDir, "/");
    }
    pattern = StringUtils.stripEnd(pattern, "/");
  }
}
