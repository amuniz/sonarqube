/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
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
package org.sonar.server.cluster;

import org.sonar.core.cluster.IndexAction;
import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;

public class LocalQueueWorkerTest {

  private static final String WORKING_IDNEX = "working_index";

  @Test
  public void test_worker_dequeue(){
    LocalNonBlockingWorkQueue queue = new LocalNonBlockingWorkQueue();
    LocalQueueWorker worker = new LocalQueueWorker(queue);
    worker.start();
    queue.enqueue(new IndexAction(WORKING_IDNEX, IndexAction.Method.INSERT,new Integer(33)));
    queue.enqueue(new IndexAction(WORKING_IDNEX, IndexAction.Method.INSERT,new Integer(33)));
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    assertThat(queue.dequeue()).isNull();
    queue = null;
  }
}