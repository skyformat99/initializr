/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.initializr.generator.spike.build;

import io.spring.initializr.generator.ProjectDescription;
import io.spring.initializr.generator.buildsystem.Build;
import io.spring.initializr.generator.project.build.BuildCustomizer;
import io.spring.initializr.generator.spike.ConceptTranslator;
import io.spring.initializr.metadata.Dependency;
import io.spring.initializr.metadata.InitializrMetadata;

/**
 * A {@link BuildCustomizer} that configures the necessary web-related dependency when
 * packaging an application as a war.
 *
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 */
public class WarPackagingWebStarterBuildCustomizer implements BuildCustomizer<Build> {

	private final ProjectDescription projectDescription;

	private final InitializrMetadata metadata;

	public WarPackagingWebStarterBuildCustomizer(ProjectDescription projectDescription,
			InitializrMetadata metadata) {
		this.projectDescription = projectDescription;
		this.metadata = metadata;
	}

	@Override
	public void customize(Build build) {
		if (!hasWebFacet()) {
			// Need to be able to bootstrap the web app
			Dependency dependency = determineWebDependency(this.metadata);
			build.addDependency(dependency.getId(),
					ConceptTranslator.toDependency(dependency));
		}
		// Add the tomcat starter in provided scope
		Dependency tomcat = new Dependency().asSpringBootStarter("tomcat");
		tomcat.setScope(Dependency.SCOPE_PROVIDED);
		build.addDependency("tomcat", ConceptTranslator.toDependency(tomcat));
	}

	private Dependency determineWebDependency(InitializrMetadata metadata) {
		Dependency web = metadata.getDependencies().get("web");
		return (web != null) ? web : Dependency.withId("web").asSpringBootStarter("web");
	}

	private boolean hasWebFacet() {
		return this.projectDescription.getDependencies().keySet().stream()
				.anyMatch((id) -> {
					Dependency dependency = this.metadata.getDependencies().get(id);
					if (dependency != null) {
						return dependency.getFacets().contains("web");
					}
					return false;
				});
	}

}
