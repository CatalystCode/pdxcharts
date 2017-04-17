# Deploy Trend Micro Deep Security in the context of K8s on Azure

## Considerations

Before I begin listing what works and what doesn't, let me start with saying that there was a project named container-poc in Deep Security's GitHub repo. It seems to be deleted last month. You can see it in Google's cache [here](https://webcache.googleusercontent.com/search?q=cache:qkhlc0_l9VYJ:https://github.com/deep-security/container-poc/tree/master/docker+&cd=1&hl=en&ct=clnk&gl=us). Contacting your Trend Micro account manager would be good idea to learn if the project is still active.

Here's the summary of what I tried:

- Deploying the Trend Micro Deep Security (TMDS) agents to Kubernetes nodes deployed by Azure Container Service (ACS) is pretty straightforward since ACS deploys Ubuntu 16.04 nodes. The easisest way to deploy the agents to Kubernetes nodes is using the [Custom Script Extension (CSE)](https://github.com/deep-security/azure-vm-extensions). CSE is also available in Terraform.

- Tectonic uses CoreOS as the base OS. Since CoreOS doesn't have a package manager by design and Trend Micro doesn't provide a containerized agent, it's not possible to deploy the TMDS agents to Tectonic nodes.

- The workaround is running the agent in a Super Priviliged container that would have access to the nodes' resources.

- I tried running the TMDS agents in Ubuntu 14.04 & 16.04 containers. Trend Micro states that TMDS agents can be deployed to both. 14.04 works fine. However, deploying the agent to 16.04 ends up with an "unsupported version" error message. From what I understand from debugging and their older helper script [here](https://github.com/deep-security/ops-tools/blob/master/deepsecurity/agent/bash/install-dsa.sh), it checks the version info from `/etc/os-release`. The agents can be deployed to a 16.04 VM without any issues. I tried comparing `/etc/os-release` in a VM and in a container and they seem to be identical. So I don't know why it fails to deploy the TMDS agent to a 16.04 container.

- One important point running the agent in a container is to make sure you edit the `policy-rc.d` to allow the agent to run as a service. The sed line in the following examples changes the value from 101 (not allowed) to 0 (allowed). Otherwise, the agent cannot start.

- If you run a 14.04 container interactively (`docker run -it ubuntu:14.04`) and deploy the agent using the script Trend Micro provides, it deploys successfully and starts reporting to Deep Security Manager almost instantly.

## Deploying TDMS to a container

Here's the script (To simplify troubleshooting, I used the Deep Security `TenantID` and `token` inside the Dockerfiles/scripts instead of setting them as variables):

```Bash
#!/usr/bin/env bash
wget https://app.deepsecurity.trendmicro.com:443/software/agent/Ubuntu_14.04/x86_64/ -O /tmp/agent.deb --quiet
dpkg -i /tmp/agent.deb
sleep 15
/opt/ds_agent/dsa_control -r
/opt/ds_agent/dsa_control -a dsm://agents.deepsecurity.trendmicro.com:443/ "tenantID:91783418-02F4-23BE-3B03-ED1B2DE5ACE1" "token:38E9EA0F-274E-D1DE-5D7D-B00DDCDC359D"
```

However, if you put the same instructions to a Dockerfile using separate RUN commands, it fails because it does not create the cert `/var/opt/ds_agent/dsa_core/ds_agent.crt`

Example Dockerfile below:

```Dockerfile
FROM ubuntu:14.04

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update
RUN apt-get install -y wget apt-utils
RUN sed -i.bak 's/101/0/' /usr/sbin/policy-rc.d
RUN wget https://app.deepsecurity.trendmicro.com:443/software/agent/Ubuntu_14.04/x86_64/ -O /tmp/agent.deb --quiet
RUN dpkg -i /tmp/agent.deb
RUN sleep 15
RUN /opt/ds_agent/dsa_control -r
RUN /opt/ds_agent/dsa_control -a dsm://agents.deepsecurity.trendmicro.com:443/ "tenantID:91783418-02F4-23BE-3B03-ED1B2DE5ACE1" "token:38E9EA0F-274E-D1DE-5D7D-B00DDCDC359D"
```

If you combine the instructions in a single RUN like below, the cert is created successfully.

```Dockerfile
FROM ubuntu:14.04

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update \
    && apt-get install -y wget apt-utils \
    && sed -i.bak 's/101/0/' /usr/sbin/policy-rc.d \
    && wget https://app.deepsecurity.trendmicro.com:443/software/agent/Ubuntu_14.04/x86_64/ -O /tmp/agent.deb --quiet \
    && dpkg -i /tmp/agent.deb \
    && sleep 15 \
    && /opt/ds_agent/dsa_control -r \
    && /opt/ds_agent/dsa_control -a dsm://agents.deepsecurity.trendmicro.com:443/ "tenantID:91783418-02F4-23BE-3B03-ED1B2DE5ACE1" "token:38E9EA0F-274E-D1DE-5D7D-B00DDCDC359D"
```

So while this Dockerfile builts succesfully and the resulting image can be pushed to Docker Hub (or any other registry), the agent does not start when you run that image (the image is `oguzpastirmaci/trendmicro:single-run-in-dockerfile`).

So I tried deploying the agent with the shell script I got from my Deep Security Manager inside the Dockerfile and that works:

```Dockerfile
FROM ubuntu:14.04

ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update \
    && apt-get install --assume-yes apt-utils wget \
    && sed -i.bak 's/101/0/' /usr/sbin/policy-rc.d
ADD run.sh /usr/local/bin/run.sh
RUN chmod +x /usr/local/bin/run.sh
CMD /usr/local/bin/run.sh
```

HOWEVER, the issue with using a shell script in Dockerfile is that Docker will run the script to install the agent and then quit unless you start the container with the `-d` parameter to run the container in detached mode. That works in Docker, but not in Kubernetes since Kubernetes doesn't have the concept of detached mode (or I'm now aware of it). That means if you put the image in a Daemonset yaml, the container will run but the agent won't be running.

So, added a very ugly workaround to keep the container running (last line):

```Dockerfile
FROM ubuntu:14.04

ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update \
    && apt-get install --assume-yes apt-utils wget \
    && sed -i.bak 's/101/0/' /usr/sbin/policy-rc.d
ADD run.sh /usr/local/bin/run.sh
RUN chmod +x /usr/local/bin/run.sh
CMD /usr/local/bin/run.sh
CMD exec /bin/bash -c "trap : TERM INT; sleep infinity & wait"
```

This one works, but there's probably a more elegant solution out there in the internets.


Since this container should be able to access the node, mounting nodes root to container. Open ports are from Deep Security documentation [here](https://success.trendmicro.com/solution/1060007-communication-ports-used-by-deep-security).

I did my tests with a [Deep Security SaaS](https://azuremarketplace.microsoft.com/en-us/marketplace/apps/trendmicro.accounts) Manager so if you're using your own Deep Security Manager with a different configuration, you might need to edit the ports.

```YAML
apiVersion: extensions/v1beta1
kind: DaemonSet
metadata:
 name: trendmicro
spec:
 template:
  metadata:
   labels:
    app: trendmicro
  spec:
   containers:
     - name: trendmicro
       image: "oguzpastirmaci/trendmicro:shell-script"
       imagePullPolicy: Always
       securityContext:
         privileged: true
       ports:
       - containerPort: 4118
         protocol: TCP
       - containerPort: 4120
         protocol: TCP
       - containerPort: 5274
         protocol: TCP
       - containerPort: 443
         protocol: TCP
       volumeMounts:
        - mountPath: /mnt/ROOT
          name: root
   volumes:
    - name: root
      hostPath:
       path: /
```

This Daemonset successfully creates the container and it starts reporting to the Deep Security Manager. Depending on the policies/settings you have (e.g. real-time AV scanning) you might hit some issues.

- [Back to homepage](../../README.md)