package org.htmlunit.maven;

/** Manages the {@link WebDriver} lifecycle.
 */
public interface WebDriverRunner {

  /** Initializes the runner with the specified configuration.
   * @param context Configuration to initialize this runner. Cannot be
   *    null.
   */
  void initialize(final RunnerContext context);

  /** Runs included files in the web driver. It's invoked after
   * {@link #initialize(RunnerContext)}.
   */
  void run();

  /** Returns the runner's name.
   * @return Any valid name. Never returns null.
   */
  String getName();
}
