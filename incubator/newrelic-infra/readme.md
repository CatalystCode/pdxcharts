# Deploy New Relic Infrastructure inside a Azure Kubernetes Cluster

First, we will create a Kubernetes Azure Container Service. Later, we will create Dockerfile to push an image into to the Azure Container Registry. After that, we will create a helm charts to wrap our environment and finally, we will deploy it in our cluster.


Basically, we will create a Docker image with the New Relic infrastructure. After that, we will upload that image to the ACR where it will be pulled by a helm chart to deploy it into our kubernetes cluster.

## Create docker image and push it to ACR

Let's assume you have your terminal open in the 'newrelic-infra' folder of our current repository.

```bash
cd incubator/newrelic-infra
```

Inside, we will find a docker file and a helm chart. Let's connect our docker service running in our local machine to the ACR we have created in the previous steps when we defined `ACR_CREDS`, `ACR_USER` and `ACR_PASS`.

We will need the url of the ACR loginServer. If you have `jq` installed you can run the following command to obtain it.

```bash
ACR_URL=`az acr  show -n $ACR_NAME | jq -r '.loginServer'
```

With the url, user and password of the registry, we can login with docker

```bash
docker login $ACR_URL -u $ACR_USER -p $ACR_PASS
```

Build the image provided in this repo:

```bash
docker build -t newrelic-infra .
```

To test the image we must now have the New Relic infrastructure License key to our disposal. Add it as an environment variable:

```bash
NR_KEY=<<<PUT YOUR KEY HERE>>>
```

```bash
docker run \
  --name newrelic-infra \
  --uts=host \
  --pid=host \
  --net=host \
  -v /:/mnt/ROOT:ro \
  -d \
  -e NRIA_LICENSE_KEY=$NR_KEY \
  $ACR_URL/newrelic-infra:latest
```

### Create helm chart

### Deploy New Relic into the cluster

