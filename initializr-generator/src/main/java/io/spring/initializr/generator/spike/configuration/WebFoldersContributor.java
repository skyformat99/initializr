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

package io.spring.initializr.generator.spike.configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import io.spring.initializr.generator.ProjectContributor;
import io.spring.initializr.generator.ProjectDescription;
import io.spring.initializr.metadata.Dependency;
import io.spring.initializr.metadata.InitializrMetadata;

/**
 * A {@link ProjectContributor} that creates web-specific directories when a web-related
 * project is detected.
 *
 * @author Stephane Nicoll
 */
public class WebFoldersContributor implements ProjectContributor {

	private final ProjectDescription projectDescription;

	private final InitializrMetadata metadata;

	public WebFoldersContributor(ProjectDescription projectDescription,
			InitializrMetadata metadata) {
		this.projectDescription = projectDescription;
		this.metadata = metadata;
	}

	@Override
	public void contribute(Path projectRoot) throws IOException {
		if (hasWebFacet()) {
			Files.createDirectories(projectRoot.resolve("src/main/resources/templates"));
			Files.createDirectories(projectRoot.resolve("src/main/resources/static"));
		}
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
