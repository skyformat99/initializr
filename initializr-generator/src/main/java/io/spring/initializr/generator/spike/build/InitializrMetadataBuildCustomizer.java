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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.spring.initializr.generator.ProjectDescription;
import io.spring.initializr.generator.buildsystem.Build;
import io.spring.initializr.generator.project.build.BuildCustomizer;
import io.spring.initializr.generator.spike.ConceptTranslator;
import io.spring.initializr.metadata.BillOfMaterials;
import io.spring.initializr.metadata.Dependency;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.metadata.Repository;
import io.spring.initializr.util.Version;

/**
 * A {@link BuildCustomizer} that configures the {@link Build} based on the metadata.
 *
 * @author Stephane Nicoll
 */
public class InitializrMetadataBuildCustomizer implements BuildCustomizer<Build> {

	private final ProjectDescription projectDescription;

	private final InitializrMetadata metadata;

	public InitializrMetadataBuildCustomizer(ProjectDescription projectDescription,
			InitializrMetadata metadata) {
		this.projectDescription = projectDescription;
		this.metadata = metadata;
	}

	@Override
	public void customize(Build build) {
		contributeDependencyManagement(build);
	}

	protected void contributeDependencyManagement(Build build) {
		Map<String, BillOfMaterials> boms = new LinkedHashMap<>();
		Map<String, Repository> repositories = new LinkedHashMap<>();
		mapDependencies().forEach((dependency) -> {
			if (dependency.getBom() != null) {
				resolveBom(boms, dependency.getBom(),
						mapVersion(this.projectDescription.getSpringBootVersion()));
			}
			if (dependency.getRepository() != null) {
				String repositoryId = dependency.getRepository();
				repositories.computeIfAbsent(repositoryId, (key) -> this.metadata
						.getConfiguration().getEnv().getRepositories().get(key));
			}
		});
		boms.values().forEach((bom) -> {
			bom.getRepositories()
					.forEach((repositoryId) -> repositories.computeIfAbsent(repositoryId,
							(key) -> this.metadata.getConfiguration().getEnv()
									.getRepositories().get(key)));
		});

		boms.values().forEach((bom) -> {
			build.addBom(ConceptTranslator.toBom(bom));
			if (bom.getVersionProperty() != null) {
				build.addVersionProperty(
						ConceptTranslator.toVersionProperty(bom.getVersionProperty()),
						bom.getVersion());
			}
		});
		repositories.forEach((id, repository) -> {
			if (repository.isSnapshotsEnabled()) {
				build.addSnapshotMavenRepository(id, repository.getName(),
						repository.getUrl().toExternalForm());
			}
			else {
				build.addMavenRepository(id, repository.getName(),
						repository.getUrl().toExternalForm());
			}
		});
	}

	private List<Dependency> mapDependencies() {
		List<Dependency> dependenciesMetadata = new ArrayList<>();
		this.projectDescription.getDependencies().forEach((id, dependency) -> {
			Dependency dependencyMetadata = this.metadata.getDependencies().get(id);
			// root starter or on the fly dependencies have no metadata
			if (dependencyMetadata != null) {
				dependenciesMetadata.add(dependencyMetadata);
			}
		});
		return dependenciesMetadata;
	}

	private void resolveBom(Map<String, BillOfMaterials> boms, String bomId,
			Version requestedVersion) {
		if (!boms.containsKey(bomId)) {
			BillOfMaterials bom = this.metadata.getConfiguration().getEnv().getBoms()
					.get(bomId).resolve(requestedVersion);
			bom.getAdditionalBoms()
					.forEach((id) -> resolveBom(boms, id, requestedVersion));
			boms.put(bomId, bom);
		}
	}

	private Version mapVersion(io.spring.initializr.generator.util.Version version) {
		return Version.parse(version.toString());
	}

}
