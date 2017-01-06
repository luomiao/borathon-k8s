/*
 * Copyright 2015 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, without warranties or
 * conditions of any kind, EITHER EXPRESS OR IMPLIED.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.vmware.photon.controller.clustermanager.statuschecks;

import com.vmware.photon.controller.clustermanager.clients.EtcdClient;
import com.vmware.photon.controller.clustermanager.servicedocuments.ClusterManagerConstants;

import com.google.common.util.concurrent.FutureCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

/**
 * Determines the Status of a Etcd Node.
 */
public class EtcdStatusChecker implements StatusChecker {

  private static final Logger logger = LoggerFactory.getLogger(EtcdStatusChecker.class);
  private EtcdClient etcdClient;

  public EtcdStatusChecker(EtcdClient etcdClient) {
    this.etcdClient = etcdClient;
  }

  @Override
  public void checkNodeStatus(final String nodeAddress,
                              final FutureCallback<Boolean> callback) {
    logger.info("Checking Etcd, IP: {}", nodeAddress);
    // TODO : Adding a hack to send success
    //callback.onSuccess(true);
    
    try {
      String connectionString = createConnectionString(nodeAddress);
      etcdClient.checkStatus(connectionString, new FutureCallback<Boolean>() {
        @Override
        public void onSuccess(@Nullable Boolean isReady) {
          try {
            callback.onSuccess(isReady);
          } catch (Throwable t) {
            logger.warn("Etcd call failed: ", t);
            callback.onFailure(t);
          }
        }

        @Override
        public void onFailure(Throwable t) {
          logger.warn("Etcd call failed: ", t);
          callback.onSuccess(false);
        }
      });
    } catch (Exception e) {
      logger.warn("Etcd call failed: ", e);
      callback.onSuccess(false);
    }
  }

  private static String createConnectionString(String serverAddress) {
    return "http://" + serverAddress + ":" + ClusterManagerConstants.Swarm.ETCD_PORT;
  }
}
