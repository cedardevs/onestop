<div align="center"><a href="/onestop/developer">Developer Documentation Home</a></div>
<hr>

**Estimated Reading Time: 5 minutes**
# Developer Guide
## Table of Contents
* [Setup](#setup)
    * [Clone OneStop Code](#clone-onestop-code)
    * [Minimum Local Requirements](#minimum-local-requirements)
* [Run OneStop](#run-onestop)
* [Verify OneStop Starts Up](#verify-onestop-starts-up)

## Setup
### Clone OneStop Code
    `git clone https://github.com/cedardevs/onestop.git`
    
### Minimum Local Requirements
 * **[Java](https://www.java.com/)** - Minimum Java 11 (13 not supported), can get via sdkman. 
 * **[Docker](https://www.docker.com/)** - [Mac](https://hub.docker.com/editions/community/docker-ce-desktop-mac), [Windows](https://hub.docker.com/editions/community/docker-ce-desktop-windows)
    * **Recommended Settings:** `Docker Desktop > Preferences > Resources`, 6.0 GB Memory and 40 GB disk usage.
 * **[Node](https://nodejs.org/)**
 * **[Kubernetes](https://kubernetes.io/)**
    * **Enable Kubernetes:** `Docker Desktop > Preferences > Kubernetes`. **Note**: Only select the `Enable Kubernetes` option.
 * **[Helm](https://helm.sh/)** 3
 * **[Skaffold](https://skaffold.dev/)**
 * **infraInstall script**
    * **NOTE:** This script needs to be run after a fresh install of Docker, update to Docker, or a reset of the Kubernetes cluster to setup the K8s.
      ```
      # Run this at the root of the OneStop code:
      ./helm/infraInstall.sh
      ```

## Run OneStop
Run the command below to startup OneStop via the skaffold **`dev`** configuration:

`skaffold dev`

If you are running this for the first time it may fail to download all the images at once. Try re-executing `skaffold dev` a few times.
You can also try `skaffold dev --status-check=false`. View the [quick-tips](quick-tips) for more options.

### Verify OneStop Starts Up
Estimate startup time is ~13 minutes, ~18 minutes if this is a fresh startup and it has to download images.

1. Run **`kubectl get pods`** and verify it displays:
    * List of expected pods
    * The number in the `Ready` column should indicate all complete, like 1/1 or 2/2 but not a partial like 1/2
    * `Status` column should be Running. CrashLoopBackOff indicates the pod failed to start.
1. Browse to [OneStop](http://localhost/onestop)

View [Quick Tips](quick-tips) for troubleshooting information.

<hr>
<div align="center"><a href="#">Top of Page</a></div>
