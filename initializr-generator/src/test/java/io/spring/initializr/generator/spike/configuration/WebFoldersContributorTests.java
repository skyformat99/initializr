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
import java.nio.file.Path;
import java.util.Collections;

import io.spring.initializr.generator.ProjectDescription;
import io.spring.initializr.generator.spike.ConceptTranslator;
import io.spring.initializr.metadata.Dependency;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.test.metadata.InitializrMetadataTestBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link WebFoldersContributor}
 *
 * @author Stephane Nicoll
 */
public class WebFoldersContributorTests {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void webFoldersCreatedWithWebDependency() throws IOException {
		Dependency simple = Dependency.withId("simple", "com.example", "simple", null,
				Dependency.SCOPE_COMPILE);
		Dependency web = Dependency.withId("web", "com.example", "web", null,
				Dependency.SCOPE_COMPILE);
		web.setFacets(Collections.singletonList("web"));
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("test", simple, web).build();
		ProjectDescription description = new ProjectDescription();
		description.addDependency("simple", ConceptTranslator.toDependency(simple));
		description.addDependency("web", ConceptTranslator.toDependency(web));
		Path projectDir = contribute(description, metadata);
		assertThat(projectDir.resolve("src/main/resources/templates")).isDirectory();
		assertThat(projectDir.resolve("src/main/resources/static")).isDirectory();
	}

	@Test
	public void webFoldersNotCreatedWithoutWebDependency() throws IOException {
		Dependency simple = Dependency.withId("simple", "com.example", "simple", null,
				Dependency.SCOPE_COMPILE);
		Dependency web = Dependency.withId("web", "com.example", "web", null,
				Dependency.SCOPE_COMPILE);
		web.setFacets(Collections.singletonList("web"));
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("test", simple, web).build();
		ProjectDescription description = new ProjectDescription();
		description.addDependency("simple", ConceptTranslator.toDependency(simple));
		Path projectDir = contribute(description, metadata);
		assertThat(projectDir.resolve("src/main/resources/templates")).doesNotExist();
		assertThat(projectDir.resolve("src/main/resources/static")).doesNotExist();
	}

	private Path contribute(ProjectDescription description, InitializrMetadata metadata)
			throws IOException {
		Path projectDir = this.folder.newFolder().toPath();
		new WebFoldersContributor(description, metadata).contribute(projectDir);
		return projectDir;
	}

}
