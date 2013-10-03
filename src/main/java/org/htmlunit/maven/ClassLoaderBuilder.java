package org.htmlunit.maven;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.project.MavenProject;

/** Class loader used to build a custom class loader with dependencies
 * and reactor artifacts.
 */
public class ClassLoaderBuilder {

  /** Dependencies resolver; it's never null.
   */
  private final ArtifactResolver artifactResolver;

  /** Maven local repository; it's never null. */
  private final ArtifactRepository localRepository;

  /** Provides some metadata operations, like querying the remote repository
   * for a list of versions available for an artifact. It's never null.
   */
  private final ArtifactMetadataSource metadataSource;

  /** The Maven project object, used to generate a classloader to access the
   * classpath resources from the project; it's never null.
   */
  private final MavenProject project;

  /** Indicates if dependencies will be added to the class loader.
   */
  private boolean includeDependencies;

  /** Indicates if test dependencies will be added to the class loader.
   */
  private boolean includeTestDependencies;

  /** Parent class loader, if any. */
  private ClassLoader parent;

  /** Creates a new maven class loader builder.
   *
   * @param theArtifactResolver Resolver to download dependencies. Cannot be
   *    null.
   * @param theMetadataSource Provides artifacts metadata. Cannot be null.
   * @param theLocalRepository Maven local repository. Cannot be null.
   * @param theProject The reference maven project. Cannot be null.
   */
  public ClassLoaderBuilder(final ArtifactResolver theArtifactResolver,
      final ArtifactMetadataSource theMetadataSource,
      final ArtifactRepository theLocalRepository,
      final MavenProject theProject) {
    Validate.notNull(theArtifactResolver,
        "The artifact resolver cannot be null.");
    Validate.notNull(theMetadataSource,
        "The graph builder cannot be null.");
    Validate.notNull(theLocalRepository,
        "The local repository cannot be null.");
    Validate.notNull(theProject,
        "The maven projectcannot be null.");

    // TODO(matias.mirabelli): Artifact resolver is removed in Maven 3, change
    // resolution strategy to migrate this plugin.
    artifactResolver = theArtifactResolver;
    metadataSource = theMetadataSource;
    project = theProject;
    localRepository = theLocalRepository;
  }

  /** Specifies if project dependencies will be added to the class loader.
   *
   * @param mustIncludeDependencies True to add dependencies, false otherwise.
   * @return Returns this builder to continue with the class loader
   *    configuration.
   */
  public ClassLoaderBuilder includeDependencies(
      final boolean mustIncludeDependencies) {
    includeDependencies = mustIncludeDependencies;
    return this;
  }

  /** Specifies if project test dependencies will be added to the class loader.
   *
   * @param mustIncludeTestDependencies True to add dependencies, false
   *    otherwise.
   * @return Returns this builder to continue with the class loader
   *    configuration.
   */
  public ClassLoaderBuilder includeTestDependencies(
      final boolean mustIncludeTestDependencies) {
    includeTestDependencies = mustIncludeTestDependencies;
    return this;
  }

  /** Sets the parent class loader.
   *
   * @param theParent Parent class loader. Can be null.
   * @return Returns this builder to continue with the class loader
   *    configuration.
   */
  public ClassLoaderBuilder setParent(final ClassLoader theParent) {
    parent = theParent;
    return this;
  }

  /** Builds the class loader using the current configuration.
   * @return Returns a valid class loader. Never returns null.
   */
  @SuppressWarnings("unchecked")
  public ClassLoader create() {
    ClassLoader classLoader = parent;

    if (includeDependencies || includeTestDependencies) {
      List<URL> artifactsUrls = new ArrayList<URL>();

      try {
        Set<Artifact> artifacts = new HashSet<Artifact>();

        artifacts.addAll(project.getDependencyArtifacts());

        if (includeTestDependencies) {
          artifacts.addAll(project.getTestArtifacts());
        }

        ArtifactResolutionResult result;
        result = artifactResolver.resolveTransitively(artifacts,
            project.getArtifact(), project.getRemoteArtifactRepositories(),
            localRepository, metadataSource);

        for (Object artifact : result.getArtifacts()) {
          artifactsUrls.add(((Artifact) artifact).getFile().toURI().toURL());
        }
      } catch (Exception ex) {
        throw new RuntimeException("Cannot resolve the artifact.", ex);
      }

      classLoader = new URLClassLoader(artifactsUrls.toArray(new URL[] {}),
          parent);
    }

    return classLoader;
  }
}
