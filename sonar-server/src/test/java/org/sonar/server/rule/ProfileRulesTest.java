/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2013 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.rule;

import com.github.tlrx.elasticsearch.test.EsSetup;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.client.Requests;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.config.Settings;
import org.sonar.core.profiling.Profiling;
import org.sonar.server.qualityprofile.Paging;
import org.sonar.server.search.SearchIndex;
import org.sonar.server.search.SearchNode;
import org.sonar.test.TestUtils;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProfileRulesTest {

  private ProfileRules profileRules;
  private EsSetup esSetup;

  @Before
  public void setUp() {
    esSetup = new EsSetup();
    esSetup.execute(EsSetup.deleteAll());

    SearchNode searchNode = mock(SearchNode.class);
    when(searchNode.client()).thenReturn(esSetup.client());

    Settings settings = new Settings();
    settings.setProperty("sonar.log.profilingLevel", "FULL");
    SearchIndex index = new SearchIndex(searchNode, new Profiling(settings));
    index.start();
    RuleRegistry registry = new RuleRegistry(index, null, null);
    registry.start();
    profileRules = new ProfileRules(index);
  }

  @After
  public void tearDown() {
    esSetup.terminate();
  }

  @Test
  public void should_find_active_rules() throws Exception {
    esSetup.client().prepareBulk()
      .add(Requests.indexRequest().index("rules").type("rule").source(testFileAsString("should_find_active_rules/rule25.json")))
      .add(Requests.indexRequest().index("rules").type("rule").source(testFileAsString("should_find_active_rules/rule759.json")))
      .add(Requests.indexRequest().index("rules").type("active_rule").parent("25").source(testFileAsString("should_find_active_rules/active_rule25.json")))
      .add(Requests.indexRequest().index("rules").type("active_rule").parent("759").source(testFileAsString("should_find_active_rules/active_rule391.json")))
      .add(Requests.indexRequest().index("rules").type("active_rule").parent("759").source(testFileAsString("should_find_active_rules/active_rule523.json")))
      .setRefresh(true).execute().actionGet();

    Paging paging = Paging.create(10, 1);

    // All rules for profile 1
    assertThat(profileRules.searchActiveRules(ProfileRuleQuery.create(1), paging)).hasSize(2);

    // All rules for profile 2
    assertThat(profileRules.searchActiveRules(ProfileRuleQuery.create(2), paging)).hasSize(1);

    // Inexistent profile
    assertThat(profileRules.searchActiveRules(ProfileRuleQuery.create(3), paging)).hasSize(0);

    // Inexistent name/key
    assertThat(profileRules.searchActiveRules(ProfileRuleQuery.create(1).setNameOrKey("polop"), paging)).hasSize(0);

    // Match on key
    assertThat(profileRules.searchActiveRules(ProfileRuleQuery.create(1).setNameOrKey("DM_CONVERT_CASE"), paging)).hasSize(1);

    // Match on name
    assertThat(profileRules.searchActiveRules(ProfileRuleQuery.create(1).setNameOrKey("Unused Check"), paging)).hasSize(1);

    // Match on repositoryKey
    assertThat(profileRules.searchActiveRules(ProfileRuleQuery.create(1).addRepositoryKeys("findbugs"), paging)).hasSize(1);
  }

  private String testFileAsString(String testFile) throws Exception {
    return IOUtils.toString(TestUtils.getResource(getClass(), testFile).toURI());
  }
}