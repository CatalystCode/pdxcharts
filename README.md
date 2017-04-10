# Splunk, New Relic, Trend Micro Deep Security on a Kubernetes cluster on Azure

This repo contains helm charts, scripts and notes on how to provide a configurable environment to deploy applications to a Kubernetes cluster on Azure. This cluster uses [New Relic infrastructure](https://newrelic.com/infrastructure) and [Splunk](https://www.splunk.com/) for monitoring and logging. Additionally, for security [Trend Micro Deep Security](https://www.trendmicro.com/en_us/business/products/hybrid-cloud/deep-security-data-center.html) is installed in the VMs of the cluster.

## Requirements

- [Azure Cli 2.0](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli)
- [New Relic Infrastructure](https://newrelic.com/infrastructure) - License Key and access to the portal (We can use a free trial)
- [Splunk](https://www.splunk.com/)
- [Trend Micro Deep Security](https://www.trendmicro.com/en_us/business/products/hybrid-cloud/deep-security-data-center.html)
- [Docker](https://docs.docker.com/engine/installation/)
- [Kubectl](https://kubernetes.io/docs/user-guide/prereqs/)
- [Helm client](https://github.com/kubernetes/helm/blob/master/docs/install.md)

## Set up

### Deploying Kubernetes on ACS

First of all, we need to set some environment variables to make this process a little bit easier. Feel free to edit them and paste them into your terminal.

```Bash
RESOURCE_GROUP=coolrgname111
LOCATION=southcentralus
CLUSTER_NAME=k8s-clus2s3r2
CLUSTER_DNS=k8s-brusmx1213
ACR_NAME=coolacr12
```

Now, login to Azure in your CLI:

```Bash
az login
```

After that, deploy a new resource group (it might take a couple of minutes):

```Bash
az group create -n $RESOURCE_GROUP -l $LOCATION
```

It should return `"provisioningState": "Succeeded"`.

Then, deploy the Azure Container Service ([full documentation](https://docs.microsoft.com/en-us/azure/container-service/container-service-kubernetes-walkthrough)). This next command asumes you dont have ssh keys in your terminal, but you can remove the `--generate-ssh-keys` if you would like `az` to use your usual pair of ssh keys (uploads `~/.ssh/id_rsa.pub` to the VMs):

```Bash
az acs create --orchestrator-type=kubernetes -n $CLUSTER_NAME -g $RESOURCE_GROUP -d $CLUSTER_DNS --generate-ssh-keys
```

It should take about 10 minutes to finish and it will return a `"provisioningState": "Succeeded"`.

The next step is to install `kubectl` by running:

```Bash
az acs kubernetes install-cli
```

Obtain the `.kube/config`:

```Bash
az acs kubernetes get-credentials -g=$RESOURCE_GROUP -n=$CLUSTER_NAME
```

And finally, verify you can connect to your cluster by getting your pods:

```Bash
kubect get pods
```

### Deploying ACR

In addition to the [Azure Cli 2.0](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli) and depending on your version you might have to install the `acr` component in your Az CLI.

```Bash
az component update --add  acr
```

This component allow us to manage the Azure Container Registry through our CLI.
We will use a ACR to host our Docker images that will be deployed later in our cluster. Deploy one on your subscription with the following command ([full documentation](https://docs.microsoft.com/en-us/azure/container-registry/container-registry-get-started-azure-cli)):

```bash
az acr create -n $ACR_NAME -g $RESOURCE_GROUP -l $LOCATION
```

Allow admin access to your ACR to retrieve the username and password:

```bash
az acr update -n $ACR_NAME --admin-enabled true
```

And get the credentials:

```bash
ACR_CREDS=`az acr credential show -n $ACR_NAME` | echo $ACR_CREDS
```

If you have [jq](https://stedolan.github.io/jq/) installed you can do the following:

```bash
ACR_USER=`echo $ACR_CREDS | jq -r '.username'`
ACR_PASS=`echo $ACR_CREDS | jq -r '.password'`
```

With these credentials we will be able to push the images to the registry.

These are the following steps to run:

1. Install TMDS through Custom Script Extension.
1. Deploy New Relic infra in the agents.
1. Deploy Splunk.
1. Deploy Application.
1. Test monitoring, logging and security.

## Deploy New Relic Infrastructure inside a Azure Kubernetes Cluster

First, we will create a Kubernetes Azure Container Service. Later, we will create Dockerfile to push an image into to the Azure Container Registry. After that, we will create a helm charts to wrap our environment and finally, we will deploy it in our cluster.


Basically, we will create a Docker image with the New Relic infrastructure. After that, we will upload that image to the ACR where it will be pulled by a helm chart to deploy it into our kubernetes cluster.

### Create docker image and push it to ACR

Let's start by cloning the currently repository and going to the following folder.


### Create helm chart

### Deploy New Relic into the cluster




