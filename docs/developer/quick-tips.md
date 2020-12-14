# Quick Tips
Try to keep this for brief commonly used tips. Please put more lengthy uncommon information in [additional OneStop information](additional-developer-info) docs.

## Skaffold Command Options
* If skaffold pods aren't starting up, and you see an error such as `because first deploy failed: 3/10 deployment(s) failed`:

    `—-status-check=false`
* If you're having a major problem with a scaffold object not updating you can append this:

    `--force=false`
    
* Skaffold uses profiles that are defined in the skaffold.yaml to indicate which components will be started. You can create your own or edit, temporarily, existing profiles to limit/adjust what is started up. 

    `--p psi`    

* Run skaffold without automatic port forwarding 

    `--port-forward=false -f skaffold.yaml`
 
## Kubectl
If you run `kubectl get pods` and the `Status` column has `CrashLoopBackOff` run `kubectl describe <pod_name>` for the pod in question. At the bottom of the text that appears should be `Events`. This section tells you the history of the pod's lifecycle.
 
## Making Helm Chart Changes
When do I need to run OneStop `./helm/updateRelease.sh` script?
- When `requirements.yaml` file is modified.
- When a OneStop subchart is modified that needs to be deployed. **NOTE:** Make sure to update the chart version appropriately in `helm/onestop/Chart.yaml` **before** running this script.

<hr>
<div align="center"><a href="#">Top of Page</a></div>
