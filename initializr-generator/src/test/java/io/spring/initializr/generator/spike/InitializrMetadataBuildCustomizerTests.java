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

package io.spring.initializr.generator.spike;

import io.spring.initializr.generator.ProjectDescription;
import io.spring.initializr.generator.buildsystem.Build;
import io.spring.initializr.generator.buildsystem.maven.MavenBuild;
import io.spring.initializr.generator.spike.build.InitializrMetadataBuildCustomizer;
import io.spring.initializr.generator.util.Version;
import io.spring.initializr.metadata.BillOfMaterials;
import io.spring.initializr.metadata.Dependency;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.test.metadata.InitializrMetadataTestBuilder;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link InitializrMetadataBuildCustomizer}.
 *
 * @author Stephane Nicoll
 */
public class InitializrMetadataBuildCustomizerTests {

	@Test
	public void contributeBom() { // ProjectRequestTests#resolveAdditionalBoms
		Dependency dependency = Dependency.withId("foo");
		dependency.setBom("foo-bom");
		BillOfMaterials bom = BillOfMaterials.create("com.example", "foo-bom", "1.0.0");
		bom.getAdditionalBoms().add("bar-bom");
		BillOfMaterials additionalBom = BillOfMaterials.create("com.example", "bar-bom",
				"1.1.0");
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addBom("foo-bom", bom).addBom("bar-bom", additionalBom)
				.addDependencyGroup("test", dependency).build();
		ProjectDescription projectDescription = initializeProjectDescription();
		projectDescription.addDependency(dependency.getId(),
				ConceptTranslator.toDependency(dependency));
		Build build = contributeBuild(projectDescription, metadata);
		assertThat(build.getBoms()).hasSize(2);
	}

	@Test
	public void contributeRepositories() { // ProjectRequestTests#resolveAdditionalRepositories
		Dependency dependency = Dependency.withId("foo");
		dependency.setBom("foo-bom");
		dependency.setRepository("foo-repo");
		BillOfMaterials bom = BillOfMaterials.create("com.example", "foo-bom", "1.0.0");
		bom.getRepositories().add("bar-repo");
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addBom("foo-bom", bom)
				.addRepository("foo-repo", "foo-repo", "http://example.com/foo", false)
				.addRepository("bar-repo", "bar-repo", "http://example.com/bar", false)
				.addDependencyGroup("test", dependency).build();
		ProjectDescription projectDescription = initializeProjectDescription();
		projectDescription.addDependency(dependency.getId(),
				ConceptTranslator.toDependency(dependency));
		Build build = contributeBuild(projectDescription, metadata);
		assertThat(build.getRepositories()).hasSize(2);
		assertThat(build.getPluginRepositories()).isEmpty();
	}

	private ProjectDescription initializeProjectDescription() {
		ProjectDescription projectDescription = new ProjectDescription();
		// TODO: could use the metadata to set defaults
		projectDescription.setSpringBootVersion(Version.parse("2.0.0.RELEASE"));
		return projectDescription;
	}

	private Build contributeBuild(ProjectDescription projectDescription,
			InitializrMetadata metadata) {
		Build build = new MavenBuild();
		new InitializrMetadataBuildCustomizer(projectDescription, metadata)
				.customize(build);
		return build;
	}

}
