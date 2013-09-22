package org.htmlunit.maven;

import org.openqa.selenium.WebDriver;

/** Manages the {@link WebDriver} lifecycle.
 */
public interface WebDriverRunner {

  /** Initializes the runner with the specified configuration.
   * @param context Configuration to initialize this runner. Cannot be
   *    null.
   */
  public void initialize(final RunnerContext context);

  /** Runs included files in the web driver. It's invoked after
   * {@link #initialize(RunnerContext)}.
   */
  public void run();

  /** Returns the runner's name.
   * @return Any valid name. Never returns null.
   */
  public String getName();
}
