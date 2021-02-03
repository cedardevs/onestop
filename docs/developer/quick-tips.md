<div align="center"><a href="/onestop/developer">Developer Documentation Home</a></div>
<hr>

**Estimated Reading Time: 5 minutes**
# Quick Tips
Try to keep this for brief commonly used tips. Please put more lengthy uncommon information in [additional OneStop information](additional-developer-info) docs.

## Skaffold Command Options
* If this is not your first time running OneStop, especially if your Docker was updated since your last execution, view the cleanup steps in [additional-developer-info](additional-developer-info).

* If skaffold pods aren't starting up and you see an error such as "*because first deploy failed: 3/10 deployment(s) failed*":

    `—-status-check=false`
    
* If you're having a major problem with a scaffold object not updating:

    `--force=false`
    
* Skaffold uses profiles that are defined in the skaffold.yaml to indicate which components will be started. You can create your own or edit, temporarily, existing profiles to limit/adjust what is started up. 

    `--p psi`    

* Run skaffold without automatic port forwarding:

    `--port-forward=false -f skaffold.yaml`
 

## Kubernetes/Helm
* If you run "`kubectl get pods`" and the `status` column has `CrashLoopBackOff` run "`kubectl describe <pod_name>`" for the pod in question. At the bottom of the text that appears should be `Events`. This section tells you the history of the pod's lifecycle.
 
    With "`kubectl get pods`" the `AGE` column indicates which pod is the relevant pod. If you made a code change which triggered a pod to get rebuilt the old pod will still exist while the new pod is being brought up. Then the old pod will eventually be listed as TERMINATING.

    Appending the "`-w`" to "`kubectl get pods`" will tail that command and keep a live update.

* Helm stores state as kubernetes secrets. These secrets will persist between docker restarts and will only be fixed by resetting your docker desktop kubernetes cluster. 
 The only reason to pursue this is if you run skaffold and see something like "**\<chart-name\> has no deployed releases**" and do a `helm ls -a` and if you see a chart as "**uninstalling**". Where the secret name varies, which you can find via `helm get secrets`.

  [https://kubernetes.io/docs/tasks/configmap-secret/managing-secret-using-kubectl/#clean-up](https://kubernetes.io/docs/tasks/configmap-secret/managing-secret-using-kubectl/#clean-up)

## Making Helm Chart Changes
When do I need to run OneStop `./helm/updateRelease.sh` script?
- When `requirements.yaml` file is modified.
- When a OneStop subchart is modified that needs to be deployed. **NOTE:** Make sure to update the chart version appropriately in `helm/onestop/Chart.yaml` **before** running this script.

<hr>
<div align="center"><a href="#">Top of Page</a></div>
